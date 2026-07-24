package it.unibo.tw.web.controller;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import it.unibo.tw.web.db.AssociazioneRepository;
import it.unibo.tw.web.db.CompetenzaRepository;
import it.unibo.tw.web.db.DataSource;
import it.unibo.tw.web.db.EventoRepository;
import it.unibo.tw.web.db.InsegnamentoRepository;
import it.unibo.tw.web.db.IscrizioneRepository;
import it.unibo.tw.web.db.PersistenceException;
import it.unibo.tw.web.db.PresenzaRepository;
import it.unibo.tw.web.db.UtenteRepository;
import it.unibo.tw.web.model.Costanti;
import it.unibo.tw.web.model.Evento;
import it.unibo.tw.web.model.Utente;

/**
 * Inizializzazione del database all'avvio dell'applicazione: crea le tabelle
 * (in modo idempotente, CREATE TABLE IF NOT EXISTS) e, se il DB e' vuoto,
 * inserisce alcuni dati di esempio per rendere il prototipo subito navigabile.
 */
public class InizializzazioneDB implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		DataSource ds = new DataSource(DataSource.MYSQL);

		UtenteRepository utenti = new UtenteRepository(ds);
		CompetenzaRepository competenze = new CompetenzaRepository(ds);
		AssociazioneRepository associazioni = new AssociazioneRepository(ds);
		IscrizioneRepository iscrizioni = new IscrizioneRepository(ds);
		InsegnamentoRepository insegnamenti = new InsegnamentoRepository(ds);
		EventoRepository eventi = new EventoRepository(ds);
		PresenzaRepository presenze = new PresenzaRepository(ds);

		try {
			// ordine importante per i vincoli di integrita' referenziale
			utenti.createTable();
			associazioni.createTable();
			competenze.createTable();
			iscrizioni.createTable();
			insegnamenti.createTable();
			eventi.createTable();
			presenze.createTable();

			seedSeVuoto(utenti, competenze, associazioni, iscrizioni, eventi);
			System.out.println("[MusixConnection] Inizializzazione DB completata.");
		} catch (PersistenceException e) {
			// Tipicamente: variabili d'ambiente usernameDB/passwordDB mancanti,
			// DBMS non in esecuzione o database "musixconnection" inesistente.
			System.err.println("[MusixConnection] ERRORE inizializzazione DB: " + e.getMessage());
		}
	}

	private void seedSeVuoto(UtenteRepository utenti, CompetenzaRepository competenze,
			AssociazioneRepository associazioni, IscrizioneRepository iscrizioni,
			EventoRepository eventi) throws PersistenceException {

		if (utenti.existsUsername("pippo")) {
			return; // gia' inizializzato
		}

		// --- Utenti demo ---
		Utente pippo = new Utente();
		pippo.setUsername("pippo");
		pippo.setPassword("1234");
		utenti.persist(pippo);
		competenze.persist(pippo.getId(), "Tromba");
		competenze.persist(pippo.getId(), "Salsa");

		Utente pluto = new Utente();
		pluto.setUsername("pluto");
		pluto.setPassword("1234");
		utenti.persist(pluto);
		competenze.persist(pluto.getId(), "Clarinetto");

		// --- Associazioni demo (coerenti coi mockup) ---
		Set<String> tStrumento = new HashSet<>();
		tStrumento.add(Costanti.TIPO_STRUMENTO);
		int idBlueNote = associazioni.persist("Blue Note Club", "01234567890",
				"info@bluenote.it", "Via del Jazz 1, Bologna", tStrumento);

		Set<String> tDanza = new HashSet<>();
		tDanza.add(Costanti.TIPO_DANZA);
		associazioni.persist("Rock Rebellion", "09876543210",
				"info@rockrebellion.it", "Via della Danza 5, Bologna", tDanza);

		// pippo e' Presidente del Blue Note Club; pluto vi e' iscritto
		iscrizioni.persist(pippo.getId(), idBlueNote, true, IscrizioneRepository.STATO_ATTIVA);
		iscrizioni.persist(pluto.getId(), idBlueNote, false, IscrizioneRepository.STATO_ATTIVA);

		// --- Eventi futuri demo per il Blue Note Club (per la conferma presenza) ---
		LocalDate oggi = LocalDate.now();

		Evento esib = new Evento();
		esib.setIdAssociazione(idBlueNote);
		esib.setTipo(Evento.TIPO_ESIBIZIONE);
		esib.setTitolo("Concerto di primavera");
		esib.setDescrizione("Esibizione della banda nel parco cittadino.");
		esib.setLuogo("Parco Montagnola, Bologna");
		esib.setData(Date.valueOf(oggi.plusDays(14)));
		esib.setOra(Time.valueOf("21:00:00"));
		eventi.persist(esib);

		Evento lez = new Evento();
		lez.setIdAssociazione(idBlueNote);
		lez.setTipo(Evento.TIPO_LEZIONE);
		lez.setTitolo("Lezione di tromba");
		lez.setDescrizione("Lezione collettiva per principianti.");
		lez.setLuogo("Sede del club");
		lez.setData(Date.valueOf(oggi.plusDays(7)));
		lez.setOra(Time.valueOf("18:30:00"));
		lez.setStrumentoStile("Tromba");
		eventi.persist(lez);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// niente da rilasciare: le connessioni sono aperte/chiuse per singola operazione
	}

}
