import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RemOp extends Remote {
    Result getDirectoryInfoClientActive(String directoryName) throws RemoteException;
    Result getDirectoryInfoServerActive(String directoryName, InetAddress clientHost, int clientPort) throws RemoteException;
}
