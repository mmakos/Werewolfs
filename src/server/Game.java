package server;

import java.io.IOException;
import java.util.*;

public class Game{
    private final Server s;
    private int idOfCopycat;        // Needed to show on the end whowas copycat
    public final static String MSG_SPLITTER = String.valueOf( ( char )29 );

    Game( Server server ){
        this.s = server;
    }

    void start() throws IOException, InterruptedException{
        s.writeLog( "Game started\n" );
        boolean insomniac = false;
        if( s.cardsInGame.contains( "Insomniac" ) ){
            insomniac = true;
            s.cardsInGame.remove( "Insomniac" );
        }
        if( s.cardsInGame.contains( "Copycat" ) )
            makeCopycat();
        boolean isWerewolf = false;
        for( int i = 0; i < Card.werewolvesQuant; ++i ){
            if( s.cardsInGame.contains( "Werewolf_" + i ) ){
                isWerewolf = true;
                break;
            }
        }
        String werewolvesMsg = "";
        if( isWerewolf )
            werewolvesMsg = makeWerewolves();
        if( s.cardsInGame.contains( "Mystic wolf" ) )
            makeMysticWolf();

        Random rand = new Random();

        // TODO main loop, here we have to put all the cards, and call theirs functions
        while( s.cardsInGame.size() > 0 ){
            switch( s.cardsInGame.get( rand.nextInt( s.cardsInGame.size() ) ) ){
                case "Witch" -> makeWitch();
                case "Beholder" -> makeBeholder();
                case "Seer" -> makeSeer();
                case "Minion" -> makeMinion( werewolvesMsg );
            }
        }
        if( insomniac )
            makeInsomniac();
        s.sendGame( 0, "WakeUp" );
    }

    //new function copycat
    void makeCopycat() throws IOException, InterruptedException{
        idOfCopycat = startRole( "Copycat" );
        if( idOfCopycat < 0 ) return;

        //receive his moves, which is generally numbers splitted with (char)29 sign, here is one number, but for example Seer will send two numbers
        String chosenCard = s.receiveGame( idOfCopycat ).split( MSG_SPLITTER )[ 0 ];     // first received number
        s.writeLog( "Player chose card " + chosenCard );
        //send name of card, which player has become
        int chosenCardId = s.getTableCardId( chosenCard );
        s.sendGame( idOfCopycat, s.cardsInCenter[ chosenCardId ] );
        s.writeLog( "This card is " + s.cardsInCenter[ chosenCardId ] );
        //Change his card information on server
        s.cardsNow.set( s.cardsOnBegin.indexOf( "Copycat" ), s.cardsInCenter[ chosenCardId ] );
        s.cardsOnBegin.set( s.cardsOnBegin.indexOf( "Copycat" ), s.cardsInCenter[ chosenCardId ] );

        //Remove this card from list of cards (we don't want to make copycat move again)
        s.cardsInGame.remove( "Copycat" );
    }

    String makeWerewolves() throws InterruptedException{
        s.sendGame( 0, "WEREWOLF" );
        s.writeLog( "Werewolf" + "'s move" );
        StringBuilder str = new StringBuilder();
        boolean isAnyoneWerewolf = false;
        if( s.cardsOnBegin.contains( "Mystic wolf" ) ) isAnyoneWerewolf = true;
        else{
            for( int i = 0; i < Card.werewolvesQuant; ++i ){
                if( s.cardsOnBegin.contains( "Werewolf_" + i ) ){
                    isAnyoneWerewolf = true;
                    break;
                }
            }
        }
        if( !isAnyoneWerewolf ){
            s.writeLog( "All werewolves are on table" );
            Thread.sleep( 5000 );
        }
        else{
            Vector< Integer > werewolves = new Vector<>();
            for( int i = 0; i < Card.werewolvesQuant; ++i ){
                int indexOfWerewolf = s.cardsOnBegin.indexOf( "Werewolf_" + i );
                if( indexOfWerewolf != -1 ){
                    str.append( s.players.get( indexOfWerewolf ).name ).append( MSG_SPLITTER );
                    werewolves.add( s.players.get( indexOfWerewolf ).id );
                    s.writeLog( "Werewolf is player " + s.players.get( indexOfWerewolf ).id );
                }
            }
            int indexOfMystic = s.cardsOnBegin.indexOf( "Mystic wolf" );
            if( indexOfMystic != -1 ){
                str.append( s.players.get( indexOfMystic ) ).append( MSG_SPLITTER );
                werewolves.add( s.players.get( indexOfMystic ).id );
                s.writeLog( "Mystic wolf is player " + s.players.get( indexOfMystic ).id );
            }
            for( Integer werewolf: werewolves )
                s.sendGame( werewolf, str.toString() );
            Thread.sleep( 5000 );       //Not necessary, time for werewolves to meet together
        }
        for( int i = 0; i < Card.werewolvesQuant; ++i )
            s.cardsInGame.remove( "Werewolf_" + i );
        s.writeLog( "Werewolves fall asleep" );
        return str.toString();
    }

    void  makeMinion( String werewolvesMsg ) throws InterruptedException{
        int minionsId = startRole( "Minion" );
        if( minionsId < 0 ) return;
        s.sendGame( minionsId, werewolvesMsg );
        Thread.sleep( 5000 );
        s.cardsInGame.remove( "Minion" );
    }

    void makeMysticWolf() throws InterruptedException{
        startRole( "Mystic wolf" );
    }
    void makeWitch() throws InterruptedException{
        startRole( "Witch" );
    }
    void makeBeholder() throws InterruptedException{
        startRole( "Beholder" );
    }
    void makeSeer() throws InterruptedException{
        startRole( "Seer" );
    }
    void makeInsomniac() throws InterruptedException{
        startRole( "Insomniac" );
    }

    //function does same begin of every role and returns id of player with this role, if role was not on the middle
    int startRole( String card ) throws InterruptedException{
        s.sendGame( 0, card.toUpperCase() );
        s.writeLog( card + "'s move" );
        if( !s.cardsOnBegin.contains( card ) ){
            s.writeLog( card + " is on table" );
            Thread.sleep( 5000 );
            s.cardsInGame.remove( card );
            s.writeLog( card + " falls asleep" );
            return -1;
        }
        else{
            int idOfCard = s.players.get( s.cardsOnBegin.indexOf( card ) ).id;
            s.writeLog( card + " is player " + idOfCard );
            return idOfCard;
        }
    }

}
