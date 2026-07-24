/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp extends Remote {
    int elimina_occorrenze(String nomeFile) throws RemoteException;
    Result lista_sottodirettori(String directoryName) throws RemoteException;
}
