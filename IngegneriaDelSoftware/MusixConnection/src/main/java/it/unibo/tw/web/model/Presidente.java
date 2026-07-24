package it.unibo.tw.web.model;

/**
 * Decoratore concreto: l'iscritto e' anche Presidente dell'associazione (R6F).
 * Un'associazione ha un unico Presidente alla volta (R01D).
 */
public class Presidente extends DecoratoreIscrizione {

	public Presidente(Iscrizione base) {
		super(base);
	}

	@Override
	public boolean isPresidente() {
		return true;
	}
}
