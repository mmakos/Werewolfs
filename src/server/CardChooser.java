package server;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

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
            toggles.add( toggle );
            gridpane.add( toggle, i % gridpaneWidth, i / gridpaneWidth );
        }
    }

    private static final int gridpaneWidth = 5;
    private Vector< ToggleButton > toggles = new Vector<>();
    @FXML private GridPane gridpane;
}
