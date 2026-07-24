/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
#include "xfactor.h"
#include <stdio.h>
#include <string.h>
#define ROWS 7
#define COLS 5
static struct Prenotazione matrice[ROWS][COLS];
static int inizializzato = 0;

/* Funzione per inizializzare la matrice */
int inizializza(){
    int i, j;
    
    if (inizializzato == 1)
        return 0;

    /* Prima pulisco tutto a Libero "L" */
    i = 0;
    while(i < ROWS) {
        j = 0;
        while(j < COLS) {
            strcpy(matrice[i][j].id, "L");
            strcpy(matrice[i][j].matricola, "");
            j++;
        }
        i++;
    }

    /* Inserimento dati di test sparsi nella matrice */
    strcpy(matrice[0][0].id, "AA123BB");
    strcpy(matrice[0][0].matricola, "11111");

    strcpy(matrice[0][1].id, "ED999XX");
    strcpy(matrice[0][1].matricola, "55555");

    strcpy(matrice[2][2].id, "AA223CB");
    strcpy(matrice[2][2].matricola, "11111"); /* Duplicato */

    strcpy(matrice[3][0].id, "ED555CB");
    strcpy(matrice[3][0].matricola, "55555"); /* Duplicato */

    strcpy(matrice[4][4].id, "EB578CB");
    strcpy(matrice[4][4].matricola, "22222");

    inizializzato = 1;
    return 1;
}

/* IMPLEMENTAZIONE PROCEDURA 1: Elimina prenotazione */
/* L'argomento ora è un puntatore al typedef matricola_t (array di char) */
int * elimina_prenotazione_1_svc(matricola_t matricola_input, struct svc_req *rqstp) {
    static int result;
    int i, j;
    if(inizializzato==0)
        inizializza();
    i = 0;
    result = -1;
    while(i < ROWS){
        j = 0;
        while(j < COLS){
            /* Se trovo la matricola, la resetto a "L" */
            if(strcmp(matrice[i][j].matricola, matricola_input) == 0){
                strcpy(matrice[i][j].id, "L");
                strcpy(matrice[i][j].matricola, "");
                result = 1;
            }
            j++;
        }
        i++;
    }
    
    return &result;
}

/* IMPLEMENTAZIONE PROCEDURA 2: Visualizza voti sopra soglia */
OutputLista * visualizza_multiple_1_svc(void *, struct svc_req *rqstp) {
/* VARIABILI IN TESTA */
    static OutputLista result;
    int i, j, k, count,r, c;
    int gia_in_lista;      /* Flag: l'ho già messo nel risultato? */

    /* INIZIALIZZAZIONE */
    if(inizializzato==0)
        inizializza();
    
    /* IMPORTANTE: Resetto il contatore della statica */
    result.numero_prenotazioni_trovati = 0;

    /* Scorro la matrice */
    i = 0;
    while(i < ROWS){
        j = 0;
        while(j < COLS){
            /* Se la cella non è vuota (L) */
            if(strcmp(matrice[i][j].id, "L") != 0) {
                
                /* Controllo se l'ho già messo nell'output per evitare duplicati nella lista result */
                gia_in_lista = 0;
                k = 0;
                while(k < result.numero_prenotazioni_trovati && gia_in_lista == 0){
                    if(strcmp(result.prenotazione[k].matricola,  matrice[i][j].matricola) == 0){
                        gia_in_lista = 1;
                    }
                    k++;
                }

                if(gia_in_lista == 0){
                    /* Conto le occorrenze in TUTTA la matrice */
                    count = 0;
                    r = 0;
                    while(r < ROWS){
                        c = 0;
                        while(c < COLS){
                            if(strcmp(matrice[r][c].matricola,  matrice[i][j].matricola) == 0){
                                count++;
                            }
                            c++;
                        }
                        r++;
                    }

                    /* Se occorrenze >= 2, aggiungo alla lista result */
                    if(count >= 2){
                        if(result.numero_prenotazioni_trovati < 20){
                            result.prenotazione[result.numero_prenotazioni_trovati] = matrice[i][j];
                            result.numero_prenotazioni_trovati++;
                        }
                    }
                }
            }
            j++;
        }
        i++;
    }
    
    return &result;
}