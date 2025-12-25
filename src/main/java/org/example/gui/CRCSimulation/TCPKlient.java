package org.example.gui.CRCSimulation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.gui.AppState;
import org.example.gui.CRCSimulation.ThrowErrors.NoWay;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
@NoArgsConstructor
public class TCPKlient extends Thread {

    public int sourcePort;

    public String desAdres;
    public int desPort;

    public String message;
    public String filePath;

    private int nextPort;
    
    public TCPKlient(int sourcePort,String desAdres, int desPort) {
        this.sourcePort = sourcePort;
        this.desAdres = desAdres;
        this.desPort = desPort;
    }

    public void run(){

        String nextAdres = this.desAdres;

        List<Integer> sciezka = Komputer.znajdzSciezke(this.sourcePort,this.desPort);
        if(sciezka == null){
            ResponseTCP.statistic(false,0L,0L);
            addDrogaMSG("PC-"+this.sourcePort+" not found path to PC-"+nextPort);
            throw new NoWay("Nie znaleziono ścieżki");
        }
        nextPort = sciezka.get(1);

        try(Socket socket = new Socket(nextAdres,nextPort)){

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            if(message!=null && filePath == null){
                this.sendText(dataOutputStream);
                responseStatusMSG(dataInputStream,dataOutputStream);
            }else if(message==null && filePath!=null){
                this.sendFile(dataOutputStream);
                responseStatusFILE(dataInputStream,dataOutputStream);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendText(DataOutputStream dos) throws IOException {

        dos.writeInt(1);
        dos.writeUTF(this.desAdres+":"+this.desPort);
        dos.writeLong(System.nanoTime());

        long crcKod = Siec.crc.calculateCRCFromString(message);


        if(Siec.ErrorType == 3 && (Siec.ErrorPC + 5000) == this.sourcePort){
            dos.writeLong(crcKod);

            String badMsg = this.makeTextError(this.message);
            System.out.println("PC"+this.sourcePort+" robi bład:"+badMsg);
            dos.writeUTF(badMsg);
            addDrogaMSG("PC-"+this.sourcePort+" sent { MSG[NOT OK]    CRC[OK]    BLAD[TRUE] } to PC-"+nextPort);
        }else if(Siec.ErrorType == 2 && (Siec.ErrorPC + 5000) == this.sourcePort){
            Long badCRC = this.makeCRCError(crcKod);
            dos.writeLong(badCRC);
            dos.writeUTF(this.message);
            addDrogaMSG("PC-"+this.sourcePort+" sent { MSG[OK]    CRC[NOT OK]    BLAD[TRUE] } to PC-"+nextPort);
        }
        else{
            dos.writeLong(crcKod);
            dos.writeUTF(this.message);
            addDrogaMSG("PC-"+this.sourcePort+" sent { MSG[OK]    CRC[OK]    BLAD[FALSE] } to PC-"+nextPort);
        }

        dos.flush();
    }

    public void responseStatusMSG(DataInputStream dis,DataOutputStream dos) throws IOException {
        int status = dis.readInt();
        dos.flush();

        if(status == 200){
            return;
        }

        dos.writeUTF(this.desAdres+":"+this.desPort);
        dos.writeLong(System.nanoTime());
        long crcKod = Siec.crc.calculateCRCFromString(message);
        dos.writeLong(crcKod);
        dos.writeUTF(this.message);
        addDrogaMSG("PC-"+this.sourcePort+" sent { MSG[OK]    CRC[OK]    BLAD[FALSE] } to PC-"+nextPort);
        dos.flush();
    }

    public String makeTextError(String text){
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(text);

        int index = rand.nextInt(0,sb.length());
        sb.setCharAt(index,(char)rand.nextInt(30,122));

        return sb.toString();
    }

    public Long makeCRCError(Long CRC){
        return CRC-1;
    }

    public static void addDrogaMSG(String text){
        AppState appState = AppState.getInstance();
        appState.addDrogaMSG(text);
    }

    public static void addDrogaFILE(String text){
        AppState appState = AppState.getInstance();
        appState.addDrogaFILE(text);
    }

    public void sendFile(DataOutputStream dos) throws IOException {
        File file = new File(this.filePath);

        dos.writeInt(2);
        dos.writeUTF(this.desAdres+":"+this.desPort);
        dos.writeLong(System.nanoTime());

        long crcKod = Siec.crc.calculateCRCFromFile(Paths.get(filePath));
        if(Siec.ErrorType == 2 && (Siec.ErrorPC + 5000) == this.sourcePort){
            dos.writeLong(makeCRCError(crcKod));
            addDrogaFILE("PC-"+this.sourcePort+" sent { MSG[OK]    CRC[NOT OK]    BLAD[TRUE] } to PC-"+nextPort);
        }else if(Siec.ErrorType == 3 && (Siec.ErrorPC + 5000) == this.sourcePort){
            addDrogaFILE("PC-"+this.sourcePort+" sent { MSG[NOT OK]    CRC[OK]    BLAD[TRUE] } to PC-"+nextPort);
            dos.writeLong(crcKod);

            sendBadFile(dos,file);
            return;
        }
        else{
            addDrogaFILE("PC-"+this.sourcePort+" sent { MSG[OK]    CRC[OK]    BLAD[FALSE] } to PC-"+nextPort);
            dos.writeLong(crcKod);
        }

        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        dos.flush();
    }

    public void responseStatusFILE(DataInputStream dis,DataOutputStream dos) throws IOException {
        int status = dis.readInt();
        dos.flush();

        if(status == 200){
            return;
        }

        File file = new File(this.filePath);

        dos.writeUTF(this.desAdres+":"+this.desPort);
        dos.writeLong(System.nanoTime());

        long crcKod = Siec.crc.calculateCRCFromFile(Paths.get(filePath));
        addDrogaFILE("PC-"+this.sourcePort+" sent { MSG[OK]    CRC[OK]    BLAD[FALSE] } to PC-"+nextPort);
        dos.writeLong(crcKod);
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        dos.flush();
    }

    public void sendBadFile(DataOutputStream dos,File file) throws IOException {
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                String tmp = makeTextError(Arrays.toString(buffer));
                dos.write(tmp.getBytes(), 0, bytesRead);
            }
        }
        dos.flush();
    }
}
