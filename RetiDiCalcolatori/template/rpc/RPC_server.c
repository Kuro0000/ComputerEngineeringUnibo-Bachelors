/* RPC_Server.c */
#include "xfactor.h"
#include <stdio.h>
#include <string.h>
/* Macro per inizializzare la tabella */
/* Parametri: 
   _tab: l'array di struct
   _dim: dimensione massima dell'array (es. DIM_TABELLA)
   _flag: variabile intera (0 = non init, 1 = init)
   _i: variabile contatore (dichiarata a inizio main)
*/
#define INIZIALIZZA_DB(_tab, _dim, _flag, _i) \
    if (_flag == 0) { \
        strcpy(_tab[0].matricola, "123456"); \
        strcpy(_tab[0].nome, "Mario"); \
        strcpy(_tab[0].cognome, "Rossi"); \
        _tab[0].voto = 28; \
        \
        strcpy(_tab[1].matricola, "654321"); \
        strcpy(_tab[1].nome, "Luigi"); \
        strcpy(_tab[1].cognome, "Bianchi"); \
        _tab[1].voto = 22; \
        \
        _i = 2; \
        while (_i < _dim) { \
            strcpy(_tab[_i].matricola, ""); \
            strcpy(_tab[_i].nome, ""); \
            strcpy(_tab[_i].cognome, ""); \
            _tab[_i].voto = 0; \
            _i++; \
        } \
        _flag = 1; \
        printf("Database inizializzato con successo.\n"); \
    } else { \
        printf("Database gia' inizializzato.\n"); \
    }

static struct Studente tabella[6];
static int DIM_TABELLA = 6;
static int flag = 0;


/* IMPLEMENTAZIONE PROCEDURA 1*/
/* L'argomento ora è un puntatore al typedef matricola_t (array di char) */
int * elimina_prenotazione_1_svc(char *matricola_input, struct svc_req *rqstp) {
    static int result;
    int i;
    if(flag == 0)
        INIZIALIZZA_DB(tabella, DIM_TABELLA, flag, i);
    result = -1;
    i = 0;

    /* Nota: *matricola_input è l'array di char stesso */
    printf("Richiesta eliminazione per matricola: %s\n", matricola_input);

    while (i < DIM_TABELLA && result == -1) {
        /* Confronto stringhe */
        if (strcmp(tabella[i].matricola, matricola_input) == 0) {
            /* Cancellazione logica: copio stringa vuota negli array fissi */
            strcpy(tabella[i].matricola, "");
            strcpy(tabella[i].nome, "");
            strcpy(tabella[i].cognome, "");
            tabella[i].voto = 0;
            
            result = 1; /* Successo */
            printf("Eliminato studente indice %d\n", i);
        } else {
            i++;
        }
    }

    return &result;
}

/* IMPLEMENTAZIONE PROCEDURA 2*/
OutputLista * visualizza_voto_maggiore_soglia_1_svc(int *soglia, struct svc_req *rqstp) {
    static OutputLista result;
    int i;
    int count;
    if(flag==0)
        INIZIALIZZA_DB(tabella, DIM_TABELLA, flag, i);

    /* Pulizia memoria statica: setto tutto a 0 dato che è una risorsa statica */
    memset(&result, 0, sizeof(OutputLista));
    result.numero_studenti_trovati = 0;
    
    i = 0;
    count = 0;

    printf("Richiesta ricerca voti > %d\n", *soglia);

    while (i < DIM_TABELLA && count < 5) {
        if (strcmp(tabella[i].matricola, "") != 0) {
             if (tabella[i].voto > *soglia) {
                strcpy(result.studenti[count].nome, tabella[i].nome);
                strcpy(result.studenti[count].cognome, tabella[i].cognome);
                strcpy(result.studenti[count].matricola, tabella[i].matricola);
                result.studenti[count].voto = tabella[i].voto;
                
                count++;
             }
        }
        i++;
    }

    result.numero_studenti_trovati = count;
    return &result;
}