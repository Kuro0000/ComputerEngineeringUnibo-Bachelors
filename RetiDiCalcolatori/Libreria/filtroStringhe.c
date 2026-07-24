/* --- DICHIARAZIONE VARIABILI (DA METTERE IN TESTA) --- */
    char buffer_riga[256];      // Buffer per accumulare la riga corrente
    char *p;                    // Puntatore scorrevole per la ricerca
    char *trovato;              // Puntatore all'occorrenza trovata
    char c;                     // Carattere per la lettura
    int idx;                    // Indice per riempire il buffer
    int len_parola;             // Lunghezza della parola da eliminare
    
    /* Supponiamo tu abbia già aperto:
       fd_in (file originale da leggere)
       fd_out (file temporaneo su cui scrivere il risultato filtrato)
       parola (la stringa da eliminare) 
    */

    /* --- INIZIO LOGICA FILTRAGGIO --- */
    len_parola = strlen(parola);
    idx = 0;

    /* Leggiamo il file un carattere alla volta per costruire le righe */
    while (read(fd_in, &c, 1) > 0) {
        
        /* Se troviamo un "a capo", abbiamo una riga completa da processare */
        if (c == '\n') {
            buffer_riga[idx] = '\0'; // IMPORTANTE: Trasformiamo i byte in stringa C valida per strstr

            /* --- IL TUO FILTRO ADATTATO --- */
            p = buffer_riga;
            while ((trovato = strstr(p, parola)) != NULL) {
                // Scrive su file ciò che c'è PRIMA della parola
                write(fd_out, p, (trovato - p)); 
                
                // Salta la parola
                p = trovato + len_parola;
            }
            // Scrive l'ultima parte della riga (dopo l'ultima occorrenza)
            write(fd_out, p, strlen(p));
            /* ----------------------------- */

            /* Scriviamo il carattere new line nel file di uscita */
            write(fd_out, "\n", 1);
            
            /* Resettiamo l'indice per la prossima riga */
            idx = 0;

        } else {
                buffer_riga[idx] = c;
                idx++;
        }
    }
