package org.example.gui;

// Importuj właściwą listę JavaFX
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    private ListView statusMSG;

    @FXML
    private ChoiceBox<Integer> choiceBoxA;

    @FXML
    private ChoiceBox<Integer> choiceBoxB;

    @FXML
    private ChoiceBox<String> choiceBoxError;

    @FXML
    private ChoiceBox<Integer> choiceBoxErrorPC;

    @FXML
    private VBox ErrorVbox;


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

        ObservableList<String> lista2 = FXCollections.observableArrayList();
        lista2.add("Brak błędu");
        lista2.add("Błąd połączenia między węzłami");
        lista2.add("Błąd weryfikacji CRC między węzłami");
        lista2.add("Błąd przesyłania wiadomości");
        choiceBoxError.setItems(lista2);
        choiceBoxError.getSelectionModel().selectFirst();

        choiceBoxErrorPC.setItems(listaId);
        choiceBoxErrorPC.getSelectionModel().selectFirst();

        AppState appState = AppState.getInstance();

        statusMSG.setItems(appState.getStatus());
    }

    @FXML
    protected void ShowErrorPCbox(){
        if(choiceBoxError.getSelectionModel().getSelectedIndex() != 0){
            ErrorVbox.setVisible(true);
        }else if(ErrorVbox.isVisible()){
            ErrorVbox.setVisible(false);
        }
    }

    @FXML
    protected void sendMsg() throws InterruptedException {
        AppState appState = AppState.getInstance();
        appState.clearDroga();

        String msg = msgInput.getText().trim();

        Integer source = choiceBoxA.getValue();
        Integer destination = choiceBoxB.getValue();

        if (source.equals(destination) || msg.isEmpty()) {
            responseMsg.setText("Nie prawidłowe dane");
        } else {
            int etype = choiceBoxError.getSelectionModel().getSelectedIndex();
            int ePC = choiceBoxErrorPC.getValue() - 1;
            System.out.println("etype:"+etype+" pc"+ePC);
            if(etype == 0){
                Siec.makeError(-1,0);
            }else{
                Siec.makeError(etype,ePC);
            }
            responseMsg.setText("Wysyłanie...: '" + msg + "' (Z:"+source+" Do:"+destination +")");
            String response = Siec.sendMSG(msg, source-1, destination-1);
        }
    }
}