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
    Result lista_file_carattere(String directoryName, char c, int occorrenze) throws RemoteException;
}
