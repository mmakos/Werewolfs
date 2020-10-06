package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Card{
    public static int werewolvesQuant = 3;
    public static String[] card = { "Copycat", "Werewolf_0", "Insomniac", "Mystic wolf", "Seer", "Beholder",
                                    "Witch", "Apprentice seer", "Tanner", "Minion", "Werewolf_1", "Werewolf_2" };

    public static class Client extends Application{

        @Override
        public void start( Stage primaryStage ) throws Exception{
            Parent root = FXMLLoader.load( getClass().getResource( "client/connectWindow.fxml" ) );
            primaryStage.setTitle( "client.img.Connect" );
            primaryStage.setScene( new Scene( root, 600, 400 ) );
            primaryStage.initStyle( StageStyle.TRANSPARENT );
            primaryStage.setResizable( false );
            primaryStage.show();
        }

        public static void main( String[] args ) {
            launch( args );
        }
    }
}
