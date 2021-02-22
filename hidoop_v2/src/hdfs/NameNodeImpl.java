package hdfs;


import config.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AlreadyBoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.net.InetAddress;

public class NameNodeImpl extends UnicastRemoteObject implements NameNode {
    /**
     * pile de dataNodes pour generer d'une maniere cyclique les dataNodes
     */
    private Queue<Node> queueDataNodes = new LinkedList<Node>();
    private HashMap <Node,ArrayList<Integer>> dataNodesDispo = new HashMap <Node,ArrayList<Integer>>() ;
    private HashMap<String, HashMap <Node,ArrayList<Integer>>> metaData = new HashMap<String, HashMap <Node,ArrayList<Integer>>>();

    public NameNodeImpl() throws RemoteException {}

      @Override
          public HashMap<Node, ArrayList<Integer>> demandeLecture(String fileName) throws RemoteException {
              if(fileName.contains("-map")) {
                  return this.metaData.get(fileName.split("-map")[0]);}
                              return this.metaData.get(fileName);
          }


@Override
    public Node demandeEcriture(String fileName) throws RemoteException {

        this.metaData.put(fileName, dataNodesDispo);
        Node node;
        node = queueDataNodes.remove();// cyclique
        queueDataNodes.add(node);
        return node;

    }


    @Override
    public HashMap< Node, ArrayList<Integer>> getMetaData(String fileName) throws RemoteException {
        return this.metaData.get(fileName);
    }

    @Override
    public void notifyNameNode(String fileName, int chunkNumber, Node dataNode) throws RemoteException {
        HashMap<Node, ArrayList<Integer> >  dataNodes = this.metaData.get(fileName);
        ArrayList<Integer> chunks = dataNodes.get(dataNode);
        if (chunks == null) {
            chunks = new ArrayList<Integer>();
        }
        chunks.add(chunkNumber);
        dataNodes.replace(dataNode,chunks);
        this.metaData.replace(fileName, dataNodes);
        String monfichier = "NameNodeInfo/" + fileName + "-info";
        if (!new File(monfichier).exists()) {
            try {
                new File(monfichier).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path fichier = Paths.get(monfichier);
        try {
            Files.write(fichier,
                    Arrays.asList(chunkNumber + "<->" + dataNode.getHostName() + "::" + dataNode.getPort()),
                    Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void senregistrer(Node node) throws RemoteException {
        dataNodesDispo.put(node, new ArrayList<Integer>());
        this.queueDataNodes.add(node);
        System.out.println(node.getHostName() + " is now being deployed as a datanode");
    }

public static void main(String [] args) {
        try{
	    InetAddress adresse = InetAddress.getLocalHost();
            System.out.println(adresse.getHostName()+" is being used as the NameNode");
            LocateRegistry.createRegistry(4040);
            Naming.rebind("//" + Settings.NAMENODE +":"+Settings.NAMENODE_PORT+"/NameNode", new NameNodeImpl());
        }catch(AlreadyBoundException e) {
            System.err.println("Already");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public HashMap<Node, ArrayList<Integer>> getDataNodeDispo() throws RemoteException {
        return this.dataNodesDispo;
    }

}
