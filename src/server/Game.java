package server;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

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
            }catch( TimeoutException e ){
                s.writeLog( "No response from copycat." );
            }
            s.cardsInGame.remove( "Copycat" );
        }
        boolean isWerewolf = false;
        for( int i = 0; i < CardChooser.werewolvesQuant; ++i ){
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
            }catch( TimeoutException e ){
                s.writeLog( "No response from werewolves." );
            }
            for( int i = 0; i < CardChooser.werewolvesQuant; ++i )
                s.cardsInGame.remove( "Werewolf_" + i );
        }
        if( s.cardsInGame.contains( "Mystic wolf" ) ){
            try{
                makeMysticWolf();
            }catch( TimeoutException e ){
                s.writeLog( "No response from mystic wolf." );
            }
            s.cardsInGame.remove( "Mystic wolf" );
        }
        if( s.cardsInGame.contains( "Minion" ) )
            makeMinion( werewolvesMsg );

        Random rand = new Random();

        while( s.cardsInGame.size() > 0 ){
            String card = s.cardsInGame.get( rand.nextInt( s.cardsInGame.size() ) );
            try{
                switch( card ){
                    case "Witch": makeWitch(); break;
                    case "Beholder": makeBeholder(); break;
                    case "Seer": makeSeer(); break;
                    case "Tanner": break;
                    case "Thing": makeThing(); break;
                    case "Paranormal investigator": makeParanormal(); break;
                    case "Robber": makeRobber(); break;
                    case "Troublemaker": makeTroublemaker(); break;
                    case "Apprentice seer": makeApprenticeSeer(); break;
                }
            }catch( TimeoutException e ){
                s.writeLog( "No response from " + card + "." );
            }
            s.cardsInGame.remove( card );
        }
        if( insomniac )
            makeInsomniac();
        s.send( 0, "WakeUp" );
        s.writeLog( "City wakes up.\nWhen you are ready for voting, press \"Order voting\" button." );
        s.voteButton.setDisable( false );
    }

    //new function copycat
    void makeCopycat() throws InterruptedException, TimeoutException{
        int idOfCopycat = startRole( "Copycat" );
        if( idOfCopycat < 0 ) return;

        //receive his moves, which is generally numbers splitted with (char)29 sign, here is one number, but for example Seer will send two numbers
        String chosenCard = s.receive( idOfCopycat );     // first received number
        //s.writeLog( "Player chose card " + chosenCard );
        //send name of card, which player has become
        String cardName = s.cardsInCenter[ s.getTableCardId( chosenCard ) ];
        s.realCopycat = cardName;
        s.send( idOfCopycat, cardName );
        //s.writeLog( "This card is " + cardName );
        //Change his card information on server
        s.cardsOnBegin.set( s.cardsOnBegin.indexOf( "Copycat" ), cardName );

        //Remove this card from list of cards (we don't want to make copycat move again)
        s.cardsInGame.remove( "Copycat" );
        Thread.sleep( 3000 );
    }

    String makeWerewolves() throws InterruptedException, TimeoutException{
        s.send( 0, "WEREWOLF" );
        s.writeLog( "Werewolf" + "'s move" );
        StringBuilder str = new StringBuilder();
        boolean isAnyoneWerewolf = false;
        if( s.cardsOnBegin.contains( "Mystic wolf" ) ) isAnyoneWerewolf = true;
        else{
            for( int i = 0; i < CardChooser.werewolvesQuant; ++i ){
                if( s.cardsOnBegin.contains( "Werewolf_" + i ) ){
                    isAnyoneWerewolf = true;
                    break;
                }
            }
        }
        //Not necessary, time for werewolves to meet together
        if( isAnyoneWerewolf ){
            Vector< Integer > werewolves = new Vector<>();
            for( int i = 0; i < CardChooser.werewolvesQuant; ++i ){
                int indexOfWerewolf = s.cardsOnBegin.indexOf( "Werewolf_" + i );
                if( indexOfWerewolf != -1 ){
                    str.append( s.players.get( indexOfWerewolf ).name ).append( MSG_SPLITTER );
                    werewolves.add( s.players.get( indexOfWerewolf ).id );
                    //s.writeLog( "Werewolf is player " + s.players.get( indexOfWerewolf ).id );
                }
            }
            int indexOfMystic = s.cardsOnBegin.indexOf( "Mystic wolf" );
            if( indexOfMystic != -1 ){
                str.append( s.players.get( indexOfMystic ).name ).append( MSG_SPLITTER );
                werewolves.add( s.players.get( indexOfMystic ).id );
                //s.writeLog( "Mystic wolf is player " + s.players.get( indexOfMystic ).id );
            }
            for( Integer werewolf: werewolves )
                s.send( werewolf, str.toString() );
            // Lone werewolf
            if( werewolves.size() == 1 ){
                String cardToSee = s.receive( werewolves.get( 0 ) ).split( MSG_SPLITTER )[ 0 ];
                int cardToSeeId = s.getTableCardId(cardToSee);
                s.send( werewolves.get( 0 ), s.cardsInCenter[ cardToSeeId ] );
            }
        }
        Thread.sleep( 7000 );
        s.writeLog( "Werewolves fall asleep" );
        return str.toString();
    }

    void  makeMinion( String werewolvesMsg ) throws InterruptedException{
        int minionsId = startRole( "Minion" );
        if( minionsId < 0 ) return;
        s.send( minionsId, werewolvesMsg );
        Thread.sleep( 7000 );
        s.cardsInGame.remove( "Minion" );
        s.writeLog( "Minion falls asleep" );
    }

    void makeMysticWolf() throws InterruptedException, TimeoutException{
        int idOfMysticWolf = startRole("Mystic wolf");
        if(idOfMysticWolf<0) return;
        String cardToSee = s.receive( idOfMysticWolf ).split( MSG_SPLITTER )[ 0 ];
        int cardToSeeId = s.getTableCardId(cardToSee);
        s.send( idOfMysticWolf, s.cardsInCenter[cardToSeeId] );
        //s.writeLog(s.cardsInCenter[cardToSeeId]);
        Thread.sleep( 3000 );
        s.writeLog( "Mystic wolf falls asleep" );
    }

    void makeApprenticeSeer() throws InterruptedException, TimeoutException{
        int idOfApprenticeSeer = startRole("Apprentice seer");
        if(idOfApprenticeSeer<0) return;
        String cardToSee = s.receive( idOfApprenticeSeer ).split( MSG_SPLITTER )[ 0 ];
        int cardToSeeId = s.getTableCardId(cardToSee);
        s.send( idOfApprenticeSeer, s.cardsInCenter[cardToSeeId] );
        //s.writeLog(s.cardsInCenter[cardToSeeId]);
        Thread.sleep( 3000 );
        s.writeLog( "Apprentice seer falls asleep" );
    }

    void makeWitch() throws InterruptedException, TimeoutException{
        int idOfWitch = startRole( "Witch" );
        if( idOfWitch < 0 ) return;
        String chosenCard = s.receive( idOfWitch );
        String cardName = s.cardsInCenter[ s.getTableCardId( chosenCard ) ];
        s.send( idOfWitch, cardName );
        String chosenPlayer = s.receive( idOfWitch );
        int playersId = s.players.indexOf( s.getPlayer( chosenPlayer ) );
        s.cardsInCenter[ s.getTableCardId( chosenCard ) ] = s.cardsNow.get( playersId );
        s.cardsNow.set( playersId, cardName );
        Thread.sleep( 3000 );
        s.writeLog( "Witch falls asleep" );
    }

    void makeTroublemaker() throws InterruptedException, TimeoutException{
        int idOfTroublemaker = startRole( "Troublemaker" );
        if( idOfTroublemaker < 0 ) return;
        String[] chosenCards = s.receive( idOfTroublemaker ).split( MSG_SPLITTER );
        Collections.swap( s.cardsNow, s.players.indexOf( s.getPlayer( chosenCards[ 0 ] ) ), s.players.indexOf( s.getPlayer( chosenCards[ 1 ] ) ) );
        s.writeLog( "Troublemaker falls asleep" );
    }

    void makeSeer() throws InterruptedException, TimeoutException{
        int idOfSeer = startRole( "Seer" );
        if( idOfSeer < 0 ) return;
        String[] chosenCards = s.receive( idOfSeer ).split( MSG_SPLITTER );
        int chosenCard1ID = s.getTableCardId( chosenCards[ 0 ] );
        int chosenCard2ID = s.getTableCardId( chosenCards[ 1 ] );
        String chosenCard1 = s.cardsInCenter[ chosenCard1ID ];
        String chosenCard2 = s.cardsInCenter[ chosenCard2ID ];
        s.send( idOfSeer, chosenCard1 + MSG_SPLITTER + chosenCard2 );
        Thread.sleep( 3000 );
        s.writeLog( "Seer falls asleep" );
    }

    void makeBeholder() throws InterruptedException {
        int idOfBeholder = startRole( "Beholder" );
        if( idOfBeholder < 0 ) return;
        if( s.cardsOnBegin.contains( "Seer" ) ){
            String seerName = s.players.get( s.cardsOnBegin.indexOf( "Seer" ) ).name;
            s.send( idOfBeholder, seerName );
        } else
            s.send( idOfBeholder, "NoSeer" );
        Thread.sleep( 5000 );
        s.writeLog( "Beholder falls asleep" );
    }
    void makeInsomniac() throws InterruptedException{
        int idOfInsomniac = startRole( "Insomniac" );
        if( idOfInsomniac < 0 ) return;
        String insomniacNow = s.cardsNow.get( s.cardsOnBegin.indexOf( "Insomniac" ) );
        //s.writeLog( "This card is " + insomniacNow );
        s.send( idOfInsomniac, insomniacNow );
        Thread.sleep( 5000 );
        s.cardsInGame.remove( "Insomniac" );
        s.writeLog( "Insomniac falls asleep" );
    }

    void makeThing() throws InterruptedException, TimeoutException{
        int thingId = startRole( "Thing" );
        if( thingId < 0 ){
            s.send( 0, "NOTHING" );
            return;
        }
        String chosenCard = s.receive( thingId );
        for( Server.Player player: s.players ){
            if( player.name.equals( chosenCard ) )
                s.send( player.id, "TOUCH" );
            else
                s.send( player.id, "NOTHING" );
        }
        Thread.sleep( 3000 );
        s.writeLog( "Thing falls asleep" );
    }

    void makeParanormal() throws InterruptedException, TimeoutException{
        int paranormalId = startRole( "Paranormal investigator" );
        if( paranormalId < 0 ) return;
        for( int i = 0; i < 2; ++i ){
            String chosenCard = s.receive( paranormalId );
            String card = s.cardsNow.get( s.players.indexOf( s.getPlayer( chosenCard ) ) );
            s.send( paranormalId, card );
            if( card.split( "_" )[ 0 ].equals( "Tanner" ) || card.split( "_" )[ 0 ].equals( "Werewolf" ) || card.split( "_" )[ 0 ].equals( "Mystic wolf" ) ){
                s.realParanormal = card;
                break;
            }
        }
        Thread.sleep( 3000 );
        s.writeLog( "Paranormal investigator falls asleep" );
    }

    void makeRobber() throws InterruptedException, TimeoutException{
        int robberId = startRole( "Robber" );
        if( robberId < 0 ) return;
        String chosenCard = s.receive( robberId );
        String card = s.getPlayersCard( chosenCard );
        int chosenPlayerIdx = s.players.indexOf( s.getPlayer( chosenCard ) );
        int paranormalIdx = s.players.indexOf( s.getPlayer( robberId ) );
        String paranormalsCard = s.cardsNow.get( paranormalIdx );
        s.cardsNow.set( paranormalIdx, card );
        s.cardsNow.set( chosenPlayerIdx, paranormalsCard );
        s.send( robberId, card );
        Thread.sleep( 3000 );
        s.writeLog( "Robber falls asleep" );
    }

    //function does same begin of every role and returns id of player with this role, if role was not on the middle
    int startRole( String card ) throws InterruptedException{
        int playerIdx = s.cardsOnBegin.indexOf( card );
        Server.Player p = null;
        if( playerIdx != -1 ){
            p = s.players.get( playerIdx );
            p.clearMsgQueue();
        }

        s.send( 0, card.toUpperCase() );
        s.writeLog( card + "'s move" );
        if( p == null || !p.isActive ){
            //s.writeLog( card + " is on table" );
            Thread.sleep( 7000 );
            s.cardsInGame.remove( card );
            s.writeLog( card + " falls asleep" );
            return -1;
        }
        else{
            //s.writeLog( card + " is player " + idOfCard );
            return p.id;
        }
    }
}
