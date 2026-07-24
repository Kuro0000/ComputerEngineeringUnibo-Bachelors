package it.unibo.tw.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.unibo.tw.web.db.AssociazioneRepository;
import it.unibo.tw.web.db.DataSource;
import it.unibo.tw.web.db.PersistenceException;

/**
 * Singleton che raccoglie tutte le istanze di {@link Associazione} registrate
 * nel sistema (come da progettazione di dettaglio).
 *
 * <p>E' il punto unico da cui ottenere l'elenco delle associazioni: incapsula
 * l'accesso alla persistenza ("forza bruta") interrogando direttamente
 * {@link AssociazioneRepository}, e mantiene una copia in memoria dell'ultimo
 * caricamento. Cosi' i controller e le viste non devono conoscere il repository:
 * usano semplicemente {@code Associazioni.getInstance().getTutte()} /
 * {@code getById(...)} / {@code getByCodiceFiscale(...)}.</p>
 */
public class Associazioni {

	private static Associazioni instance;

	private final List<Associazione> associazioni = new ArrayList<Associazione>();
	private AssociazioneRepository repository;

	private Associazioni() {
	}

	public static synchronized Associazioni getInstance() {
		if (instance == null) {
			instance = new Associazioni();
		}
		return instance;
	}

	private AssociazioneRepository repository() {
		if (repository == null) {
			repository = new AssociazioneRepository(new DataSource(DataSource.MYSQL));
		}
		return repository;
	}

	/**
	 * Carica dalla persistenza tutte le associazioni registrate, aggiorna la
	 * copia in memoria e la restituisce. E' qui che avviene la query di lettura.
	 */
	public synchronized List<Associazione> getTutte() throws PersistenceException {
		associazioni.clear();
		associazioni.addAll(repository().readAll());
		return new ArrayList<Associazione>(associazioni);
	}

	public synchronized Associazione getById(int id) throws PersistenceException {
		for (Associazione a : getTutte()) {
			if (a.getId() == id) {
				return a;
			}
		}
		return null;
	}

	public synchronized Associazione getByCodiceFiscale(String cf) throws PersistenceException {
		for (Associazione a : getTutte()) {
			if (a.getCodiceFiscale().equals(cf)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Factory: costruisce un'{@link Associazione} avvolgendo il componente concreto
	 * con i decoratori concreti corrispondenti alle tipologie indicate.
	 */
	public static Associazione costruisci(int id, String nome, String cf, String email,
			String indirizzo, Set<String> tipi) {
		Associazione a = new AssociazioneConcreta(id, nome, cf, email, indirizzo);
		if (tipi.contains(Costanti.TIPO_STRUMENTO)) {
			a = new AssociazioneStrumento(a);
		}
		if (tipi.contains(Costanti.TIPO_DANZA)) {
			a = new AssociazioneDanza(a);
		}
		return a;
	}
}
