public class Werewolves{
    public static void main( String[] args ){
        if( args.length > 0 && args[ 0 ].equals( "-a" ) )
            server.main.main( args );
        else
            client.Client.main( args );
    }
}
