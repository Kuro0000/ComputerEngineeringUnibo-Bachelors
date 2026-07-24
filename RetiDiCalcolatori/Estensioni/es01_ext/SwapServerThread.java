import java.io.*;
import java.net.*;

public class SwapServerThread extends Thread {
                private DatagramSocket socket = null;
    private File nomeFile = null;

        private int portaRS = -1;

    public SwapServerThread(DatagramSocket socket, int portaRS, File nomeFile) {
        this.socket = socket;
        this.portaRS = portaRS;
        this.nomeFile = nomeFile;
    }
    
    @Override
    public void run() {
        try {
            DatagramPacket packet = null;
            byte[] data = null;
            ByteArrayOutputStream boStreamResp = new ByteArrayOutputStream();
            DataOutputStream doStreamResp = new DataOutputStream(boStreamResp);
             byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);

            System.out.println("SwapServer per file " + nomeFile + " avviato sulla porta: " + portaRS);

            String numRighe = null;
            String linea = null;
            String buff1 = null;
            String buff2 = null;
            String[] split = null;
            BufferedReader br = null;
            BufferedWriter bw = null;
            File fileTemp = null;
            int countLine = -1;
            int linea1 = -1;
            int linea2 = -1;
            int res = -1;
            ByteArrayInputStream biStream = null;
            DataInputStream diStream =null;
            while (true) {
                res = -1;
                buff1 = null;
                buff2 = null;
                try {
                    packet.setData(buf);
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (SocketException e) {
                    System.out.println("Socket chiusa, terminazione server.");
                    return;
                } catch (IOException e) {
                    System.err.println("Errore ricezione: " + e.getMessage());
                    continue;
                }
                
                if (socket.isClosed()) {
                    System.out.println("Socket chiusa durante processing, terminazione...");
                    return;
                }

                try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					numRighe = diStream.readUTF();
                } catch (IOException e) {
                    System.out.println("Problemi nella lettura della richiesta. " + numRighe);
                    e.printStackTrace();
                    continue;
                }

                try {
                    split = numRighe.split("-");
                    if (split.length != 2) {
                        System.out.println("ricevuto la seguente stringa "+numRighe);
                        try {
                            boStreamResp = new ByteArrayOutputStream();
                            doStreamResp = new DataOutputStream(boStreamResp);
                            doStreamResp.writeInt(res);
                            data  = boStreamResp.toByteArray();
                            packet.setData(data, 0, data.length);
                            socket.send(packet);
                        } catch (IOException e) {
                            System.err.println("Errore durante l'invio del pacchetto: " + e.getMessage());
                        }                        continue;
                    }
                    try{
                    linea1 = Integer.parseInt(split[0]);
                    linea2 = Integer.parseInt(split[1]);
                    }catch(NumberFormatException e){
                        System.out.println("Errore delle linee ricevute");
                        e.printStackTrace();
                        try {
                            boStreamResp = new ByteArrayOutputStream();
                            doStreamResp = new DataOutputStream(boStreamResp);
                            doStreamResp.writeInt(res);
                            data  = boStreamResp.toByteArray();
                            packet.setData(data, 0, data.length);

                            socket.send(packet);
                        } catch (IOException ioe) {
                            System.err.println("Errore durante l'invio del pacchetto: " + ioe.getMessage());
                        }                        
                        continue;
                    }
                    countLine = 0;
                    br = new BufferedReader(new FileReader(nomeFile));
                    //decido di creare un file temporaneo con nomi diversi così con altri thread
                    //non ci saranno problemi di risorse condivise
                    bw = new BufferedWriter(new FileWriter("temp_"+nomeFile));

                    while ((linea = br.readLine()) != null && (buff1 == null || buff2 == null)) {
                        countLine++;
                        if (countLine == linea1) buff1 = linea;
                        else if (countLine == linea2) buff2 = linea;
                    }
                    br.close();

                    if (buff1 == null || buff2 == null) {
                        bw.close();


                        try {
                            boStreamResp = new ByteArrayOutputStream();
                            doStreamResp = new DataOutputStream(boStreamResp);
                            doStreamResp.writeInt(res);
                            data  = boStreamResp.toByteArray();
                            packet.setData(data, 0, data.length);

                            socket.send(packet);
                        } catch (IOException e) {
                            System.err.println("Errore durante l'invio del pacchetto: " + e.getMessage());
                        }




                        new File("temp_"+nomeFile).delete();
                        continue;
                    }

                    countLine = 0;
                    br = new BufferedReader(new FileReader(nomeFile));
                    while ((linea = br.readLine()) != null) {
                        countLine++;
                        if (countLine == linea1) linea = buff2;
                        else if (countLine == linea2) linea = buff1;
                        bw.write(linea);
                        bw.newLine();
                    }
                    br.close();
                    bw.close();

                    fileTemp = new File("temp_"+nomeFile);
                    if (nomeFile.delete()) {
                        if (fileTemp.renameTo(nomeFile)) res = 0;
                    }
                        try {
                            boStreamResp = new ByteArrayOutputStream();
                            doStreamResp = new DataOutputStream(boStreamResp);
                            doStreamResp.writeInt(res);
                            data  = boStreamResp.toByteArray();
                            packet.setData(data, 0, data.length);

                            socket.send(packet);
                        } catch (IOException e) {
                            System.err.println("Errore durante l'invio del pacchetto: " + e.getMessage());
                        }
                } catch (Exception e) {
                    System.err.println("Errore: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } 
        if (socket != null && !socket.isClosed()) 
            socket.close();

    }

}
