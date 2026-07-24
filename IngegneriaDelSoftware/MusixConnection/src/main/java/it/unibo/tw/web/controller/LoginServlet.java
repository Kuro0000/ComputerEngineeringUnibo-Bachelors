package it.unibo.tw.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.unibo.tw.web.db.PersistenceException;
import it.unibo.tw.web.model.Utente;

/**
 * Autenticazione dell'Utente (R2F): verifica la coppia Username/Password e,
 * se valida, apre la sessione.
 */
public class LoginServlet extends ControllerPersistenza {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		try {
			Utente u = (username != null) ? utenti().findByUsername(username.trim()) : null;
			if (u != null && u.getPassword().equals(password)) {
				competenze().caricaCompetenze(u);
				HttpSession session = request.getSession(true);
				session.setAttribute("utente", u);
				session.setAttribute("username", u.getUsername());
				scriviLog("LOGIN utente=" + u.getUsername());
				response.sendRedirect("pages/home.jsp");
			} else {
				response.sendRedirect("pages/login.jsp?errore=1");
			}
		} catch (PersistenceException e) {
			throw new ServletException("Errore di persistenza in login", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect("pages/login.jsp");
	}
}
