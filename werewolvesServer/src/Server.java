import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class Server{
    private final ServerSocket ss;
    private final LinkedList< Game > games = new LinkedList<>();
    private static final int port = 23000;
    private static final int ID_LENGTH = 6;
    private static final String UNIQUE_CHAR = String.valueOf( ( char )2 );
    private static final int INACTIVE_TIME = 3600; //in seconds
    private int logLevel = 1;
    private final LinkedList< String > viewedGames = new LinkedList<>();
    private boolean isRunning = true;

    public Server() throws IOException{
        ss = new ServerSocket( port );
        // no concrete game, list all
        Thread commands = new Thread( () -> {
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
                    case "halt":
                        endALlGames();
                        System.exit( 0 );
                        break;
                    case "listgames":
                        for( Game game : games )
                            print( game.getID() );
                        break;
                    case "listplayers":
                        if( arg == null ){      // no concrete game, list all
                            for( Game game : games ){
                                print( "Game: " + game.getID() );
                                for( Player player : game.getPlayers() ){
                                    print( "\t-> " + player.getID() );
                                }
                            }
                        } else{
                            Game g = getGame( arg );
                            if( g == null )
                                print( "No such game." );
                            else{
                                print( "Game: " + g.getID() );
                                for( Player player : g.getPlayers() ){
                                    print( "\t-> " + player.getID() );
                                }
                            }
                        }
                        break;
                    case "endgame":
                        if( arg == null )
                            print( "No game provided." );
                        else if( arg.equals( "-a" ) ){
                            endALlGames();
                        } else{
                            Game g = getGame( arg );
                            if( g == null )
                                print( "No such game." );
                            else{
                                g.send( -1, "ABORT" );
                                endGame( g );
                                print( "Game " + arg + " has been aborted." );
                            }
                        }
                        break;
                    case "loglevel":
                        if( arg == null )
                            print( "No log level provided." );
                        else{
                            try{
                                logLevel = Integer.parseInt( arg );
                                if( logLevel < 0 ) logLevel = 0;
                                print( "Log level set to " + logLevel + "." );
                            } catch( NumberFormatException e ){
                                print( "Invalid log level." );
                            }
                        }
                        break;
                    case "viewgame":
                        if( arg == null )
                            print( "No game provided." );
                        else if( getGame( arg ) == null )
                            print( "No such game." );
                        else{
                            viewedGames.add( arg );
                            print( "Game " + arg + " will display all the logs now." );
                        }
                        break;
                    case "unviewgame":
                        if( arg == null ){
                            print( "No game provided." );
                        } else{
                            boolean removed = viewedGames.remove( arg );
                            if( removed )
                                print( "Game " + arg + " will be not viewed anymore." );
                            else
                                print( "Game " + arg + " were not currently viewed." );
                        }
                        break;
                    case "help":
                    default:
                        print( "Invalid command. Here is list of available commands:" );
                        print( "\tshutdown / halt - shuts down the server." );
                        print( "\tlistgames - lists all games" );
                        print( "\tlistplayers (<gameID>) - lists players from given game. If no game provided, then lists players from all games." );
                        print( "\tendgame <gameID> - ends given game. If <gameID> is -a then ends all games." );
                        print( "\tloglevel <level> - sets amount of displayed logs (0 - only server logs (crashed etc, 3 - all logs)." );
                        print( "\tviewgame <gameID> - views all logs from given game." );
                        print( "\tunviewgame (<gameID>) - undo view game command. If no game provided, then all games will be unviewed." );
                        print( "\thelp - shows this help." );
                }
            }
        } );
        commands.start();
        print( "Server is running." );
        listen();
    }

    private void listen(){
        while( isRunning ){
            try{
                Socket s = ss.accept();
                new Player( s, this );
            } catch( IOException ignored ){}
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
        print( "New game created. Game id: " + newGameID, 1 );
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
        game.end();
        games.remove( game );
        print( "Game " + game.getID() + " has ended.", 1 );
    }

    public void endALlGames(){
        for( Game g : games ){
            g.send( -1, "ABORT" );
            endGame( g );
        }
        print( "ALL games have been aborted." );
        isRunning = false;
    }

    public void print( String log, int level ){
        if( level <= logLevel )
            System.out.println( log );
    }

    public void print( String log ){
        print( log, 0 );
    }

    public void print( String log, String g, int level ){
        if( level <= logLevel || viewedGames.contains( g ) )
            print( "Game " + g + ": " + log );
    }

    private class Game{
        private final Vector< Player > players = new Vector<>();
        private Player admin;
        private final String gameID;
        private int newPlayerID = 100;
        private boolean active = true;

        private final Thread timer = new Thread( () -> {
            while( active ){
                try{
                    active = false;
                    Thread.sleep( Server.INACTIVE_TIME * 1000 );        // if active we sleep 1 hour
                } catch( InterruptedException e ){
                    break;
                }
            }
            print( "Game " + this.getID() + " is inactive.", 1 );
            this.admin.getServer().endGame( this );
        } );

        Game( String gameID, Player admin ){
            this.gameID = gameID;
            this.admin = admin;
            timer.start();
        }

        public void end(){
            timer.interrupt();
            endPlayers();
        }

        public void active(){
            active = true;
        }

        public String getID(){
            return gameID;
        }

        public void addPlayer( Player p ){
            players.add( p );
            print( "New player: " + p.getID(), this.gameID, 2 );
        }

        public int getPlayerID(){
            return this.newPlayerID++;
        }

        public void removePlayer( int playerID ){
            Player p = getPlayer( playerID );
            if( p == null ) return;
            p.close();
            p.active = false;
            players.remove( p );
            print( "Player " + p.getID() + " removed.", this.gameID, 2 );
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

        private void endPlayers(){
            for( Player player : players )
                player.close();
            admin.close();
        }

        public void send( int to, String msg ){
            if( to == -1 ){
                admin.send( msg );
                print( "Message to admin: " + msg, this.gameID, 3 );
            }
            else if( to == -2 ){
                for( Player player : players )
                    player.send( msg );
                print( "Message to all: " + msg, this.gameID, 3 );
            }
            else{
                Player player = getPlayer( to );
                if( player != null )
                    player.send( msg );
                print( "Message to player " + to + ": " + msg, this.gameID, 3 );
            }
        }
    }

    private class Player{
        private final Server server;
        private final Socket socket;
        private Game game;
        private int playerID;
        private BufferedReader input;
        private final PrintWriter output;
        private final String SPLITTER = String.valueOf( ( char )28 );
        public boolean active = true;

        Player( Socket socket, Server server ) throws IOException{
            this.server = server;
            this.socket = socket;
            this.input = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
            this.output = new PrintWriter( new OutputStreamWriter( socket.getOutputStream(), StandardCharsets.UTF_8 ), true );
            run.start();
        }

        public void send( String msg ){
            output.println( msg );
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
            if( in == null ){
                game.removePlayer( playerID );
                game.send( -1, "REMOVE" + SPLITTER + playerID );
                if( playerID == -1 )
                    server.endGame( game );
                return;
            }
            if( in.equals( "QUIT" ) && playerID == -1 ){
                server.endGame( game );     // Admin ended the game
                return;
            }
            if( in.equals( "START" ) && playerID == -1 ){
                print( "Game started.", this.game.getID(), 2 );
                return;
            }
            if( in.equals( UNIQUE_CHAR + "ALIVE" ) ){
                print( "Message to player " + playerID + ": " + in, 4 );
                this.send( in );
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

        public Thread run = new Thread( () ->{
            try{
                if( getFirstMsg() == -1 )
                    return;
                while( active ){      // listen for messages
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
