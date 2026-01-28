package org.example.gui.CRCSimulation.ThrowErrors;

/*
 * Klasa BadMessage to niestandardowy wyjątek czasu wykonania, sygnalizuje błąd związany z niepoprawną
 *     treścią wiadomości tekstowej lub pliku.
 */


public class BadMessage extends RuntimeException {
    public BadMessage(String message) {
        super(message);
    }
}
