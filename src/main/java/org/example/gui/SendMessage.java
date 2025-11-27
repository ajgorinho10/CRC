package org.example.gui;

// Importuj właściwą listę JavaFX
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.gui.CRCSimulation.Komputer;
import org.example.gui.CRCSimulation.Siec;

import java.util.ArrayList;
import java.util.List;

// Usuń nieużywany import: eu.hansolo.toolbox.observables.ObservableList

public class SendMessage {

    public VBox root;

    @FXML
    private TextField msgInput;

    @FXML
    private Label responseMsg;

    @FXML
    private Label statusMSG;

    @FXML
    private ChoiceBox<Integer> choiceBoxA;

    @FXML
    private ChoiceBox<Integer> choiceBoxB;


    public SendMessage() {}


    @FXML
    public void initialize() {

        ObservableList<Integer> listaId = FXCollections.observableArrayList();
        for(int i=0;i<Siec.AMOUNT_OF_PC;i++){
            listaId.add(Siec.pcList.get(i).id);
        }

        choiceBoxA.setItems(listaId);
        choiceBoxA.getSelectionModel().selectFirst();

        choiceBoxB.setItems(listaId);
        choiceBoxB.getSelectionModel().selectFirst();
    }


    @FXML
    protected void sendMsg() throws InterruptedException {
        String msg = msgInput.getText().trim();

        Integer source = choiceBoxA.getValue();
        Integer destination = choiceBoxB.getValue();

        if (source.equals(destination) || msg.isEmpty()) {
            responseMsg.setText("Nie prawidłowe dane");
        } else {
            responseMsg.setText("Wysyłanie...: '" + msg + "' (Z:"+source+" Do:"+destination +")");
            statusMSG.setText("STATUS:"+Siec.sendMSG(msg, source-1, destination-1));
        }
    }
}