package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class Game{
    private static final int nicknameType = 0;
    private static final int gameType = 1;
    public static final String COM_SPLITTER = String.valueOf( ( char )28 );
    private final String MSG_SPLITTER = String.valueOf( ( char )29 );
    private Vector< Integer > players;
    private String card;

    public DataInputStream input;
    public DataOutputStream output;
    Game( Socket socket ) throws IOException{
        this.input = new DataInputStream( socket.getInputStream() );
        this.output = new DataOutputStream( socket.getOutputStream() );
    }

    public void run(){
        getPlayers();
        getCard();
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
        System.out.println( "Now you are " + card );
    }

    //TODO
    void makeWerewolf(){}
    void makeMysticWolf(){}
    void makeWitch(){}
    void makeBeholder(){}
    void makeSeer(){}
    void makeInsomniac(){}

    private void getPlayers(){
        String[] playersTab = readMsgOnly().split( MSG_SPLITTER, 0 );
        for( String player : playersTab ){
            players.add( Integer.parseInt( player ) );
        }
    }

    private void getCard(){
        card = readMsgOnly();
    }

    public void sendNickname( String nickname ){

        sendMsg( nicknameType, nickname );
    }

    private void sendMsg( int type, String str ){
        try{
            output.writeUTF( type + COM_SPLITTER + str );
        }catch( IOException ignore ){}
    }

    String readMsgOnly(){
        return receiveMsg().split( COM_SPLITTER, -1 )[ 1 ];
    }

    public String receiveMsg(){
        try{
            return input.readUTF();
        }catch( IOException e ){
            return "";
        }
    }
}
