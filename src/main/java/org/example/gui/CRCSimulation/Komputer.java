package org.example.gui.CRCSimulation;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.example.gui.CRCSimulation.ThrowErrors.BadCrc;
import org.example.gui.CRCSimulation.ThrowErrors.BadMessage;
import org.example.gui.CRCSimulation.ThrowErrors.NoWay;


/*
 * Klasa Komputer reprezentuje węzeł sieciowy odpowiedzialny za odbieranie, wysyłanie oraz wyznaczanie tras pakietów.
 *
 * run: Inicjalizuje serwer TCP na danym porcie i rozpoczyna nasłuchiwanie na połączenia przychodzące.
 * sendMessage: Tworzy obiekt TCPKlient w celu wysłania wiadomości tekstowej do sąsiedniego węzła
 *     oraz powiadamia o wystąpieniu błędu.
 * sendFile: Tworzy obiekt TCPKlient w celu wysłania pliku binarnego do sąsiedniego węzła.
 * znajdzSciezke: Wykorzystuje algorytm BFS do wyznaczenia najkrótszej ścieżki pomiędzy węzłem startowym a docelowym w oparciu o topologię sieci.
 */


public class Komputer extends Thread {

    public int serwerPort;
    TCPServer server;
    public String name;
    public int id;


    private static Map<Integer, List<Integer>> topologia = Siec.topologia;

    public void run(){
        server = new TCPServer();
        server.portAdres = serwerPort;
        server.start();
        System.out.println(this.name+" started");
    }

    public void sendMessage(String message,String desAddres,int desPort) throws NoWay, BadCrc, BadMessage, InterruptedException {
        TCPKlient klient = new TCPKlient();

        klient.sourcePort = this.serwerPort;
        klient.desPort = desPort;
        klient.desAdres = desAddres;
        klient.message = message;

        CompletableFuture<String> future = new CompletableFuture<>();
        klient.setUncaughtExceptionHandler((t,e)->{
            future.completeExceptionally(e);
        });

        klient.start();
        klient.join();

        List<Integer> lista = Komputer.znajdzSciezke(this.serwerPort,desPort);
        if(lista!= null &&!lista.contains(Siec.ErrorPC+5000)){
            return;
        }

        if(Siec.ErrorType == 3){
            throw new BadMessage("PC"+Siec.ErrorPC+" przesłał złą wiadomość!");
        }else if(Siec.ErrorType == 2){
            throw new BadMessage("PC"+Siec.ErrorPC+" przesłał zły CRC!");
        }else if(Siec.ErrorType == 1){
            throw new BadMessage("PC"+Siec.ErrorPC+" nie jest dostępny");
        }

    }

    public void sendFile(String filePath,String desAddres,int desPort) throws InterruptedException {
        TCPKlient klient = new TCPKlient();

        klient.sourcePort = this.serwerPort;
        klient.desPort = desPort;
        klient.desAdres = desAddres;
        klient.filePath = filePath;

        klient.start();
        klient.join();
    }


    public static List<Integer> znajdzSciezke(int startPort, int celPort) {
        Queue<Integer> kolejka = new LinkedList<>();
        Map<Integer, Integer> poprzednicy = new HashMap<>();

        kolejka.add(startPort);
        poprzednicy.put(startPort, null);

        while (!kolejka.isEmpty()) {
            int obecnyPort = kolejka.poll();

            if (obecnyPort == celPort) {
                List<Integer> sciezka = new LinkedList<>();
                Integer krok = celPort;
                while (krok != null) {
                    sciezka.add(krok);
                    krok = poprzednicy.get(krok);
                }
                Collections.reverse(sciezka);
                return sciezka;
            }

            if (topologia.containsKey(obecnyPort)) {
                for (int sasiadPort : topologia.get(obecnyPort)) {
                    if (!poprzednicy.containsKey(sasiadPort)) {
                        poprzednicy.put(sasiadPort, obecnyPort);
                        kolejka.add(sasiadPort);
                    }
                }
            }
        }
        return null;
    }
}
