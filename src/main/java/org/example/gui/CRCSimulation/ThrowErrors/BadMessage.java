package org.example.gui.CRCSimulation.ThrowErrors;

public class BadMessage extends RuntimeException {
    public BadMessage(String message) {
        super(message);
    }
}
