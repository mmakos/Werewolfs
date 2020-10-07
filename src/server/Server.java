package server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

public class Server{
    public Vector< Player > players = new Vector<>();
    public Vector< String > cardsOnBegin = new Vector<>();
    public Vector< String > cardsNow = new Vector<>();
    public LinkedList< String > cardsInGame;
    private static final int gameMsg = 1;
    private static final int MAX_PLAYERS = Card.card.length;
    private static final int MIN_PLAYERS = 1;
    private volatile boolean connecting = false;
    public String[] cardsInCenter;
    private final String COM_SPLITTER = String.valueOf( ( char )28 );
    private Vector< Thread > playerReaders = new Vector<>();
    private int[] votes;

    @FXML public void initialize(){
        cardsInCenter = new String[ 3 ];
        voteButton.setVisible( false );
        endVotingButton.setVisible( false );
    }

    @FXML void connect(){
        // later while( nie wciśnięto "RUN GAME" ), for now 2 clients will be accepted
        // new player with id: 100, 101, 102 itd.
        // starting thread for player
        Thread connect = new Thread( () -> {
            runServer.setDisable( true );
            ServerSocket ss;
            try{
                ss = new ServerSocket( 23000 );
                Socket socket;
                connecting = true;
                for( int i = 0; i < MAX_PLAYERS; ++i ){     // later while( nie wciśnięto "RUN GAME" ), for now 2 clients will be accepted
                    socket = ss.accept();
                    if( connecting ){
                        players.add( new Player( players.size() + 100, socket ) );  // new player with id: 100, 101, 102 itd.
                        players.get( i ).start();       // starting thread for player
                        int finalI = i;
                        String nickname = players.get( finalI ).getNickname();
                        for( int j = 0; j < players.size() - 1; ++j ){
                            if( players.get( j ).name.equals( nickname ) ){       // same nickname
                                sendMsg( players.get( finalI ).id, "0" + COM_SPLITTER + "wrongNickname" );
                                players.remove( players.lastElement() );
                                break;
                            }
                        }
                        if( i >= players.size() )   //was same nickname
                            --i;
                        else{
                            if( finalI >= MIN_PLAYERS - 1 ) startGame.setDisable( false );
                            Platform.runLater( () -> playersLabel.setText( playersLabel.getText() + " " + players.get( finalI ).name + "," ) );
                            sendMsg( players.get( finalI ).id, "0" + COM_SPLITTER + "ok" );
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
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "cardChooser.fxml" ) );
        Stage stage = new Stage();
        stage.setTitle( "Choose card" );
        stage.setScene( new Scene( fxmlLoader.load(), 720, 480 ) );
        stage.show();
        CardChooser cardChooser = fxmlLoader.getController();
        cardChooser.setPlayers( players.size() );
        cardChooser.setServer( this );
        startGame.setVisible( false );
        runServer.setVisible( false );
        playersLabel.setVisible( false );
        logField.setVisible( true );
        voteButton.setVisible( true );
    }

    @FXML void orderVoting(){
        voteButton.setDisable( true );
        voteButton.setVisible( false );
        endVotingButton.setVisible( true );
        endVotingButton.setDisable( false );
        sendGame( 0, "VOTE" );
        votes = new int[ players.size() ];
        Arrays.fill( votes, 0 );
        for( Player player: players ){
            playerReaders.add( new Thread( () -> {
                String vote;
                try{
                    vote = receiveGame( player.id );
                    int votedPlayerIdx = players.indexOf( getPlayer( vote ) );
                    if( votedPlayerIdx != -1 ){
                        ++votes[ votedPlayerIdx ];
                    }
                }catch( IOException ignored ){}
            } ) );
            playerReaders.lastElement().start();
        }
    }

    @FXML void endVoting(){
        for( Thread t: playerReaders ){
            t.interrupt();
        }
        playerReaders.removeAllElements();
        countVotes();
    }

    private void countVotes(){
        int max = Arrays.stream( votes ).max().getAsInt();
        int maxIdx = Arrays.asList( votes ).indexOf( max );
        boolean temp = true;
        if( maxIdx != -1 ){      //no one voted
            votes[ maxIdx ] = -1;
            temp = false;
        }
        if( !temp || Arrays.stream( votes ).max().getAsInt() == max )       // same votes quantity
            orderVoting();                                          // vote again
        else{
            sendGame( 0, players.get( maxIdx ).name );
            sendAllPlayers();
        }
    }

    private void sendAllPlayers(){
        StringBuilder str = new StringBuilder();
        for( String cardNow: cardsNow )
            str.append( cardNow ).append( Game.MSG_SPLITTER );
        sendGame( 0, str.toString() );
    }

    public void setSelectedCards( LinkedList< String > selectedCards ){
        cardsInGame = selectedCards;
    }

    void drawCards(){   // Randomly give cards to players and 3 on the table
        Random rand = new Random();
        LinkedList< String > temp = new LinkedList<>( cardsInGame );

        //todo to remove when not testing with one player
        cardsOnBegin.add( "Copycat" );
        cardsNow.add( cardsOnBegin.get( 0 ) );
        temp.remove( "Copycat" );

        for( int i = 0; i < 3; ++i ){
            int randInt = rand.nextInt( temp.size() );
            cardsInCenter[ i ] = temp.get( randInt );
            temp.remove( randInt );
        }
        for( int i = 1; i < players.size(); ++i ){      //todo to i=0 when not testing with one player
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

    void startGame() throws IOException, InterruptedException{
        Game game = new Game( this );
        game.start();
    }

    public void writeLog( String log ){
        Platform.runLater( () -> logField.setText( logField.getText() + "\n" + log ) );
    }

    //Comunication
    void sendGame( int id, String msg ){
        if( id == 0 ){
            for( Player player : players ){
                sendMsg( player.id, gameMsg + COM_SPLITTER + msg );
            }
        }
        else
            sendMsg( id, gameMsg + COM_SPLITTER + msg );     //send msg of type gameMsg
    }
    String receiveGame( int id ) throws IOException{
        return receiveMsg( id ).split( COM_SPLITTER )[ 1 ];
    }

    void sendMsg( int id, String str ){
        players.get( id - 100 ).output.println( str );
    }

    String receiveMsg( int id ) throws IOException{
        return players.get( id - 100 ).input.readLine();
    }

    public int getTableCardId( String str ){
        switch( str ){
            case "card0" -> { return 0; }
            case "card1" -> { return 1; }
            case "card2" -> { return 2; }
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
        return null;
    }

    @FXML private TextArea logField;
    @FXML private Button runServer;
    @FXML private Button startGame;
    @FXML public Button voteButton;
    @FXML private Button endVotingButton;
    @FXML private Label playersLabel;

    public class Player extends Thread{
        public int id;
        public String name;
        public BufferedReader input;
        public PrintWriter output;

        Player( int id, Socket socket ) throws IOException{
            this.id = id;
            this.input = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            this.output = new PrintWriter( socket.getOutputStream(), true );
        }

        @Override
        public void run(){
            System.out.println( "Client connected." );
        }

        public String getNickname(){
            try{
                this.name = input.readLine().split( COM_SPLITTER )[ 1 ];
            }
            catch( NullPointerException | IOException | ArrayIndexOutOfBoundsException e ){
                this.name = "player" + id;
            }
            return this.name;
        }
    }
}

