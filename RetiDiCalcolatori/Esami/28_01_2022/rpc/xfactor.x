/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/

/* Definisco tipi a dimensione fissa per evitare 'string' dinamiche */
typedef char nomeFile_t[20];
typedef char linea_t[257];
typedef char nomeDir_t[20];
typedef char prefisso_t[10];

struct Inputfile {
    nomeFile_t nome;
    linea_t linea;
};
struct Inputdir {
    nomeDir_t nomeDir;
    prefisso_t prefisso;
};


struct OutputLista {
    nomeFile_t nomeFile[6];
    int numero_file_trovati;
};

program GESTIONE_FILE_PROG {
    version GESTIONE_FILE_VERS {
        /* Procedura 1: Elimina. Input: matricola (array fisso), Output: int */
        int CONTA_OCCORRENZE_LINEA(Inputfile) = 1;

        /* Procedura 2: Visualizza. Input: int, Output: Lista fissa */
        OutputLista LISTA_FILE_PREFISSO(Inputdir) = 2;
    } = 1;
} = 0x20000015;