package server;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.LinkedList;
import java.util.Vector;

public class CardChooser{
    public static int werewolvesQuant = 3;
    public static String[] card = { "Copycat", "Werewolf_0", "Insomniac", "Mystic wolf", "Seer", "Beholder",
            "Witch", "Apprentice seer", "Tanner", "Minion", "Werewolf_1", "Werewolf_2",
            "Robber", "Thing", "Paranormal investigator", "Troublemaker" };

    @FXML public void initialize(){
        createToggles();
    }

    public void createToggles(){
        for( int i = 0; i < CardChooser.card.length; ++i ){
            ToggleButton toggle = new ToggleButton();
            toggle.setId( CardChooser.card[ i ] );
            toggle.setMinSize( 72, 100 );
            toggle.setMaxSize( 72, 100 );
            toggle.setWrapText( true );
            final Image unselected = new Image( "/img/smallCards/frontCardBig" + CardChooser.card[ i ].split( " " )[ 0 ] + "Dark.png" );
            final Image selected = new Image( "/img/smallCards/frontCardBig" + CardChooser.card[ i ].split( " " )[ 0 ] + ".png" );
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
            toggles.add( toggle );
            gridpane.add( toggle, i % gridpaneWidth, i / gridpaneWidth );
        }
    }

    private void toggleClicked( ActionEvent event ){
        ToggleButton selectedToggle = ( ToggleButton )event.getSource();
        if( selectedToggle.isSelected() )
            addCard( selectedToggle );
        else
            removeCard();
    }

    private void removeCard(){
        --selectedToggles;
        dealButton.setDisable( true );
    }

    private void addCard( ToggleButton selectedToggle ){
        if( selectedToggles >= players )
            selectedToggle.setSelected( false );
        else{
            ++selectedToggles;
            if( selectedToggles == players )
                dealButton.setDisable( false );
        }
    }

    @FXML public void dealTheCards(){
        Thread game = new Thread( () -> {
            server.setSelectedCards( getSelectedCards() );
            server.drawCards();
            try{
                server.sendCardsToPlayers();
                Platform.runLater( () -> dealButton.getScene().getWindow().hide() );
                server.startGame();
            }catch( InterruptedException e ){
                e.printStackTrace();
            }
        });
        game.start();
    }

    private LinkedList< String > getSelectedCards(){
        LinkedList< String > selectedCards = new LinkedList<>();
        for( ToggleButton toggle: toggles ){
            if( toggle.isSelected() ){
                selectedCards.add( toggle.getId() );
            }
        }
        return selectedCards;
    }

    private int players = 0;
    private int selectedToggles = 0;
    private static final int gridpaneWidth = 6;
    private final Vector< ToggleButton > toggles = new Vector<>();
    @FXML private GridPane gridpane;
    @FXML private Button dealButton;
    private Server server;

    public void setPlayers( int players ){
        this.players = players + 3;
    }

    public void setServer( Server server ){
        this.server = server;
    }
}
