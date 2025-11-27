package org.example.gui.CRCSimulation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class TCPServer extends Thread {
    public int portAdres;
    public boolean running = true;

    List<ResponseTCP> responseTCPList = new ArrayList<>();

    public void run(){
        try(ServerSocket socket = new ServerSocket(portAdres)){

            while(running){
                Socket clientSocket = socket.accept();
                ResponseTCP clientThread = new ResponseTCP(clientSocket,false,portAdres);
                this.responseTCPList.add(clientThread);
                clientThread.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
