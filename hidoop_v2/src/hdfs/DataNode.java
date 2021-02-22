package hdfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import config.Settings;
import hdfs.Commande;

public class DataNode extends Thread {

    private static final int bufferSize = 2000000;
    Socket comSocket;
    Node node;
    private NameNode nameNode;
    ReentrantLock moniteur = new ReentrantLock();

    public DataNode(Socket cs, Node node, NameNode nameNode) {
        this.comSocket = cs;
        this.node = node;
        this.nameNode = nameNode;
    }

    public void run() {
        try {
            BufferedOutputStream bo;
            BufferedInputStream bi;
            int nbLu;
            ObjectInputStream ois = new ObjectInputStream(comSocket.getInputStream());
            OutputStream os = comSocket.getOutputStream();
            InputStream is = comSocket.getInputStream();
            Commande cmd = (Commande) ois.readObject();
            if (cmd == Commande.CMD_WRITE) {
                byte[] buf = new byte[4080];
                moniteur.lock();
                String fileName = (String) ois.readObject();
                int chunkNumber = (int) ois.readObject();
                bo = new BufferedOutputStream(new FileOutputStream(
                        "DataNodeServers/DataNodeServer-"+ node.getHostName() + "-" + node.getPort() + "/" + fileName + "-" + chunkNumber),
                        bufferSize);
                while ((nbLu = is.read(buf)) != -1) {
                    bo.write(buf, 0, nbLu);
                }
                System.out.println("chunk n° " + chunkNumber + " du fichier :" + fileName);
                nameNode.notifyNameNode(fileName, chunkNumber, node);
                bo.close();
                ois.close();
                is.close();
                comSocket.close();
                moniteur.unlock();
            } else if (cmd == Commande.CMD_READ) {
                byte[] buf = new byte[bufferSize];
                String fileName = (String) ois.readObject();
                ArrayList<Integer> chunks = nameNode.demandeLecture(fileName).get(node);
                for (int chunk : chunks) {
                bi = new BufferedInputStream(new FileInputStream("DataNodeServers/DataNodeServer-"+ node.getHostName() + "-"
                + node.getPort() + "/" + fileName + "-" + chunk));
                while((nbLu = bi.read(buf)) != -1) {
                    os.write(buf, 0, nbLu);
                }
                bi.close();
                System.out.println("Lecture chunk n° " + chunk + " du fichier :" + fileName);
                }
                comSocket.close();
                ois.close();
                os.close();
            } else if (cmd == Commande.CMD_DELETE) {
              String fileName =(String) ois.readObject();
               ArrayList<Integer> chunks = nameNode.getDataNodeDispo().get(node);
               for (int chunk : chunks) {
                   File fichier = new File("DataNodeServers/DataNodeServer-" + node.getHostName() + "-"
                           + node.getPort() + "/" + fileName + "-" + chunk);
                   System.out.println("chunk n° " + chunk + " a été supprimé ");
                   fichier.delete();
                   ois.close();
               }

            }
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("Connexion DataNode echoue");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Node node;
        ServerSocket listenSock;
        try {
        InetAddress  adresse = InetAddress.getLocalHost();
            node = new Node(adresse.getHostName(), Integer.parseInt(args[0]));
        new File("DataNodeServers/DataNodeServer-" + node.getHostName() + "-" + node.getPort()).mkdir();
            listenSock = new ServerSocket(Integer.parseInt(args[0]));
            NameNode nameNode = (NameNode) Naming.lookup("//" + Settings.NAMENODE +":"+ Settings.NAMENODE_PORT+"/NameNode");
            nameNode.senregistrer(node);
            System.out.println(adresse.getHostName() + " now host a data node, listenning at port " + args[0] );
            while (true) {
                Thread t = new DataNode(listenSock.accept(), node, nameNode);
                t.start();
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }




}
