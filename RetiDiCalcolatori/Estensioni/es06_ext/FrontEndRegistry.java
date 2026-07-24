import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FrontEndRegistry extends Remote {
    // Metodo per registrare un RegistryRemoto specificando l'intervallo di competenza
    public boolean registraFE(char inizio, char fine, RegistryRemotoServer registry) throws RemoteException;
}