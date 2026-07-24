/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp extends Remote {
    int elimina_sci(String id) throws RemoteException;
    int noleggia_sci(String id, int giorno, int mese, int anno, int durata ) throws RemoteException;
}
