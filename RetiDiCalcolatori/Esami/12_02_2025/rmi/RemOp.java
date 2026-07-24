/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp extends Remote {
    int elimina_parola(String nomeFile, String parola) throws RemoteException;
    Result lista_nomifile_soglia(String directoryName, int soglia) throws RemoteException;
}
