package org.example.gui;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppState {

    private static final AppState instance = new AppState();

    private final IntegerProperty successfulMessages = new SimpleIntegerProperty(0);
    private final IntegerProperty errorMessages = new SimpleIntegerProperty(0);
    private final ObservableList<Double> deliveryTimes = FXCollections.observableArrayList();
    private final ObservableList<String> drogaWiadomosci = FXCollections.observableArrayList();
    private final ObservableList<String> drogaPliku= FXCollections.observableArrayList();

    private static int numberOfMSg = 0;
    private static int numberOfFILe = 0;

    private AppState() {
    }

    public static AppState getInstance() {
        return instance;
    }

    public void reportSuccess(double timeInMs) {
        Platform.runLater(() -> {
            successfulMessages.set(successfulMessages.get() + 1);
            deliveryTimes.add(timeInMs);
        });
    }

    public void reportError() {
        Platform.runLater(() -> {
            errorMessages.set(errorMessages.get() + 1);
        });
    }

    public static void addDrogaMSGGlobal(String text){
        getInstance().addDrogaMSG(text);
    }

    public static void addDrogaFILEGlobal(String text){
        getInstance().addDrogaFILE(text);
    }

    public void addDrogaMSG(String text) {
        if (text == null || text.trim().isEmpty()) return;

        Platform.runLater(() -> {
            String czystyTekst = text.trim();

            if (!drogaWiadomosci.contains(czystyTekst)) {
                drogaWiadomosci.add(czystyTekst);
                numberOfMSg += 1;
            } else {
            }

            if (numberOfMSg%2 == 0){
                drogaWiadomosci.add(" ");
            }
        });
    }

    public void clearDrogaMSG() {
        Platform.runLater(drogaWiadomosci::clear);
        numberOfMSg = 0;
    }

    public void addDrogaFILE(String text) {
        if (text == null || text.trim().isEmpty()) return;

        Platform.runLater(() -> {
            String czystyTekst = text.trim();

            if (!drogaPliku.contains(czystyTekst)) {
                drogaPliku.add(czystyTekst);
                numberOfFILe += 1;
            } else {
            }

            if (numberOfFILe%2 == 0){
                drogaPliku.add(" ");
            }
        });
    }

    public void clearDrogaFILE() {
        Platform.runLater(drogaPliku::clear);
        numberOfFILe = 0;
    }

    public ObservableList<String> getStatus(){
        return drogaWiadomosci;
    }

    public ObservableList<String> getStatusFILE(){return drogaPliku;}

    public IntegerProperty successfulMessagesProperty() {
        return successfulMessages;
    }

    public IntegerProperty errorMessagesProperty() {
        return errorMessages;
    }

    public ObservableList<Double> getDeliveryTimes() {
        return deliveryTimes;
    }

}
