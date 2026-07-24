#include "xfactor.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* === STRUTTURA DINAMICA === */
typedef struct Nodo {
    char candidato[MAX_NAME_SIZE];
    char giudice[MAX_NAME_SIZE];
    char categoria[2];
    char nomeFile[FILENAME_MAX];
    char fase[10];
    int  voto;
    struct Nodo *next;
} Nodo;

static Nodo *lista = NULL;
static int inizializzato = 0;

/* Macro per aggiunta candidato */
#define AGGIUNGI_CANDIDATO_S(nome_c, giudice_c, cat, nomefile, fase_c, voti) \
    do { \
        Nodo *n = (Nodo*)malloc(sizeof(Nodo)); \
        int i; \
        for(i=0;i<MAX_NAME_SIZE;i++){ \
            n->candidato[i]=0; \
            n->giudice[i]=0; \
        } \
        for(i=0;i<FILENAME_MAX;i++) \
            n->nomeFile[i]=0; \
        for(i=0;i<10;i++) \
            n->fase[i]=0; \
        for(i=0;i<2;i++) \
            n->categoria[i]=0; \
        strcpy(n->candidato, nome_c); \
        strcpy(n->giudice, giudice_c); \
        strcpy(n->categoria, cat); \
        strcpy(n->nomeFile, nomefile); \
        strcpy(n->fase, fase_c); \
        n->voto = voti; \
        n->next = lista; \
        lista = n; \
    } while(0)

/* Inizializza con alcuni candidati */
void inizializza() {
    if (inizializzato == 1) return;

    AGGIUNGI_CANDIDATO_S("Brasco","Bowie","U","brasco.txt","A",87);
    AGGIUNGI_CANDIDATO_S("Viga","Winehouse","D","viga.txt","S",52);
    AGGIUNGI_CANDIDATO_S("Paperino","Bowie","O","paperino.txt","A",12);
    AGGIUNGI_CANDIDATO_S("Mike","Steve","B","mike.txt","B",76);

    inizializzato = 1;
}

/* === PROCEDURE REMOTE === */

// Classifica giudici esistente
Output *classifica_giudici_1_svc(void *void_in, struct svc_req *reqstp) {
    static Output res;
    int i, k, ind, trovato;
    Nodo *c;
    Giudice swap;
    char nome_giudici[NUM_GIUDICI][MAX_NAME_SIZE];
    int punteggi[NUM_GIUDICI];

    if (inizializzato == 0) inizializza();

    for (i=0; i<NUM_GIUDICI; i++) {
        strcpy(res.classificaGiudici[i].nomeGiudice, "L");
        res.classificaGiudici[i].punteggioTot = -1;
        for (k=0;k<MAX_NAME_SIZE;k++) nome_giudici[i][k] = 0;
        punteggi[i] = 0;
    }

    ind = 0;
    c = lista;
    while (c != NULL) {
        trovato = 0;
        for(k=0;k<ind;k++) {
            if(strcmp(nome_giudici[k], c->giudice)==0) {
                punteggi[k] += c->voto;
                trovato = 1;
            }
        }
        if (!trovato) {
            strcpy(nome_giudici[ind], c->giudice);
            punteggi[ind] = c->voto;
            ind++;
        }
        c = c->next;
    }

    for(i=0;i<ind;i++) {
        strcpy(res.classificaGiudici[i].nomeGiudice, nome_giudici[i]);
        res.classificaGiudici[i].punteggioTot = punteggi[i];
    }

    for(i=0;i<ind-1;i++) {
        for(k=0;k<ind-i-1;k++) {
            if(res.classificaGiudici[k].punteggioTot < res.classificaGiudici[k+1].punteggioTot) {
                swap = res.classificaGiudici[k];
                res.classificaGiudici[k] = res.classificaGiudici[k+1];
                res.classificaGiudici[k+1] = swap;
            }
        }
    }
    return &res;
}

// Esprimi voto esistente
int *esprimi_voto_1_svc(Input *input, struct svc_req *reqstp) {
    static int ris = -1;
    Nodo *c;
    ris = -1;
    if (inizializzato==0) inizializza();
    c = lista;
    while(c!=NULL) {
        if(strcmp(c->candidato,input->nomeCandidato)==0) {
            if (input->tipoOp == 'A') c->voto++;
            else if (input->tipoOp == 'S' && c->voto > 0) c->voto--;
            ris = 0;
            break;
        }
        c = c->next;
    }
    return &ris;
}

/* === NUOVE PROCEDURE PER L'ESTENSIONE === */

// Aggiungi nuovo candidato
int *aggiungi_candidato_1_svc(CandidatoInfo *candidato, struct svc_req *reqstp) {
    static int ris = -1;
    Nodo *c;
    ris = -1;
    if (inizializzato==0) inizializza();
    
    // Verifica se il candidato esiste già
    c = lista;
    while(c!=NULL) {
        if(strcmp(c->candidato, candidato->candidato)==0) {
            return &ris; // Candidato già esistente
        }
        c = c->next;
    }
    
    // Aggiungi nuovo candidato
    AGGIUNGI_CANDIDATO_S(candidato->candidato, candidato->giudice, 
                         candidato->categoria, candidato->nomeFile, 
                         candidato->fase, candidato->voto);
    ris = 0;
    return &ris;
}

// Rimuovi candidato
int *rimuovi_candidato_1_svc(Input *input, struct svc_req *reqstp) {
    static int ris = -1;
    Nodo *c, *prev;
    ris = -1;
    if (inizializzato==0) inizializza();
    
    c = lista;
    prev = NULL;
    while(c!=NULL) {
        if(strcmp(c->candidato, input->nomeCandidato)==0) {
            if (prev == NULL) {
                lista = c->next;
            } else {
                prev->next = c->next;
            }
            free(c);
            ris = 0;
            break;
        }
        prev = c;
        c = c->next;
    }
    return &ris;
}

// Filtra candidati per categoria o punteggio minimo
Output *candidati_filtrati_1_svc(FiltroInput *filtro, struct svc_req *reqstp) {
    static Output res;
    Nodo *c;
    int i, count;
    
    if (inizializzato == 0) inizializza();

    // Inizializza output
    for (i=0; i<NUM_GIUDICI; i++) {
        strcpy(res.classificaGiudici[i].nomeGiudice, "L");
        res.classificaGiudici[i].punteggioTot = -1;
    }

    c = lista;
    count = 0;
    while (c != NULL && count < NUM_GIUDICI) {
        int match = 0;
        
        if (filtro->tipoFiltro == 'C') { // Filtro per categoria
            if (strcmp(c->categoria, filtro->valore) == 0) {
                match = 1;
            }
        } else if (filtro->tipoFiltro == 'P') { // Filtro per punteggio minimo
            if (c->voto >= filtro->punteggioMin) {
                match = 1;
            }
        } else if (filtro->tipoFiltro == 'G') { // Filtro per giudice
            if (strcmp(c->giudice, filtro->valore) == 0) {
                match = 1;
            }
        }
        
        if (match) {
            strcpy(res.classificaGiudici[count].nomeGiudice, c->candidato);
            res.classificaGiudici[count].punteggioTot = c->voto;
            count++;
        }
        c = c->next;
    }
    
    return &res;
}