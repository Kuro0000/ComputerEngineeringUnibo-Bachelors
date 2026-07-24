package it.unibo.tw.web.model;

/** Decoratore astratto del pattern Decorator per l'Iscrizione. */
public abstract class DecoratoreIscrizione extends Iscrizione {

	protected final Iscrizione base;

	protected DecoratoreIscrizione(Iscrizione base) {
		this.base = base;
	}

	@Override
	public int getIdUtente() {
		return base.getIdUtente();
	}

	@Override
	public String getUsername() {
		return base.getUsername();
	}

	@Override
	public int getIdAssociazione() {
		return base.getIdAssociazione();
	}

	@Override
	public boolean isPresidente() {
		return base.isPresidente();
	}

	@Override
	public boolean isInsegnante() {
		return base.isInsegnante();
	}
}
