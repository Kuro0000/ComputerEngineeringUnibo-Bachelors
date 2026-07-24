

struct OutputFileScan {
	int caratteri;
	int parole;
	int linee;
  	int codiceErrore;
};




OutputFileScan *file_scan_1_svc(FileName *input, struct svc_req *rp) {
    static OutputFileScan result;
    int                   fd_file, nread;
    char                  cCorr;

    result.caratteri    = 0;
    result.parole       = 0;
    result.linee        = 0;
    result.codiceErrore = -1;

    printf("Richiesto file %s \n", input->name);

    fd_file = open(input->name, O_RDONLY);
    if (fd_file < 0) {
        printf("File inesistente\n");
        return (&result);
    } else {
        result.codiceErrore = 0;

        /* Conteggio caratteri */
        int  caratteri = 0;
        int  parole    = 0;
        int  linee     = 0;
        int  nread;
        char car;
        char prev = ' ';

        while (nread = read(fd_file, &car, 1) > 0) {
            caratteri += 1;
            if (car == '\n') {
                linee += 1;
            }
            if ((car == ' ' || car == '\n') && prev != ' ' && prev != '\n')
            { // Potrei usare un numero arbitrario di separatori.
                parole += 1;
            }
            prev = car;
        }

        if (nread < 0) { // Errore in lettura
            caratteri           = 0;
            parole              = 0;
            linee               = 0;
            result.codiceErrore = -1;
        } else {
            result.caratteri = caratteri;
            result.parole    = parole;
            result.linee     = linee;
            printf("Ho letto %d caratteri, %d parole e %d linee\n", result.caratteri, result.parole,
                   result.linee);
        }
        close(fd_file);
        return (&result);
    }
}
