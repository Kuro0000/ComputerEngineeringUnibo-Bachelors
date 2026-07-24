package it.unibo.tw.web.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.unibo.tw.web.db.IscrizioneRepository;
import it.unibo.tw.web.db.PersistenceException;
import it.unibo.tw.web.model.Associazione;
import it.unibo.tw.web.model.Costanti;
import it.unibo.tw.web.model.Evento;
import it.unibo.tw.web.model.Utente;

/**
 * Controller delle operazioni sulle Associazioni. In base al parametro {@code action}:
 * <ul>
 *   <li>create: registrazione di una nuova associazione; l'utente ne diventa Presidente
 *       (R3F/R4F/R6F). Validazione CF locale e provvisoria.</li>
 *   <li>richiedi: presentazione di una richiesta di iscrizione (R10F); non e' ammessa
 *       verso associazioni di cui si e' gia' Presidente (R5F).</li>
 *   <li>approva / rifiuta: il Presidente accetta o rifiuta una richiesta (R15F).</li>
 *   <li>confirmEvent: conferma presenza ad un evento futuro (R35F), una sola volta (R34F);
 *       per le Lezioni solo se si pratica lo strumento/stile della lezione (R34F).</li>
 *   <li>nominate: nomina di un iscritto a Insegnante di una materia praticata e coerente
 *       con la tipologia dell'associazione (R26F/R30F/R37F).</li>
 * </ul>
 */
public class AssociazioneServlet extends ControllerPersistenza {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;
		if (utente == null) {
			response.sendRedirect("pages/login.jsp");
			return;
		}

