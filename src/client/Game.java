package client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class Game{
    private volatile boolean waitingForButton = false;
    private String clickedCard = "card0";
    private static final int nicknameType = 0;
    private static final int gameType = 1;
    public static final String COM_SPLITTER = String.valueOf( ( char )28 );
    public final static String MSG_SPLITTER = String.valueOf( ( char )29 );
    public final static String UNIQUE_CHAR = String.valueOf( ( char )2 );
    public final static int MAX_ROLE_TIME = 30;
    public Vector< String > players = new Vector<>();
    private String card;
    public String displayedCard;        // When you are copycat or paranormal then its value is different than card line above
    private GameWindow gameWindow;
    public String nickname;

    private MediaPlayer wakeUpSignal = null;
    private MediaPlayer roleSignal = null;

    public BufferedReader input;
    public PrintWriter output;

    Game( Socket socket ) throws IOException{
        this.input = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        this.output = new PrintWriter( socket.getOutputStream(), true );
        try{
            Media media2 = new Media( new File( "audio/role.wav" ).toURI().toString() );
            Media media = new Media( new File( "audio/wakeUp.mp3" ).toURI().toString() );
            roleSignal = new MediaPlayer( media2 );
            wakeUpSignal = new MediaPlayer( media );
        }
        catch( MediaException ignored ){}
    }

    public void run() throws IOException{
        getPlayers();
        getCard();
        gameWindow();
    }

    public void gameLogic(){
        Thread gameLogic = new Thread( () -> {
            while( true ){
                String msg = readMsgOnly();
                if( msg.equals( "WakeUp" ) ){
                    wakeUp();
                    break;
                }
                gameWindow.setStatementLabel( msg.substring( 0, 1 ) + msg.substring( 1 ).toLowerCase() + " wakes up" );
                if( msg.equals( card.split( "_" )[ 0 ].toUpperCase() ) || ( msg.equals( "WEREWOLF" ) && card.equals( "Mystic wolf" ) ) ){
                    gameWindow.setStatementLabel( msg.substring( 0, 1 ) + msg.substring( 1 ).toLowerCase() + " wakes up - YOUR TURN" );
                    proceedCard( msg.substring( 0, 1 ) + msg.substring( 1 ).toLowerCase() );
                }
                if( msg.equals( "THING" ) )
                    waitForTingsTouch();
            }
            if( readMsgOnly().equals( UNIQUE_CHAR + "VOTE" ) ){
                gameWindow.setStatementLabel( "Vote" );
                vote();
            }
        } );
        gameLogic.start();
    }

    private void proceedCard( String card ){
        try{
            roleSignal.seek( Duration.ZERO );
            roleSignal.play();
        }
        catch( NullPointerException ignored ){}

        switch( card.split( "_" )[ 0 ] ){
            case "Mystic wolf": makeMysticWolf(); break;
            case "Minion": makeMinion(); break;
            case "Copycat": makeCopycat(); break;
            case "Insomniac": makeInsomniac(); break;
            case "Werewolf": makeWerewolf(); break;
            case "Witch": makeWitch();
            case "Beholder": makeBeholder(); break;
            case "Seer": makeSeer(); break;
            case "Thing": makeThing(); break;
            case "Paranormal investigator": makeParanormal(); break;
            case "Robber": makeRobber(); break;
        }
    }

    void makeCopycat(){
        gameWindow.setRoleInfo( "Choose one card from the middle. From this moment you will become the card you chose." );
        waitingForButton = true;
        gameWindow.setTableCardsActive( true );

        // Waiting for clicked card, but with time limit of 30 seconds
        long start = System.currentTimeMillis();
        while( waitingForButton && System.currentTimeMillis() - start < MAX_ROLE_TIME * 1000 );
        // If time is up, card will be selected randomly
        if( waitingForButton ){
            int rand = new Random().nextInt( 3 );
            clickedCard = "card" + rand;
            gameWindow.setRoleInfo( "Time's up. Card will be randomly selected.\n" +
                    "On the top left corner you can see which card you were, and which card you are now." );
            waitingForButton = false;
        }
        else
            gameWindow.setRoleInfo( "On the top left corner you can see which card you were, and which card you are now." );
        gameWindow.setTableCardsActive( false );
        gameWindow.setTableCardsSelected( false );
        sendMsg( gameType, clickedCard );
        card = readMsgOnly();
        gameWindow.setStatementLabel( "You became " + card.split( "_" )[ 0 ] );
        gameWindow.reverseCard( clickedCard, card );
        gameWindow.setCardLabel( " -> " + card.split( "_" )[ 0 ] );
    }

    //TODO
    void makeWerewolf(){
        StringBuilder str = new StringBuilder();
        String[] werewolves = readMsgOnly().split( MSG_SPLITTER );
        for( String werewolf: werewolves ){
            if( !werewolf.equals( nickname ) ){
                gameWindow.reverseCard( werewolf, "Werewolf_0" );
                str.append( " " ).append( werewolf );
            }
        }
        if( str.toString().isEmpty() ){
            gameWindow.setRoleInfo( "You are the only werewolf. Select one card from the middle you wish to see." );
            waitingForButton = true;
            gameWindow.setTableCardsActive( true );
            long start1 = System.currentTimeMillis();
            while( waitingForButton && System.currentTimeMillis() - start1 < MAX_ROLE_TIME * 1000 );
            if( waitingForButton ){
                int rand = new Random().nextInt( 3 );
                clickedCard = "card" + rand;
                gameWindow.setRoleInfo( "Time's up. Card will be randomly selected.\n" );
                waitingForButton = false;
            }
            gameWindow.setTableCardsActive( false );
            gameWindow.setTableCardsSelected( false );
            sendMsg( gameType, clickedCard );
            card = readMsgOnly();
            gameWindow.reverseCard( clickedCard, card );
        }
        else
            gameWindow.setRoleInfo( "Other werewolves are" + str.toString() + "." );
    }

    void makeMinion(){
        StringBuilder str = new StringBuilder();
        String[] werewolves = readMsgOnly().split( MSG_SPLITTER, 0 );
        if( !werewolves[ 0 ].equals( "" ) ){
            for( String werewolf : werewolves ){
                gameWindow.reverseCard( werewolf, "Werewolf_0" );
                str.append( " " ).append( werewolf );
            }
        }
        if( str.toString().isEmpty() )
            gameWindow.setRoleInfo( "There is no werewolves among the players." );
        else
            gameWindow.setRoleInfo( "Werewolves are" + str.toString() + "." );
    }

    void makeMysticWolf(){
        gameWindow.setRoleInfo( "Choose one card to reverse" );
        waitingForButton = true;
        gameWindow.setTableCardsActive( true );

        // Waiting for clicked card, but with time limit of 30 seconds
        long start1 = System.currentTimeMillis();
        while( waitingForButton && System.currentTimeMillis() - start1 < MAX_ROLE_TIME * 1000 );
        // If time is up, card will be selected randomly
        if( waitingForButton ){
            int rand = new Random().nextInt( 3 );
            clickedCard = "card" + rand;
            gameWindow.setRoleInfo( "Time's up. Card will be randomly selected.\n" );
            waitingForButton = false;
        }
        gameWindow.setTableCardsActive( false );
        gameWindow.setTableCardsSelected( false );
        sendMsg( gameType, clickedCard );
        String chosenCard = readMsgOnly();
        gameWindow.reverseCard( clickedCard, chosenCard );
    }
    void makeWitch(){}
    void makeBeholder(){}
    void makeSeer(){}
    void makeInsomniac(){
        String insomniacNow = readMsgOnly();
        //String idOfInsomniac = readMsgOnly();       //TODO po co to?
        gameWindow.setCardLabel( " -> " + insomniacNow );
        gameWindow.updateMyCard( insomniacNow );
    }

    void makeParanormal(){
        for( int i = 0; i < 2; ++i ){
            waitingForButton = true;
            gameWindow.setPlayersCardsActive( true );
            long start = System.currentTimeMillis();
            while( waitingForButton && System.currentTimeMillis() - start < MAX_ROLE_TIME * 1000 );
            if( waitingForButton ){
                clickedCard = getRandomPlayerCard();
                gameWindow.setRoleInfo( "Time's up. Card will be randomly selected." );
                waitingForButton = false;
            }
            gameWindow.setPlayersCardsActive( false );
            sendMsg( gameType, clickedCard );
            String msg = readMsgOnly();
            if( !msg.equals( "AGAIN" ) ){
                msg = msg.split( "_" )[ 0 ];
                gameWindow.setCardLabel( " -> " + msg );
                gameWindow.setStatementLabel( "You became " + msg );
                gameWindow.reverseCard( clickedCard, msg );
                break;
            }
        }
        gameWindow.setPlayersCardsSelected( false );
    }

    void makeRobber(){
        waitingForButton = true;
        gameWindow.setPlayersCardsActive( true );
        long start = System.currentTimeMillis();
        while( waitingForButton && System.currentTimeMillis() - start < MAX_ROLE_TIME * 1000 );
        if( waitingForButton ){
            clickedCard = getRandomPlayerCard();
            gameWindow.setRoleInfo( "Time's up. Card will be randomly selected." );
            waitingForButton = false;
        }
        gameWindow.setPlayersCardsActive( false );
        gameWindow.setPlayersCardsSelected( false );
        sendMsg( gameType, clickedCard );
        String msg = readMsgOnly();
        String msg2 = msg.split( "_" )[ 0 ];
        gameWindow.setCardLabel( " -> " + msg2 );
        gameWindow.setStatementLabel( "You became " + msg2 );
        gameWindow.reverseCard( clickedCard, displayedCard );
        gameWindow.updateMyCard( msg );
    }

    void makeThing(){
        int myIndex = players.indexOf( nickname );
        waitingForButton = true;
        gameWindow.setPlayerCardActive( ( myIndex + 1 ) % players.size(), true );
        if( myIndex == 0 )
            gameWindow.setPlayerCardActive( players.size() - 1, true );
        else
            gameWindow.setPlayerCardActive( myIndex - 1, true );
        long start = System.currentTimeMillis();
        while( waitingForButton && System.currentTimeMillis() - start < MAX_ROLE_TIME * 1000 );
        if( waitingForButton ){
            clickedCard = players.get( ( myIndex + 1 ) % players.size() );
            gameWindow.setRoleInfo( "Time's up. Card will be randomly selected." );
            waitingForButton = false;
        }
        gameWindow.setPlayersCardsActive( false );
        gameWindow.setPlayersCardsSelected( false );
        sendMsg( gameType, clickedCard );
    }

    void waitForTingsTouch(){
        if( readMsgOnly().equals( "TOUCH" ) ){
            gameWindow.setCardLabel( " -> you've been touched" );
            gameWindow.setStatementLabel( "Thing touches you" );
        }
    }

    void wakeUp(){
        gameWindow.setStatementLabel( "City wakes up!" );
        gameWindow.setRoleInfo( "Now you need to connect with other players via outer application, such as Zoom, to establish who is who. " +
                "When you will be ready, admin will press 'start vote' button and you'll be able to make your vote on person, you wish to be dead." );
        try{
            wakeUpSignal.play();
        }
        catch( NullPointerException ignored ){}
    }

    void vote(){
        gameWindow.setPlayersCardsActive( true );
        waitingForButton = true;
        Thread voteEnd = new Thread( () -> {
            if( readMsgOnly().equals( UNIQUE_CHAR + "VOTEEND" ) )
                waitingForButton = false;
        } );
        voteEnd.start();
        while( waitingForButton );
        gameWindow.setPlayersCardsActive( false );
        gameWindow.setPlayersCardsSelected( false );
        sendMsg( gameType, clickedCard );
        String voteResult = readMsgOnly();
        if( voteResult.equals( UNIQUE_CHAR + "VOTE" ) ){      // vote again
            gameWindow.setStatementLabel( "Vote again, decision must be unequivocal" );
            vote();
        }
        else{
            Vector< String > cardsNow = new Vector<>( Arrays.asList( readMsgOnly().split( MSG_SPLITTER ) ) );
            for( int i = 0; i < players.size(); ++i ){
                gameWindow.reverseCard( players.get( i ), cardsNow.get( i ) );
            }
            if( voteResult.equals( nickname ) )
                gameWindow.setStatementLabel( "You have been killed - " + whoWins( voteResult, cardsNow ) + "." );
            else
                gameWindow.setStatementLabel( voteResult + " has been killed - " + whoWins( voteResult, cardsNow ) + "." );
            gameWindow.quitButton.setDisable( false );
        }
    }

    private String whoWins( String player, Vector< String > cardsNow ){
        if( cardsNow.get( players.indexOf( player ) ).equals( "Tanner" ) )
            return "tanner wins";
        if( cardsNow.get( players.indexOf( player ) ).split( "_" )[ 0 ].equals( "Werewolf" ) )
            return "city wins";
        else{
            if( cardsNow.get( players.indexOf( player ) ).equals( "Minion" ) )
                return "werewolves win";
            else
                return "werewolves and minion win";
        }
    }

    private void gameWindow() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "gameWindow.fxml" ) );
        Stage stage = new Stage();
        stage.setTitle( "Werewolves" );
        stage.setScene( new Scene( fxmlLoader.load(), 1280, 820 ) );
        stage.initStyle( StageStyle.TRANSPARENT);
        stage.show();
        gameWindow = fxmlLoader.getController();
        gameWindow.setCardLabel( card.split( "_" )[ 0 ] );
        gameWindow.setGame( this );
        gameWindow.createPlayersCards();
        gameWindow.setNicknameLabel( nickname );
        gameWindow.updateMyCard( card );
        gameLogic();
    }

    private void getPlayers(){
        String[] playersTab = readMsgOnly().split( MSG_SPLITTER, 0 );
        players.addAll( Arrays.asList( playersTab ) );
    }

    private String getRandomPlayerCard(){
        int rand = new Random().nextInt( players.size() - 1 );
        if( rand == players.indexOf( nickname ) ) rand = players.size() - 1;
        return players.get( rand );
    }

    private void getCard(){
        card = readMsgOnly();
    }

    public void sendNickname( String nickname ){
        sendMsg( nicknameType, nickname );
        this.nickname = nickname;
    }

    private void sendMsg( int type, String str ){
        output.println( type + COM_SPLITTER + str );
    }

    String readMsgOnly(){
        try{
            return receiveMsg().split( COM_SPLITTER, -1 )[ 1 ];
        } catch( ArrayIndexOutOfBoundsException e ){
            return "";
        }
    }

    public String receiveMsg(){
        try{
            return input.readLine();
        }catch( IOException e ){
            return "";
        }
    }

    public void setWaitingForButton( boolean value ){ waitingForButton = value; }
    public void setClickedCard( String card ){ clickedCard = card; }
}
