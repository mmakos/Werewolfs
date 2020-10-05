import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Card{
    public static String[] card = { "Copycat", "Werewolf", "Insomniac", "Mysticwolf", "Seer", "Beholder", "Witch" };

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
