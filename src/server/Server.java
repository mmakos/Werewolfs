package server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.stream.IntStream;

public class Server{
    public Vector< Player > players = new Vector<>();
    public Vector< String > cardsOnBegin;
    public Vector< String > cardsNow;
    public List< String > cardsInGame;
    private static final int gameMsg = 1;
    private volatile boolean connecting = false;
    public String[] cardsInCenter;
    private final String COM_SPLITTER = String.valueOf( ( char )28 );
    private Thread connect;
    private CardChooser cardChooser;

    @FXML public void initialize(){
        cardsInCenter = new String[ 3 ];
    }

    @FXML void connect(){
        connect = new Thread( () -> {
            runServer.setDisable( true );
            ServerSocket ss;
            try{
                ss = new ServerSocket( 23000 );
                Socket socket;
                connecting = true;
                for( int i = 0; ; ++i ){     // later while( nie wciśnięto "RUN GAME" ), for now 2 clients will be accepted
                    socket = ss.accept();
                    if( connecting ){
                        players.add( new Player( players.size() + 100, socket ) );  // new player with id: 100, 101, 102 itd.
                        players.get( i ).start();       // starting thread for player
                        int finalI = i;
                        Platform.runLater( () -> playersLabel.setText( playersLabel.getText() + " " + players.get( finalI ).getNickname() + "," ) );
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
        playersLabel.setText( "Starting game..." );
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "cardChooser.fxml" ) );
        Stage stage = new Stage();
        stage.setTitle( "Choose card" );
        stage.setScene( new Scene( fxmlLoader.load(), 720, 480 ) );
        stage.showAndWait();
        cardChooser = fxmlLoader.getController();
        cardChooser.setPlayers( players.size() );
        cardChooser.setServer( this );
    }

    public void setSelectedCards( String[] selectedCards ){
        cardsInGame = Arrays.asList( selectedCards );
    }

    void giveAwayCards(){   // Randomly give cards to players and 3 on the table
        Random rand = new Random();
        List< String > temp = cardsInGame;
        for( int i = 0; i < 3; ++i ){
            cardsInCenter[ i ] = temp.get( rand.nextInt( temp.size() ) );
            temp.remove( i );
        }
        for( int i = 0; i < players.size(); ++i ){
            cardsOnBegin.add( temp.get( rand.nextInt( temp.size() ) ) );
            cardsNow.add( cardsOnBegin.get( i ) );
            temp.remove( i );
        }
    }

    void sendCardsToPlayers(){} //TODO

    //Comunication
    void sendGame( int id, String msg ) throws IOException{
        if( id == 0 ){
            for( Player player : players ){
                sendMsg( player.id, msg );
            }
        }
        else
            sendMsg( id, gameMsg + COM_SPLITTER + msg );     //send msg of type gameMsg
    }
    String receiveGame( int id ) throws IOException{
        return receiveMsg( id ).split( COM_SPLITTER )[ 1 ];
    }

    void sendMsg( int id, String str ) throws IOException{
        players.get( id - 100 ).output.println( str );
    }

    String receiveMsg( int id ) throws IOException{
        return players.get( id - 100 ).input.readLine();
    }

    @FXML private Button runServer;
    @FXML private Button startGame;
    @FXML private Label playersLabel;

    public class Player extends Thread{
        public int id;
        public String name;
        private final Socket socket;
        public BufferedReader input;
        public PrintWriter output;

        Player( int id, Socket socket ) throws IOException{
            this.id = id;
            this.socket = socket;
            this.input = new BufferedReader( new InputStreamReader( this.socket.getInputStream() ) );
            this.output = new PrintWriter( this.socket.getOutputStream(), true );
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

