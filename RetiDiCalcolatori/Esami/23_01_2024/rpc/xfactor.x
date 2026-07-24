/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/

/* Definisco tipi a dimensione fissa per evitare 'string' dinamiche */
typedef char identificatore_t[10];
typedef char matricola_t[10];

struct Prenotazione {
    identificatore_t id;
    matricola_t matricola; 
};

struct OutputLista {
    struct Prenotazione prenotazione[5];
    int numero_prenotazioni_trovati;
};

program GESTIONE_PRENOTAZIONI_PROG {
    version GESTIONE_PRENOTAZIONI_VERS {
        /* Procedura 1: Elimina. Input: matricola (array fisso), Output: int */
        int ELIMINA_PRENOTAZIONE(matricola_t) = 1;

        /* Procedura 2: Visualizza. Input: int, Output: Lista fissa */
        OutputLista VISUALIZZA_MULTIPLE(void) = 2;
    } = 1;
} = 0x20000015;