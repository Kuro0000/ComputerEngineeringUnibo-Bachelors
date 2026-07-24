package it.unibo.tw.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.unibo.tw.web.db.PersistenceException;
import it.unibo.tw.web.model.Costanti;
import it.unibo.tw.web.model.Utente;

/**
 * Aggiunta di uno strumento o stile praticato al profilo dell'Utente (R7F).
 * Le competenze sono scelte tra quelle supportate dal sistema (R8F/R9F).
 */
public class ProfiloServlet extends ControllerPersistenza {

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

		String valore = request.getParameter("valore");
		if (valore == null || (!Costanti.isStrumentoValido(valore) && !Costanti.isStileValido(valore))) {
			response.sendRedirect("pages/profilo.jsp?errore=valore");
			return;
		}

		try {
			if (!competenze().exists(utente.getId(), valore)) {
				competenze().persist(utente.getId(), valore);
				scriviLog("AGGIUNTA_COMPETENZA utente=" + utente.getUsername() + " valore=" + valore);
			}
			// aggiorna la copia in sessione
			competenze().caricaCompetenze(utente);
			session.setAttribute("utente", utente);
			response.sendRedirect("pages/profilo.jsp?aggiunto=1");
		} catch (PersistenceException e) {
			throw new ServletException("Errore di persistenza in aggiunta competenza", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect("pages/profilo.jsp");
	}
}
