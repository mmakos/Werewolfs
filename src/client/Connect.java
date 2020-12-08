package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Connect{
    public static final int MAX_LOGIN_LENGTH = 8;
    private int port = 23000;
    private String ip = "localhost";

    @FXML
    public void initialize(){
        setOnDrag();
        langChoiceBox();
        getDefaultSettings();
    }

    private void getDefaultSettings(){
        try{
            Scanner scan = new Scanner( new File( "default.cfg" ) );
            String[] config = scan.nextLine().split( ":" );
            port = Integer.parseInt( config[ 1 ] );
            ip = config[ 0 ];
            scan.close();
        }catch( IOException | IndexOutOfBoundsException | NumberFormatException ignored ){}
        portField.setText( Integer.toString( port ) );
        ipField.setText( ip );
    }

    private void saveDefault(){
        try{
            if( !ipField.getText().equals( ip ) || !portField.getText().equals( Integer.toString( port ) ) ){
                FileWriter wr = new FileWriter( "default.cfg" );
                wr.write( ipField.getText() + ":" + portField.getText() );
                wr.close();
            }
        }catch( IOException | NumberFormatException ignored ){}
    }

    private void langChoiceBox(){
        lang.getItems().add( "polski" );
        lang.getItems().add( "english" );
        lang.setValue( "polski" );
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
            ipField.setText( ip );
            ipField.setDisable( true );
            portField.setText( Integer.toString( port ) );
            portField.setDisable( true );
        } else{
            ipField.setText( "" );
            ipField.setDisable( false );
            portField.setText( "" );
            portField.setDisable( false );
        }
    }

    @FXML protected void connect(){
        if( loginField.getText().length() <= 0 ){
            infoLabel.setText( "Empty login" );
            return;
        }
        if( loginField.getText().length() > MAX_LOGIN_LENGTH ){
            infoLabel.setText( "Login too long" );
            return;
        }

        Socket socket;
        String ip = ipField.getText();
        int port = 23000;
        try{
            port = Integer.parseInt( portField.getText() );
        } catch( NumberFormatException ignored ){}
        saveDefault();
        try{
            socket = new Socket( ip, port );
            Game game = new Game( socket, getLanguage() );
            game.sendNickname( loginField.getText() );
            String nickInfo = game.receiveMsg();
            if( nickInfo.equals( "0" + Game.COM_SPLITTER + "wrongNickname" ) ){
                infoLabel.setText( "Nickname already taken." );
                return;
            }
            if( !nickInfo.equals( "0" + Game.COM_SPLITTER + "ok" ) ) return;
            Platform.runLater( ( ) -> {
                infoLabel.setText( "Game will start soon. Please don't close this window." );
                loginButton.setDisable( true );
                loginButton.setText( "Connected" );
                quitButton.setDisable( true );
                quitButton.setVisible( false );
            } );
            game.run( loginField.getScene().getWindow() );
        } catch( UnknownHostException e ){
            infoLabel.setText( "Cannot connect to " + ip + " on port " + port );
        } catch( IOException e ){
            infoLabel.setText( "Cannot connect to server." );
        }
    }

    private String getLanguage(){
        return ( String )lang.getValue();
    }

    @FXML private TextField loginField;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML public Label infoLabel;
    @FXML private CheckBox defaultCheckBox;
    @FXML private GridPane dragField;
    @FXML private ChoiceBox lang;
    @FXML private Button quitButton;
    @FXML private Button loginButton;
    private double x = 0, y = 0;
}