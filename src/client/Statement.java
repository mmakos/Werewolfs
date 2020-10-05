package client;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Statement{
    public static void display( String title, String errMsg ){
        Stage stage = new Stage();
        stage.setTitle( title );
        Label l = new Label( errMsg );
        l.setAlignment( Pos.CENTER );
        stage.setScene( new Scene( l, 300, 100 ) );
        stage.initModality( Modality.APPLICATION_MODAL );
        if( title.equals( "Error" ) )
            stage.showAndWait();
        else
            stage.showAndWait();
    }
}
