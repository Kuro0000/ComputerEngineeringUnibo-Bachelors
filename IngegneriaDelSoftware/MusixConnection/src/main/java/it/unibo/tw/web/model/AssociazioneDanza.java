package it.unibo.tw.web.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Decoratore concreto: caratterizza l'associazione come associazione di
 * tipo "Danza". Non aggiunge ancora ulteriori "decorazioni" di comportamento,
 * ma rende il modello aperto alle estensioni future.
 */
public class AssociazioneDanza extends DecoratoreAssociazione {

	public AssociazioneDanza(Associazione base) {
		super(base);
	}

	@Override
	public Set<String> getTipi() {
		Set<String> tipi = new HashSet<>(base.getTipi());
		tipi.add(Costanti.TIPO_DANZA);
		return tipi;
	}
}
