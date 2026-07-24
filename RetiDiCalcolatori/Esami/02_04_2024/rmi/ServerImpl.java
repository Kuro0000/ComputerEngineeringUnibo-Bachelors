/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


import java.io.*;

public class ServerImpl extends UnicastRemoteObject implements RemOp1 {
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

    public Risposta lista_filetesto(String directoryName) throws RemoteException {
        Risposta result = new Risposta(); //dichiaro le variabili nel metodo per non avere sovrapposizione
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
            if(files[i].isFile() && files[i].getName().endsWith(".txt")){
                   
                System.out.println("salvando: " + files[i].getName() );

                result.getFiles()[size] = files[i].getName();
                size++;
            }
            
        }
        

        return result;
    }

    public int elimina_linee_contenenti_parola(String nomeFile, String parola) throws RemoteException {
        int result = -1;
        if(nomeFile == null || nomeFile.trim().isEmpty() || parola == null || parola.trim().isEmpty()){
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
            String linea = null;
            result= 0;
            while((linea = br.readLine())!=null){
 
                if(!linea.contains(parola)){
                    fw.append(linea+System.lineSeparator());
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
}
