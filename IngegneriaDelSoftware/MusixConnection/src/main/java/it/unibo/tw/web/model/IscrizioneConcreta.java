package it.unibo.tw.web.model;

/**
 * Componente concreto del pattern Decorator: rappresenta la semplice iscrizione
 * di un Utente ad un'Associazione (Iscritto), senza ruoli speciali.
 */
public class IscrizioneConcreta extends Iscrizione {

	private int idUtente;
	private String username;
	private int idAssociazione;

	public IscrizioneConcreta() {
	}

	public IscrizioneConcreta(int idUtente, String username, int idAssociazione) {
		this.idUtente = idUtente;
		this.username = username;
		this.idAssociazione = idAssociazione;
	}

	@Override
	public int getIdUtente() {
		return idUtente;
	}

	public void setIdUtente(int idUtente) {
		this.idUtente = idUtente;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public int getIdAssociazione() {
		return idAssociazione;
	}

	public void setIdAssociazione(int idAssociazione) {
		this.idAssociazione = idAssociazione;
	}
}
