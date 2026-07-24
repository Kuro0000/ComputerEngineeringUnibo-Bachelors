package it.unibo.tw.web.model;

import java.sql.Date;
import java.sql.Time;

/**
 * Evento pubblicato sulla bacheca di un'associazione (R19F/R21F).
 * Per il prototipo Esibizione e Lezione sono distinte dal campo {@code tipo}
 * ("Esibizione" | "Lezione"); la Lezione valorizza inoltre {@code strumentoStile}.
 */
public class Evento {

	public static final String TIPO_ESIBIZIONE = "Esibizione";
	public static final String TIPO_LEZIONE = "Lezione";

	private int id;
	private int idAssociazione;
	private String tipo;
	private String titolo;
	private String descrizione;
	private String luogo;
	private Date data;
	private Time ora;
	/** Valorizzato solo per le Lezioni. */
	private String strumentoStile;

	public Evento() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdAssociazione() {
		return idAssociazione;
	}

	public void setIdAssociazione(int idAssociazione) {
		this.idAssociazione = idAssociazione;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getLuogo() {
		return luogo;
	}

	public void setLuogo(String luogo) {
		this.luogo = luogo;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Time getOra() {
		return ora;
	}

	public void setOra(Time ora) {
		this.ora = ora;
	}

	public String getStrumentoStile() {
		return strumentoStile;
	}

	public void setStrumentoStile(String strumentoStile) {
		this.strumentoStile = strumentoStile;
	}

	public boolean isLezione() {
		return TIPO_LEZIONE.equals(tipo);
	}

	/** Evento futuro rispetto ad ora (R35F). */
	public boolean isFuturo() {
		if (data == null) {
			return false;
		}
		long oggi = System.currentTimeMillis();
		long quando = data.getTime() + (ora != null ? ora.getTime() : 0);
		return quando >= oggi - (24L * 60 * 60 * 1000); // tollera la giornata in corso
	}

	@Override
	public String toString() {
		return "Evento[" + tipo + " '" + titolo + "' " + data + " " + ora + "]";
	}
}
