package client;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Vector;

public class GameWindow{
    private Game game;

    public void setGame( Game game ){
        this.game = game;
        cardLabel.setText( game.statements[ 38 ] + " " );
    }

    @FXML public void initialize(){ setOnDrag();
        card0.setId( Game.UNIQUE_CHAR + "card0" );
        card1.setId( Game.UNIQUE_CHAR + "card1" );
        card2.setId( Game.UNIQUE_CHAR + "card2" );
        reverseCardButton.setVisible( false );
    }

    private void setOnDrag(){
        gamePane.setOnMousePressed( mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        } );

        gamePane.setOnMouseDragged( mouseEvent -> {
            Stage stage = ( Stage ) gamePane.getScene().getWindow();
            stage.setX( mouseEvent.getScreenX() - x );
            stage.setY( mouseEvent.getScreenY() - y );
            stage.setOpacity( 0.9 );
        } );

        gamePane.setOnMouseReleased( mouseEvent -> gamePane.getScene().getWindow().setOpacity( 1.0 ) );
    }

    @FXML public void quit(){
        quitButton.getScene().getWindow().hide();
    }

    public void setCardLabel( String str ){
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
        int idx;
        switch( player ){
            case ( char )2 + "card0": toggle = card0; idx = knownCards.size() - 3; break;
            case ( char )2 + "card1": toggle = card1; idx = knownCards.size() - 2; break;
            case ( char )2 + "card2": toggle = card2; idx = knownCards.size() - 1; break;
            default: idx = game.players.indexOf( player ); toggle = playersCards.get( idx );
        }
        knownCards.set( idx, card );
        Platform.runLater( () -> {
            reverseCardButton.setText( game.statements[ 36 ] );
            reverseCardButton.setVisible( true );
        } );
        if( !game.players.contains( player ) )
            Platform.runLater( () -> toggle.setStyle( "-fx-background-image: url(\"img/smallCards/frontCardBig" + card.split( " " )[ 0 ] + ".png\")" ) );
        else
            Platform.runLater( () -> toggle.setStyle( "-fx-graphic: url(\"img/smallCards/frontCardBig" + card.split( " " )[ 0 ] + ".png\")" ) );
        toggle.setOpacity( 1.0 );
    }

    @FXML public void hideShowCardNames(){
        if( reverseCardButton.getText().equals( game.statements[ 36 ] ) ){
            card0.setStyle( "-fx-background-image: url(\"img/backCardSmall.png\")" );
            card1.setStyle( "-fx-background-image: url(\"img/backCardSmall.png\")" );
            card2.setStyle( "-fx-background-image: url(\"img/backCardSmall.png\")" );
            if( card0.isDisabled() ) card0.setOpacity( 0.5 );
            if( card1.isDisabled() ) card1.setOpacity( 0.5 );
            if( card2.isDisabled() ) card2.setOpacity( 0.5 );
            for( int i = 0; i < playersCards.size(); ++i ){
                if( knownCards.get( i ) != null ){
                    playersCards.get( i ).setStyle( "-fx-graphic: url(\"img/backCardSmall.png\")" );
                    if( playersCards.get( i ).isDisabled() ) playersCards.get( i ).setOpacity( 0.5 );
                }
            }
            reverseCardButton.setText( game.statements[ 37 ] );
        } else{
            for( int i = 0; i < playersCards.size(); ++i ){
                if( knownCards.get( i ) != null )
                    reverseCard( game.players.get( i ), knownCards.get( i ) );
            }
            for( int i = playersCards.size(), j = 0; i < knownCards.size(); ++i, ++j ){
                if( knownCards.get( i ) != null )
                    reverseCard( Game.UNIQUE_CHAR + "card" + j, knownCards.get( i ) );
            }
            reverseCardButton.setText( game.statements[ 36 ] );
        }
    }

    public void hideCenterCard( String card ){
        ToggleButton toggle;
        int idx;
        switch( card ){
            case ( char )2 + "card0": toggle = card0; idx = knownCards.size() - 3; break;
            case ( char )2 + "card1": toggle = card1; idx = knownCards.size() - 2; break;
            case ( char )2 + "card2": toggle = card2; idx = knownCards.size() - 1; break;
            default: return;
        }
        toggle.setStyle( "-fx-background-image: url(\"img/backCardSmall.png\")" );
        toggle.setOpacity( 0.5 );
        knownCards.set( idx, null );
    }

    public void updateMyCard( String card ){
        ToggleButton toggle = playersCards.get( game.players.indexOf( game.nickname ) );
        game.displayedCard = card;
        Platform.runLater( () -> toggle.setStyle( "-fx-graphic: url(\"img/smallCards/frontCardBig" + card.split( " " )[ 0 ] + ".png\")" ) );
    }

    public void createPlayersCards(){
        int a = 500, b = 280, p = game.players.size();

        playersCards.setSize( p );
        knownCards.setSize( p + 3 );
        knownCards.forEach( ( knownCard ) -> knownCard = "" );
        int ourPos = game.players.indexOf( game.nickname );        //start drawing cadrs from ours
        ToggleButton toggle = getPlayerCard( game.nickname );
        toggle.setId( Game.UNIQUE_CHAR + "You" );
        toggle.setText( "You\n\n\n\n\n." );
        double ti = Math.toRadians( -90 );
        toggle.setLayoutX( a * Math.cos( ti ) + ( sceneWidth / 2.0 ) - ( cardWidth / 2.0 ) + 20 );
        toggle.setLayoutY( -1 * ( b * Math.sin( ti ) ) + ( sceneHeight / 2.0 ) - ( cardHeight / 2.0 ) + 40 );
        toggle.setOpacity( 1.0 );
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
        ToggleButton toggle = new ToggleButton(  nickname + "\n\n\n\n\n." );
        toggle.setId( nickname );
        toggle.setMinSize( 72, 100 );
        toggle.setMaxSize( 72, 100 );
        toggle.setFont( new Font( 12 ) );
        toggle.setDisable( true );
        final Image unselected = new Image( "img/backCardSmall.png" );
        final Image selected = new Image( "img/backCardSmall.png" );
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
        ( ( ToggleButton )event.getSource() ).setDisable(true);
        game.setClickedCard( selected );
        game.setWaitingForButton( false );
    }

    public void setPlayersCardsActive( boolean active ){
        for( ToggleButton toggle: playersCards ){
            if( !toggle.getId().equals( Game.UNIQUE_CHAR + "You" ) ){
                toggle.setDisable( !active );
                if( reverseCardButton.getText().equals( game.statements[ 37 ] ) || knownCards.get( playersCards.indexOf( toggle ) ) == null )
                    toggle.setOpacity( active ? 1.0 : 0.5 );
            }
        }
    }

    public void setPlayersCardsSelected( boolean selected ){
        for( ToggleButton toggle: playersCards )
            toggle.setSelected( selected );
    }

    public void setPlayerCardActive( int playerIndex, boolean active ){
        playersCards.get( playerIndex ).setDisable( !active );
        if( reverseCardButton.getText().equals( game.statements[ 37 ] ) || knownCards.get( playerIndex ) == null )
            playersCards.get( playerIndex ).setOpacity( active ? 1.0 : 0.5 );
    }

    public void setPlayerCardSelected( int playerIndex, boolean selected ){
        playersCards.get( playerIndex ).setSelected( selected );
    }

    public void setCenterCardSelected(String cardID,boolean selected){
        switch (cardID){
            case ( char )2 + "card0": card0.setSelected( selected ); break;
            case ( char )2 + "card1": card1.setSelected( selected ); break;
            case ( char )2 + "card2": card2.setSelected( selected ); break;
            default: break;
        }
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
        if( reverseCardButton.getText().equals( game.statements[ 37 ] ) || knownCards.get( knownCards.size() - 3 ) == null ) card0.setOpacity( active ? 1.0 : 0.5 );
        if( reverseCardButton.getText().equals( game.statements[ 37 ] ) || knownCards.get( knownCards.size() - 2 ) == null ) card1.setOpacity( active ? 1.0 : 0.5 );
        if( reverseCardButton.getText().equals( game.statements[ 37 ] ) || knownCards.get( knownCards.size() - 1 ) == null ) card2.setOpacity( active ? 1.0 : 0.5 );
    }
