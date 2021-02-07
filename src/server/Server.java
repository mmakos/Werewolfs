package server;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server{
    public Vector< Player > players = new Vector<>();
    private static String ip = "localhost";
    private static int port = 23000;
    private final String COM_SPLITTER = String.valueOf( ( char )28 );
    private final String UNIQUE_CHAR = String.valueOf( ( char )2 );
    private String gameId;
    private boolean listening = true;
    private boolean reading = false;
    private boolean isVoting = false;
    private BufferedReader input;
    private PrintWriter output;
    private Socket s;
    private static final int MAX_PLAYERS = CardChooser.card.length;
    private static final int MIN_PLAYERS = 3;
    private static final int MAX_READ_TIME = 45;
    private volatile boolean connecting = false;
    public String[] cardsInCenter;
    public Vector< String > cardsOnBegin = new Vector<>();
    public Vector< String > cardsNow = new Vector<>();
    public LinkedList< String > cardsInGame;
    public String realCopycat;        // Needed to show on the end who was copycat
    public String realParanormal;       // --- || ---
    private Vector< Integer > votes = new Vector<>();
    private Thread voting;

    @FXML private TextArea logField;
    @FXML private Button runServer;
    @FXML private Button startGame;
    @FXML public Button voteButton;
    @FXML private Button kickOut;
    @FXML private Button endVotingButton;
    @FXML private Label playersLabel;
    @FXML private Label gameIdLabel;
    @FXML private Button copyIdButton;

    @FXML public void initialize(){
        cardsInCenter = new String[ 3 ];
        voteButton.setVisible( false );
        endVotingButton.setVisible( false );
        kickOut.setVisible( false );
        gameIdLabel.setVisible( false );
        copyIdButton.setVisible( false );
        runServer.setText( "New Game" );
        getDefaultSettings();
    }

    private void getDefaultSettings(){
        try{
            Scanner scan = new Scanner( new File( "default.cfg" ) );
            String[] config = scan.nextLine().split( ":" );
            port = Integer.parseInt( config[ 1 ] );
            ip = config[ 0 ];
            scan.close();
        }catch( IOException | IndexOutOfBoundsException | NumberFormatException ignored ){}
    }

    @FXML private void connect(){
        try{
            this.s = new Socket( ip, port );
            this.input = new BufferedReader( new InputStreamReader( this.s.getInputStream(), StandardCharsets.UTF_8 ) );;
            this.output = new PrintWriter( new OutputStreamWriter( this.s.getOutputStream(), StandardCharsets.UTF_8 ), true );
            this.output.println( "NEWGAME" );
            String response = this.input.readLine();
            this.gameId = response;
            gameIdLabel.setText( "Game ID: " + this.gameId );

            runServer.setDisable( true );
            runServer.setVisible( false );
            kickOut.setVisible( true );
            kickOut.setDisable( false );
            gameIdLabel.setVisible( true );
            copyIdButton.setVisible( true );

            listen.start();
        } catch( IOException e ){
            System.out.println( "Cannot connect to the server." );
        }
    }

    @FXML void copyId(){
        StringSelection stringSelection = new StringSelection( gameId );
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents( stringSelection, null );
    }

    private Thread listen = new Thread( () -> {
        while( listening && players.size() < MAX_PLAYERS ){         // create new player
            try{
                while( !input.ready() );
                String[] msg = input.readLine().split( COM_SPLITTER );
                if( msg.length == 2 ){
                    int playerID = Integer.parseInt( msg[ 0 ] );
                    String playerNickname = msg[ 1 ];
                    if( playerNickname.equals( "" ) )
                        continue;
                    if( isPlayer( playerNickname ) ){
                        send( playerID + COM_SPLITTER + "WRONGNICK" );
                        send( "REMOVE" + COM_SPLITTER + playerID );
                        continue;
                    }
                    players.add( new Player( playerID, playerNickname ) );
                    if( players.size() >= MIN_PLAYERS - 1 ) startGame.setDisable( false );
                    Platform.runLater( ( ) -> playersLabel.setText( playersLabel.getText() + " " + playerNickname + "," ) );
                    send( playerID, "OK" );
                }
            } catch( NumberFormatException | IOException ignored ){}
        }
    } );

    private Thread reader = new Thread( () -> {
        while( reading ){
            try{
                String[] msg = input.readLine().split( COM_SPLITTER );
                System.out.println( "From: " + msg[ 0 ] );
                if( msg.length == 2 )      // message from player, [ 0 ] is id, [ 1 ] is message
                    System.out.println( "msg: " + msg[ 1 ] );
                    getPlayer( Integer.parseInt( msg[ 0 ] ) ).addMsg( msg[ 1 ] );
            } catch( IOException | NumberFormatException ignored ){}
        }
    } );

    public void send( String msg ){
        output.println( msg );
    }

    public void send( Player p, String msg ){
        send( p.id + COM_SPLITTER + msg );
    }

    public void send( int id, String msg ){
        if( id == 0 )       // send ALL
            send( "ALL" + COM_SPLITTER + msg );
        else
            send( id + COM_SPLITTER + msg );
    }

    public String receive( int id ) throws IOException{
        return getPlayer( id ).getMsg();
    }

    public Player getPlayer( int id ){
        for( Player player : players ){
            if( player.id == id )
                return player;
        }
        return null;
    }

    public Player getPlayer( String name ){
        for( Player player : players ){
            if( player.name.equals( name ) )
                return player;
        }
        return null;
    }

    //------------GAME--------------------
    void startGame() throws InterruptedException{
        Game game = new Game( this );
        game.start();
    }

    @FXML void runGame() throws IOException{
        listening = false;
        reading = true;
        listen.stop();
        reader.start();
        sendPlayersList();
        playersLabel.setText( "Starting game..." );
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "fxml/cardChooser.fxml" ) );
        Stage stage = new Stage();
        stage.setTitle( "Choose card" );
        stage.getIcons().add( new Image( this.getClass().getResourceAsStream( "/img/sericon.png" ) ) );
        stage.setScene( new Scene( fxmlLoader.load(), 720, 480 ) );
        stage.show();
        stage.getScene().getWindow().addEventFilter( WindowEvent.WINDOW_CLOSE_REQUEST, this::endGame );
        CardChooser cardChooser = fxmlLoader.getController();
        cardChooser.setPlayers( players.size() );
        cardChooser.setServer( this );
        startGame.setVisible( false );
        kickOut.setVisible( false );
        playersLabel.setVisible( false );
        logField.setVisible( true );
        voteButton.setVisible( true );
    }

    void drawCards(){   // Randomly give cards to players and 3 on the table
        Random rand = new Random();
        LinkedList< String > temp = new LinkedList<>( cardsInGame );

//        cardsOnBegin.add( "Thing" );
//        cardsNow.add( cardsOnBegin.get( 0 ) );
//        temp.remove( "Thing" );

//        boolean insomniacForStasiek = false;
//        try{
//            getPlayer( "Michał" );
//            insomniacForStasiek = temp.contains( "Insomniac" );
//            if( insomniacForStasiek ) temp.remove( "Insomniac" );
//        } catch( NullPointerException ignored ){}

        for( int i = 0; i < 3; ++i ){
            int randInt = rand.nextInt( temp.size() );
            cardsInCenter[ i ] = temp.get( randInt );
            temp.remove( randInt );
        }
        for( int i = 0; i < players.size(); ++i ){
//            if( insomniacForStasiek && players.get( i ).name.equals( "Michał" ) ){
//                cardsOnBegin.add( "Insomniac" );
//                cardsNow.add( "Insomniac " );
//                continue;
//            }
            int randInt = rand.nextInt( temp.size() );
            cardsOnBegin.add( temp.get( randInt ) );
            cardsNow.add( cardsOnBegin.get( i ) );
            temp.remove( randInt );
        }
    }

    @FXML void orderVoting(){
        voteButton.setDisable( true );
        voteButton.setVisible( false );
        endVotingButton.setVisible( true );
        endVotingButton.setDisable( false );
        send( 0, UNIQUE_CHAR + "VOTE" );
        writeLog( "Voting ordered" );
        votes.removeAllElements();
        AtomicInteger votesQuant = new AtomicInteger();
        votes.setSize( players.size() + 1 );    //last element is table
        for( int i = 0; i < votes.size(); ++i )
            votes.set( i, 0 );
        isVoting = true;
        Vector< Player > playersNotVoted = new Vector<>( players );
        voting = new Thread( () -> {
            while( isVoting ){
                for( Iterator< Player > it = playersNotVoted.iterator(); it.hasNext(); ){
                    Player player = it.next();
                    if( player.ready() ){
                        try{
                        String vote = player.getMsg();
                        if( vote.equals( UNIQUE_CHAR + "table" ) ){
                            writeLog( "Player " + player.name + " voted for the table." );
                            votes.set( votes.size() - 1, votes.lastElement() + 1 );
                        } else{
                            int votedPlayerIdx = players.indexOf( getPlayer( vote ) );
                            writeLog( "Player " + player.name + " voted for " + players.get( votedPlayerIdx ).name );
                            if( votedPlayerIdx != -1 )
                                votes.set( votedPlayerIdx, votes.get( votedPlayerIdx ) + 1 );
                        }
                        send( 0, player.name + Game.MSG_SPLITTER + vote );
                        votesQuant.incrementAndGet();
                        if( votesQuant.get() == players.size() ){
                            writeLog( "Everyone has already voted. Press \"End voting\" button" );
                            break;
                        }
                        it.remove();
                        } catch( IOException ignored ){}
                    }
                }
            }
        } );
        voting.start();
    }

    @FXML void endVoting(){
        endVotingButton.setDisable( true );
        isVoting = false;
        voting.stop();
        send( 0, UNIQUE_CHAR + "VOTEEND" );
        writeLog( "Voting ended" );
        countVotes();
    }

    private void countVotes(){
        for( int i = 0; i < players.size(); ++i )
            writeLog( players.get( i ).name + " got " + votes.get( i ) + " votes." );
        writeLog( "Table got " + votes.lastElement() + " votes." );
        int max = Collections.max( votes );
        int maxIdx = votes.indexOf( max );
        boolean temp = true;
        if( maxIdx == -1 )      //no one voted
            temp = false;
        votes.set( maxIdx, -1 );
        if( !temp || Collections.max( votes ) == max ){       // same votes quantity
            writeLog( "Unequivocal vote result. Voting will be repeated.\nPress vote button again." );
            endVotingButton.setVisible( false );
            voteButton.setVisible( true );
            voteButton.setDisable( false );
        } else{
            if( maxIdx == votes.size() - 1 ){
                send( 0, UNIQUE_CHAR + "table" );
                writeLog( "Nobody has been killed." );
            } else{
                send( 0, players.get( maxIdx ).name );
                writeLog( players.get( maxIdx ).name + " has been killed." );
            }
            sendAllPlayers();
            endGame();
        }
    }

    public < T extends Event > void endGame( T t ){
        endGame();
        Platform.exit();
        System.exit( 0 );
    }

    private void endGame(){
        reading = false;
        reader.stop();
        if( this.s != null ){
            send( "QUIT" );
            try{
                this.s.close();
            } catch( IOException ignored ){
            }
        }
    }

    private void sendAllPlayers(){
        StringBuilder str = new StringBuilder();
        for( String cardNow: cardsNow )
            str.append( cardNow ).append( Game.MSG_SPLITTER );
        for( String middleCard: cardsInCenter )
            str.append( middleCard ).append( Game.MSG_SPLITTER );
        send( 0, str.toString() );

        StringBuilder str2 = new StringBuilder();
        if( cardsNow.contains( "Paranormal investigator" ) )
            cardsNow.set( cardsNow.indexOf( "Paranormal investigator" ), realParanormal );
        if( cardsNow.contains( "Copycat" ) )
            cardsNow.set( cardsNow.indexOf( "Copycat" ), realCopycat );
        for( String cardNow: cardsNow )
            str2.append( cardNow ).append( Game.MSG_SPLITTER );
        send( 0, str2.toString() );
        writeLog( "All cards have been revealed." );
    }

    @FXML private void kickOut(){
        Stage stage = new Stage();
        stage.setTitle( "Kick sb out" );
        Label l = new Label( "Players nickname:" );
        TextField t = new TextField();
        t.setPromptText( "Nickname" );
        Button b = new Button( "Kick out" );
        t.setPrefWidth( 200 );
        t.setMaxWidth( 200 );
        b.setPrefWidth( 200 );
        l.setFont( Font.font( 14 ) );
        t.setFont( Font.font( 14 ) );
        b.setFont( Font.font( 14 ) );
        b.setOnAction( event -> {
            try{
                removePlayerFromGame( getPlayer( t.getText() ) );
                stage.close();
            } catch( NullPointerException e ){
                Platform.runLater( () -> l.setText( "No such player!" ) );
            }
        } );
        VBox v = new VBox( l, t, b );
        v.setAlignment( Pos.BASELINE_CENTER );
        Scene s = new Scene( v, 300, 200 );
        stage.setScene( s );
        stage.show();
    }

    private void removePlayerFromGame( Player player ){
        send( "REMOVE" + COM_SPLITTER + player.id );
        players.remove( player );
        Platform.runLater( () -> {
            if( players.size() < MIN_PLAYERS )
                startGame.setDisable( true );
            StringBuilder s = new StringBuilder( "Players: " );
            for( Player value : players ) s.append( value.name ).append( ", " );
            playersLabel.setText( s.toString() );
        } );
    }

    public void setSelectedCards( LinkedList< String > selectedCards ){
        cardsInGame = selectedCards;
    }

    public String getPlayersCard( String name ){
        return cardsNow.get( players.indexOf( getPlayer( name ) ) );
    }

    public int getTableCardId( String str ){
        String sub = str.substring( 1 );
        switch( sub ){
            case "card0": return 0;
            case "card1": return 1;
            case "card2": return 2;
        }
        return -1;
    }

    void sendCardsToPlayers(){
        for( int i = 0; i < players.size(); ++i ){
            send( players.get( i ).id, cardsOnBegin.get( i ) );
        }
    }

    void sendPlayersList(){
        StringBuilder playersList = new StringBuilder();
        for( Player player : players ) playersList.append( player.name ).append( Game.MSG_SPLITTER );
        send( 0, playersList.toString() );
    }

    public void writeLog( String log ){
        Platform.runLater( () -> {
            logField.setText( logField.getText() + "\n" + log );
            logField.positionCaret( logField.getText().length() );
        } );
    }

    private boolean isPlayer( String name ){
        for( Player player : players ){
            if( player.name.equals( name ) )
                return true;
        }
        return false;
    }


    public class Player{
        int id;
        String name;
        LinkedList< String > msg = new LinkedList<>();

        Player( int id, String nickname ){
            this.id = id;
            this.name = nickname;
        }

        public boolean ready(){
            return !msg.isEmpty();
        }

        public void addMsg( String msg ){
            this.msg.addLast( msg );
        }

        public String getMsg() throws IOException{
            System.out.println( "Waiting for msg from player " + id );
            long start = System.currentTimeMillis();
            while( msg.isEmpty() && System.currentTimeMillis() - start < MAX_READ_TIME * 1000 ){
                try{
                    Thread.sleep( 10 );
                } catch( InterruptedException ignored ){}
            };
            System.out.println( "Got msg from player " + id );
            if( !msg.isEmpty() )
                return msg.removeFirst();
            else
                throw new IOException( "Time's up, cannot read a message." );
        }
    }
}
