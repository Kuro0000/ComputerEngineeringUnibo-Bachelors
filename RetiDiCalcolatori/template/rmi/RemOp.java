/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/

import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp extends Remote {
    int metodo1(String directoryName) throws RemoteException;
    Result metodo2(String directoryName) throws RemoteException;
}
