/* RPC_xFile.x */

/* Definisco tipi a dimensione fissa per evitare 'string' dinamiche */
typedef char nome_t[20];
typedef char cognome_t[20];
typedef char matricola_t[10];

struct Studente {
    nome_t nome;           /* Tradotto in C come: char nome[20]; */
    cognome_t cognome;     /* Tradotto in C come: char cognome[20]; */
    matricola_t matricola; /* Tradotto in C come: char matricola[10]; */
    int voto;
};

struct OutputLista {
    struct Studente studenti[5];
    int numero_studenti_trovati;
};

program GESTIONE_STUDENTI_PROG {
    version GESTIONE_STUDENTI_VERS {
        /* Procedura 1: Elimina. Input: matricola (array fisso), Output: int */
        int ELIMINA_PRENOTAZIONE(matricola_t) = 1;

        /* Procedura 2: Visualizza. Input: int, Output: Lista fissa */
        OutputLista VISUALIZZA_VOTO_MAGGIORE_SOGLIA(int) = 2;
    } = 1;
} = 0x20000015;