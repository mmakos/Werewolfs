package client;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Error{
    public static void display( String errMsg ){
        Stage stage = new Stage();
        stage.setTitle( "Error" );
        Label l = new Label( errMsg );
        l.setAlignment( Pos.CENTER );
        stage.setScene( new Scene( l, 300, 100 ) );
        stage.initModality( Modality.APPLICATION_MODAL );
        stage.showAndWait();
    }
}
