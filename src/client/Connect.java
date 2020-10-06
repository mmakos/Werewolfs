package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connect{
    @FXML
    public void initialize(){
        setOnDrag();
    }

    private void setOnDrag(){
        dragField.setOnMousePressed( mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        } );

        dragField.setOnMouseDragged( mouseEvent -> {
            Stage stage = ( Stage ) dragField.getScene().getWindow();
            stage.setX( mouseEvent.getScreenX() - x );
            stage.setY( mouseEvent.getScreenY() - y );
            stage.setOpacity( 0.9 );
        } );

        dragField.setOnMouseReleased( mouseEvent -> dragField.getScene().getWindow().setOpacity( 1.0 ) );
    }

    @FXML private void quit(){
        dragField.getScene().getWindow().hide();
        Platform.exit();
        System.exit( 0 );
    }

    @FXML protected void setDefault(){
        if( defaultCheckBox.isSelected() ){
            ipField.setText( "localhost" );
            ipField.setDisable( true );
            portField.setText( "23000" );
            portField.setDisable( true );
        } else{
            ipField.setText( "" );
            ipField.setDisable( false );
            portField.setText( "" );
            portField.setDisable( false );
        }
    }

    @FXML protected void connect(){
        Socket socket;
        String ip = ipField.getText();
        int port = 23000;
        try{
            port = Integer.parseInt( portField.getText() );
        } catch( NumberFormatException ignored ){        }
        try{
            socket = new Socket( ip, port );
            System.out.println( "Connected :)" );       //TODO to delete
            Game game = new Game( socket );
            game.sendNickname( loginField.getText() );
            String nickInfo = game.receiveMsg();
            if( nickInfo.equals( "0" + Game.COM_SPLITTER + "wrongNickname" ) ){
                Statement.display( "Error", "Nickname already taken." );
                return;
            }
            if( !nickInfo.equals( "0" + Game.COM_SPLITTER + "ok" ) ) return;
            Statement.display( "Info", "Game will start soon.\nWait for other players to join." );
            loginField.getScene().getWindow().hide();
            game.run();
        }catch( UnknownHostException e ){
            Statement.display( "Error", "Cannot connect to " + ip + " on port " + port );
        }catch( IOException e ){
            Statement.display( "Error", "Cannot connect to server." );
        }
    }

    @FXML private TextField loginField;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox defaultCheckBox;
    @FXML private GridPane dragField;
    private double x = 0, y = 0;
}