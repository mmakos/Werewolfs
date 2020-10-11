package server;

import java.io.IOException;
import java.util.*;

public class Game{
    private final Server s;
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
                    case "Troublemaker": makeTroublemaker(); break;
                    case "Apprentice seer": makeApprenticeSeer(); break;
                }
            }catch( IOException e ){
                s.writeLog( "No response from " + card + "." );
            }
        }
        if( insomniac )
            makeInsomniac();
        s.sendGame( 0, "WakeUp" );
        s.voteButton.setDisable( false );
    }

    //new function copycat
    void makeCopycat() throws IOException, InterruptedException{
        int idOfCopycat = startRole( "Copycat" );
        if( idOfCopycat < 0 ) return;

        //receive his moves, which is generally numbers splitted with (char)29 sign, here is one number, but for example Seer will send two numbers
        String chosenCard = s.receiveGame( idOfCopycat );     // first received number
        s.writeLog( "Player chose card " + chosenCard );
        //send name of card, which player has become
        String cardName = s.cardsInCenter[ s.getTableCardId( chosenCard ) ];
        s.realCopycat = cardName;
        s.sendGame( idOfCopycat, cardName );
        s.writeLog( "This card is " + cardName );
        //Change his card information on server
        s.cardsOnBegin.set( s.cardsOnBegin.indexOf( "Copycat" ), cardName );

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

    void makeApprenticeSeer() throws InterruptedException, IOException {
        int idOfApprenticeSeer = startRole("Apprentice seer");
        if(idOfApprenticeSeer<0) return;
        String cardToSee = s.receiveGame( idOfApprenticeSeer ).split( MSG_SPLITTER )[ 0 ];
        int cardToSeeId = s.getTableCardId(cardToSee);
        s.sendGame( idOfApprenticeSeer, s.cardsInCenter[cardToSeeId] );
        s.writeLog(s.cardsInCenter[cardToSeeId]);
        Thread.sleep( 3000 );
        s.cardsInGame.remove( "Apprentice seer" );
    }

    void makeWitch() throws InterruptedException, IOException{
        int idOfWitch = startRole( "Witch" );
        if( idOfWitch < 0 ) return;
        String chosenCard = s.receiveGame( idOfWitch );
        String cardName = s.cardsInCenter[ s.getTableCardId( chosenCard ) ];
        s.sendGame( idOfWitch, cardName );
        String chosenPlayer = s.receiveGame( idOfWitch );
        int playersId = s.players.indexOf( s.getPlayer( chosenPlayer ) );
        s.cardsInCenter[ s.getTableCardId( chosenCard ) ] = s.cardsNow.get( playersId );
        s.cardsNow.set( playersId, cardName );
        s.cardsInGame.remove( "Witch" );
    }

    void makeTroublemaker() throws InterruptedException, IOException{
        int idOfTroublemaker = startRole( "Troublemaker" );
        if( idOfTroublemaker < 0 ) return;
        String[] chosenCards = s.receiveGame( idOfTroublemaker ).split( MSG_SPLITTER );
        Collections.swap( s.cardsNow, s.cardsNow.indexOf( chosenCards[ 0 ] ), s.cardsNow.indexOf( chosenCards[ 1 ] ) );
        s.cardsInGame.remove( "Troublemaker" );
    }
    void makeSeer() throws InterruptedException, IOException {
        int idOfSeer = startRole("Seer");
        if(idOfSeer<0) return;
        String[] chosenCards = s.receiveGame( idOfSeer ).split( MSG_SPLITTER );
        int chosenCard1ID = s.getTableCardId(chosenCards[0]);
        int chosenCard2ID = s.getTableCardId(chosenCards[1]);
        String chosenCard1 = s.cardsInCenter[chosenCard1ID];
        String chosenCard2 = s.cardsInCenter[chosenCard2ID];
        s.sendGame(idOfSeer,chosenCard1+MSG_SPLITTER+chosenCard2);
        s.cardsInGame.remove( "Seer" );
    }
    void makeBeholder() throws InterruptedException {
        int idofBeholder = startRole("Beholder");
        if (idofBeholder < 0) return;
        if( !s.cardsOnBegin.contains( "Seer" ) ) {
            int idOfSeer = s.players.get(s.cardsOnBegin.indexOf("Seer")).id;
            String idOfSeerStr = Integer.toString(idOfSeer);
            s.sendGame(idofBeholder, idOfSeerStr);
        }
        else{
            s.sendGame(idofBeholder,"NoSeer");
        }
    }
    void makeInsomniac() throws InterruptedException{
        int idOfInsomniac = startRole( "Insomniac" );
        if(idOfInsomniac<0) return;
        String insomniacNow = s.cardsNow.get(s.cardsOnBegin.indexOf("Insomniac"));
        s.writeLog( "This card is " + insomniacNow );
        s.sendGame(idOfInsomniac, insomniacNow );
        Thread.sleep( 3000 );
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
            if( card.split( "_" )[ 0 ].equals( "Tanner" ) || card.split( "_" )[ 0 ].equals( "Werewolf" ) || card.split( "_" )[ 0 ].equals( "Mystic wolf" ) ){
                s.realParanormal = card;
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
