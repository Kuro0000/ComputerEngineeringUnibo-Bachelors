package it.unibo.tw.web.model;

import java.util.Set;

/**
 * Decoratore astratto del pattern Decorator per l'Associazione.
 * Delega tutte le operazioni al componente avvolto; i decoratori concreti
 * aggiungono la propria tipologia ridefinendo {@link #getTipi()}.
 */
public abstract class DecoratoreAssociazione extends Associazione {

	protected final Associazione base;

	protected DecoratoreAssociazione(Associazione base) {
		this.base = base;
	}

	@Override
	public int getId() {
		return base.getId();
	}

	@Override
	public String getNome() {
		return base.getNome();
	}

	@Override
	public String getCodiceFiscale() {
		return base.getCodiceFiscale();
	}

	@Override
	public String getEmail() {
		return base.getEmail();
	}

	@Override
	public String getIndirizzo() {
		return base.getIndirizzo();
	}

	@Override
	public Set<String> getTipi() {
		return base.getTipi();
	}
}
