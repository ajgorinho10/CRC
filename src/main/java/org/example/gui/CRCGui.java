package org.example.gui;

import org.example.gui.CRCSimulation.Siec;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


/*
 * Klasa CRCGui inicjalizuje GUI aplikacji i uruchamia symulację sieci CRC
 *
 * start: Wywołuje Siec.start() która inicjalizuje symulację sieci,
 *     za pomocą FXMLLoader ładuje główny widok aplikacji, ustawia tytuł i rozdzielczość okna
 * main: Uruchamia aplikację JavaFX
 */


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