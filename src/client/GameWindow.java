package client;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.Vector;

public class GameWindow{
    private Game game;

    public void setGame( Game game ){ this.game = game; }

    @FXML public void initialize(){}

    public void setCardButton( String str ){
       Platform.runLater( () -> cardLabel.setText( cardLabel.getText() + str ) );
    }

    public void setStatementLabel( String str ){
        Platform.runLater( () -> statementLabel.setText( str ) );
    }
    public void setRoleInfo( String str ){
        Platform.runLater( () -> roleInfo.setText( str ) );
    }

    public void setCards012( boolean active ){
        card0.setDisable( !active );
        card1.setDisable( !active );
        card2.setDisable( !active );
    }

    @FXML void tableCardClicked(){
        String selected = getToggleId( card0.getToggleGroup().getSelectedToggle() );
        System.out.println( selected );
        setCards012( false );
        switch( selected ){
            case "card0" -> game.setClickedCard( 0 );
            case "card1" -> game.setClickedCard( 1 );
            case "card2" -> game.setClickedCard( 2 );
        }
        game.setWaitingForButton( false );
    }

    public void createPlayersCards(){
        int a = 500, b = 280, p = game.players.size(), t = 360 / p;
        p = 20;     //todo to delete
        t = 360 / p;//todo to delete
        for( int i = 0, j = 0; i < p - 1; ++i, ++j ){
            if( j < game.players.size() && game.players.get( j ).equals( game.nickname ) ){         //Todo to delete first condition
                --i;
                ToggleButton toggle = getPlayerCard( "You" );
                double ti = Math.toRadians( -90 );
                toggle.setLayoutX( a * Math.cos( ti ) + ( sceneWidth / 2.0 ) - ( cardWidth / 2.0 ) + 20 );
                toggle.setLayoutY( -1 * ( b * Math.sin( ti ) ) + ( sceneHeight / 2.0 ) - ( cardHeight / 2.0 ) + 40 );
                toggle.setOpacity( 1.0 );
                toggle.setSelected( true );
                playersCards.add( toggle );
                gamePane.getChildren().add( toggle );
            }
            else{
                //ToggleButton toggle = getPlayerCard( game.players.get( j ) );
                ToggleButton toggle = getPlayerCard( "player" + j );    //todo to delete
                double ti = Math.toRadians( -90 - ( 360.0 / p ) * ( i + 1 ) - angleDiffFunction( i + 1, p ) );
                toggle.setLayoutX( a * Math.cos( ti ) + ( sceneWidth / 2.0 ) - ( cardWidth / 2.0 ) + 20 );
                toggle.setLayoutY( -1 * ( b * Math.sin( ti ) ) + ( sceneHeight / 2.0 ) - ( cardHeight / 2.0 ) + 40 );
                playersCards.add( toggle );
                gamePane.getChildren().add( toggle );
            }
        }
    }

    private double angleDiffFunction( int i, int p ){
        return  ( -10 ) * Math.sin( Math.toRadians( ( 720.0 * i ) / p ) );
    }

    private ToggleButton getPlayerCard( String nickname ){
        ToggleButton toggle = new ToggleButton( "\n\n\n\n" + nickname );
        toggle.setMinSize( 72, 100 );
        toggle.setMaxSize( 72, 100 );
        toggle.setDisable( true );
        final Image unselected = new Image( "/img/backCardSmallDark.png" );
        final Image selected = new Image( "/img/backCardSmall.png" );
        final ImageView toggleImage = new ImageView();
        toggleImage.setFitWidth( 72 );      // scaling
        toggleImage.setPreserveRatio( true );
        toggle.setTextFill( Color.WHITE );
        toggle.setGraphic( toggleImage );
        toggle.setContentDisplay( ContentDisplay.CENTER );
        toggleImage.imageProperty().bind( Bindings
                .when( toggle.selectedProperty() )
                .then( selected )
                .otherwise( unselected )
        );
        //toggle.setOnAction( this::toggleClicked );
        return toggle;
    }

    private String getToggleId( Toggle toggle ){
        return toggle.toString().split( "=" )[ 1 ].split( "," )[ 0 ];
    }

    @FXML private AnchorPane gamePane;
    @FXML private ToggleButton card0;
    @FXML private ToggleButton card1;
    @FXML private ToggleButton card2;
    private Vector< ToggleButton > playersCards = new Vector<>();
    @FXML private Label cardLabel;
    @FXML public Label statementLabel;
    @FXML private Label nicknameLabel;
    @FXML private Label roleInfo;
    private static final int sceneWidth = 1280, sceneHeight = 820;
    private static final int cardWidth = 100, cardHeight = 72;

    public void setNicknameLabel( String nickname ){
        this.nicknameLabel.setText( nickname );
    }
}
