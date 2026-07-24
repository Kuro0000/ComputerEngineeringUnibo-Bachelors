package it.unibo.tw.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema di messaggistica condiviso, tenuto interamente IN MEMORIA (Singleton).
 *
 * <p>Conformemente alla definizione del prototipo, lo scambio di messaggi avviene
 * "senza persistenza e senza notifiche push": i messaggi non vengono mai scritti
 * sul DB e vanno persi al riavvio del server. Per ogni associazione vengono
 * mantenuti i soli messaggi scambiati dagli iscritti a quella associazione (R29F/R32F).</p>
 */
public class SistemaMessaggistica {

	private static SistemaMessaggistica instance;

	/** idAssociazione -> lista messaggi (in ordine di invio). */
	private final Map<Integer, List<Messaggio>> messaggiPerAssociazione = new HashMap<>();

	private SistemaMessaggistica() {
	}

	public static synchronized SistemaMessaggistica getInstance() {
		if (instance == null) {
			instance = new SistemaMessaggistica();
		}
		return instance;
	}

	public synchronized void invia(int idAssociazione, Messaggio messaggio) {
		messaggiPerAssociazione
				.computeIfAbsent(idAssociazione, k -> new ArrayList<>())
				.add(messaggio);
	}

	/** Restituisce (copia di) tutti i messaggi inviati nell'associazione indicata. */
	public synchronized List<Messaggio> getMessaggi(int idAssociazione) {
		List<Messaggio> lista = messaggiPerAssociazione.get(idAssociazione);
		if (lista == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(lista);
	}
}
