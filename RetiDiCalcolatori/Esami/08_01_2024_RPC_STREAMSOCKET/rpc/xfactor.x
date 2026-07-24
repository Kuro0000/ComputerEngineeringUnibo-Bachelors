/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/

/* Definisco tipi a dimensione fissa per evitare 'string' dinamiche */
typedef char nomeFile_t[20];
typedef char nomeDir_t[20];


struct Inputdir {
    nomeDir_t nomeDir;
    char carattere;
    int occorrenze;
};


struct OutputLista {
    nomeFile_t nomeFile[7];
    int numero_file_trovati;
};

program GESTIONE_FILE_PROG {
    version GESTIONE_FILE_VERS {
        /* Procedura 1: Elimina. */
        int ELIMINA_OCCORRENZE(nomeFile_t) = 1;

        /* Procedura 2: Visualizza. */
        OutputLista LISTA_FILE_CARATTERE(Inputdir) = 2;
    } = 1;
} = 0x20000015;