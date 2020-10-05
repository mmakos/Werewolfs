import java.util.*;
import java.util.stream.IntStream;

public class Server{
    public Vector< Player > players;
    public Vector< String > cardsOnBegin;
    public Vector< String > cardsNow;
    public List< String > cardsInGame;
    public String[] cardsInCenter;
    private Game game;

    Server(){
        cardsInCenter = new String[ 3 ];
    }

    void collectPlayers(){  //TODO function listens for players and add them to players vector
        // add as many players as cards - 3
        IntStream.range( 0, Card.card.length - 3 ).forEach( i -> players.add( new Player( i + 99, "player" + Integer.toString( i ) ) ) );
    }

    void chooseCards(){
        //TODO function somehow have to fulfill cards in game vector
        IntStream.range( 0, Card.card.length ).forEach( i -> cardsInGame.add( Card.card[ i ] ) );   //Add all cards, for now
    }

    void runGame(){
        giveAwayCards();
        sendCardsToPlayers();
        game = new Game( this );
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
    void sendGame( int id, String msg ){} //TODO
    String receiveGame( int id ){ return new String(); }    //TODO

    public class Player{
        public int id;
        public String name;

        Player( int id, String name ){
            this.id = id;
            this.name = name;
        }
    }

    public static void main( String[] args ){}
}

