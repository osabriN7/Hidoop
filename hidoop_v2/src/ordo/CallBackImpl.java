package ordo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

public class CallBackImpl extends UnicastRemoteObject implements CallBack{

    /**Mutex pour synchroniser les les apples de Callbacks entre les mappers**/
    private Semaphore mutex;

    //Contructeur
    public CallBackImpl() throws RemoteException {
        this.mutex = new Semaphore(0);
    }

    @Override
    public void increment()
    {
        this.mutex.release();
    }

    @Override
    public void decrement() throws InterruptedException
    {
        this.mutex.acquire();
    }
}
