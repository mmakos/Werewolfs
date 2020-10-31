package server;

import javafx.application.Platform;
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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Server{
    public Vector< Player > players = new Vector<>();
    public Vector< String > cardsOnBegin = new Vector<>();
    public Vector< String > cardsNow = new Vector<>();
    public LinkedList< String > cardsInGame;
    public String realCopycat;        // Needed to show on the end who was copycat
    public String realParanormal;       // --- || ---
    private static final int gameMsg = 1;
    private static final int MAX_PLAYERS = Card.card.length;
    private static final int MIN_PLAYERS = 3;
    private static final int MAX_READ_TIME = 45;
    private volatile boolean connecting = false;
    public String[] cardsInCenter;
    private final String COM_SPLITTER = String.valueOf( ( char )28 );
    private final String UNIQUE_CHAR = String.valueOf( ( char )2 );
    private Vector< Thread > playerReaders = new Vector<>();
    private Vector< Integer > votes = new Vector<>();
    private AtomicInteger votesQuant = new AtomicInteger();

    @FXML public void initialize(){
        cardsInCenter = new String[ 3 ];
        voteButton.setVisible( false );
        endVotingButton.setVisible( false );
        kickOut.setVisible( false );
    }

    @FXML void connect(){
        // later while( nie wciśnięto "RUN GAME" ), for now 2 clients will be accepted
        // new player with id: 100, 101, 102 itd.
        // starting thread for player
        Thread connect = new Thread( () -> {
            runServer.setDisable( true );
            runServer.setVisible( false );
            kickOut.setVisible( true );
            kickOut.setDisable( false );
            ServerSocket ss;
            try{
                ss = new ServerSocket( 23000 );
                Socket socket;
                connecting = true;
                for( int i = 0; i < MAX_PLAYERS; ++i ){     // later while( nie wciśnięto "RUN GAME" ), for now 2 clients will be accepted
                    socket = ss.accept();
                    if( connecting ){
                        Player p = new Player( players.size() == 0 ? 100 : players.lastElement().id + 1, socket );
                        players.add( p );  // new player with id: 100, 101, 102 itd.
                        String nickname = p.getNickname();
                        if( nickname.equals( "" ) ){
                            players.remove( p );
                            --i;
                            continue;
                        }
                        for( int j = 0; j < players.size() - 1; ++j ){
                            if( players.get( j ).name.equals( nickname ) ){       // same nickname
                                sendMsg( p.id, "0" + COM_SPLITTER + "wrongNickname" );
                                players.remove( p );
                                break;
                            }
                        }
                        if( i >= players.size() )   //was same nickname
                            --i;
                        else{
                            if( i >= MIN_PLAYERS - 1 ) startGame.setDisable( false );
                            Platform.runLater( () -> playersLabel.setText( playersLabel.getText() + " " + p.name + "," ) );
                            sendMsg( p.id, "0" + COM_SPLITTER + "ok" );
                        }
                    }
                }
            }catch( IOException e ){
                runServer.setDisable( false );
            }
        } );
        connect.start();
    }

    @FXML void runGame() throws IOException{
        connecting = false;
        sendPlayersList();
        playersLabel.setText( "Starting game..." );
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "fxml/cardChooser.fxml" ) );
        Stage stage = new Stage();
        stage.setTitle( "Choose card" );
        stage.getIcons().add( new Image( this.getClass().getResourceAsStream( "/img/sericon.png" ) ) );
        stage.setScene( new Scene( fxmlLoader.load(), 720, 480 ) );
        stage.show();
        CardChooser cardChooser = fxmlLoader.getController();
        cardChooser.setPlayers( players.size() );
        cardChooser.setServer( this );
        startGame.setVisible( false );
        kickOut.setVisible( false );
        playersLabel.setVisible( false );
        logField.setVisible( true );
        voteButton.setVisible( true );
    }

    @FXML void orderVoting(){
        voteButton.setDisable( true );
        voteButton.setVisible( false );
        endVotingButton.setVisible( true );
        endVotingButton.setDisable( false );
        sendGame( 0, UNIQUE_CHAR + "VOTE" );
        writeLog( "Voting ordered" );
        votes.removeAllElements();
        votesQuant.set( 0 );
        votes.setSize( players.size() + 1 );    //last element is table
        for( int i = 0; i < votes.size(); ++i )
            votes.set( i, 0 );
        for( Player player: players ){
            playerReaders.add( new Thread( () -> {
                String vote;
                try{
                    while( !player.input.ready() );
                    vote = receiveGame( player.id );
                    if( vote.equals( UNIQUE_CHAR + "table" ) ){
                        writeLog( "Player " + player.name + " voted for the table." );
                        votes.set( votes.size() - 1, votes.lastElement() + 1 );
                    }
                    else{
                        int votedPlayerIdx = players.indexOf( getPlayer( vote ) );
                        writeLog( "Player " + player.name + " voted for " + players.get( votedPlayerIdx ).name );
                        if( votedPlayerIdx != -1 ){
                            votes.set( votedPlayerIdx, votes.get( votedPlayerIdx ) + 1 );
                        }
                    }
                    sendGame( 0, player.name + Game.MSG_SPLITTER + vote );
                    votesQuant.incrementAndGet();
                    if( votesQuant.get() == players.size() )
                        writeLog( "Everyone has already voted. Press \"End voting\" button" );
                }catch( IOException ignored ){}
            } ) );
            playerReaders.lastElement().start();
        }
    }

    @FXML void endVoting(){
        endVotingButton.setDisable( true );
        for( Thread t: playerReaders ){
            t.stop();
        }
        playerReaders.removeAllElements();
        sendGame( 0, UNIQUE_CHAR + "VOTEEND" );
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
                sendGame( 0, UNIQUE_CHAR + "table" );
                writeLog( "Nobody has been killed." );
            } else{
                sendGame( 0, players.get( maxIdx ).name );
                writeLog( players.get( maxIdx ).name + " has been killed." );
            }
            sendAllPlayers();
        }
}

    private void sendAllPlayers(){
        StringBuilder str = new StringBuilder();
        for( String cardNow: cardsNow )
            str.append( cardNow ).append( Game.MSG_SPLITTER );
        for( String middleCard: cardsInCenter )
            str.append( middleCard ).append( Game.MSG_SPLITTER );
        sendGame( 0, str.toString() );

        StringBuilder str2 = new StringBuilder();
        if( cardsNow.contains( "Paranormal investigator" ) )
            cardsNow.set( cardsNow.indexOf( "Paranormal investigator" ), realParanormal );
        if( cardsNow.contains( "Copycat" ) )
            cardsNow.set( cardsNow.indexOf( "Copycat" ), realCopycat );
        for( String cardNow: cardsNow )
            str2.append( cardNow ).append( Game.MSG_SPLITTER );
        sendGame( 0, str2.toString() );
        writeLog( "All cards have been revealed." );
    }

    public void setSelectedCards( LinkedList< String > selectedCards ){
        cardsInGame = selectedCards;
    }

    void drawCards(){   // Randomly give cards to players and 3 on the table
        Random rand = new Random();
        LinkedList< String > temp = new LinkedList<>( cardsInGame );

//        //todo to remove when not testing with one player
//        cardsOnBegin.add( "Thing" );
//        cardsNow.add( cardsOnBegin.get( 0 ) );
//        temp.remove( "Thing" );

        for( int i = 0; i < 3; ++i ){
            int randInt = rand.nextInt( temp.size() );
            cardsInCenter[ i ] = temp.get( randInt );
            temp.remove( randInt );
        }
        for( int i = 0; i < players.size(); ++i ){      //todo to i=0 when not testing with one player
            int randInt = rand.nextInt( temp.size() );
            cardsOnBegin.add( temp.get( randInt ) );
            cardsNow.add( cardsOnBegin.get( i ) );
            temp.remove( randInt );
        }
    }

    void sendCardsToPlayers(){
        for( int i = 0; i < players.size(); ++i ){
            sendGame( players.get( i ).id, cardsOnBegin.get( i ) );
        }
    }

    void sendPlayersList(){
        StringBuilder playersList = new StringBuilder();
        for( Player player : players ) playersList.append( player.name ).append( Game.MSG_SPLITTER );
        sendGame( 0, playersList.toString() );
    }

    void startGame() throws InterruptedException{
        Game game = new Game( this );
        game.start();
    }

    public void writeLog( String log ){
        Platform.runLater( () -> {
            logField.setText( logField.getText() + "\n" + log );
            logField.positionCaret( logField.getText().length() );
        } );
    }

    //Comunication
    void sendGame( int id, String msg ){
        if( id == 0 ){
            for( Player player : players )
                sendMsg( player.id, gameMsg + COM_SPLITTER + msg );
        }
        else
            sendMsg( id, gameMsg + COM_SPLITTER + msg );     //send msg of type gameMsg
    }

    String receiveGame( int id ) throws IOException{
        AtomicReference< String > msg = new AtomicReference<>();
        AtomicBoolean read = new AtomicBoolean( false );
        new Thread( () -> {
            try{
                msg.set( receiveMsg( id ) );
                read.set( true );
            }catch( IOException ignored ){}
        } ).start();
        long start = System.currentTimeMillis();
        while( !read.get() && System.currentTimeMillis() - start < MAX_READ_TIME * 1000 );
        if( read.get() ){
            return msg.get().split( COM_SPLITTER )[ 1 ];
        }
        else
            throw new IOException( "Time's up, cannot read a message." );
    }

    void sendMsg( int id, String str ){
        getPlayer( id ).output.println( str );
    }

    String receiveMsg( int id ) throws IOException{
        return getPlayer( id ).input.readLine();
    }

    public int getTableCardId( String str ){
        switch( str ){
            case ( char )2 + "card0": return 0;
            case ( char )2 + "card1": return 1;
            case ( char )2 + "card2": return 2;
        }
        return -1;
    }

    Player getPlayer( int id ){
        for( Player player: players ){
            if( player.id == id )
                return player;
        }
        return null;
    }

    Player getPlayer( String name ){
        for( Player player: players ){
            if( player.name.equals( name ) )
                return player;
        }
        throw new NullPointerException( "No player with that name" );
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
        players.remove( player );
        Platform.runLater( () -> {
            if( players.size() < MIN_PLAYERS )
                startGame.setDisable( true );
            StringBuilder s = new StringBuilder( "Players: " );
            for( Player value : players ) s.append( value.name ).append( ", " );
            playersLabel.setText( s.toString() );
        } );
    }

    String getPlayersCard( String name ){
        return cardsNow.get( players.indexOf( getPlayer( name ) ) );
    }

    @FXML private TextArea logField;
    @FXML private Button runServer;
    @FXML private Button startGame;
    @FXML public Button voteButton;
    @FXML private Button kickOut;
    @FXML private Button endVotingButton;
    @FXML private Label playersLabel;

    public class Player{
        public int id;
        public String name;
        public BufferedReader input;
        public PrintWriter output;

        Player( int id, Socket socket ) throws IOException{
            this.id = id;
            this.input = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
            this.output = new PrintWriter( new OutputStreamWriter( socket.getOutputStream(), StandardCharsets.UTF_8 ), true );
        }

        public String getNickname(){
            try{
                this.name = input.readLine().split( COM_SPLITTER )[ 1 ];
            }
            catch( NullPointerException | IOException | ArrayIndexOutOfBoundsException e ){
                return "";
            }
            return this.name;
        }
    }
}

