package org.example.gui.CRCSimulation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.example.gui.AppState;

@AllArgsConstructor
@NoArgsConstructor
public class ResponseTCP extends Thread {

    public Socket socket;
    public boolean klient;
    public int SourcePort;

    public static int AmountOfSuccessfullMessages = 0;
    public static int AmountOferroeMessages = 0;
    public static List<Double> deliveredTime = new ArrayList<Double>();

    public static CRC crc = new CRC(0x2F,0xFF,0xFF,8);


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
                int command = dis.readInt();

                if (command == 1) {
                    this.reciveMSG(dis);
                }else if (command == 2) {
                    reciveFile(dis);
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

    public void reciveMSG(DataInputStream dis) throws IOException, InterruptedException {

        try {
            String destinationAddress = dis.readUTF();
            Long timeStart = dis.readLong();
            long crckod = dis.readLong();
            String receivedText = dis.readUTF();

            if(crckod == crc.calculateCRCFromString(receivedText)){
                ResponseTCP.statistic(true,timeStart,System.nanoTime());
                addDroga("PC-"+this.SourcePort+" RECIVED {MSG[OK] CRC[OK] BLAD[FALSE]}");
            }else{
                ResponseTCP.statistic(false,timeStart,System.nanoTime());
                addDroga("PC-"+this.SourcePort+"RECIVED {wiadomosc[OK] CRC[NOT OK] BLAD[TRUE]}");
                return;
            }

            String ipAddress = destinationAddress.split(":")[0];
            int port = Integer.parseInt(destinationAddress.split(":")[1]);


            if (this.SourcePort == port) {
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
        }catch (Exception e){
            ResponseTCP.statistic(false, null,null);
        }

    }

    public static void addDroga(String text){
        AppState appState = AppState.getInstance();
        appState.addDroga(text);
    }

    public void reciveFile(DataInputStream dis) throws IOException, InterruptedException {

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

        long calculatedCrc = crc.calculateCRCFromFile(Paths.get(recivedFileName));
        System.out.println("File calculated crc:"+calculatedCrc+", odebrany Crc:"+crckod);

        if(crckod == calculatedCrc){
            ResponseTCP.statistic(true,timeStart,System.nanoTime());
        }else{
            ResponseTCP.statistic(false,timeStart,System.nanoTime());
        }

        if(this.SourcePort == port){
            System.out.println("PC withId: "+this.SourcePort+" Recived:"+recivedFileName);
        }else{

            TCPKlient klient = new TCPKlient();

            klient.sourcePort = this.SourcePort;
            klient.desAdres = ipAddress;
            klient.desPort = port;
            klient.filePath = recivedFileName;

            klient.start();
            klient.join();
        }
    }
}
