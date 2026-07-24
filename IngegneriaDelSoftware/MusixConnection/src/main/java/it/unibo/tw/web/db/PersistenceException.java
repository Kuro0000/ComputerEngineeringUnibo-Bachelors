package it.unibo.tw.web.db;

/** Eccezione applicativa sollevata dallo strato di persistenza (forza bruta). */
public class PersistenceException extends Exception {

	private static final long serialVersionUID = -3835068319580102263L;

	public PersistenceException(String msg) {
		super(msg);
	}
}
