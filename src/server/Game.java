package server;

import java.io.IOException;
import java.util.*;

public class Game{
    private final Server s;
    private int idOfCopycat;        // Needed to show on the end who was copycat
    private int paranormalId;       // --- || ---
    public final static String MSG_SPLITTER = String.valueOf( ( char )29 );

    Game( Server server ){
        this.s = server;
    }

    void start() throws InterruptedException{
        s.writeLog( "Game started\n" );
        boolean insomniac = false;
        if( s.cardsInGame.contains( "Insomniac" ) ){
            insomniac = true;
            s.cardsInGame.remove( "Insomniac" );
        }
        if( s.cardsInGame.contains( "Copycat" ) ){
            try{
                makeCopycat();
            }catch( IOException e ){
                s.writeLog( "No response from copycat." );
            }
        }
        boolean isWerewolf = false;
        for( int i = 0; i < Card.werewolvesQuant; ++i ){
            if( s.cardsInGame.contains( "Werewolf_" + i ) ){
                isWerewolf = true;
                break;
            }
        }
        if( s.cardsInGame.contains( "Mystic wolf" ) ) isWerewolf = true;
        String werewolvesMsg = "";
        if( isWerewolf ){
            try{
                werewolvesMsg = makeWerewolves();
            }catch( IOException e ){
                s.writeLog( "No response from werewolves." );
            }
        }
        if( s.cardsInGame.contains( "Minion" ) )
            makeMinion( werewolvesMsg );
        if( s.cardsInGame.contains( "Mystic wolf" ) ){
            try{
                makeMysticWolf();
            }catch( IOException e ){
                s.writeLog( "No response from mystic wolf." );
            }
        }

        Random rand = new Random();

        // TODO main loop, here we have to put all the cards, and call theirs functions
        while( s.cardsInGame.size() > 0 ){
            String card = s.cardsInGame.get( rand.nextInt( s.cardsInGame.size() ) );
            try{
                switch( card ){
                    case "Witch": makeWitch(); break;
                    case "Beholder": makeBeholder(); break;
                    case "Seer": makeSeer(); break;
                    case "Tanner": makeTanner(); break;
                    case "Thing": makeThing(); break;
                    case "Paranormal investigator": makeParanormal(); break;
                    case "Robber": makeRobber(); break;
                }
            } catch( IOException e ){
                s.writeLog( "No response from " + card + "." );
            }
        }
        if( insomniac )
            makeInsomniac();
        s.sendGame( 0, "WakeUp" );
        s.voteButton.setDisable( false );
        try{
            s.cardsInGame.set( s.players.indexOf( s.getPlayer( idOfCopycat ) ), "Copycat" );
        }catch( IndexOutOfBoundsException ignored ){}
        try{
            s.cardsInGame.set( s.players.indexOf( s.getPlayer( paranormalId ) ), "Paranormal investigator" );
        }catch( IndexOutOfBoundsException ignored ){}
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

    String makeWerewolves() throws InterruptedException, IOException{
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
                str.append( s.players.get( indexOfMystic ).name ).append( MSG_SPLITTER );
                werewolves.add( s.players.get( indexOfMystic ).id );
                s.writeLog( "Mystic wolf is player " + s.players.get( indexOfMystic ).id );
            }
            for( Integer werewolf: werewolves )
                s.sendGame( werewolf, str.toString() );
            // Lone werewolf
            if( werewolves.size() == 1 ){
                String cardToSee = s.receiveGame( werewolves.get( 0 ) ).split( MSG_SPLITTER )[ 0 ];
                int cardToSeeId = s.getTableCardId(cardToSee);
                s.sendGame( werewolves.get( 0 ), s.cardsInCenter[ cardToSeeId ] );
            }
            Thread.sleep( 3000 );       //Not necessary, time for werewolves to meet together
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
        int idOfMysticWolf = startRole("Mystic wolf");
        if(idOfMysticWolf<0) return;
        String cardToSee = s.receiveGame( idOfMysticWolf ).split( MSG_SPLITTER )[ 0 ];
        int cardToSeeId = s.getTableCardId(cardToSee);
        s.sendGame( idOfMysticWolf, s.cardsInCenter[cardToSeeId] );
        s.writeLog(s.cardsInCenter[cardToSeeId]);
        Thread.sleep( 3000 );
        s.cardsInGame.remove( "Mystic wolf" );
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
//        String idOfInsomniacString = Integer.toString(idOfInsomniac);         // TODO po co to jest?
//        s.sendGame(idOfInsomniac, idOfInsomniacString );
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
        paranormalId = startRole( "Paranormal investigator" );
        if( paranormalId < 0 ) return;
        for( int i = 0; i < 2; ++i ){
            String chosenCard = s.receiveGame( paranormalId );
            String card = s.getPlayersCard( chosenCard );
            if( card.split( "_" )[ 0 ].equals( "Tanner" ) || card.split( "_" )[ 0 ].equals( "Werewolf" ) || card.split( "_" )[ 0 ].equals( "Mystic wolf" ) ){
                int paranormalIdx = s.players.indexOf( s.getPlayer( paranormalId ) );
                s.cardsOnBegin.set( paranormalIdx, card );
                s.sendGame( paranormalId, card );
                break;
            }
            else
                s.sendGame( paranormalId, "AGAIN" );
        }
        Thread.sleep( 3000 );
        s.cardsInGame.remove( "Paranormal investigator" );
    }

    void makeRobber() throws IOException, InterruptedException{
        int robberId = startRole( "Robber" );
        if( robberId < 0 ) return;
        String chosenCard = s.receiveGame( robberId );
        String card = s.getPlayersCard( chosenCard );
        int chosenPlayerIdx = s.players.indexOf( s.getPlayer( chosenCard ) );
        int paranormalIdx = s.players.indexOf( s.getPlayer( robberId ) );
        String paranormalsCard = s.cardsNow.get( paranormalIdx );
        s.cardsNow.set( paranormalIdx, card );
        s.cardsNow.set( chosenPlayerIdx, paranormalsCard );
        s.sendGame( robberId, card );
        Thread.sleep( 3000 );
        s.cardsInGame.remove( "Robber" );
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
