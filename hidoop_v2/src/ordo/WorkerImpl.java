package ordo;

import formats.Format;
import map.Mapper;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class WorkerImpl extends UnicastRemoteObject implements Worker {

    //Contructeur par defaut
    public WorkerImpl() throws RemoteException {
        super();
    }

    @Override
    public void runMap(Mapper m, Format reader, Format writer, CallBack cb) throws RemoteException {

        System.out.println("Starting Worker");
        reader.open(Format.OpenMode.R);
        writer.open(Format.OpenMode.W);
        System.out.println("Launching the Map Operation...");
        m.map(reader, writer);
        reader.close();
        writer.close();

        /** Incrementer le nombre de Callbacks pour notifier la fin de Map **/
        /*try {
            cb.increment();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        System.out.println("The Map Operation is Finished");
    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            LocateRegistry.createRegistry(port);
            //InetAddress  adresse = InetAddress.getLocalHost();
            //Naming.rebind("//" +adresse.getHostName()+ ":" + port + "/worker", new WorkerImpl());
            InetAddress  adresse = InetAddress.getLocalHost();
            Naming.rebind("//" + adresse.getHostName()+ ":" + port + "/worker", new WorkerImpl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
