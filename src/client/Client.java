package client;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Client extends Application{
    Connect c;

    @Override
    public void start( Stage primaryStage ) throws Exception{
        FXMLLoader root = new FXMLLoader( getClass().getResource( "fxml/connectWindow.fxml" ) );
        primaryStage.setTitle( "Connect" );
        primaryStage.getIcons().add( new Image( this.getClass().getResourceAsStream( "/img/icon.png" ) ) );
        primaryStage.setScene( new Scene( root.load(), 600, 400 ) );
        primaryStage.initStyle( StageStyle.TRANSPARENT );
        primaryStage.setResizable( false );
        primaryStage.show();
        primaryStage.getScene().getWindow().addEventFilter( WindowEvent.WINDOW_CLOSE_REQUEST, this::quit );
        c = root.getController();
    }

    private < T extends Event > void quit( T t ){
        c.quit();
    }

    public static void main( String[] args ) {
        launch( args );
    }
}
