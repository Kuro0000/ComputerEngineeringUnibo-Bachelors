package it.unibo.tw.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.unibo.tw.web.db.PersistenceException;
import it.unibo.tw.web.model.Utente;

/**
 * Registrazione di un nuovo Utente (R1F): richiede Username e Password.
 * Lo Username deve essere univoco nel sistema (R02NF).
 */
public class RegistrazioneServlet extends ControllerPersistenza {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
			response.sendRedirect("pages/registrazione.jsp?errore=campi");
			return;
		}
		username = username.trim();

		try {
			if (utenti().existsUsername(username)) {
				response.sendRedirect("pages/registrazione.jsp?errore=esistente");
				return;
			}
			Utente u = new Utente();
			u.setUsername(username);
			u.setPassword(password);
			utenti().persist(u);
			scriviLog("REGISTRAZIONE utente=" + username);
			response.sendRedirect("pages/login.jsp?registrato=1");
		} catch (PersistenceException e) {
			throw new ServletException("Errore di persistenza in registrazione", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect("pages/registrazione.jsp");
	}
}
