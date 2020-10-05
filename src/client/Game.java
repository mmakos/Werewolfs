package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Game{
    private static final int nicknameType = 0;
    private static final int gameType = 1;
    public static final String COM_SPLITTER = String.valueOf( ( char )28 );

    public DataInputStream input;
    public DataOutputStream output;
    Game( Socket socket ) throws IOException{
        this.input = new DataInputStream( socket.getInputStream() );
        this.output = new DataOutputStream( socket.getOutputStream() );
    }

    public void run(){

    }

    public void sendNickname( String nickname ) throws IOException{
        sendMsg( nicknameType, nickname );
    }

    private void sendMsg( int type, String str ) throws IOException{
        output.writeUTF( type + COM_SPLITTER + str );
    }

    public String receiveMsg() throws IOException{
        return input.readUTF();
    }
}
