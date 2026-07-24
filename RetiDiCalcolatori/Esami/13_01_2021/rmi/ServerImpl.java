/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class ServerImpl extends UnicastRemoteObject implements RemOp {
    public static final int MAX_FILES = 256;
    public ServerImpl() throws RemoteException { 
        super(); 
    }
    public static void main(String[] args) {
        int registryPort = 1099;
        String registryHost = "localhost";
        String serviceName = "Server";
        if (args.length == 1) {
            try { 
                registryPort = Integer.parseInt(args[0]); 
            } catch (Exception e) { 
                System.exit(2); 
            }
        }
            // if (System.getSecurityManager() == null){
            //     System.setSecurityManager(new RMISecurityManager()); 
            // }
        String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
        System.out.println(completeName);
        try {
            ServerImpl serverRMI = new ServerImpl();
            Naming.rebind(completeName, serverRMI);
            System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
        } catch (Exception e) {
            System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public int elimina_occorrenze(String nomeFile) throws RemoteException {
        int result = -1;
        if(nomeFile == null || nomeFile.trim().isEmpty()){
            return result;
        }
        File file = new File(nomeFile);
        if(!file.exists()){
            return result;
        }
        if(!file.isFile() || !file.canRead() || !file.canWrite()){
            return result;
        }
        try{
            File outFile = new File("temp");
            FileWriter fw = new FileWriter(outFile);
            BufferedReader br = new BufferedReader(new FileReader(file));
            int car;
            result= 0;
            
            while((car = br.read())!=-1){
                if(!(car>='0' && car<='9')){
                    fw.append((char)car);
                }else{
                    result++;
                }
            }
            fw.close();
            br.close();

            
            // Sostituisce il file originale
            if (file.delete()) {
                if (!outFile.renameTo(file)) {
                    outFile.delete();// errore, eliminiamo il file temporaneo creato
                    System.err.println("Errore nella sostituzione del file originale: " + file.getName());
                } else {
                    System.out.println("File " + file.getName());
                }
            } else {
                outFile.delete();// errore, eliminiamo il file temporaneo creato
            }
        }catch(Exception e){
            return -1;
        }
        return result;
    }

    public Result lista_sottodirettori (String directoryName) throws RemoteException {
        Result result = new Result();
        File[] files = null;
        if(directoryName == null || directoryName.trim().isEmpty()){
            result.setErrorMessage("input invalido");
            return result;
        }
        File dir = new File(directoryName);
        if(!dir.exists()){
            result.setErrorMessage("non esiste il direttorio");
            return result;
        }
        if(!dir.isDirectory()){
            result.setErrorMessage("non è un direttorio");
            return result;
        }
        files = dir.listFiles();
        File[] subdir = null;
        int temp, index, i;
        index = 0;
        i = 0;
       while(i<files.length && index<6){
            temp = 0;
            if(files[i].isDirectory()){
                subdir =files[i].listFiles();
                for(int j = 0;j<subdir.length;j++){
                    if(subdir[j].isFile() && subdir[j].getName().endsWith(".txt")){
                        temp++;
                    }
                }
                if(temp>=6){
                    result.getFiles()[index] = files[i].getName();
                    index++;
                }
            }
            i++;
        }
       
        return result;
    }
}
