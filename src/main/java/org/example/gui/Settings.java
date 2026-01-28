package org.example.gui;

import org.example.gui.CRCSimulation.CRC;
import org.example.gui.CRCSimulation.Siec;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/*
 * Klasa Settings obsługuje GUI do konfiguracji parametrów algorytmu CRC oraz wybór
 * scenariuszy błędów w symulowanej sieci.
 *
 * initialize: Zapełnia listy wyboru oraz ładuje bieżące parametry wielomianu i flag CRC.
 * Save: Pobiera i waliduje dane z interfejsu, tworzy nową instancję klasy CRC z zebranymi parametrami oraz
 *     aktualizuje globalny stan błędów.
 * ShowErrorPCbox: Pokazuje dodatkowy element interfejsu gdy wybrany jest jeden ze scenariuszy błędów.
 */



public class Settings {

    @FXML
    private ChoiceBox<String> choiceBoxError;

    @FXML
    private ChoiceBox<Integer> choiceBoxErrorPC;

    @FXML
    private HBox ErrorVbox;

    @FXML
    private TextField polly;

    @FXML
    private TextField init;

    @FXML
    private TextField XorOut;

    @FXML
    private TextField dlugosc;

    @FXML
    private CheckBox refIn;

    @FXML
    private CheckBox refOut;

    @FXML
    private Label status;

    @FXML
    public void initialize(){

        ObservableList<String> lista2 = FXCollections.observableArrayList();
        lista2.add("Brak błędu");
        lista2.add("Błąd połączenia między węzłami");
        lista2.add("Błąd weryfikacji CRC między węzłami");
        lista2.add("Błąd przesyłania wiadomości");
        choiceBoxError.setItems(lista2);
        choiceBoxError.getSelectionModel().selectFirst();

        choiceBoxErrorPC.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10));
        choiceBoxErrorPC.getSelectionModel().selectFirst();

        Siec.makeError(0,1);

        this.polly.setText(String.valueOf(Siec.crc.polynomial));
        this.init.setText(String.valueOf(Siec.crc.initialValue));
        this.XorOut.setText(String.valueOf(Siec.crc.xorOut));
        this.dlugosc.setText(String.valueOf(Siec.crc.width));
        this.refIn.setSelected(Siec.crc.refIn);
        this.refOut.setSelected(Siec.crc.refOut);
    }

    @FXML
    protected void Save(){
        Siec.makeError(
                choiceBoxError.getSelectionModel().getSelectedIndex(),
                choiceBoxErrorPC.getSelectionModel().getSelectedIndex()
                );

        try {
            long ply = Long.parseLong(polly.getText());
            long ini = Long.parseLong(init.getText());
            long xor = Long.parseLong(XorOut.getText());
            int wid = Integer.parseInt(dlugosc.getText());
            boolean refi = refIn.isSelected();
            boolean refo = refOut.isSelected();

            Siec.crc = new CRC(ply,ini,xor,wid,refi,refo);
            status.setText("STATUS: OK!");
        }catch (Exception e){
            status.setText("STATUS: BŁĄD!");
        }


    }

    @FXML
    protected void ShowErrorPCbox(){
        if(choiceBoxError.getSelectionModel().getSelectedIndex() != 0){
            ErrorVbox.setVisible(true);
        }else {
            ErrorVbox.setVisible(false);
        }
    }
}
