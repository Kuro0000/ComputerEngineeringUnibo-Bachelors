package it.unibo.tw.web.model;

import java.util.Set;

/**
 * Componente astratto del pattern Decorator per l'Associazione.
 *
 * <p>Come da progettazione di dettaglio, l'Associazione e' modellata con il pattern
 * Decorator per rendere il sistema aperto alle estensioni (nuove tipologie/comportamenti).
 * Le tipologie concrete {@link AssociazioneStrumento} e {@link AssociazioneDanza} sono
 * realizzate come decoratori concreti che si applicano ad un {@link AssociazioneConcreta}.</p>
 */
public abstract class Associazione {

	public abstract int getId();

	public abstract String getNome();

	public abstract String getCodiceFiscale();

	public abstract String getEmail();

	public abstract String getIndirizzo();

	/**
	 * Insieme delle tipologie dell'associazione (es. {"Strumento"}, {"Danza"},
	 * oppure entrambe). Ogni decoratore concreto aggiunge la propria tipologia.
	 */
	public abstract Set<String> getTipi();

	public boolean isStrumento() {
		return getTipi().contains(Costanti.TIPO_STRUMENTO);
	}

	public boolean isDanza() {
		return getTipi().contains(Costanti.TIPO_DANZA);
	}

	@Override
	public String toString() {
		return "Associazione[" + getNome() + " " + getTipi() + "]";
	}
}
