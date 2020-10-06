package client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

public class Game{
    private volatile boolean waitingForButton = false;
    private int clickedCard = 0;
    private static final int nicknameType = 0;
    private static final int gameType = 1;
    public static final String COM_SPLITTER = String.valueOf( ( char )28 );
    private final String MSG_SPLITTER = String.valueOf( ( char )29 );
    public Vector< String > players = new Vector<>();
    private String card;
    private GameWindow gameWindow;
    public String nickname;

    public BufferedReader input;
    public PrintWriter output;
    private Thread gameLogic;

    Game( Socket socket ) throws IOException{
        this.input = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        this.output = new PrintWriter( socket.getOutputStream(), true );
    }

    public void run() throws IOException{
        getPlayers();
        getCard();
        gameWindow();
    }

    public void gameLogic(){
        gameLogic = new Thread( () -> {
            while( true ){
                String msg = readMsgOnly();
                if( msg.equals( card.toUpperCase() ) ) proceedCard();
                if( msg.equals( "WakeUp" ) ) break;
            }
        });
        gameLogic.start();
    }

    private void proceedCard(){
        switch( card ){
            case "Mysticwolf" -> makeMysticWolf();
            case "Copycat" -> makeCopycat();
            case "Insomniac" -> makeInsomniac();
            case "Werewolf" -> makeWerewolf();
            case "Witch" -> makeWitch();
            case "Beholder" -> makeBeholder();
            case "Seer" -> makeSeer();
        }
    }

    void makeCopycat(){
        gameWindow.setStatementLabel( "Copycat wakes up" );
        waitingForButton = true;
        gameWindow.setCards012( true );
        while( waitingForButton ) Thread.onSpinWait();      // waits, until button pressed
        sendMsg( gameType, Integer.toString( clickedCard ) );
        card = readMsgOnly();
        gameWindow.setCardButton( " -> " + card );
        System.out.println( "Now you are " + card );
    }

    //TODO
    void makeWerewolf(){}
    void makeMysticWolf(){}
    void makeWitch(){}
    void makeBeholder(){}
    void makeSeer(){}
    void makeInsomniac(){}

    private void gameWindow() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "gameWindow.fxml" ) );
        Stage stage = new Stage();
        stage.setTitle( "Werewolfs" );
        stage.setScene( new Scene( fxmlLoader.load(), 1280, 820 ) );
        stage.initStyle( StageStyle.TRANSPARENT);
        stage.show();
        gameWindow = fxmlLoader.getController();
        gameWindow.setCardButton( card );
        gameWindow.setGame( this );
        gameWindow.createPlayersCards();
        gameWindow.setNicknameLabel( nickname );
        gameLogic();
    }

    private void getPlayers(){
        String[] playersTab = readMsgOnly().split( MSG_SPLITTER, 0 );
        players.addAll( Arrays.asList( playersTab ) );
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
            String msg = input.readLine();
            System.out.println( msg );
            return msg;
            //return input.readLine();
        }catch( IOException e ){
            return "";
        }
    }

    public void setWaitingForButton( boolean value ){ waitingForButton = value; }
    public void setClickedCard( int i ){ clickedCard = i; }
}
