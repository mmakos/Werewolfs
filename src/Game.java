import java.util.*;

public class Game{
    private final Server s;
    private int idOfCopycat;        // Needed to show on the end whowas copycat
    private final String MSG_SPLITTER = String.valueOf( ( char )29 );

    Game( Server server ){
        this.s = server;
    }

    void start(){
        boolean insomniac = false;
        if( s.cardsInGame.contains( "Insomniac" ) ){
            insomniac = true;
            s.cardsInGame.remove( "Insomniac" );
        }
        makeCopycat();
        makeWerewolfs();
        makeMysticWolf();

        Random rand = new Random();

        // TODO main loop, here we have to put all the cards, and call theirs functions
        while( s.cardsInGame.size() != 0 ){
            switch( s.cardsInGame.get( rand.nextInt( s.cardsInGame.size() ) ) ){
                case "Witch" -> makeWitch();
                case "Beholder" -> makeBeholder();
                case "Seer" -> makeSeer();
            }
        }
        if( insomniac )
            makeInsomniac();

    }

    void makeCopycat(){
        //Check if copycat is not "one of 3 cards"
        if( !s.cardsNow.contains( "Copycat" ) ) return;

        //find players id who has copycat card
        idOfCopycat = s.players.get( s.cardsNow.indexOf( "Copycat" ) ).id;
        //send to all players info, whose move is now, proper player will make his move
        s.sendGame( 0, "COPYCAT" );     // 0 means, we will send msg to all players.
        //receive his moves, which is generally numbers splitted with "_" sign, here is one number, but for example Seer will send two numbers
        int chosenCardId = Integer.parseInt( s.receiveGame( idOfCopycat ).split( MSG_SPLITTER )[ 0 ] );     // first received number
        //send name of card, which player has become
        s.sendGame( idOfCopycat, s.cardsInCenter[ chosenCardId ] );
        //Change his card information on server
        s.cardsNow.set( s.cardsOnBegin.indexOf( "Copycat" ), s.cardsInCenter[ chosenCardId ] );
        s.cardsOnBegin.set( s.cardsOnBegin.indexOf( "Copycat" ), s.cardsInCenter[ chosenCardId ] );

        //Remove this card from list of cards (we don't want to make copycat move again)
        s.cardsInGame.remove( "Copycat" );
    }

    //TODO
    void makeWerewolfs(){}
    void makeMysticWolf(){}
    void makeWitch(){}
    void makeBeholder(){}
    void makeSeer(){}
    void makeInsomniac(){}

}
