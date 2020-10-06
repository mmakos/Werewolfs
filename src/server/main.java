package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application{

    @Override
    public void start( Stage stage ) throws Exception{
        Parent root = FXMLLoader.load( getClass().getResource( "runWindow.fxml" ) );
        stage.setTitle( "Players" );
        stage.setScene( new Scene( root, 600, 400 ) );
        stage.setResizable( false );
        stage.show();
    }

    public static void main( String[] args ) {
        launch( args );
    }
}
