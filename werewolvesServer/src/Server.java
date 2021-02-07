import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class Server{
    private ServerSocket ss;
    private LinkedList< Game > games = new LinkedList<>();
    private static final int port = 23000;
    private static final int ID_LENGTH = 6;
    private static final int INACTIVE_TIME = 3600; //in seconds

    public Server() throws IOException{
        ss = new ServerSocket( port );
        commands.start();
        System.out.println( "Server is running." );
        listen();
    }

    private Thread commands = new Thread( () -> {
        Scanner in = new Scanner( System.in );
        while( true ){
            String[] input = in.nextLine().split( " " );
            String command = input[ 0 ];
            String arg = null;
            if( input.length > 1 ){
                arg = input[ 1 ];
            }
            switch( command ){
                case "shutdown":
                    System.exit( 0 );
                    break;
                case "listgames":
                    for( Game game : games )
                        System.out.println( game.getID() );
                    break;
                case "listplayers":
                    if( arg == null ){      // no concrete game, list all
                        for( Game game : games ){
                            System.out.println( "Game: " + game.getID() );
                            for( Player player : game.getPlayers() ){
                                System.out.println( "\t-> " + player.getID() );
                            }
                        }
                    }
                    else{
                        Game g = getGame( arg );
                        if( g == null )
                            System.out.println( "No such game." );
                        else{
                            System.out.println( "Game: " + g.getID() );
                            for( Player player : g.getPlayers() ){
                                System.out.println( "\t-> " + player.getID() );
                            }
                        }
                    }
                    break;
                case "endgame":
                    if( arg == null )
                        System.out.println( "No game provided." );
                    else{
                        Game g = getGame( arg );
                        if( g == null )
                            System.out.println( "No such game." );
                        else{
                            endGame( g );
                            System.out.println( "Game " + arg + " ended." );
                        }
                    }
                    break;
                case "help": default:
                    System.out.println( "Invalid command. Here is list of available commands:" );
                    System.out.println( "\tshutdown - shuts down the server." );
                    System.out.println( "\tlistgames - lists all games" );
                    System.out.println( "\tlistplayers (<gameID>) - lists players from given game. If no game provided, then lists players from all games." );
                    System.out.println( "\tendgame <gameID> - ends given game." );
                    System.out.println( "\thelp - shows this help." );
            }
        }
    } );

    private void listen(){
        while( true ){
            try{
                Socket s = ss.accept();
                new Player( s, this );
            } catch( IOException e ){
                e.printStackTrace();
            }
        }
    }

    public Game getGame( String id ){
        for( Game game : games ){
            if( game.getID().equals( id ) ){
                return game;
            }
        }
        return null;
    }

    public Game newGame( Player admin ){
        String newGameID;
        while( true ){
            boolean sameId = false;
            newGameID = getRandomID();
            for( Game game : games ){
                if( game.getID().equals( newGameID ) ){
                    sameId = true;
                    break;
                }
            }
            if( !sameId )
                break;
        }
        Game game = new Game( newGameID, admin );
        games.add( game );
        return game;
    }

    private String getRandomID(){
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1 )
                .filter( i -> ( i <= 57 || i >= 65 ) && ( i <= 90 || i >= 97 ) )
                .limit( ID_LENGTH )
                .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                .toString();
    }

    public void endGame( Game game ){
        System.out.println( "Ending game " + game.getID() );
        game.endPlayers();
        games.remove( game );
    }


    private class Game{
        private Vector< Player > players = new Vector<>();
        private Player admin;
        private final String gameID;
        private int newPlayerID = 100;
        private boolean active = true;
        private boolean started = false;        // has game already started

        private final Thread timer = new Thread( () -> {
            while( active ){
                try{
                    active = false;
                    Thread.sleep( Server.INACTIVE_TIME * 1000 );        // if active we sleep 1 hour
                } catch( InterruptedException e ){
                    e.printStackTrace();
                }
            }
            this.admin.getServer().endGame( this );
            System.out.println( "END GAME" );
        } );

        Game( String gameID, Player admin ){
            this.gameID = gameID;
            this.admin = admin;
            timer.start();
        }

        public void setStarted( boolean started ){
            this.started = started;
        }

        public void active(){
            active = true;
        }

        public String getID(){
            return gameID;
        }

        public void addPlayer( Player p ){
            players.add( p );
        }

        public int getPlayerID(){
            return this.newPlayerID++;
        }

        public void removePlayer( int playerID ){
            Player p = getPlayer( playerID );
            if( p == null ) return;
            p.close();
            players.remove( p );
        }

        public Player getPlayer( int id ){
            for( Player player : players ){
                if( player.getID() == id )
                    return player;
            }
            return null;
        }

        public Vector<Player> getPlayers(){
            return players;
        }

        public void endPlayers(){
            for( Player player : players )
                player.close();
            admin.close();
        }

        public void send( int to, String msg ){
            if( to == -1 )
                admin.send( msg );
            else if( to == -2 ){
                for( Player player : players )
                    player.send( msg );
            }
            else{
                Player player = getPlayer( to );
                if( player != null )
                    player.send( msg );
            }
        }
    }

    private class Player{
        private Server server;
        private Socket socket;
        private Game game;
        private int playerID;
        private BufferedReader input;
        private PrintWriter output;
        private final String SPLITTER = String.valueOf( ( char )28 );

        Player( Socket socket, Server server ) throws IOException{
            this.server = server;
            this.socket = socket;
            this.input = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
            this.output = new PrintWriter( new OutputStreamWriter( socket.getOutputStream(), StandardCharsets.UTF_8 ), true );
            run.start();
        }

        public void send( String msg ){
            output.println( msg );
            System.out.println( "Message to " + playerID + "\tmsg: " + msg );
        }

        public int getID(){
            return playerID;
        }

        public Server getServer(){
            return server;
        }

        public void close(){
            try{
                socket.close();
            } catch( IOException e ){
                e.printStackTrace();
            }
        }

        private void proceedMsg( String in ){
            if( in.equals( "QUIT" ) && playerID == -1 ){
                server.endGame( game );     // Admin ended the game
                return;
            }
            if( in.equals( "START" ) && playerID == -1 ){
                game.setStarted( true );
                return;
            }
            if( playerID == -1 )
                game.active();      // Admin is still there

            String[] inSplit = in.split( SPLITTER );
            if( inSplit.length != 2 )
                return;
            String id = inSplit[ 0 ];       // ALL - from admin to all, ADM - to admin, <playerID> - to concrete player
            String msg = inSplit[ 1 ];
            if( id.equals( "REMOVE" ) && playerID == -1 ){
                try{
                    int idToRemove = Integer.parseInt( msg );
                    game.removePlayer( idToRemove );
                    System.out.println( "Player " + idToRemove + " removed." );
                    StringBuilder players = new StringBuilder();
                    for( Player p : game.players )
                        players.append( p.playerID ).append( ", " );
                    System.out.println( "Players: " + players.toString() );
                } catch( NumberFormatException ignored ){}
                return;
            }
            int who;
            if( id.equals( "ADM" ) ){
                msg = playerID + SPLITTER + msg;
                who = -1;
            }
            else if( id.equals( "ALL" ) )
                who = -2;
            else{
                try{
                    who = Integer.parseInt( id );
                } catch( NumberFormatException ignored ){
                    who = -10;
                }
            }
            if( who != -10 )
                game.send( who, msg );
        }

        private int getFirstMsg() throws IOException{
            String beginMsg = input.readLine();
            if( beginMsg.equals( "NEWGAME" ) ){     // new game
                this.game = server.newGame( this );
                this.playerID = -1; // Admin
                game.send( -1, game.gameID );
                System.out.println( "Create new game." );
            }
            else{   // join game
                try{
                    this.game = server.getGame( beginMsg );
                    if( this.game != null ){
                        this.playerID = game.getPlayerID();
                        this.game.addPlayer( this );
                        this.send( "GOOD" );
                    }
                    else{
                        noSuchGame();
                        return -1;
                    }
                } catch( NumberFormatException e ){
                    noSuchGame();
                    return -1;
                }
            }
            return 0;
        }

        private void noSuchGame() throws IOException{
            this.send( "NOGAME" );
            socket.close();
            if( game != null ){
                game.players.remove( this );
            }
        }

        private Thread run = new Thread( () ->{
            try{
                if( getFirstMsg() == -1 )
                    return;
                while( true ){      // listen for messages
                    proceedMsg( input.readLine() );
                }
            }
            catch( IOException ignored ){}
        } );
    }

    public static void main( String[] args ) throws IOException{
        new Server();
    }
}
