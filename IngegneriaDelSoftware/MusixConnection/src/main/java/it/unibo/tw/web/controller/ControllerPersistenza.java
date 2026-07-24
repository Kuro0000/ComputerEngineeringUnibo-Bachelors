package it.unibo.tw.web.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServlet;

import it.unibo.tw.web.db.AssociazioneRepository;
import it.unibo.tw.web.db.CompetenzaRepository;
import it.unibo.tw.web.db.DataSource;
import it.unibo.tw.web.db.EventoRepository;
import it.unibo.tw.web.db.InsegnamentoRepository;
import it.unibo.tw.web.db.IscrizioneRepository;
import it.unibo.tw.web.db.PresenzaRepository;
import it.unibo.tw.web.db.UtenteRepository;

/**
 * Classe base implementata da tutti i controller (servlet) dell'applicazione,
 * come previsto dalla progettazione di dettaglio: centralizza la gestione della
 * connessione al DBMS (tramite {@link DataSource}) e la scrittura sul log.
 *
 * <p>Nota sul prototipo: la gestione completa del log per la sicurezza e l'attore
 * GestoreSicurezza NON e' implementata in questa fase; {@link #scriviLog(String)}
 * e' un segnaposto che emette le operazioni nel formato "Timestamp Operazione".</p>
 */
public abstract class ControllerPersistenza extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/** Unico DataSource condiviso (MySQL per il prototipo). */
	private final DataSource dataSource = new DataSource(DataSource.MYSQL);

	protected DataSource getDataSource() {
		return this.dataSource;
	}

	// --- Factory dei repository (forza bruta), legati al DataSource condiviso ---------

	protected UtenteRepository utenti() {
		return new UtenteRepository(dataSource);
	}

	protected CompetenzaRepository competenze() {
		return new CompetenzaRepository(dataSource);
	}

	protected AssociazioneRepository associazioni() {
		return new AssociazioneRepository(dataSource);
	}

	protected IscrizioneRepository iscrizioni() {
		return new IscrizioneRepository(dataSource);
	}

	protected InsegnamentoRepository insegnamenti() {
		return new InsegnamentoRepository(dataSource);
	}

	protected EventoRepository eventi() {
		return new EventoRepository(dataSource);
	}

	protected PresenzaRepository presenze() {
		return new PresenzaRepository(dataSource);
	}

	/**
	 * Scrittura sul log delle operazioni di sistema (formato "Timestamp Operazione").
	 * Segnaposto per il prototipo: scrive su standard output.
	 */
	protected void scriviLog(String operazione) {
		System.out.println(LocalDateTime.now().format(FMT) + " " + operazione);
	}
}
