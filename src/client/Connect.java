package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
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

    @FXML private void newGame(){
        try{
            Runtime.getRuntime().exec( "SerWerewolves.exe" );
        } catch( IOException ignored ){
            infoLabel.setText( "No SerWerewolves.exe app in your game folder." );
        }
    }

    @FXML private void quit(){
        dragField.getScene().getWindow().hide();
        Platform.exit();
        System.exit( 0 );
    }


    @FXML protected void connect(){
        String login = loginField.getText();
        if( login.length() <= 0 ){
            infoLabel.setText( "Empty login" );
            return;
        }
        if( login.length() > MAX_LOGIN_LENGTH ){
            infoLabel.setText( "Login too long" );
            return;
        }

        Socket socket;
        try{
            socket = new Socket( ip, port );
            Game game = new Game( socket, getLanguage() );
            game.sendToServer( gameIdField.getText() );
            if( !game.receive().equals( "GOOD" ) ){
                infoLabel.setText( "No such game. Make sure you have correct id." );
                return;
            }
            game.sendMsg( login );
            String nickInfo = game.receive();
            if( nickInfo.equals( "WRONGNICK" ) ){
                infoLabel.setText( "Nickname already taken." );
                return;
            }
            if( !nickInfo.equals( "OK" ) ){
                infoLabel.setText( "Something went wrong." );
                return;
            }
            game.setNickname( login );
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
    @FXML private TextField gameIdField;
    @FXML public Label infoLabel;
    @FXML private GridPane dragField;
    @FXML private ChoiceBox lang;
    @FXML private Button quitButton;
    @FXML private Button loginButton;
    private double x = 0, y = 0;
}