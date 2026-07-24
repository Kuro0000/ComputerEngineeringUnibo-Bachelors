#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <fcntl.h>
#include <unistd.h>

#define DIM_BUFF 1024
#define MAX_WORD 100

int main(int argc, char **argv) {
    /* ------------------------------------------------------------
       AREA DICHIARAZIONI (TUTTO QUI)
       ------------------------------------------------------------ */
    
    /* Buffer e stringhe */
    char testo[DIM_BUFF];
    char parola_cercata[MAX_WORD];
    char temp_buff[DIM_BUFF];
    char cmd[MAX_WORD], arg1[MAX_WORD], arg2[MAX_WORD]; // Per parsing comandi
    
    /* Variabili numeriche e contatori */
    int i, j, k, len, len_parola;
    int conteggio;
    int fd; // File descriptor se serve leggere da file
    int nread;

    /* Flag e indici per logica senza break */
    int trovato, stop_search, mismatch;
    int start_idx, end_idx;
    int is_palindroma;

    /* Variabili per parsing */
    int num_args;
    int val_intero;

    /* ------------------------------------------------------------
       SIMULAZIONE DATI (O lettura da file/socket)
       ------------------------------------------------------------ */
    
    /* Immagina che questo sia il contenuto ricevuto da una socket o letto da file */
    strcpy(testo, "GET foto.jpg 2048\nQuesto e' un testo di prova.\nCerca la parola chiave.\n");
    
    printf("--- TESTO ORIGINALE ---\n%s\n-----------------------\n", testo);


    /* ============================================================
       1. PULIZIA STRINGA (RIMOZIONE \n o \r a fine riga)
       Utile dopo: fgets, read da socket (se il protocollo manda newline)
       ============================================================ */
    printf("\n--- 1. PULIZIA BUFFER (TRIM) ---\n");

    /* Copio una stringa "sporca" */
    strcpy(temp_buff, "comando_con_invio\n");
    len = strlen(temp_buff);
    
    if (len > 0) {
        if (temp_buff[len - 1] == '\n') {
            temp_buff[len - 1] = '\0';
        }
    }
    printf("Stringa pulita: '%s'\n", temp_buff);


    /* ============================================================
       2. CONTEGGIO OCCORRENZE DI UN CARATTERE
       Utile per: contare le linee (contando \n), verificare integrità
       ============================================================ */
    printf("\n--- 2. CONTEGGIO LINEE (carattere '\\n') ---\n");
    
    conteggio = 0;
    i = 0;
    while (testo[i] != '\0') {
        if (testo[i] == '\n') {
            conteggio++;
        }
        i++;
    }
    printf("Numero di righe trovate: %d\n", conteggio);


    /* ============================================================
       3. RICERCA DI UNA SOTTOSTRINGA (Manuale, senza strstr)
       Utile per: trovare se una parola esiste nel testo
       Sostituisce 'strstr' se vietata o se serve l'indice preciso
       ============================================================ */
    printf("\n--- 3. RICERCA PAROLA 'chiave' ---\n");
    
    strcpy(parola_cercata, "chiave");
    len = strlen(testo);
    len_parola = strlen(parola_cercata);
    
    trovato = -1; /* -1 = non trovato */
    i = 0;
    stop_search = 0; /* Flag per fermare il while esterno */

    while (i <= len - len_parola && stop_search == 0) {
        /* Controllo match carattere per carattere */
        j = 0;
        mismatch = 0; /* 0 = finora corrispondono */
        
        while (j < len_parola && mismatch == 0) {
            if (testo[i + j] != parola_cercata[j]) {
                mismatch = 1;
            }
            j++;
        }

        if (mismatch == 0) {
            trovato = i;
            stop_search = 1; /* Simula il break */
        }
        i++;
    }

    if (trovato != -1) printf("Parola trovata all'indice: %d\n", trovato);
    else printf("Parola non trovata.\n");


    /* ============================================================
       4. PARSING PROTOCOLLO (SSCANF) - FONDAMENTALE
       Utile per: interpretare comandi server "COMANDO ARGOMENTO VALORE"
       Es: "PUT immagine.jpg 1024" o "SUM 10 20"
       ============================================================ */
    printf("\n--- 4. PARSING COMANDO (sscanf) ---\n");
    
    strcpy(temp_buff, "PUT vacanze.jpg 5000");

    /* sscanf ritorna il numero di variabili riempite con successo */
    /* %s legge stringa fino allo spazio, %d legge intero */
    num_args = sscanf(temp_buff, "%s %s %d", cmd, arg1, &val_intero);

    if (num_args == 3) {
        if (strcmp(cmd, "PUT") == 0) {
            printf("Rilevato comando UPLOAD:\n");
            printf("- File: %s\n- Dimensione: %d bytes\n", arg1, val_intero);
        }
        else if (strcmp(cmd, "GET") == 0) {
            printf("Rilevato comando DOWNLOAD su %s\n", arg1);
        }
        else {
            printf("Comando sconosciuto: %s\n", cmd);
        }
    } else {
        printf("Formato comando non valido (argomenti insufficienti)\n");
    }


    /* ============================================================
       5. VERIFICA PALINDROMA
       Utile per: esercizi di logica sulle stringhe ricevute
       ============================================================ */
    printf("\n--- 5. CHECK PALINDROMO ---\n");
    
    strcpy(temp_buff, "anna"); /* Prova con "ciao" per fallimento */
    len = strlen(temp_buff);
    
    is_palindroma = 1; /* Assumo vero */
    start_idx = 0;
    end_idx = len - 1;

    /* Scorro dagli estremi verso il centro */
    while (start_idx < end_idx && is_palindroma == 1) {
        if (temp_buff[start_idx] != temp_buff[end_idx]) {
            is_palindroma = 0;
        }
        start_idx++;
        end_idx--;
    }

    if (is_palindroma == 1) printf("La parola '%s' e' palindroma.\n", temp_buff);
    else printf("La parola '%s' NON e' palindroma.\n", temp_buff);


    /* ============================================================
       6. CONVERSIONE IN MAIUSCOLO (Case Insensitive)
       Utile per: rendere i comandi validi sia come "get" che "GET"
       ============================================================ */
    printf("\n--- 6. TO UPPER CASE ---\n");
    
    strcpy(temp_buff, "Comando Misto");
    i = 0;
    while (temp_buff[i] != '\0') {
        temp_buff[i] = toupper(temp_buff[i]); /* serve <ctype.h> */
        i++;
    }
    printf("Stringa convertita: %s\n", temp_buff);


    /* ============================================================
       7. CONTARE PAROLE (TOKEN COUNT)
       Utile per: sapere quanti argomenti ha mandato il client
       senza usare strtok (che modifica la stringa originale)
       ============================================================ */
    printf("\n--- 7. CONTA PAROLE (spazi separatori) ---\n");
    
    strcpy(temp_buff, "  parola1   parola2 parola3  ");
    conteggio = 0;
    i = 0;
    /* Stato: 0 = fuori parola, 1 = dentro parola */
    int in_word = 0; 

    while (temp_buff[i] != '\0') {
        /* Se trovo un carattere non spazio e non ero in una parola -> nuova parola */
        if (temp_buff[i] != ' ' && temp_buff[i] != '\n' && in_word == 0) {
            in_word = 1;
            conteggio++;
        }
        /* Se trovo uno spazio, esco dalla parola */
        else if ((temp_buff[i] == ' ' || temp_buff[i] == '\n') && in_word == 1) {
            in_word = 0;
        }
        i++;
    }
    printf("Numero parole trovate: %d\n", conteggio);

    return 0;
}