
        try {
            String outFile = nomeFile;
            int min = min;
            int max =max;
            
            File dir = new File("."); // Directory corrente remota
            File[] files = dir.listFiles();
            int count = 0;
            
            FileWriter fw = new FileWriter(outFile);
            BufferedWriter bw = new BufferedWriter(fw);
             long len ;
             BufferedReader br = null;
             String line = null;
            if (files != null) {
                for (int i= 0; i<files.length;i++) {
                    // Controlla se file, se .txt e soglie dimensione
                    if (f.isFile() && f.getName().endsWith(".txt")) {
                        len = f.length();
                        if (len > min && len < max) {
                            // Unione contenuto
                             br = new BufferedReader(new FileReader(f));
                            while((line = br.readLine()) != null) {
                                bw.write(line);
                                bw.newLine();
                            }
                            br.close();
                            count++;
                            // Rimozione file
                            f.delete();
                        }
                    }
                }
            }
            bw.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorMessage(e.getMessage());
        }

