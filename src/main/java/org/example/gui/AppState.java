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

    public static void addDrogaGlobal(String text){
        getInstance().addDroga(text);
    }

    public void addDroga(String text) {
        if (text == null || text.trim().isEmpty()) return;

        Platform.runLater(() -> {
            String czystyTekst = text.trim();

            if (!drogaWiadomosci.contains(czystyTekst)) {
                drogaWiadomosci.add(czystyTekst);
            } else {
            }
        });
    }

    public void clearDroga() {
        Platform.runLater(drogaWiadomosci::clear);
    }

    public ObservableList<String> getStatus(){
        return drogaWiadomosci;
    }

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
