package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class main extends Application{

    @Override
    public void start( Stage stage ) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "fxml/runWindow.fxml" ) );
        Parent root = fxmlLoader.load();
        stage.getIcons().add( new Image( this.getClass().getResourceAsStream( "/img/sericon.png" ) ) );
        stage.setTitle( "Players" );
        stage.setScene( new Scene( root, 600, 400 ) );
        stage.setResizable( false );
        stage.show();
        Server server = fxmlLoader.getController();
        stage.getScene().getWindow().addEventFilter( WindowEvent.WINDOW_CLOSE_REQUEST, server::endGame );
    }

    public static void main( String[] args ) {
        launch( args );
    }
}
