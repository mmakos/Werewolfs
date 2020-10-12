package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Client extends Application{

    @Override
    public void start( Stage primaryStage ) throws Exception{
        Parent root = FXMLLoader.load( getClass().getResource( "fxml/connectWindow.fxml" ) );
        primaryStage.setTitle( "Connect" );
        primaryStage.getIcons().add( new Image( this.getClass().getResourceAsStream( "/img/icon.png" ) ) );
        primaryStage.setScene( new Scene( root, 600, 400 ) );
        primaryStage.initStyle( StageStyle.TRANSPARENT );
        primaryStage.setResizable( false );
        primaryStage.show();
        primaryStage.getScene().getWindow().addEventFilter( WindowEvent.WINDOW_CLOSE_REQUEST, this::quit );
    }

    private < T extends Event > void quit( T t ){
        Platform.exit();
        System.exit( 0 );
    }

    public static void main( String[] args ) {
        launch( args );
    }
}
