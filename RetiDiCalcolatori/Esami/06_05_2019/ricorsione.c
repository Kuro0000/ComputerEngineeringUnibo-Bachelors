#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

// Macro per dimensione fissa, visto che malloc è vietata
#define MAX_PATH 256
#define MAX_STACK 100

int main() {
    // 1. DICHIARAZIONE VARIABILI (TUTTE ALL'INIZIO OBBLIGATORIAMENTE)
    char rootDir[MAX_PATH];
    char stackDir[MAX_STACK][MAX_PATH]; // Stack simulato: array 2D di stringhe
    char currentDir[MAX_PATH];
    char fullPath[MAX_PATH];
    char entryName[MAX_PATH];
    
    // Puntatori per directory
    DIR *d;
    struct dirent *dir;
    struct stat st; // Per controllare se è file o directory (lstat)
    
    // Variabili contatori e indici
    int stackTop;
    int len;
    int i;
    int hasVowel;
    int hasConsonant;
    int isDir;
    char c;
    int finishedDir; // Flag per simulare il break nel while di lettura directory

    // Inizializzazione variabili
    stackTop = 0;
    d = NULL;
    dir = NULL;
    stackTop = 0;
    i = 0;
    hasVowel = 0;
    hasConsonant = 0;
    isDir = 0;
    finishedDir = 0;
    c = ' ';

    // Richiesta input (usando gets come richiesto)
    printf("Inserisci direttorio di partenza: ");
    gets(rootDir);

    // Push della root nello stack
    strcpy(stackDir[stackTop], rootDir);
    stackTop = stackTop + 1;

    // Loop principale (simulazione ricorsione)
    while (stackTop > 0) {
        // Pop dallo stack
        stackTop = stackTop - 1;
        strcpy(currentDir, stackDir[stackTop]);

        d = opendir(currentDir);

        if (d != NULL) {
            // Loop lettura entry directory
            finishedDir = 0;
            while (finishedDir == 0) {
                dir = readdir(d);
                
                if (dir == NULL) {
                    finishedDir = 1; // Uscita dal ciclo
                } else {
                    strcpy(entryName, dir->d_name);

                    // Ignoro "." e ".."
                    if (strcmp(entryName, ".") != 0 && strcmp(entryName, "..") != 0) {
                        
                        // Costruzione path completo: currentDir + "/" + entryName
                        // In C è necessario per fare lstat sulla sottocartella
                        strcpy(fullPath, currentDir);
                        strcat(fullPath, "/");
                        strcat(fullPath, entryName);

                        // Controllo tipo file (stat o lstat)
                        if (lstat(fullPath, &st) == 0) {
                            
                            // Verifica se Directory (S_ISDIR è una macro standard)
                            if (S_ISDIR(st.st_mode)) {
                                if (stackTop < MAX_STACK) {
                                    strcpy(stackDir[stackTop], fullPath);
                                    stackTop = stackTop + 1;
                                }
                            } 
                            // Verifica se File Regolare (S_ISREG)
                            else if (S_ISREG(st.st_mode)) {
                                // LOGICA SPECIFICA: 1 Vocale e 1 Consonante
                                len = strlen(entryName);
                                i = 0;
                                hasVowel = 0;
                                hasConsonant = 0;

                                while (i < len) {
                                    c = entryName[i];
                                    
                                    // Controllo Vocale
                                    if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' ||
                                        c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') {
                                        hasVowel = 1;
                                    } 
                                    // Controllo Consonante (lettera e non vocale)
                                    else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                                        hasConsonant = 1;
                                    }
                                    i = i + 1;
                                }

                                // Se soddisfa la condizione
                                if (hasVowel == 1 && hasConsonant == 1) {
                                    // QUI AVVIENE L'AZIONE
                                    // In un server reale: invio del file tramite socket
                                    // Per ora stampiamo a video per verifica
                                    printf("File da trasferire: %s\n", fullPath);
                                   
                                }
                            }
                        }
                    }
                }
            }
            closedir(d);
        }
    }

    return 0;
}