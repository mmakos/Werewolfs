package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class main extends Application{

    @Override
    public void start( Stage stage ) throws Exception{
        Parent root = FXMLLoader.load( getClass().getResource( "fxml/runWindow.fxml" ) );
        stage.setTitle( "Players" );
        stage.setScene( new Scene( root, 600, 400 ) );
        stage.setResizable( false );
        stage.show();
        stage.getScene().getWindow().addEventFilter( WindowEvent.WINDOW_CLOSE_REQUEST, this::quit );
    }

    private < T extends Event > void quit( T t ){
        Platform.exit();
        System.exit( 0 );
    }

    public static void main( String[] args ) {
        launch( args );
    }
}
