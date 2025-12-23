package org.example.gui.CRCSimulation.ThrowErrors;

public class BadCrc extends RuntimeException {
    public BadCrc(String message) {
        super(message);
    }
}
