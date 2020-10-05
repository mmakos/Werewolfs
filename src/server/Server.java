package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.IntStream;

public class Server{
    public Vector< Player > players;
    public Vector< String > cardsOnBegin;
    public Vector< String > cardsNow;
    public List< String > cardsInGame;
    private static final int gameMsg = 1;
    public String[] cardsInCenter;
    private final String COM_SPLITTER = String.valueOf( ( char )28 );

    Server() throws IOException{
        cardsInCenter = new String[ 3 ];
    }

    void connect() throws IOException{
        ServerSocket ss = new ServerSocket( 23000 );
        Socket socket = new Socket();
        for( int i = 0; i < 2; ++ i ){     // later while( nie wciśnięto "RUN GAME" ), for now 2 clients will be accepted
            socket = ss.accept();
            players.add( new Player( players.size() + 100, socket ) );  // new player with id: 100, 101, 102 itd.
            players.get( i ).start();       // starting thread for player
        }
    }

    void collectPlayers(){  //TODO to remove, when connect() done
        // add as many players as cards - 3
        IntStream.range( 0, Card.card.length - 3 ).forEach( i -> {
            try{
                players.add( new Player( i + 99, new Socket() ) );
            }catch( IOException e ){
                e.printStackTrace();
            }
        } );
    }

    void chooseCards(){
        //TODO function somehow have to fulfill cards in game vector
        IntStream.range( 0, Card.card.length ).forEach( i -> cardsInGame.add( Card.card[ i ] ) );   //Add all cards, for now
    }

    void runGame() throws IOException{
        giveAwayCards();
        sendCardsToPlayers();
        Game game = new Game( this );
        game.start();
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
        players.get( id - 100 ).output.writeUTF( str );
    }

    String receiveMsg( int id ) throws IOException{
        return players.get( id - 100 ).input.readUTF();
    }

    public class Player extends Thread{
        public int id;
        public String name;
        private final Socket socket;
        public DataInputStream input;
        public DataOutputStream output;

        Player( int id, Socket socket ) throws IOException{
            this.id = id;
            this.socket = socket;
            this.input = new DataInputStream( socket.getInputStream() );
            this.output = new DataOutputStream( socket.getOutputStream() );
        }

        @Override
        public void run(){
            System.out.println( "server.Card.Client connected." );
        }
    }

    public static void main( String[] args ) throws IOException{
        Server server = new Server();
        server.connect();
        server.runGame();
    }
}

