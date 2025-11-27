package org.example.gui.CRCSimulation;

import lombok.Data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class CRC {

    private final long polynomial;
    private final long initialValue;
    private final long xorOut;
    private final int width;
    private final long[] crcTable = new long[256];
    private final long topBit;
    public final long mask;

    public CRC(long poly, long init, long xorOut, int width) {
        this.polynomial = poly;
        this.initialValue = init;
        this.xorOut = xorOut;
        this.width = width;
        this.topBit = 1L << (width - 1);
        this.mask = (1L << width) - 1;
        generateCRCTable();
    }


    private void generateCRCTable() {
        for (int i = 0; i < 256; i++) {
            long crc = (long) i;

            crc = crc << (width - 8);
            for (int j = 0; j < 8; j++) {
                if ((crc & topBit) != 0) {
                    crc = (crc << 1) ^ polynomial;
                } else {
                    crc = crc << 1;
                }
            }
            crcTable[i] = crc & mask;
        }
    }

    public long calculateCRCFromFile(Path filePath) throws IOException {

        long crc = initialValue;

        try (InputStream in = new BufferedInputStream(Files.newInputStream(filePath))) {
            int currentByte;

            while ((currentByte = in.read()) != -1) {
                int index = (int) (((crc >> (width - 8)) & 0xFF) ^ currentByte);
                crc = crc << 8;
                crc ^= crcTable[index];
            }
        }

        return (crc & mask) ^ xorOut;
    }

    public long calculateCRCFromString(String text) {

        byte[] data = text.getBytes();
        long crc = initialValue;

        for (byte b : data) {
            int currentByte = b & 0xFF;
            int index = (int) (((crc >> (width - 8)) & 0xFF) ^ currentByte);
            crc = crc << 8;
            crc ^= crcTable[index];
        }

        return (crc & mask) ^ xorOut;
    }
}
