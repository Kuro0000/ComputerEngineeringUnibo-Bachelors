package it.unibo.tw.web.model;

import java.util.Collections;
import java.util.Set;

/**
 * Componente concreto del pattern Decorator: contiene i dati anagrafici
 * dell'associazione (R4F). Da solo non possiede alcuna tipologia: le tipologie
 * sono aggiunte avvolgendolo con i decoratori concreti
 * {@link AssociazioneStrumento} e/o {@link AssociazioneDanza}.
 */
public class AssociazioneConcreta extends Associazione {

	private int id;
	private String nome;
	private String codiceFiscale;
	private String email;
	private String indirizzo;

	public AssociazioneConcreta() {
	}

	public AssociazioneConcreta(int id, String nome, String codiceFiscale, String email, String indirizzo) {
		this.id = id;
		this.nome = nome;
		this.codiceFiscale = codiceFiscale;
		this.email = email;
		this.indirizzo = indirizzo;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public String getCodiceFiscale() {
		return codiceFiscale;
	}

	public void setCodiceFiscale(String codiceFiscale) {
		this.codiceFiscale = codiceFiscale;
	}

	@Override
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getIndirizzo() {
		return indirizzo;
	}

	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	@Override
	public Set<String> getTipi() {
		return Collections.emptySet();
	}
}
