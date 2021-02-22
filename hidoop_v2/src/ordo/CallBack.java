package ordo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallBack extends Remote {

    public void increment() throws RemoteException;
    public void decrement() throws RemoteException, InterruptedException;
}
