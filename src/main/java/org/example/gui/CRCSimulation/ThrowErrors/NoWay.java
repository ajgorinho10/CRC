package org.example.gui.CRCSimulation.ThrowErrors;

/*
 * Klasa NoWay to niestandardowy wyjątek czasu wykonania, sygnalizuje błędy routingu i brak połączenia między węzłami w sieci.
 */

public class NoWay extends RuntimeException {
    public NoWay(String message) {
        super(message);
    }
}
