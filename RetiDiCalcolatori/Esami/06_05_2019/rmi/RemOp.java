/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp extends Remote {
    Result lista_file(String directoryName) throws RemoteException;
     int numerazione_righe(String nomeFile) throws RemoteException;
}
