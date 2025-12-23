package org.example.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.gui.CRCSimulation.Siec;

public class CRCGui extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Siec.start();
        Siec.makeError(-1,0);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("CRC-view.fxml"));
        Scene scene = new Scene(loader.load(),800,600);

        stage.setTitle("CRC-GRAPH");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}