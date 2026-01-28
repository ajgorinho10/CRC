package org.example.gui.CRCSimulation;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/*
 * Klasa TCPServer realizuje funkcjonalność serwera sieciowego, działając w osobnym wątku 
 * i zarządzając przyjmowaniem nowych połączeń TCP.
 *
 * run: Inicjalizuje ServerSocket na określonym porcie i w pętli oczekuje na próby połączenia. Po nawiązaniu połączenia
 *     tworzy dedykowany obiekt Socket dla danej sesji komunikacyjnej i dodaje go do listy aktywnych połączeń.
 *
 */



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
