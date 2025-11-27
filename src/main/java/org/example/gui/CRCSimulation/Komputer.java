package org.example.gui.CRCSimulation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


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

    public void sendMessage(String message,String desAddres,int desPort) throws InterruptedException {
        TCPKlient klient = new TCPKlient();

        klient.sourcePort = this.serwerPort;
        klient.desPort = desPort;
        klient.desAdres = desAddres;
        klient.message = message;

        klient.start();
        klient.join();
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
