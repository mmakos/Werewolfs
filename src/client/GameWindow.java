package client;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Vector;

public class GameWindow{
    private Game game;

    public void setGame( Game game ){ this.game = game; }

    @FXML public void initialize(){
    }

    public void setCardButton( String str ){
       Platform.runLater( () -> cardLabel.setText( cardLabel.getText() + str ) );
    }

    public void setStatementLabel( String str ){
        Platform.runLater( () -> statementLabel.setText( str ) );
    }
    public void setRoleInfo( String str ){
        Platform.runLater( () -> roleInfo.setText( str ) );
    }


    public void reverseCard( String player, String card ){
        ToggleButton toggle;
        switch( player ){
            case "card0": toggle = card0; break;
            case "card1": toggle = card1; break;
            case "card2": toggle = card2; break;
            default: toggle = playersCards.get( game.players.indexOf( player ) );
        }
        if( !game.players.contains( player ) ){
            Platform.runLater( () -> {
                toggle.setText( card );
                toggle.setTextFill( Color.WHITE );
                toggle.setStyle( "-fx-background-image: url(\"/img/backCardSmall.png\")" );
            } );
        }
        else{
            Platform.runLater( () -> {
                toggle.setText( card + "\n\n\n\n" + player );
                toggle.setStyle( "-fx-graphic: url(\"/img/backCardSmall.png\")" );
            } );
        }
        toggle.setOpacity( 1.0 );
    }

    public void createPlayersCards(){
        int a = 500, b = 280, p = game.players.size(), t = 360 / p;

        playersCards.setSize( p );
        int ourPos = game.players.indexOf( game.nickname );        //start drawing cadrs from ours
        ToggleButton toggle = getPlayerCard( "You" );
        toggle.setId( Game.UNIQUE_CHAR + "You" );
        double ti = Math.toRadians( -90 );
        toggle.setLayoutX( a * Math.cos( ti ) + ( sceneWidth / 2.0 ) - ( cardWidth / 2.0 ) + 20 );
        toggle.setLayoutY( -1 * ( b * Math.sin( ti ) ) + ( sceneHeight / 2.0 ) - ( cardHeight / 2.0 ) + 40 );
        toggle.setOpacity( 1.0 );
        toggle.setStyle( "-fx-graphic: url(\"/img/backCardSmall.png\")" );
        playersCards.set( ourPos, toggle );
        gamePane.getChildren().add( toggle );

        int pos = 0;
        for( int player = ourPos + 1; player < p; ++pos, ++player )
            addToggle( pos, player, a, b, p );
        for( int player = 0; player < ourPos; ++pos, ++player )
            addToggle( pos, player, a, b, p );
    }

    private void addToggle( int pos, int player, int a, int b, int p ){
        ToggleButton toggle = getPlayerCard( game.players.get( player ) );
        double ti = Math.toRadians( -90 - ( 360.0 / p ) * ( pos + 1 ) - angleDiffFunction( pos + 1, p ) );
        toggle.setLayoutX( a * Math.cos( ti ) + ( sceneWidth / 2.0 ) - ( cardWidth / 2.0 ) + 20 );
        toggle.setLayoutY( -1 * ( b * Math.sin( ti ) ) + ( sceneHeight / 2.0 ) - ( cardHeight / 2.0 ) + 40 );
        playersCards.set( player, toggle );
        gamePane.getChildren().add( toggle );
    }

    private double angleDiffFunction( int i, int p ){
        return  ( -10 ) * Math.sin( Math.toRadians( ( 720.0 * i ) / p ) );
    }

    private ToggleButton getPlayerCard( String nickname ){
        ToggleButton toggle = new ToggleButton( "\n\n\n\n" + nickname );
        toggle.setId( nickname );
        toggle.setMinSize( 72, 100 );
        toggle.setMaxSize( 72, 100 );
        toggle.setFont( new Font( 12 ) );
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
        toggle.setOnAction( this::toggleClicked );
        return toggle;
    }

    @FXML private void toggleClicked( ActionEvent event ){
        String selected = ( ( ToggleButton )event.getSource() ).getId();
        game.setClickedCard( selected );
        game.setWaitingForButton( false );
    }

    public void setPlayersCardsActive( boolean active ){
        for( ToggleButton toggle: playersCards ){
            if( !toggle.getId().equals( Game.UNIQUE_CHAR + "You" ) )
                toggle.setDisable( !active );
        }
    }

    public void setPlayersCardsSelected( boolean selected ){
        for( ToggleButton toggle: playersCards )
            toggle.setSelected( selected );
    }

    public void sePlayerCardActive( int playerIndex, boolean active ){
        playersCards.get( playerIndex ).setDisable( !active );
    }

    public void setTableCardsSelected( boolean selected ){
        card0.setSelected( selected );
        card1.setSelected( selected );
        card2.setSelected( selected );
    }

    public void setTableCardsActive( boolean active ){
        card0.setDisable( !active );
        card1.setDisable( !active );
        card2.setDisable( !active );
    }

    @FXML void tableCardClicked(){
        String selected = getToggleId( card0.getToggleGroup().getSelectedToggle() );
        setTableCardsActive( false );
        game.setClickedCard( selected );
        game.setWaitingForButton( false );
    }

    private String getToggleId( Toggle toggle ){
        return toggle.toString().split( "=" )[ 1 ].split( "," )[ 0 ];
    }

    @FXML private AnchorPane gamePane;
    @FXML private ToggleButton card0;
    @FXML private ToggleButton card1;
    @FXML private ToggleButton card2;
    private Vector< ToggleButton > playersCards = new Vector<>();;
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
