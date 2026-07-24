public class DiscoveryServer {
    public static void main(String[] args){
        
        if (args.length < 2) {
            System.out.println("Errore: numero degli argomenti errati " + args.length);
            System.out.println("Uso: DiscoveryServer portaClienti portaRegistrazioneRS");
            System.exit(1);
        }
         int portaClienti = -1;
        int portaRS = -1;
        Registro registro = null;
        ThreadClient threadClient = null;
        ThreadServer threadServer = null;
        try {
            portaClienti = Integer.parseInt(args[0]);
            portaRS = Integer.parseInt(args[1]);

            if (portaClienti <= 1024 || portaClienti > 65535 || 
                portaRS <= 1024 || portaRS > 65535) {
                System.out.println("Porta non valida");
                System.exit(2);
            }
        } catch (NumberFormatException e) {
            System.out.println("Porta non numerica");
            System.exit(2);
        }

        registro = new Registro();
        //creo due thread uno che gestisce il client e l'altro che gestisce il server
         threadClient = new ThreadClient(portaClienti, registro);
        threadClient.start();

         threadServer = new ThreadServer(portaRS, registro);
        threadServer.start();

        System.out.println("DiscoveryServer avviato:");
        System.out.println("  - Porta clienti: " + portaClienti);
        System.out.println("  - Porta registrazione RS: " + portaRS);

        try {
            threadClient.join();
            threadServer.join();
        } catch (InterruptedException e) {
            System.out.println("DiscoveryServer interrotto");
        }
    }

    
}

