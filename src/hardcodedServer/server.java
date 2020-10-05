package hardcodedServer;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class server{
    public static void main( String args[] ) throws IOException{
        ServerSocket ss = new ServerSocket( 23000 );
        Socket s = ss.accept();
        System.out.println( "Klient podlaczony" );
        Scanner scan = new Scanner( System.in );

        BufferedReader input = new BufferedReader( new InputStreamReader( s.getInputStream() ) );
        PrintWriter output = new PrintWriter( s.getOutputStream(), true );

        System.out.print( "Received: " + input.readLine() + "\n" );
        output.println( "0" + ( char )28 + "ok" );
        output.println( "1" + ( char )28 + "player1" + ( char )29 + "player2" );
        output.println( "1" + ( char )28 + "Copycat" );
        scan.nextLine();
        output.println( "1" + ( char )28 + "COPYCAT" );
        System.out.print( "Received: " + input.readLine() + "\n" );
        output.println( "1" + ( char )28 + "Insomniac" );

        Thread writer = new Thread( () -> {
            //try{
            String messageOut = "0" + ( char )28 + "ok";
            while( !messageOut.equals( "q" ) && !messageOut.equals( "quit" ) ){
                output.print( messageOut );
                messageOut = scan.nextLine();
            }
            //} catch( IOException ex ){};
        });
        Thread reader = new Thread( () -> {
            try{
                String messageIn = "";
                while( true ){
                    messageIn = input.readLine();
                    System.out.print( "Michal: " + messageIn + "\n" );
                    messageIn = "";
                }
            } catch( IOException ex ){};
        });

        //writer.start();
        //reader.start();
    }
}
