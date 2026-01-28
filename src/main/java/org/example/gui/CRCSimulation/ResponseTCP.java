package org.example.gui.CRCSimulation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.example.gui.AppState;


/*
 * Klasa ResponseTCP obsługuje strumień wejściowy na serwerze, weryfikuje integralność danych
 *    oraz zarządza statystykami transmisji.
 *
 * run: Rozpoznaje typ przychodzących danych i wywołuje odpowiednią metodę odbiorczą
 * reciveMSG: Odbiera wiadomość, weryfikuje jej kod CRC i decyduje czy wyświetlić treść, czy przekazać ją dalej
 * reciveFile: Odbiera strumień bajtów pliku, weryfikuje jego kod CRC, zapisuje go na dysku pod nową nazwą,
 *     sprawdza czy przekazać plik dalej
 * sendStatus: Przesyła do nadawcy kod statusu informujący o powodzeniu/błędzie w trakcie przesłania.
 * statistic: Zbiera dane o czasie i wyniku operacji, po czym przekazuje je do Singletona do aktualizacji globalnych liczników.
 */


@AllArgsConstructor
@NoArgsConstructor
public class ResponseTCP extends Thread {

    public Socket socket;
    public boolean klient;
    public int SourcePort;

    public static int AmountOfSuccessfullMessages = 0;
    public static int AmountOferroeMessages = 0;
    public static List<Double> deliveredTime = new ArrayList<Double>();


    public ResponseTCP(Socket socket,boolean klient,int id) {
        this.socket = socket;
        this.klient = klient;
        this.SourcePort = id;
    }

    public String RecivedMSG;
    public String RecivedFile;

    public void run(){
        if(klient){
            System.out.println("Client connected:"+this.socket.getPort());
        }else{
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                int command = dis.readInt();

                if (command == 1) {
                    boolean result = this.reciveMSG(dis);
                    if(result){
                        this.sendStatus(dos,true);
                    }else{
                        this.sendStatus(dos,false);
                        this.reciveMSG(dis);
                    }


                }else if (command == 2) {
                    boolean result = reciveFile(dis);
                    if(result){
                        sendStatus(dos,true);
                    }else{
                        sendStatus(dos,false);
                        this.reciveFile(dis);
                    }
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void statistic(boolean success, Long timeStart, Long timeEnd){

        AppState appState = AppState.getInstance();
        if(success){
            long czasTrwaniaNs = timeEnd - timeStart;
            double czasTrwaniaMs = (double) czasTrwaniaNs / 1_000_000.0;
            deliveredTime.add(czasTrwaniaMs);

            ResponseTCP.AmountOfSuccessfullMessages++;
            appState.reportSuccess(czasTrwaniaMs);
        }else{
            ResponseTCP.AmountOferroeMessages++;
            appState.reportError();
        }

    }

    public boolean reciveMSG(DataInputStream dis) throws IOException, InterruptedException {

        try {
            String destinationAddress = dis.readUTF();
            Long timeStart = dis.readLong();
            long crckod = dis.readLong();
            String receivedText = dis.readUTF();

            boolean CRCOK = false;
            if(crckod == Siec.crc.calculateCRCFromString(receivedText)){
                ResponseTCP.statistic(true,timeStart,System.nanoTime());
                addDrogaMSG("PC-"+this.SourcePort+" RECIVED { MSG[OK]["+receivedText+"]    CRC[OK]["+crckod+"]    BLAD[FALSE] }");
                CRCOK = true;
            }else{
                ResponseTCP.statistic(false,timeStart,System.nanoTime());
                addDrogaMSG("PC-"+this.SourcePort+" RECIVED { MSG[OK]["+receivedText+"]    CRC[NOT OK]["+crckod+"]    BLAD[TRUE] }");
                CRCOK = false;
                return false;
            }

            String ipAddress = destinationAddress.split(":")[0];
            int port = Integer.parseInt(destinationAddress.split(":")[1]);


            if (this.SourcePort == port) {
                if(CRCOK){
                    addDrogaMSG("PC-"+this.SourcePort+" RECIVED MESSAGE SUCCESSFULLY!");
                }
            } else {
                TCPKlient klient = new TCPKlient();

                klient.sourcePort = this.SourcePort;
                klient.desAdres = ipAddress;
                klient.desPort = port;
                klient.message = receivedText;

                klient.start();
                klient.join();
            }

            this.RecivedMSG = receivedText;

            return true;
        }catch (Exception e){
            ResponseTCP.statistic(false, null,null);
            return false;
        }
    }

    public void sendStatus(DataOutputStream dos,boolean ok) throws IOException {
        if(ok) {
            dos.writeInt(200);

        }else{
            dos.writeInt(0);
        }
        dos.flush();
    }

    public static void addDrogaMSG(String text){
        AppState appState = AppState.getInstance();
        appState.addDrogaMSG(text);
    }

    public static void addDrogaFile(String text){
        AppState appState = AppState.getInstance();
        appState.addDrogaFILE(text);
    }


    public boolean reciveFile(DataInputStream dis) throws IOException, InterruptedException {

        String destinationAddress = dis.readUTF();
        Long timeStart = dis.readLong();
        long crckod = dis.readLong();

        String ipAddress = destinationAddress.split(":")[0];
        int port = Integer.parseInt(destinationAddress.split(":")[1]);

        String fileName = dis.readUTF();
        long fileSize = dis.readLong();

        String recivedFileName = "RECEIVED_"+ this.SourcePort + fileName;
        try (FileOutputStream fos = new FileOutputStream(recivedFileName)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int bytesRead;

            // Odbieramy tylko tyle bajtów, ile wynosi rozmiar pliku!
            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }

            this.RecivedFile = fos.toString();
        }

        long calculatedCrc = Siec.crc.calculateCRCFromFile(Paths.get(recivedFileName));
        System.out.println("File calculated crc:"+calculatedCrc+", odebrany Crc:"+crckod);

        if(crckod == calculatedCrc){
            ResponseTCP.statistic(true,timeStart,System.nanoTime());
            addDrogaFile("PC-"+this.SourcePort+" RECIVED { MSG[OK]    CRC[OK]["+crckod+"]    BLAD[FALSE] }");
        }else{
            ResponseTCP.statistic(false,timeStart,System.nanoTime());
            addDrogaFile("PC-"+this.SourcePort+" RECIVED { MSG[OK]    CRC[NOT OK]["+crckod+"]    BLAD[TRUE] }");
            return false;
        }

        if(this.SourcePort == port){
            addDrogaFile("PC-"+this.SourcePort+" RECIVED FILE SUCCESSFULLY!");
        }else{

            TCPKlient klient = new TCPKlient();

            klient.sourcePort = this.SourcePort;
            klient.desAdres = ipAddress;
            klient.desPort = port;
            klient.filePath = recivedFileName;

            klient.start();
            klient.join();
        }

        return true;
    }
}
