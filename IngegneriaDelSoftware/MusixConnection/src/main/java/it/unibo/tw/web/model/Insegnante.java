package it.unibo.tw.web.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Decoratore concreto: l'iscritto e' anche Insegnante dell'associazione per
 * uno o piu' strumenti/stili per i quali e' stato nominato dal Presidente (R26F, R30F).
 */
public class Insegnante extends DecoratoreIscrizione {

	private final List<String> materie;

	public Insegnante(Iscrizione base, List<String> materie) {
		super(base);
		this.materie = (materie != null) ? materie : new ArrayList<>();
	}

	/** Strumenti/stili per i quali l'iscritto e' insegnante in questa associazione. */
	public List<String> getMaterie() {
		return materie;
	}

	@Override
	public boolean isInsegnante() {
		return true;
	}
}
