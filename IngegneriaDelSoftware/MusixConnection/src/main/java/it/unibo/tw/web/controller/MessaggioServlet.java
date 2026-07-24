package it.unibo.tw.web.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.unibo.tw.web.db.PersistenceException;
import it.unibo.tw.web.model.Messaggio;
import it.unibo.tw.web.model.SistemaMessaggistica;
import it.unibo.tw.web.model.Utente;

/**
 * Invio di un messaggio nella bacheca/chat di un'associazione (R23F/R25F).
 *
 * <p>Conformemente al prototipo, lo scambio avviene SENZA persistenza e senza
 * notifiche push: il messaggio viene depositato nel {@link SistemaMessaggistica}
 * in memoria. Solo gli iscritti all'associazione possono inviare messaggi (R23F).</p>
 */
public class MessaggioServlet extends ControllerPersistenza {

	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter FMT_ORA = DateTimeFormatter.ofPattern("HH:mm");

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Utente utente = (session != null) ? (Utente) session.getAttribute("utente") : null;
		if (utente == null) {
			response.sendRedirect("pages/login.jsp");
			return;
		}

		int idAssoc = parseInt(request.getParameter("idAssociazione"));
		String corpo = request.getParameter("corpo");

		try {
			// R23F: solo gli iscritti possono inviare messaggi
			if (idAssoc <= 0 || !iscrizioni().isMembroAttivo(utente.getId(), idAssoc)) {
				response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "&errore=noniscritto");
				return;
			}
			if (corpo != null && !corpo.trim().isEmpty()) {
				LocalDateTime now = LocalDateTime.now();
				Messaggio m = new Messaggio(utente.getUsername(), corpo.trim(),
						now.format(FMT_DATA), now.format(FMT_ORA));
				SistemaMessaggistica.getInstance().invia(idAssoc, m);
				scriviLog("MESSAGGIO assoc=" + idAssoc + " mittente=" + utente.getUsername());
			}
			response.sendRedirect("pages/associazione.jsp?id=" + idAssoc + "#chat");
		} catch (PersistenceException e) {
			throw new ServletException("Errore di persistenza in MessaggioServlet", e);
		}
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
