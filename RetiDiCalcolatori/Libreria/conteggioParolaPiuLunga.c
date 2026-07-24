
            // Operazione di conteggio delle parole (in linea)
            currCharCount = 0;
            while ((nread = read(fd, &read_char, sizeof(char))) != 0) {
                if (nread < 0) {
                    sprintf(err, "(PID %d) impossibile leggere dal file", getpid());
                    perror(err);
                    exit(0);
                } else {
                    if (read_char == ' ' || read_char == '\r' || read_char == '\n')
                    { // separatore: è finita una parola
                        if (currCharCount > charCount) {
                            charCount = currCharCount;
                        }
                        currCharCount = 0;
                    } else { // carattere
                        currCharCount++;
                    }
                }
            }
