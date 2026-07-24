package it.unibo.tw.web.model;

import java.util.Arrays;
import java.util.List;

/**
 * Costanti di dominio del sistema MusixConnection.
 *
 * Raccoglie gli insiemi chiusi previsti dai requisiti:
 *  - tipologie di associazione supportate (R5F)
 *  - strumenti musicali supportati (R8F)
 *  - stili di ballo supportati (R9F)
 *
 * Sono esposte come liste immodificabili cosi' da poter essere usate sia dalla
 * logica di validazione (controller) sia dalle viste (JSP) per popolare le select.
 */
public final class Costanti {

	private Costanti() {
		// classe di sole costanti: non istanziabile
	}

	/** Tipologie di associazione supportate dal sistema (R5F). */
	public static final String TIPO_STRUMENTO = "Strumento";
	public static final String TIPO_DANZA = "Danza";

	public static final List<String> TIPI_ASSOCIAZIONE =
			List.of(TIPO_STRUMENTO, TIPO_DANZA);

	/** Strumenti musicali supportati dal sistema (R8F). */
	public static final List<String> STRUMENTI = List.of(
			"Tromba",
			"Trombone",
			"Tuba",
			"Sax soprano",
			"Sax alto",
			"Sax tenore",
			"Sax baritono",
			"Flicorno soprano",
			"Clarinetto",
			"Flauto traverso",
			"Corno");

	/** Stili di ballo supportati dal sistema (R9F). */
	public static final List<String> STILI = List.of(
			"Hip hop",
			"Waacking",
			"Ballet",
			"Bachata",
			"Salsa",
			"Popping",
			"Tutting",
			"Vogue",
			"House");

	public static boolean isStrumentoValido(String s) {
		return s != null && STRUMENTI.contains(s);
	}

	public static boolean isStileValido(String s) {
		return s != null && STILI.contains(s);
	}

	public static boolean isTipoValido(String t) {
		return t != null && TIPI_ASSOCIAZIONE.contains(t);
	}

	public static List<String> tutteLeCompetenze() {
		String[] tutte = new String[STRUMENTI.size() + STILI.size()];
		int i = 0;
		for (String s : STRUMENTI) tutte[i++] = s;
		for (String s : STILI) tutte[i++] = s;
		return Arrays.asList(tutte);
	}
}
