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
