package org.example.gui.CRCSimulation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.Data;


/*
 * Klasa CRC realizuje logikę obliczania i weryfikacji algorytmu CRC
 *
 * generateCRCTable: Generuje statyczną tablicę wartości dla danego wielomianu, optymalizuje proces obliczeń
 * calculateCRCFromFile: Wyznacza wartość CRC dla pliku binarnego bajt po bajcie.
 * calculateCRCFromString: Wyznacza wartość CRC dla danej wiadomości.
 * Verify: Weryfikuje poprawność odebranych danych przez obliczenie CRC i porównanie z przesłaną sumą kontrolną.
 * reverseBits: Wykonuje bit reversal dla danych wejściowych/wyniku końcowego.
 */

@Data
public class CRC {

    public final long polynomial;
    public final long initialValue;
    public final boolean refIn;
    public final boolean refOut;
    public final long xorOut;
    public final int width;


    private final long[] crcTable = new long[256];
    public int[] refTab = new int[256];
    public final long mask;

    public CRC(long poly, long init, long xorOut, int width, boolean refIn, boolean refOut) {
        this.polynomial = poly;
        this.initialValue = init;
        this.xorOut = xorOut;
        this.width = width;
        this.refIn = refIn;
        this.refOut = refOut;

        this.mask = (1L << width) - 1;

        generateCRCTable();
        if(refIn) {
            GenerateRefTable();
        }
    }

    public CRC(long poly, long init, long xorOut, int width) {
        this.polynomial = poly;
        this.initialValue = init;
        this.xorOut = xorOut;
        this.width = width;
        this.refIn = false;
        this.refOut = false;

        this.mask = (1L << width) - 1;

        generateCRCTable();
    }

    private void generateCRCTable() {
        long topBit = 1L << (width - 1);

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

    private void GenerateRefTable(){
        for (int i = 0; i < 256; i++) {
            refTab[i] = reverseBits(i);
        }
    }

    public long calculateCRCFromFile(Path filePath) throws IOException {

        long crc = initialValue;

        try (InputStream in = new BufferedInputStream(Files.newInputStream(filePath))) {
            int currentByte;

            while ((currentByte = in.read()) != -1) {

                if(refIn){
                    currentByte = refTab[currentByte];
                }
                int index = (int) (((crc >> (width - 8)) & 0xFF) ^ currentByte);
                crc = crc << 8;
                crc ^= crcTable[index];
            }
        }

        if(refOut){
            crc = reverseBits(crc);
        }

        return (crc & mask) ^ xorOut;
    }

    public long calculateCRCFromString(String text) {

        byte[] data = text.getBytes();
        long crc = initialValue;

        for (byte b : data) {
            int currentByte = b & 0xFF;
            if(refIn){
                currentByte = refTab[currentByte];
            }

            int index = (int) (((crc >> (width - 8)) & 0xFF) ^ currentByte);
            crc = crc << 8;
            crc ^= crcTable[index];
        }

        if(refOut){
            crc = reverseBits(crc);
        }

        return (crc & mask) ^ xorOut;
    }

    public boolean Verify(String text, long crc){
        return this.calculateCRCFromString(text) == crc;
    }

    public boolean Verify(Path filePath,long crc) throws IOException {
        return this.calculateCRCFromFile(filePath) == crc;
    }

    private long reverseBits(long x) {
        return Long.reverse(x) >>>(64-width);
    }

    private int reverseBits(int x){
        return Integer.reverse(x)>>>(24);
    }
}
