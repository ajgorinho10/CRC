package org.example.gui.CRCSimulation.ThrowErrors;

/*
 * Klasa BadCrc to niestandardowy wyjątek czasu wykonania, sygnalizuje błędy weryfikacji sumy kontrolnej pakietu.
 */


public class BadCrc extends RuntimeException {
    public BadCrc(String message) {
        super(message);
    }
}
