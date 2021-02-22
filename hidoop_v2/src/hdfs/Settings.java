package hdfs;

import formats.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import formats.KVFormat;
import formats.LineFormat;

public class Settings {

    public static String getMasterNodeAddress() {
        if ((new File(System.getProperty("java.class.path") + "/config/servers.config")).exists()){
            LineFormat lineFormat = new LineFormat(System.getProperty("java.class.path") + "/config/servers.config");
            lineFormat.open(Format.OpenMode.R);
            String valeur = lineFormat.read().v;
            lineFormat.close();
            return valeur;
        }else {
            System.err.println("Impossible d'ouvrir le fichier serveur");
            return null;
        }
    }
    public static int getDataNode(){
        try { 
            return Integer.parseInt(lireSetting("portdatanode"));
        }catch (NumberFormatException e) {
            System.err.println("Error: le port du dataNode doit etre entier" );
            return -1;
        }
        
    }
    
    public static int getPortNameNode() {
        try {
            return Integer.parseInt(lireSetting("portnamenode"));
        }catch (NumberFormatException e) {
            System.err.println("Error: le port du nameNode doit etre entier" );
            return -1;
        }
        
    }

    public static int getChunkSize() {
        try {
            return Integer.parseInt(lireSetting("chunksize"));//en octets
        } catch (NumberFormatException e) {
            System.err.println("Error: chunksize du nameNode doit etre entier" );
            return -1;
        }
    }

    public static String getDataPath() {
        return lireSetting("datapath");
    }

    public static int getPortJobMaster() {
        try {
            return Integer.parseInt(lireSetting ("portjobmaster"));

        } catch (NumberFormatException e) { 
            System.err.println("** SeetingManager : JobMaster's port number must be an intger");
            return -1;
        }

    }
    public static int getPortWorker() {
        try {
            return Integer.parseInt(lireSetting("portworker"));
        } catch (NumberFormatException e) {
            System.err.println("** SettingManager : The Worker's port number must be an integer");
            return -1;
        }
    }
    private static String lireSetting(String cle) {
        if((new File(System.getProperty("java.class.path") + "/config/settings.config")).exists()) {
            KVFormat kvFormat = new KVFormat(System.getProperty("java.class.path") + "/config/settings.config");
            kvFormat.open(Format.OpenMode.R);
            KV valeur;
            while(((valeur = kvFormat.read()) != null) && !valeur.k.equals(cle));
            if (valeur == null) {
                System.err.println("Error : " + cle +" n'existe pas "  );
                return null;
            }
            return valeur.v;
        }else {
            System.err.println("Error :" + "impossible de charger fichier de configuration");
            return null;
        }
    }
	public static ArrayList<Integer> getPortsDataNodes() {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 1; i <= Integer.parseInt(lireSetting("NB_DATA_NODES")); i++) {
         result.add(Integer.parseInt(lireSetting("portdatanode_"+i)));
        }
        return result;
	}
	public static int getNombreDataNodeServers() {
		return Integer.parseInt(lireSetting("NB_DATA_NODES"));
	}
	public static HashMap<Integer, Node> getRepData(String fileName) {
            HashMap<Integer, Node> repData= new HashMap<Integer, Node>();
            KVFormat kvFormat = new KVFormat("NameNodeInfo/"+fileName+"-info");
            kvFormat.open(Format.OpenMode.R);
            KV valeur;
            while(((valeur = kvFormat.read()) != null)){
                String[] tokens = valeur.v.split("::");
                repData.put(Integer.parseInt(valeur.k),new Node(tokens[0], Integer.parseInt(tokens[1])));
            }
            return repData;	
	}
	public static LinkedList<Node> getDataNodes() {
        LinkedList<Node> l = new LinkedList<Node>();
        if((new File(System.getProperty("java.class.path") + "/config/servers.config")).exists()) {
            KVFormat kvFormat = new KVFormat(System.getProperty("java.class.path") + "/config/servers.config");
            kvFormat.open(Format.OpenMode.R);
            KV valeur;
            while(((valeur = kvFormat.read()) != null)) {
            l.add(new Node(valeur.k,Integer.parseInt(valeur.v)));
            }
            return l;
        }else {
            System.err.println("Error :" + "impossible de charger fichier de configuration serveurs");
            return null;
        }
        
		
	}

}
