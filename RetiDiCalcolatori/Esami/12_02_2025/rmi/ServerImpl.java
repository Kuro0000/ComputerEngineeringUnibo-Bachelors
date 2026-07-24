/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.net.*;

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

    public int elimina_parola(String nomeFile, String parola) throws RemoteException {
        int result = -1; //dichiaro le variabili nel metodo per non avere sovrapposizione
        if(nomeFile==null || nomeFile.trim().isEmpty() || !nomeFile.endsWith(".txt") || parola == null || parola.trim().isEmpty()){
            return result;
        }
        File file = null;
        File outFile = null;
        FileWriter fw = null;
        BufferedReader br      = null;   
        String line = null;
        int pos = -1;
        int cont = 0;
        try{
            file = new File(nomeFile);
            outFile = new File("temp");
            if(!file.exists() || !file.canWrite()){
                return result;
            }
            fw = new FileWriter(outFile);
            br = new BufferedReader(new FileReader(file));
            while((line = br.readLine())!=null){
                        // Continua finché indexOf non restituisce -1
                while ((pos = line.indexOf(parola)) != -1) {
                    cont++;
                    // Rimuovi la parola trovata
                    line = line.substring(0, pos) + line.substring(pos + parola.length());
    
                }
                fw.append(line+'\n');

            }
            br.close();
            fw.close();
            // Sostituisce il file originale
            if (file.delete()) {
                if (!outFile.renameTo(file)) {
                    outFile.delete();// errore, eliminiamo il file temporaneo creato
                    System.err.println("Errore nella sostituzione del file originale: " + file.getName());
                } 
            } else {
                outFile.delete();// errore, eliminiamo il file temporaneo creato
                System.err.println("Errore eliminando il file originale " + file.getName());
            }

        
        }catch(Exception e){
            return -1;
        }
        if(cont>=0){
            result = cont;
        }
        return result;
    }

    public Result lista_nomifile_soglia(String directoryName, int soglia) throws RemoteException {
        Result result = new Result();
        //controlli
        if(directoryName == null || directoryName.trim().isEmpty()){
            result.setErrorMessage("directoryName vuoto");
            return result;
        }
        File dir = new File(directoryName);
        if(!dir.exists()){
            result.setErrorMessage("directory non esiste");
            return result;
        }
        if(!dir.isDirectory())
            {
            result.setErrorMessage("l'input inserito non è una direcotory");
            return result;
        }
        File[]files = dir.listFiles();
        
        if(files == null){
            result.setErrorMessage("directory vuoto");
            return result;
        }
        int size = 0;
        for(int i = 0;i<files.length;i++){
            if(files[i].isFile() && files[i].length()>=soglia){
                   
                System.out.println("salvando: " + files[i].getName() );

                result.getFiles()[size] = files[i].getName();
                size++;
            }
            
        }
        
       
        return result;
    }
}
