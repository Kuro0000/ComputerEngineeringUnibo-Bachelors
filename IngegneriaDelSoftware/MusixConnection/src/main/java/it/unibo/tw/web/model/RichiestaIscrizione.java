package it.unibo.tw.web.model;

/**
 * Richiesta di iscrizione di un Utente ad un'Associazione (caso d'uso
 * RichiestaIscrizione, R10F/R11F). Oltre ai dati identificativi porta con se'
 * lo stato di approvazione (in attesa / accettata / rifiutata) e una breve
 * descrizione fornita dall'utente.
 */
public class RichiestaIscrizione {

	private int idUtente;
	private String username;
	private int idAssociazione;
	private String nomeAssociazione;
	private String descrizione;
	private String stato;

	public RichiestaIscrizione(int idUtente, String username, int idAssociazione,
			String nomeAssociazione, String descrizione, String stato) {
		this.idUtente = idUtente;
		this.username = username;
		this.idAssociazione = idAssociazione;
		this.nomeAssociazione = nomeAssociazione;
		this.descrizione = descrizione;
		this.stato = stato;
	}

	public int getIdUtente() { return idUtente; }
	public String getUsername() { return username; }
	public int getIdAssociazione() { return idAssociazione; }
	public String getNomeAssociazione() { return nomeAssociazione; }
	public String getDescrizione() { return descrizione; }
	public String getStato() { return stato; }
}
