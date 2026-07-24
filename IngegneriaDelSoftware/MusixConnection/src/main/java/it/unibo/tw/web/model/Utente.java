package it.unibo.tw.web.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Utente registrato nel sistema.
 * Possiede un insieme (eventualmente vuoto) di strumenti e stili praticati (R7F).
 */
public class Utente {

	private int id;
	private String username;
	private String password;

	/** Competenze caricate dal repository (tabelle di relazione). */
	private List<String> strumenti = new ArrayList<>();
	private List<String> stili = new ArrayList<>();

	public Utente() {
	}

	public Utente(int id, String username, String password) {
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getStrumenti() {
		return strumenti;
	}

	public void setStrumenti(List<String> strumenti) {
		this.strumenti = strumenti;
	}

	public List<String> getStili() {
		return stili;
	}

	public void setStili(List<String> stili) {
		this.stili = stili;
	}

	/** True se l'utente pratica lo strumento/stile indicato (utile per R34F). */
	public boolean praticaStrumentoOStile(String strumentoStile) {
		return strumenti.contains(strumentoStile) || stili.contains(strumentoStile);
	}

	@Override
	public String toString() {
		return "Utente[" + username + "]";
	}
}
