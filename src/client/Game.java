package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

public class Game{
    private static final int nicknameType = 0;
    private static final int gameType = 1;
    public static final String COM_SPLITTER = String.valueOf( ( char )28 );
    private final String MSG_SPLITTER = String.valueOf( ( char )29 );
    private Vector< String > players = new Vector<>();
    private String card;
    private GameWindow gameWindow;

    public BufferedReader input;
    public PrintWriter output;

    Game( Socket socket ) throws IOException{
        this.input = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        this.output = new PrintWriter( socket.getOutputStream(), true );
    }

    public void run() throws IOException{
        getPlayers();
        getCard();
        gameWindow();
        while( true ){
            String msg = readMsgOnly();
            if( msg.equals( card.toUpperCase() ) ) proceedCard();
            if( msg.equals( "WakeUp" ) ) break;
        }
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
        //TODO, now I assume we choose card in the middle
        sendMsg( gameType, "1" );
        card = readMsgOnly();
        gameWindow.cardLabel.setText( card );
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
        stage.setScene( new Scene( fxmlLoader.load(), 1280, 720 ) );
        stage.initStyle( StageStyle.TRANSPARENT);
        stage.show();
        gameWindow = fxmlLoader.getController();
    }

    private void getPlayers(){
        String[] playersTab = readMsgOnly().split( MSG_SPLITTER, 0 );
        players.addAll( Arrays.asList( playersTab ) );
    }

    private void getCard(){
        card = readMsgOnly();
        gameWindow.cardLabel.setText( card );
    }

    public void sendNickname( String nickname ){
        sendMsg( nicknameType, nickname );
    }

    private void sendMsg( int type, String str ){
        output.println( type + COM_SPLITTER + str );
    }

    String readMsgOnly(){

        return receiveMsg().split( COM_SPLITTER, -1 )[ 1 ];
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
}
