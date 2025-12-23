package org.example.gui.CRCSimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Siec {

    public static final int AMOUNT_OF_PC = 10;
    public static List<Komputer> pcList = new ArrayList<Komputer>();
    public static Map<Integer, List<Integer>> topologia = new ConcurrentHashMap<>();

    public static int ErrorType;
    public static int ErrorPC;

    public static Boolean start(){
        try{
            stworzTopologie();
            for(int i=1;i<=AMOUNT_OF_PC;i++){

                Komputer k = new Komputer();
                k.id = i;
                k.name="PC-"+i;
                k.serwerPort = 5000+i;
                pcList.add(k);
                k.start();
            }

            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static void stworzTopologie() {
        polacz(5001, 5002);
        polacz(5001, 5005);

        polacz(5002, 5003);
        polacz(5002, 5004);

        polacz(5003, 5006);

        polacz(5004, 5007);

        polacz(5005, 5010);

        polacz(5006, 5008);

        polacz(5007, 5009);

        polacz(5008, 5009);
    }

    private static void polacz(int id1, int id2) {
        topologia.computeIfAbsent(id1, k -> new ArrayList<>()).add(id2);
        topologia.computeIfAbsent(id2, k -> new ArrayList<>()).add(id1);
    }

    public static String sendMSG(String msg,int sourceId,int destinationId) {
        try {
            Komputer k = Siec.pcList.get(sourceId);
            Komputer k2 = Siec.pcList.get(destinationId);
            k.sendMessage(msg, "127.0.0.1", k2.serwerPort);

            return "OK";
        }catch (Exception e){
            return e.getMessage();
        }
    }

    public static String sendFILE(String filePath,int sourceId,int destinationId) {
        try {
            Komputer k = Siec.pcList.get(sourceId);
            Komputer k2 = Siec.pcList.get(destinationId);
            k.sendFile(filePath, "127.0.0.1", k2.serwerPort);

            return "OK";
        }catch (Exception e){
            return e.toString();
        }
    }

    public static void makeError(int eType, int ePC){
        topologia.clear();
        stworzTopologie();

        ErrorPC = ePC+1;
        ErrorType = eType;
        Komputer pc = pcList.get(ePC);

        if(pc == null){
            return;
        }

        if(eType == 1){
            topologia.remove(pc.serwerPort);
            for(Komputer k : pcList){
                if(!topologia.containsKey(k.serwerPort)){
                    continue;
                }
                List<Integer> polaczenia = topologia.get(k.serwerPort);
                if(!polaczenia.isEmpty() && polaczenia.contains(pc.serwerPort)){
                    //topologia.remove(pc.serwerPort);
                    topologia.computeIfAbsent(k.serwerPort, l -> new ArrayList<>()).remove(polaczenia.indexOf(pc.serwerPort));
                }
            }
        }
    }
}
