package org.example.gui.CRCSimulation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.gui.AppState;
import org.example.gui.CRCSimulation.ThrowErrors.BadCrc;
import org.example.gui.CRCSimulation.ThrowErrors.BadMessage;
import org.example.gui.CRCSimulation.ThrowErrors.NoWay;
import org.example.gui.SendMessage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
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

    public static CRC crc = new CRC(0x2F,0xFF,0xFF,8);


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
            addDroga("PC-"+this.sourcePort+" not found path to PC-"+nextPort);
            throw new NoWay("Nie znaleziono ścieżki");
        }
        nextPort = sciezka.get(1);

        try(Socket socket = new Socket(nextAdres,nextPort)){

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            if(message!=null && filePath == null){
                this.sendText(dataOutputStream);
            }else if(message==null && filePath!=null){
                this.sendFile(dataOutputStream);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendText(DataOutputStream dos) throws IOException {

        dos.writeInt(1);
        dos.writeUTF(this.desAdres+":"+this.desPort);
        dos.writeLong(System.nanoTime());

        long crcKod = crc.calculateCRCFromString(message);


        if(Siec.ErrorType == 3 && (Siec.ErrorPC + 5000) == this.sourcePort){
            dos.writeLong(crcKod);

            String badMsg = this.makeTextError(this.message);
            System.out.println("PC"+this.sourcePort+" robi bład:"+badMsg);
            dos.writeUTF(badMsg);
            addDroga("PC-"+this.sourcePort+" sent {MSG[NOT OK] CRC[OK] BLAD[TRUE]} to PC-"+nextPort);
        }else if(Siec.ErrorType == 2 && (Siec.ErrorPC + 5000) == this.sourcePort){
            Long badCRC = this.makeCRCError(crcKod);
            dos.writeLong(badCRC);
            dos.writeUTF(this.message);
            addDroga("PC-"+this.sourcePort+" sent {MSG[OK] CRC[NOT OK] BLAD[TRUE]} to PC-"+nextPort);
        }
        else{
            dos.writeLong(crcKod);
            dos.writeUTF(this.message);
            addDroga("PC-"+this.sourcePort+" sent {MSG[OK] CRC[OK] BLAD[FALSE]} to PC-"+nextPort);
        }

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

    public static void addDroga(String text){
        AppState appState = AppState.getInstance();
        appState.addDroga(text);
    }

    public void sendFile(DataOutputStream dos) throws IOException {
        File file = new File(this.filePath);

        dos.writeInt(2);
        dos.writeUTF(this.desAdres+":"+this.desPort);
        dos.writeLong(System.nanoTime());

        long crcKod = crc.calculateCRCFromFile(Paths.get(filePath));
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
}
