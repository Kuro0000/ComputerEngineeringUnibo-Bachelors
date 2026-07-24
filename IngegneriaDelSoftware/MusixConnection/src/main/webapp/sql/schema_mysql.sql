-- =====================================================================
-- MusixConnection - Schema MySQL di riferimento
-- =====================================================================
-- NOTA: le tabelle vengono create automaticamente all'avvio
-- dell'applicazione dal listener InizializzazioneDB (CREATE TABLE IF NOT
-- EXISTS). Questo file e' fornito come documentazione e per eventuale
-- creazione manuale.
--
-- Prima dell'avvio creare il database:
--   CREATE DATABASE musixconnection CHARACTER SET utf8mb4;
-- =====================================================================

CREATE TABLE IF NOT EXISTS utente (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS associazione (
    id INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cf VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    indirizzo VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS associazione_tipo (
    idAssociazione INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,          -- 'Strumento' | 'Danza'
    PRIMARY KEY (idAssociazione, tipo),
    CONSTRAINT fk_at_assoc FOREIGN KEY (idAssociazione) REFERENCES associazione(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS competenza (
    id INT NOT NULL AUTO_INCREMENT,
    idUtente INT NOT NULL,
    valore VARCHAR(50) NOT NULL,        -- nome strumento o stile
    tipo VARCHAR(20) NOT NULL,          -- 'Strumento' | 'Stile'
    PRIMARY KEY (id),
    CONSTRAINT uq_competenza UNIQUE (idUtente, valore),
    CONSTRAINT fk_competenza_utente FOREIGN KEY (idUtente) REFERENCES utente(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS iscrizione (
    id INT NOT NULL AUTO_INCREMENT,
    idUtente INT NOT NULL,
    idAssociazione INT NOT NULL,
    presidente TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uq_iscrizione UNIQUE (idUtente, idAssociazione),
    CONSTRAINT fk_iscr_utente FOREIGN KEY (idUtente) REFERENCES utente(id),
    CONSTRAINT fk_iscr_assoc FOREIGN KEY (idAssociazione) REFERENCES associazione(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS insegnamento (
    id INT NOT NULL AUTO_INCREMENT,
    idIscrizione INT NOT NULL,
    materia VARCHAR(50) NOT NULL,       -- strumento o stile insegnato
    PRIMARY KEY (id),
    CONSTRAINT uq_insegnamento UNIQUE (idIscrizione, materia),
    CONSTRAINT fk_ins_iscr FOREIGN KEY (idIscrizione) REFERENCES iscrizione(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS evento (
    id INT NOT NULL AUTO_INCREMENT,
    idAssociazione INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,          -- 'Esibizione' | 'Lezione'
    titolo VARCHAR(150) NOT NULL,
    descrizione VARCHAR(500),
    luogo VARCHAR(200),
    data DATE NOT NULL,
    ora TIME,
    strumentoStile VARCHAR(50),         -- valorizzato per le Lezioni
    PRIMARY KEY (id),
    CONSTRAINT fk_evento_assoc FOREIGN KEY (idAssociazione) REFERENCES associazione(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS presenza (
    id INT NOT NULL AUTO_INCREMENT,
    idUtente INT NOT NULL,
    idEvento INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_presenza UNIQUE (idUtente, idEvento),
    CONSTRAINT fk_pres_utente FOREIGN KEY (idUtente) REFERENCES utente(id),
    CONSTRAINT fk_pres_evento FOREIGN KEY (idEvento) REFERENCES evento(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
