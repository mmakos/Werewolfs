package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class GameWindow{
    private Game game;

    public void setGame( Game game ){ this.game = game; }

    @FXML public void initialize(){}

    public void setCardButton( String str ){
       Platform.runLater( () -> cardLabel.setText( str ) );
    }

    public void setStatementLabel( String str ){
        Platform.runLater( () -> statementLabel.setText( str ) );
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

    private String getToggleId( Toggle toggle ){
        return toggle.toString().split( "=" )[ 1 ].split( "," )[ 0 ];
    }
    @FXML private ToggleButton card0;
    @FXML private ToggleButton card1;
    @FXML private ToggleButton card2;
    @FXML private Button cardLabel;
    @FXML private Label statementLabel;
}