		String action = request.getParameter("action");
		try {
			if ("create".equals(action)) {
				creaAssociazione(request, response, utente);
			} else if ("richiedi".equals(action)) {
				richiediIscrizione(request, response, utente);
			} else if ("approva".equals(action)) {
				valutaRichiesta(request, response, utente, IscrizioneRepository.STATO_ATTIVA);
			} else if ("rifiuta".equals(action)) {
				valutaRichiesta(request, response, utente, IscrizioneRepository.STATO_RIFIUTATA);
			} else if ("confirmEvent".equals(action)) {
				confermaPresenza(request, response, utente);
			} else if ("nominate".equals(action)) {
				nominaInsegnante(request, response, utente);
			} else {
				response.sendRedirect("pages/home.jsp");
			}
		} catch (PersistenceException e) {
			throw new ServletException("Errore di persistenza in AssociazioneServlet", e);
		}
	}

	private void creaAssociazione(HttpServletRequest request, HttpServletResponse response, Utente utente)
			throws PersistenceException, IOException {
		String nome = trim(request.getParameter("nome"));
		String cf = trim(request.getParameter("cf"));
		String email = trim(request.getParameter("email"));
		String indirizzo = trim(request.getParameter("indirizzo"));
		String[] tipiParam = request.getParameterValues("tipo");

		Set<String> tipi = new HashSet<String>();
		if (tipiParam != null) {
			for (String t : tipiParam) {
				if (Costanti.TIPO_STRUMENTO.equals(t) || Costanti.TIPO_DANZA.equals(t)) {
					tipi.add(t);
				}
			}
		}

		if (nome.isEmpty() || tipi.isEmpty() || !cfValido(cf)) {
			response.sendRedirect("pages/aggiungi_associazione.jsp?errore=dati");
			return;
		}
		if (associazioni().existsCf(cf)) {
			response.sendRedirect("pages/aggiungi_associazione.jsp?errore=cf");
			return;
		}

		int idAssoc = associazioni().persist(nome, cf, email, indirizzo, tipi);
		// L'utente diventa Presidente dell'associazione appena creata (R6F)
		iscrizioni().persist(utente.getId(), idAssoc, true, IscrizioneRepository.STATO_ATTIVA);
		scriviLog("CREAZIONE_ASSOCIAZIONE nome=" + nome + " cf=" + cf + " presidente=" + utente.getUsername());
		response.sendRedirect("pages/home.jsp?creata=1");
	}

	private void richiediIscrizione(HttpServletRequest request, HttpServletResponse response, Utente utente)
			throws PersistenceException, IOException {
		int idAssoc = parseInt(request.getParameter("idAssociazione"));
		if (idAssoc <= 0) {
			response.sendRedirect("pages/home.jsp");
			return;
		}
		// R5F: non si puo' richiedere l'iscrizione ad associazioni di cui si e' Presidente
		if (iscrizioni().isPresidente(utente.getId(), idAssoc)) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=presidente");
			return;
		}
		String descr = trim(request.getParameter("descrizione"));
		iscrizioni().richiedi(utente.getId(), idAssoc, descr.isEmpty() ? null : descr);
		scriviLog("RICHIESTA_ISCRIZIONE utente=" + utente.getUsername() + " assoc=" + idAssoc);
		response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&msg=richiesta");
	}

	private void valutaRichiesta(HttpServletRequest request, HttpServletResponse response, Utente utente,
			String nuovoStato) throws PersistenceException, IOException {
		int idAssoc = parseInt(request.getParameter("idAssociazione"));
		int idTarget = parseInt(request.getParameter("targetUser"));
		boolean ritornoHome = "home".equals(request.getParameter("ritorno"));

		// R15F: solo il Presidente dell'associazione puo' valutare le richieste
		if (!iscrizioni().isPresidente(utente.getId(), idAssoc)) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=nonpresidente");
			return;
		}
		// si valuta solo una richiesta effettivamente in attesa
		if (IscrizioneRepository.STATO_IN_ATTESA.equals(iscrizioni().getStato(idTarget, idAssoc))) {
			iscrizioni().setStato(idTarget, idAssoc, nuovoStato);
			scriviLog("VALUTA_RICHIESTA assoc=" + idAssoc + " utente=" + idTarget + " esito=" + nuovoStato);
		}
		String esito = IscrizioneRepository.STATO_ATTIVA.equals(nuovoStato) ? "approvata" : "rifiutata";
		if (ritornoHome) {
			response.sendRedirect("pages/home.jsp?msg=" + esito);
		} else {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&msg=" + esito);
		}
	}

	private void confermaPresenza(HttpServletRequest request, HttpServletResponse response, Utente utente)
			throws PersistenceException, IOException {
		int idAssoc = parseInt(request.getParameter("idAssociazione"));
		int idEvento = parseInt(request.getParameter("idEvento"));
		Evento evento = eventi().getById(idEvento);

		if (evento == null) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=evento");
			return;
		}
		// R22F: solo iscritti ATTIVI
		if (!iscrizioni().isMembroAttivo(utente.getId(), idAssoc)) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=noniscritto");
			return;
		}
		// R35F: solo eventi futuri
		if (!evento.isFuturo()) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=passato");
			return;
		}
		// R34F: per le Lezioni, solo se si pratica lo strumento/stile della lezione
		if (evento.isLezione()) {
			Utente u = new Utente();
			u.setId(utente.getId());
			competenze().caricaCompetenze(u);
			if (!u.praticaStrumentoOStile(evento.getStrumentoStile())) {
				response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=competenza");
				return;
			}
		}
		// R34F: una sola conferma
		if (!presenze().exists(utente.getId(), idEvento)) {
			presenze().persist(utente.getId(), idEvento);
			scriviLog("CONFERMA_PRESENZA utente=" + utente.getUsername() + " evento=" + idEvento);
		}
		response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&msg=presenza");
	}

	private void nominaInsegnante(HttpServletRequest request, HttpServletResponse response, Utente utente)
			throws PersistenceException, IOException {
		int idAssoc = parseInt(request.getParameter("idAssociazione"));
		int idTarget = parseInt(request.getParameter("targetUser"));
		String materia = trim(request.getParameter("materia"));

		// Solo il Presidente puo' nominare (R15F/R26F)
		if (!iscrizioni().isPresidente(utente.getId(), idAssoc)) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=nonpresidente");
			return;
		}
		int idIscrizione = iscrizioni().getIdAttiva(idTarget, idAssoc);
		if (idIscrizione == -1 || materia.isEmpty()) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=nomina");
			return;
		}
		// R30F: solo per strumenti/stili effettivamente praticati dall'iscritto
		Utente target = new Utente();
		target.setId(idTarget);
		competenze().caricaCompetenze(target);
		if (!target.praticaStrumentoOStile(materia)) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=praticata");
			return;
		}
		// R30F: tipologia dell'associazione coerente con la materia
		Associazione a = trovaAssociazione(idAssoc);
		boolean materiaStrumento = Costanti.isStrumentoValido(materia);
		if (a == null
				|| (materiaStrumento && !a.isStrumento())
				|| (!materiaStrumento && !a.isDanza())) {
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=tipologia");
			return;
		}
		// R37F: non nominare due volte per la stessa materia
		if (!insegnamenti().exists(idIscrizione, materia)) {
			insegnamenti().persist(idIscrizione, materia);
			scriviLog("NOMINA_INSEGNANTE assoc=" + idAssoc + " iscritto=" + idTarget + " materia=" + materia);
		}
		response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&msg=insegnante");
	}

	// --- helper -----------------------------------------------------------------------

	private Associazione trovaAssociazione(int idAssoc) throws PersistenceException {
		return it.unibo.tw.web.model.Associazioni.getInstance().getById(idAssoc);
	}

	/**
	 * Validazione locale e provvisoria del Codice Fiscale di un'associazione
	 * (nel prototipo NON si interroga il sistema esterno): 11 cifre numeriche
	 * (P.IVA/CF enti) oppure 16 caratteri alfanumerici.
	 */
	private boolean cfValido(String cf) {
		if (cf == null) {
			return false;
		}
		cf = cf.trim().toUpperCase();
		return cf.matches("[0-9]{11}") || cf.matches("[A-Z0-9]{16}");
	}

	private static String trim(String s) {
		return (s == null) ? "" : s.trim();
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
