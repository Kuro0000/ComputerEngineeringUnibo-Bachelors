import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FrontEndClientServer extends Remote {
    // Cerca un singolo servizio (usato dal Client)
    public Remote cercaFE(String nomeLogico) throws RemoteException;
    
    // Cerca tutti i servizi con lo stesso nome (richiesto da specifica)
    public Remote[] cercaTuttiFE(String nomeLogico) throws RemoteException;
    
    // Registra un servizio di dominio (usato dal ServerCongresso)
    public boolean registra(String nomeLogico, Remote riferimento) throws RemoteException;
}