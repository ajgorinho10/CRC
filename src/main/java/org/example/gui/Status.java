package org.example.gui;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/*
 * Klasa Status obsługuje GUI podglądu statystyk.
 *
 * initialize: Konfiguruje automatyczne odświeżanie etykiet dla liczników.
 * updateAverageTime: Oblicza średni czas przesyłania danych.
 */



public class Status {

    @FXML
    private Label successLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Label avgTimeLabel;

    @FXML
    public void initialize() {
        AppState appState = AppState.getInstance();

        successLabel.textProperty().bind(
                appState.successfulMessagesProperty().asString()
        );

        errorLabel.textProperty().bind(
                appState.errorMessagesProperty().asString()
        );


        appState.getDeliveryTimes().addListener((ListChangeListener<Double>) c -> {
            updateAverageTime(appState.getDeliveryTimes());
        });

        updateAverageTime(appState.getDeliveryTimes());
    }

    private void updateAverageTime(List<Double> times) {
        if (times.isEmpty()) {
            avgTimeLabel.setText("N/A");
            return;
        }

        double sum = 0;
        for (double time : times) {
            sum += time;
        }
        double avg = sum / times.size();

        avgTimeLabel.setText(String.format("%.4f ms", avg));
    }
}
