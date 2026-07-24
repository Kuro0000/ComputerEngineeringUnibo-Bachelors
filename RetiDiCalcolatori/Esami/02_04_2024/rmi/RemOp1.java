/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp1 extends Remote {
    Risposta lista_filetesto(String directoryName) throws RemoteException;
    int elimina_linee_contenenti_parola(String file, String parola) throws RemoteException;
}
