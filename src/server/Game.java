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
                case "Witch": makeWitch(); break;
                case "Beholder": makeBeholder(); break;
                case "Seer": makeSeer(); break;
                case "Minion": makeMinion( werewolvesMsg ); break;
                case "Tanner": makeTanner(); break;
                case "Thing": makeThing(); break;
                case "Paranormal investigator": makeParanormal(); break;
            }
        }
        if( insomniac )
            makeInsomniac();
        s.sendGame( 0, "WakeUp" );
        s.voteButton.setDisable( false );
    }

    //new function copycat
    void makeCopycat() throws IOException, InterruptedException{
        idOfCopycat = startRole( "Copycat" );
        if( idOfCopycat < 0 ) return;

        //receive his moves, which is generally numbers splitted with (char)29 sign, here is one number, but for example Seer will send two numbers
        String chosenCard = s.receiveGame( idOfCopycat );     // first received number
        s.writeLog( "Player chose card " + chosenCard );
        //send name of card, which player has become
        int chosenCardId = s.getTableCardId( chosenCard );
        s.sendGame( idOfCopycat, s.cardsInCenter[ chosenCardId ] );
        s.writeLog( "This card is " + s.cardsInCenter[ chosenCardId ] );
        //Change his card information on server
        //s.cardsNow.set( s.cardsOnBegin.indexOf( "Copycat" ), s.cardsInCenter[ chosenCardId ] );   - to chyba nie
        s.cardsOnBegin.set( s.cardsOnBegin.indexOf( "Copycat" ), s.cardsInCenter[ chosenCardId ] );

        //Remove this card from list of cards (we don't want to make copycat move again)
        s.cardsInGame.remove( "Copycat" );
        Thread.sleep( 2000 );
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

    void makeMysticWolf() throws InterruptedException, IOException {
        int idOfMysticWolf = startRole("MysticWolf");
        if(idOfMysticWolf<0) return;
        String cardToSee = s.receiveGame( idOfMysticWolf ).split( MSG_SPLITTER )[ 0 ];
        int cardToSeeId = s.getTableCardId(cardToSee);
        s.sendGame( idOfMysticWolf, s.cardsInCenter[cardToSeeId] );
        s.writeLog(s.cardsInCenter[cardToSeeId]);
    }
    void makeWitch() throws InterruptedException{
        if(startRole( "Witch" )<0) return;
    }
    void makeBeholder() throws InterruptedException{
        if(startRole( "Beholder" )<0) return;
    }
    void makeSeer() throws InterruptedException{
        if(startRole( "Seer" )<0) return;

    }
    void makeInsomniac() throws InterruptedException{
        int idOfInsomniac = startRole( "Insomniac" );
        if(idOfInsomniac<0) return;
        String insomniacNow = s.cardsNow.get(s.cardsOnBegin.indexOf("Insomniac"));
        s.writeLog( "This card is " + insomniacNow );
        s.sendGame(idOfInsomniac, insomniacNow );
        String idOfInsomniacString = Integer.toString(idOfInsomniac);
        s.sendGame(idOfInsomniac, idOfInsomniacString );
        Thread.sleep( 5000 );
        s.cardsInGame.remove( "Insomniac" );
    }

    void makeTanner(){
        s.cardsInGame.remove( "Tanner" );
    }

    void makeThing() throws InterruptedException, IOException{
        int thingId = startRole( "Thing" );
        if( thingId < 0 ){
            s.sendGame( 0, "NOTHING" );
            return;
        }
        String chosenCard = s.receiveGame( thingId );
        for( Server.Player player: s.players ){
            if( player.name.equals( chosenCard ) )
                s.sendGame( player.id, "TOUCH" );
            else
                s.sendGame( player.id, "NOTHING" );
        }
        Thread.sleep( 3000 );
        s.cardsInGame.remove( "Thing" );
    }

    void makeParanormal() throws InterruptedException, IOException{
        int paranormalId = startRole( "Paranormal investigator" );
        if( paranormalId < 0 ) return;
        for( int i = 0; i < 2; ++i ){
            String chosenCard = s.receiveGame( paranormalId );
            String card = s.getPlayersCard( chosenCard );
            if( card.split( "_" )[ 0 ].equals( "Tanner" ) || card.split( "_" )[ 0 ].equals( "Werewolf" ) ){
                int chosenPlayerIdx = s.players.indexOf( s.getPlayer( chosenCard ) );
                int paranormalIdx = s.players.indexOf( s.getPlayer( paranormalId ) );
                String paranormalsCard = s.cardsNow.get( paranormalIdx );
                s.cardsNow.set( paranormalIdx, card );
                s.cardsNow.set( chosenPlayerIdx, paranormalsCard );
                s.sendGame( paranormalId, card );
                break;
            }
            else
                s.sendGame( paranormalId, "AGAIN" );
        }
        Thread.sleep( 3000 );
        s.cardsInGame.remove( "Paranormal investigator" );
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
