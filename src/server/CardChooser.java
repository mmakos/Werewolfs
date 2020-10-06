package server;

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

import java.util.Vector;

public class CardChooser{
    @FXML
    public void initialize(){
        createToggles();
    }

    public void createToggles(){
        for( int i = 0; i < Card.card.length; ++i ){
            ToggleButton toggle = new ToggleButton( "\n\n\n\n" + Card.card[ i ] );
            toggle.setMinSize( 72, 100 );
            toggle.setMaxSize( 72, 100 );
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
            toggles.add( toggle );
            gridpane.add( toggle, i % gridpaneWidth, i / gridpaneWidth );
        }
    }

    private void toggleClicked( ActionEvent event ){
        ToggleButton selectedToggle = ( ToggleButton )event.getSource();
        String[] temp = ( selectedToggle.getText().split( "\n" ) );
        String clickedCard = temp[ temp.length - 1 ];
        if( selectedToggle.isSelected() )
            addCard( clickedCard, selectedToggle );
        else
            removeCard( clickedCard, selectedToggle );
    }

    private void removeCard( String clickedCard, ToggleButton selectedToggle ){
        --selectedToggles;
        dealButton.setDisable( true );
    }

    private void addCard( String clickedCard, ToggleButton selectedToggle ){
        if( selectedToggles >= players )
            selectedToggle.setSelected( false );
        else{
            ++selectedToggles;
            if( selectedToggles == players )
                dealButton.setDisable( false );
        }
    }

    @FXML public void dealTheCards(){
        server.setSelectedCards( getSelectedCards() );
        server.giveAwayCards();
    }

    private String[] getSelectedCards(){
        String[] string = new String[ players + 3 ];
    }

    private int players = 0;
    private int selectedToggles = 0;
    private static final int gridpaneWidth = 5;
    private Vector< ToggleButton > toggles = new Vector<>();
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
