package org.example.gui.CRCSimulation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;

@AllArgsConstructor
@NoArgsConstructor
public class TCPKlient extends Thread {

    public int sourcePort;

    public String desAdres;
    public int desPort;

    public String message;
    public String filePath;

    public static CRC crc = new CRC(0x2F,0xFF,0xFF,8);


    public TCPKlient(int sourcePort,String desAdres, int desPort) {
        this.sourcePort = sourcePort;
        this.desAdres = desAdres;
        this.desPort = desPort;
    }

    public void run(){

        String nextAdres = this.desAdres;
        int nextPort = Komputer.znajdzSciezke(this.sourcePort,this.desPort).get(1);

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
        dos.writeLong(crcKod);
        dos.writeUTF(this.message);

        dos.flush();
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
