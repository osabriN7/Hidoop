/* une PROPOSITION de squelette, incomplète et adaptable... */

package hdfs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.RuntimeErrorException;

import config.Settings;
import formats.Format;
import formats.KV;
import formats.KVFormat;
import formats.LineFormat;
import formats.Format.Type;

    public class HdfsClient {

        private static void usage() {
            System.out.println("Usage: java HdfsClient read <file>");
            System.out.println("Usage: java HdfsClient write <line|kv> <file>");
            System.out.println("Usage: java HdfsClient delete <file>");
        }

        private static final int bufferSize = 2000000;

        public static void HdfsDelete(String hdfsFname) {
            long t1 = System.currentTimeMillis();
                   NameNode nameNode;
                   Socket socket;
                   HashMap<Node,ArrayList<Integer>> dataNodes;
                   try { // Se connecter au NameNode (rmi)
                       nameNode = (NameNode) Naming
                               .lookup("//" + Settings.NAMENODE +":"+ Settings.NAMENODE_PORT+"/NameNode");
                       dataNodes = nameNode.getDataNodeDispo();
                       } catch (NotBoundException e) {
                       System.err.println(" ERROR : Le NameNode ne figure pas dans le registre >> exit ...");
                       return;
                   } catch (ConnectException e) {
                       System.err.println("EROOR : Le nameNode ne répond pas >> exit ...");
                       return;
                   } catch (Exception e) {
                       e.printStackTrace();
                       return;
                   }

                   for(Node n : dataNodes.keySet()) {
                       try {
                       socket = new Socket(n.getHostName(), n.getPort());
                       ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                       oos.writeObject(Commande.CMD_DELETE);
                       oos.writeObject(hdfsFname);
                       }catch (Exception e) {
                           e.printStackTrace();
                           return;
                       }
                   }
                  
                   long t2 = System.currentTimeMillis();
                   System.out.println("La suppression des données est réussie :) !  " + (t2-t1));


               }


        public static void HdfsWrite(Format.Type fmt, String localFSSourceFname, int repFactor) {
            long t1 = System.currentTimeMillis();
            Node node;
            NameNode nameNode;
            long curseur = 0L;
            Socket socket;
            byte[] buf;
            String fileName = ((localFSSourceFname.contains("/")) //Removes path from file name
            ? localFSSourceFname.substring(localFSSourceFname.lastIndexOf('/')+1) :
                ((localFSSourceFname.contains("\\")) ? localFSSourceFname.substring(localFSSourceFname.lastIndexOf('\\')+1) : localFSSourceFname));
            Format input;
            KV structKV;
            int chunkNumber = 0, chunkSize = Settings.CHUNKSIZE;
            System.out
                    .println(" Hello ! HDFSClient  is Processing file " + localFSSourceFname + " for writing operation :");

            try { // Se connecter au NameNode (rmi)
                nameNode = (NameNode) Naming
                        .lookup("//" + Settings.NAMENODE +":"+ Settings.NAMENODE_PORT+"/NameNode");
                } catch (NotBoundException e) {
                System.err.println(" ERROR : Le NameNode ne figure pas dans le registre >> exit ...");
                return;
            } catch (ConnectException e) {
                System.err.println("EROOR : Le nameNode ne répond pas >> exit ...");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            input = choisirFormat(fmt, localFSSourceFname);
            input.open(Format.OpenMode.R);
            while ((structKV = input.read()) != null) {
                try {
                    node = nameNode.demandeEcriture(fileName);
                    socket = new Socket(node.getHostName(), node.getPort());
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    OutputStream os = socket.getOutputStream();
                    oos.writeObject(Commande.CMD_WRITE);
                    oos.writeObject(fileName);
                    oos.writeObject(chunkNumber);
                    buf = structKV.convert(fmt);
                    os.write(buf,0, buf.length);
                    curseur = input.getIndex();// position du curseur
                    while ((input.getIndex() - curseur <= chunkSize) && ((structKV = input.read()) != null)) {// ecrire
                        buf = structKV.convert(fmt);
                        os.write(buf, 0, buf.length);                                                                                          // les                                                                                          // ligne                                                                                          // restantes

                    }
                    os.close();
                    oos.close();
                    socket.close();
                    if ((structKV != null) && (structKV.v.length() > chunkSize)) {
                        System.out.println(chunkSize);
                        System.out.println(structKV.v.length());
                        System.out.println(structKV.k);
                        System.err.println("Le fichier contient une valeur qui eccede le chunk size !");

                    }
                    chunkNumber++;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            input.close();
            
            long t2 = System.currentTimeMillis();
            System.out.println("Bien ! L'ecriture est réussie :) avec un temps de : "+ (t2-t1) + " ms");

        }

        private static Format choisirFormat(Type fmt, String localFSSourceFname) throws RuntimeErrorException {
            if(fmt == Format.Type.LINE) {
                return new LineFormat(localFSSourceFname);
            }else if( fmt == Format.Type.KV) {
                return new KVFormat(localFSSourceFname);
            }
            else throw new RuntimeException("format non supporté !");
        }


        /**
         * Lire un fichier dans Hdfs. les chunks sont colléctés dans les serveurs
         * (DataNode) designés par le NameNode/
         *
         * @param hdfsFname
         * @param localFSDestFname
         */
   public static void HdfsRead(String hdfsFname, String localFSDestFname) {
    long t1 = System.currentTimeMillis();
        NameNode nameNode;
        String fileName = ((hdfsFname.contains("/")) //Removes path from file name
				? hdfsFname.substring(hdfsFname.lastIndexOf('/')+1) :
					((hdfsFname.contains("\\")) ? hdfsFname.substring(hdfsFname.lastIndexOf('\\')+1) : hdfsFname));
		String outputFileFolder = ((localFSDestFname.contains("/"))
				? localFSDestFname.substring(0,localFSDestFname.lastIndexOf('/')+1) :
					((localFSDestFname.contains("\\")) ? localFSDestFname.substring(0,localFSDestFname.lastIndexOf('\\')+1) : null));

        Socket socket;
        InputStream is;
        ObjectOutputStream oos;
        Node node;
        byte[] bi = new byte[bufferSize];
        BufferedOutputStream bo;
        int nbLu = 0;
        HashMap<Node,ArrayList<Integer>> dataNodes;
        System.out.println(" Hello ! HDFSClient  is Processing file " + localFSDestFname + " for reading operation :");
        try {// Connecting to NameNode
            nameNode = (NameNode) Naming
                    .lookup("//" + Settings.NAMENODE +":"+ Settings.NAMENODE_PORT+"/NameNode");
            bo = new BufferedOutputStream(new FileOutputStream(localFSDestFname), bufferSize);
            dataNodes = nameNode.demandeLecture(fileName);
            for (Node n : dataNodes.keySet()) {
                socket = new Socket(n.getHostName(), n.getPort());
                oos = new ObjectOutputStream(socket.getOutputStream());
                is = socket.getInputStream();
                oos.writeObject(Commande.CMD_READ);
                oos.writeObject(fileName);
                while (((nbLu = is.read(bi)) != -1)) {
                    bo.write(bi, 0, nbLu);// copier/coller dans le fs
                }
                is.close();
                socket.close();
            }
          bo.close();
        } catch (NotBoundException e) {
            System.err.println();
            return;
        } catch (ConnectException e) {
            System.err.println("");;
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();
        System.out.println("Bien ! La lecture est réussie :) avec un temps de : " + (t2-t1));

    }
    public static void main(String[] args) {
        // java HdfsClient <read|write> <line|kv> <file>

        try {
            if (args.length < 2) {
                usage();
                return;
            }

            switch (args[0]) {
                case "read":
                   HdfsRead(args[1],args[2]);
                    break;
                case "delete":
                    HdfsDelete(args[1]);
                    break;
                case "write":
                    Format.Type fmt;
                    if (args.length < 3) {
                        usage();
                        return;
                    }
                    if (args[1].equals("line"))
                        fmt = Format.Type.LINE;
                    else if (args[1].equals("kv"))
                        fmt = Format.Type.KV;
                    else {
                        usage();
                        return;
                    }
                    HdfsWrite(fmt, args[2], 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
