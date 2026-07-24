/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp1 extends Remote {
    Risposta visualizza_prenotazioni(String tipo) throws RemoteException;
     int  elimina_prenotazione( String patente) throws RemoteException;
}
