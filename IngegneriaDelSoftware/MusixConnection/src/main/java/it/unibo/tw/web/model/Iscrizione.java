package it.unibo.tw.web.model;

/**
 * Componente astratto del pattern Decorator per l'Iscrizione di un Utente ad
 * un'Associazione. I ruoli {@link Presidente} ed {@link Insegnante} sono
 * realizzati come decoratori concreti, in modo da rendere il modello aperto
 * a futuri ruoli (es. Tesoriere) senza modificare quelli esistenti.
 */
public abstract class Iscrizione {

	public abstract int getIdUtente();

	public abstract String getUsername();

	public abstract int getIdAssociazione();

	public boolean isPresidente() {
		return false;
	}

	public boolean isInsegnante() {
		return false;
	}

	@Override
	public String toString() {
		return "Iscrizione[utente=" + getUsername() + ", assoc=" + getIdAssociazione() + "]";
	}
}