//
//    @FXML void tableCardClicked(){
//        String selected = getToggleId( card0.getToggleGroup().getSelectedToggle() );
//        setTableCardsActive( false );
//        game.setClickedCard( selected );
//        game.setWaitingForButton( false );
//    }

    public void drawArrow( String from, String to ){
        ToggleButton fromToggle = playersCards.get( game.players.indexOf( from ) );
        double x1, y1, toLayoutX, toLayoutY;
        x1 = fromToggle.getLayoutX() + 36;
        y1 = fromToggle.getLayoutY() + 50;
        if( to.equals( Game.UNIQUE_CHAR + "table" ) ){
            toLayoutX = ( double )sceneWidth / 2;
            toLayoutY = ( double )sceneHeight / 2 - 50;
        }
        else{
            ToggleButton toToggle = playersCards.get( game.players.indexOf( to ) );
            int a = 62;
            double x2 = toToggle.getLayoutX() + 36;
            double y2 = toToggle.getLayoutY() + 50;
            double dist = Math.hypot( x2 - x1, y2 - y1 );
            toLayoutX = x2 - ( a * ( x2 - x1 ) / dist );
            toLayoutY = y2 - ( a * ( y2 - y1 ) / dist );
        }
        Vector< Line > v = getArrow( x1, toLayoutX, y1, toLayoutY );
        lines.addAll( v );
        gamePane.getChildren().add( lines.get( lines.size() - 3 ) );
        gamePane.getChildren().add( lines.get( lines.size() - 2 ) );
        gamePane.getChildren().add( lines.get( lines.size() - 1 ) );
    }

    private Vector< Line > getArrow( double fromX, double toX, double fromY, double toY ){
        Vector< Line > v = new Vector<>();
        Line line = new Line( fromX, fromY, toX, toY );
        line.setStroke( Color.RED );
        v.add( line );
        double hypo = Math.hypot( fromX - toX, fromY - toY );
        double factor = 20 / hypo;
        double factorO = 7 / hypo;
        double dx = ( fromX - toX ) * factor;
        double dy = ( fromY - toY ) * factor;
        double ox = ( fromX - toX ) * factorO;
        double oy = ( fromY - toY ) * factorO;
        line = new Line( toX + dx - oy, toY + dy + ox, toX, toY );
        line.setStroke( Color.RED );
        v.add( line );
        line = new Line( toX + dx + oy, toY + dy - ox, toX, toY );
        line.setStroke( Color.RED );
        v.add( line );
        return v;
    }

    public void clearArrows(){
        Platform.runLater( () -> lines.forEach( ( line ) -> gamePane.getChildren().remove( line ) ) );
        lines.removeAllElements();
    }

    private String getToggleId( Toggle toggle ){
        return toggle.toString().split( "=" )[ 1 ].split( "," )[ 0 ];
    }

    @FXML private AnchorPane gamePane;
    @FXML private ToggleButton card0;
    @FXML private ToggleButton card1;
    @FXML private ToggleButton card2;
    private Vector< ToggleButton > playersCards = new Vector<>();
    private Vector< Line > lines = new Vector<>();
    public Vector< String > knownCards = new Vector<>();
    @FXML private Label cardLabel;
    @FXML public Label statementLabel;
    @FXML private Label nicknameLabel;
    @FXML private Label roleInfo;
    @FXML private Button reverseCardButton;
    @FXML public Button quitButton;
    private static final int sceneWidth = 1280, sceneHeight = 820;
    private static final int cardWidth = 100, cardHeight = 72;
    private double x = 0, y = 0;

    public void setNicknameLabel( String nickname ){
        this.nicknameLabel.setText( nickname );
    }
}
