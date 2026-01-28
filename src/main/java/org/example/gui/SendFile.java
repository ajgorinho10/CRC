package org.example.gui;

import org.example.gui.CRCSimulation.Siec;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


/*
 * Klasa SendFile obsługuje GUI odpowiedzialne za wysyłanie plików
 *
 * initialize: Ustawia listę dostępnych komputerów i zbiera dane o wybranym nadawcy i odbiorcy
 * sendMsg: Weryfikuje dane wejściowe, wywołuje metodę Siec.sendFILE w celu wysłania pliku
 */



public class SendFile {

    public VBox root;

    @FXML
    private TextField msgInput;

    @FXML
    private Label responseMsg;

    @FXML
    private ListView statusMSG;

    @FXML
    private ChoiceBox<Integer> choiceBoxA;

    @FXML
    private ChoiceBox<Integer> choiceBoxB;

    public SendFile() {}


    @FXML
    public void initialize() {

        ObservableList<Integer> listaId = FXCollections.observableArrayList();
        for(int i = 0; i< Siec.AMOUNT_OF_PC; i++){
            listaId.add(Siec.pcList.get(i).id);
        }

        choiceBoxA.setItems(listaId);
        choiceBoxA.getSelectionModel().selectFirst();

        choiceBoxB.setItems(listaId);
        choiceBoxB.getSelectionModel().selectFirst();

        AppState appState = AppState.getInstance();

        statusMSG.setItems(appState.getStatusFILE());
    }


    @FXML
    protected void sendMsg() throws InterruptedException {
        AppState appState = AppState.getInstance();
        appState.clearDrogaFILE();

        String msg = msgInput.getText().trim();

        Integer source = choiceBoxA.getValue();
        Integer destination = choiceBoxB.getValue();

        if (source.equals(destination) || msg.isEmpty()) {
            responseMsg.setText("Nie prawidłowe dane");
        } else {
            responseMsg.setText("Wysłano: '" + msg + "' (Z:"+source+" Do:"+destination +")");
            String response = Siec.sendFILE(msg, source-1, destination-1);
        }
    }
}