#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* DEFINIZIONI COSTANTI COMUNI */
#define DIM_BUFF 256
#define MAX_ELEM 100
#define R 3
#define C 3

/* STRUTTURA D'ESEMPIO (es. Utente, File, Prenotazione) */
typedef struct {
    char nome[DIM_BUFF];
    int valore;     /* es. dimensione file, voto, porta UDP */
    int valido;     /* 1 = slot occupato, 0 = libero */
} Elemento;

int main(int argc, char **argv) {
    /* ------------------------------------------------------------
       AREA DICHIARAZIONE VARIABILI (TUTTE QUI PER REGOLA)
       ------------------------------------------------------------ */
    
    /* Variabili per gli array/struct */
    Elemento lista[MAX_ELEM];
    Elemento temp_elem;
    int num_elementi_effettivi; /* Quanti elementi ho davvero nell'array */
    
    /* Variabili per cicli e logica generale */
    int i, j, k, flag, trovato;
    int index_to_remove;
    
    /* Variabili per Bubble Sort */
    int ordinato;

    /* Variabili per Binary Search */
    int left, right, mid;
    char key_string[DIM_BUFF];
    int key_val;

    /* Variabili per Matrici (es. gioco Tris/Battaglia Navale) */
    char matrice[R][C];
    int vittoria_riga, vittoria_colonna, vittoria_diag;
    char giocatore_corrente;

    /* Variabili per Parsing Stringhe (es. comando "PUT nomefile 100") */
    char buffer_input[DIM_BUFF];
    char cmd[DIM_BUFF], arg1[DIM_BUFF];
    int arg2;
    int scan_res;

    /* ------------------------------------------------------------
       FINE DICHIARAZIONI - INIZIO LOGICA
       ------------------------------------------------------------ */

    /* Inizializzazione fittizia per test */
    num_elementi_effettivi = 5;
    strcpy(lista[0].nome, "z_file.txt"); lista[0].valore = 100;
    strcpy(lista[1].nome, "a_file.txt"); lista[1].valore = 50;
    strcpy(lista[2].nome, "m_file.txt"); lista[2].valore = 200;
    strcpy(lista[3].nome, "c_file.txt"); lista[3].valore = 10;
    strcpy(lista[4].nome, "b_file.txt"); lista[4].valore = 150;


    /* ============================================================
       1. BUBBLE SORT (Ordinamento Array di Struct)
       Utile per: ordinare file per nome (ls) o classifica (voti)
       ============================================================ */
    printf("\n--- 1. BUBBLE SORT (per nome) ---\n");
    
    i = 0;
    ordinato = 0; /* 0 = falso, 1 = vero */
    
    /* NOTA: Senza break, usiamo la condizione !ordinato */
    while (i < num_elementi_effettivi - 1 && ordinato == 0) {
        ordinato = 1; /* Assumo che sia ordinato */
        j = 0;
        while (j < num_elementi_effettivi - 1 - i) {
            /* CAMBIARE QUI: strcmp > 0 (crescente), < 0 (decrescente) */
            /* Se fosse per interi: if (lista[j].valore > lista[j+1].valore) */
            if (strcmp(lista[j].nome, lista[j+1].nome) > 0) {
                /* Swap */
                temp_elem = lista[j];
                lista[j] = lista[j+1];
                lista[j+1] = temp_elem;
                ordinato = 0; /* Ho fatto uno scambio, forse non è finito */
            }
            j++;
        }
        i++;
    }

    /* Stampa di verifica */
    i = 0;
    while(i < num_elementi_effettivi){
        printf("%s (%d)\n", lista[i].nome, lista[i].valore);
        i++;
    }


    /* ============================================================
       2. BINARY SEARCH (Ricerca Binaria)
       Utile per: cercare velocemente in array ORDINATO (es. login)
       Vincolo: L'array DEVE essere ordinato (appena fatto sopra)
       ============================================================ */
    printf("\n--- 2. BINARY SEARCH (Cerco 'm_file.txt') ---\n");
    
    strcpy(key_string, "m_file.txt");
    left = 0;
    right = num_elementi_effettivi - 1;
    trovato = -1; /* -1 non trovato, altrimenti indice */

    while (left <= right && trovato == -1) {
        mid = left + (right - left) / 2;
        
        /* Confronto */
        flag = strcmp(lista[mid].nome, key_string);
        
        if (flag == 0) {
            trovato = mid;
        } else {
            if (flag < 0) {
                left = mid + 1; /* Cerca a destra */
            } else {
                right = mid - 1; /* Cerca a sinistra */
            }
        }
    }

    if (trovato != -1) printf("Trovato all'indice: %d\n", trovato);
    else printf("Non trovato.\n");


    /* ============================================================
       3. RICERCA LINEARE E CANCELLAZIONE CON SHIFT (Array Compatto)
       Utile per: utente che si disconnette, eliminare file
       Evita "buchi" nell'array.
       ============================================================ */
    printf("\n--- 3. ELIMINAZIONE 'c_file.txt' CON SHIFT ---\n");
    
    /* Fase A: Cerco l'indice */
    strcpy(key_string, "c_file.txt");
    index_to_remove = -1;
    i = 0;
    while (i < num_elementi_effettivi && index_to_remove == -1) {
        if (strcmp(lista[i].nome, key_string) == 0) {
            index_to_remove = i;
        }
        i++;
    }

    /* Fase B: Se trovato, shifto tutto a sinistra */
    if (index_to_remove != -1) {
        j = index_to_remove;
        /* Sposto tutti gli elementi successivi indietro di 1 */
        while (j < num_elementi_effettivi - 1) {
            lista[j] = lista[j+1];
            j++;
        }
        /* Pulisco l'ultimo (opzionale ma pulito) */
        memset(&lista[num_elementi_effettivi-1], 0, sizeof(Elemento));
        num_elementi_effettivi--; /* Decremento contatore globale */
        printf("Elemento eliminato. Nuova dim: %d\n", num_elementi_effettivi);
    } else {
        printf("Elemento da eliminare non trovato.\n");
    }


    /* ============================================================
       4. PARSING COMANDI STRINGA (Senza strtok dinamico)
       Utile per: interpretare comandi client tipo "SUM 10 20" o "LOGIN user pass"
       ============================================================ */
    printf("\n--- 4. PARSING COMANDO ---\n");
    
    strcpy(buffer_input, "PUT foto.jpg 1024");
    
    /* Metodo sscanf: il più sicuro e rapido se il formato è fisso */
    /* Ritorna il numero di variabili riempite correttamente */
    scan_res = sscanf(buffer_input, "%s %s %d", cmd, arg1, &arg2);

    if (scan_res == 3 && strcmp(cmd, "PUT") == 0) {
        printf("Comando: %s, File: %s, Dimensione: %d\n", cmd, arg1, arg2);
    } else {
        printf("Formato comando errato.\n");
    }


    /* ============================================================
       5. MATRICI: CONTROLLO VITTORIA (Es. Tris/Forza 4)
       Utile per: giochi a turni su griglia
       ============================================================ */
    printf("\n--- 5. CHECK MATRICE (TRIS) ---\n");

    /* Setup matrice test (diagonale vincente X) */
    for(i=0; i<R; i++) for(j=0; j<C; j++) matrice[i][j] = ' ';
    matrice[0][0] = 'X'; matrice[1][1] = 'X'; matrice[2][2] = 'X';
    matrice[0][1] = 'O'; matrice[0][2] = 'O';

    giocatore_corrente = 'X';
    trovato = 0; /* Usato come flag vittoria */

    /* Controllo RIGHE */
    i = 0;
    while (i < R && trovato == 0) {
        if (matrice[i][0] == giocatore_corrente && 
            matrice[i][1] == giocatore_corrente && 
            matrice[i][2] == giocatore_corrente) {
            trovato = 1;
        }
        i++;
    }

    /* Controllo COLONNE (se non vinto per riga) */
    j = 0;
    while (j < C && trovato == 0) {
        if (matrice[0][j] == giocatore_corrente && 
            matrice[1][j] == giocatore_corrente && 
            matrice[2][j] == giocatore_corrente) {
            trovato = 1;
        }
        j++;
    }

    /* Controllo DIAGONALI (se non vinto prima) */
    if (trovato == 0) {
        if (matrice[0][0] == giocatore_corrente && 
            matrice[1][1] == giocatore_corrente && 
            matrice[2][2] == giocatore_corrente) {
            trovato = 1;
        }
    }
    if (trovato == 0) {
        if (matrice[0][2] == giocatore_corrente && 
            matrice[1][1] == giocatore_corrente && 
            matrice[2][0] == giocatore_corrente) {
            trovato = 1;
        }
    }

    if (trovato) printf("Il giocatore %c ha vinto!\n", giocatore_corrente);
    else printf("Nessuna vittoria.\n");


    /* ============================================================
       6. TROVARE SLOT LIBERO (Prima posizione vuota)
       Utile per: aggiungere nuovo client/file se array non è compatto
       ============================================================ */
    printf("\n--- 6. RICERCA SLOT LIBERO ---\n");
    
    /* Supponiamo che valido=0 significhi libero */
    lista[num_elementi_effettivi].valido = 0; /* Simulo slot libero alla fine */
    
    trovato = -1;
    i = 0;
    while (i < MAX_ELEM && trovato == -1) {
        if (lista[i].valido == 0) { /* O strcmp(..., "") == 0 */
            trovato = i;
        }
        i++;
    }

    if (trovato != -1) printf("Slot libero trovato indice: %d\n", trovato);
    else printf("Nessuno slot libero (Server Pieno).\n");

    return 0;
}