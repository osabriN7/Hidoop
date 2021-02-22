package hdfs;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;


public interface NameNode extends Remote {
    /**
    * 
    * @param fileName
    * @return
    * @throws RemoteException
    */
    public Node demandeEcriture(String fileName) throws RemoteException;

    /**
     * Renvoie la répartion des chunks dans les dataNodes
     * @param fileName fichier 
     * @return répartition des chunks 
     * @throws RemoteException
     */
    public HashMap<Node, ArrayList<Integer>> getMetaData(String fileName) throws RemoteException;

    /**
     * Notifier le name Node d'une ecriture d'un chunk dans un dataNode
     * @param fileName nom du fichier 
     * @param chunkNumber numéro du chunk hebergé 
     * @param dataNode dataNode qui heberge 
     * @throws RemoteException
     */
    public void notifyNameNode(String fileName, int chunkNumber, Node dataNode ) throws RemoteException;

    /**
     * 
     * @param node
     * @throws RemoteException
     */
    public void senregistrer(Node node) throws RemoteException;

    /**
     * 
     * @param fileName
     * @return
     * @throws RemoteException
     */
    public HashMap<Node, ArrayList<Integer>> demandeLecture(String fileName) throws RemoteException;
    /**
     * 
     * @return
     * @throws RemoteException
     */

    public HashMap<Node, ArrayList<Integer>> getDataNodeDispo() throws RemoteException;
    
}
