package it.unibo.tw.web.model;

/**
 * Messaggio scambiato nel Sistema di messaggistica di un'associazione (R25F):
 * mittente, corpo, data e ora. Nel prototipo i messaggi NON sono persistiti
 * (nessuna scrittura su DB, nessuna notifica push): risiedono solo in memoria.
 */
public class Messaggio {

	private final String mittente;
	private final String corpo;
	private final String data;
	private final String ora;

	public Messaggio(String mittente, String corpo, String data, String ora) {
		this.mittente = mittente;
		this.corpo = corpo;
		this.data = data;
		this.ora = ora;
	}

	public String getMittente() {
		return mittente;
	}

	public String getCorpo() {
		return corpo;
	}

	public String getData() {
		return data;
	}

	public String getOra() {
		return ora;
	}
}
