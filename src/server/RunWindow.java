package server;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class RunWindow{
    private Server server;
    @FXML
    public void initialize(){}

    public void setServer( Server server ){
        this.server = server;
    }
}