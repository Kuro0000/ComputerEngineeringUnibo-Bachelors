<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="it.unibo.tw.web.model.*" %>
<%@ page import="it.unibo.tw.web.db.*" %>
<%
	String ctx = request.getContextPath();
	Utente utente = (Utente) session.getAttribute("utente");
	if (utente == null) {
		response.sendRedirect(ctx + "/pages/login.jsp");
		return;
	}

	int idAssoc;
	try { idAssoc = Integer.parseInt(request.getParameter("id")); }
	catch (Exception e) { response.sendRedirect(ctx + "/pages/home.jsp"); return; }

	Associazione assoc = null;
	List<Evento> eventi = new ArrayList<Evento>();
	List<Iscrizione> membri = new ArrayList<Iscrizione>();
	List<Integer> mieEventiConfermati = new ArrayList<Integer>();
	List<RichiestaIscrizione> richieste = new ArrayList<RichiestaIscrizione>();
	List<Messaggio> messaggi = SistemaMessaggistica.getInstance().getMessaggi(idAssoc);
	boolean iscritto = false;
	boolean presidente = false;
	String mioStato = null;

	try {
		DataSource ds = new DataSource(DataSource.MYSQL);
		assoc = Associazioni.getInstance().getById(idAssoc);
		if (assoc == null) { response.sendRedirect(ctx + "/pages/home.jsp"); return; }

		eventi = new EventoRepository(ds).readByAssociazione(idAssoc);
		IscrizioneRepository ir = new IscrizioneRepository(ds);
		membri = ir.readMembri(idAssoc);
		mieEventiConfermati = new PresenzaRepository(ds).readEventiByUtente(utente.getId());
		mioStato = ir.getStato(utente.getId(), idAssoc);
		for (Iscrizione i : membri) {
			if (i.getIdUtente() == utente.getId()) {
				iscritto = true;
				presidente = i.isPresidente();
			}
		}
		if (presidente) {
			richieste = ir.readRichiestePendenti(idAssoc);
		}
	} catch (Exception e) {
		throw new javax.servlet.ServletException("Errore caricamento associazione", e);
	}

	SimpleDateFormat fmtData = new SimpleDateFormat("dd/MM/yyyy");
	SimpleDateFormat fmtOra = new SimpleDateFormat("HH:mm");
	String msg = request.getParameter("msg");
	String errore = request.getParameter("errore");

	List<String> materie = new ArrayList<String>();
	if (assoc.isStrumento()) { for (String __m : Costanti.STRUMENTI) { materie.add(__m); } }
	if (assoc.isDanza()) { for (String __m : Costanti.STILI) { materie.add(__m); } }
%>
<!DOCTYPE html>
<html lang="it">
<head>
	<meta charset="UTF-8">
	<title><%= assoc.getNome() %> - MusixConnection</title>
	<link rel="stylesheet" href="<%= ctx %>/styles/musix.css">
</head>
<body>
	<div class="topbar">
		<a href="<%= ctx %>/pages/home.jsp">← Home</a>
		<a href="<%= ctx %>/Logout">Logout</a>
	</div>

	<div class="card wide">
		<img src="<%= ctx %>/images/logo.png" class="logo small" alt="MusixConnection">
		<h1><%= assoc.getNome() %></h1>
		<p><% for (String t : assoc.getTipi()) { %><span class="tag"><%= t %></span><% } %></p>
		<p style="color:#777">CF: <%= assoc.getCodiceFiscale() %>
			<% if (assoc.getEmail() != null && !assoc.getEmail().isEmpty()) { %> &middot; <%= assoc.getEmail() %><% } %>
			<% if (assoc.getIndirizzo() != null && !assoc.getIndirizzo().isEmpty()) { %> &middot; <%= assoc.getIndirizzo() %><% } %>
		</p>

		<% if ("richiesta".equals(msg)) { %><div class="msg-ok">Richiesta di iscrizione inviata! In attesa di approvazione.</div><% } %>
		<% if ("presenza".equals(msg)) { %><div class="msg-ok">Presenza confermata!</div><% } %>
		<% if ("insegnante".equals(msg)) { %><div class="msg-ok">Insegnante nominato.</div><% } %>
		<% if ("approvata".equals(msg)) { %><div class="msg-ok">Richiesta approvata.</div><% } %>
		<% if ("rifiutata".equals(msg)) { %><div class="msg-ok">Richiesta rifiutata.</div><% } %>
		<% if ("passato".equals(errore)) { %><div class="msg-err">Non puoi confermare la presenza ad un evento passato.</div><% } %>
		<% if ("noniscritto".equals(errore)) { %><div class="msg-err">Devi essere iscritto (e approvato) all'associazione.</div><% } %>
		<% if ("nonpresidente".equals(errore)) { %><div class="msg-err">Solo il Presidente puo' eseguire questa operazione.</div><% } %>
		<% if ("nomina".equals(errore)) { %><div class="msg-err">Nomina non valida.</div><% } %>
		<% if ("praticata".equals(errore)) { %><div class="msg-err">L'iscritto non pratica lo strumento/stile selezionato (R30F).</div><% } %>
		<% if ("tipologia".equals(errore)) { %><div class="msg-err">Materia non coerente con la tipologia dell'associazione (R30F).</div><% } %>
		<% if ("competenza".equals(errore)) { %><div class="msg-err">Puoi confermare la presenza a questa lezione solo se pratichi lo strumento/stile previsto (R34F).</div><% } %>
		<% if ("presidente".equals(errore)) { %><div class="msg-err">Non puoi iscriverti ad un'associazione di cui sei Presidente.</div><% } %>

		<!-- ===== Stato iscrizione / Richiesta ===== -->
		<% if (presidente) { %>
			<p><span class="tag">Sei il Presidente</span></p>
		<% } else if (iscritto) { %>
			<p><span class="tag">Sei iscritto</span></p>
		<% } else if (IscrizioneRepository.STATO_IN_ATTESA.equals(mioStato)) { %>
			<p><span class="tag">Richiesta inviata - in attesa di approvazione</span></p>
		<% } else { %>
			<form method="post" action="<%= ctx %>/Associazione" style="margin:15px 0">
				<input type="hidden" name="action" value="richiedi">
				<input type="hidden" name="idAssociazione" value="<%= idAssoc %>">
				<input type="text" name="descrizione" placeholder="Due righe di presentazione (facoltativo)">
				<button type="submit">
					<%= IscrizioneRepository.STATO_RIFIUTATA.equals(mioStato) ? "Ripresenta richiesta di iscrizione" : "Richiedi iscrizione" %>
				</button>
			</form>
		<% } %>

		<!-- ===== EVENTI ===== -->
		<div class="section">
			<h3>Eventi in bacheca</h3>
			<table>
				<tr><th>Data</th><th>Ora</th><th>Evento</th><th>Tipo</th><th>Azione</th></tr>
				<% if (eventi.isEmpty()) { %><tr><td colspan="5">Nessun evento pubblicato.</td></tr><% } %>
				<% for (Evento ev : eventi) {
						boolean confermato = mieEventiConfermati.contains(ev.getId());
						boolean futuro = ev.isFuturo();
						boolean praticaMateria = !ev.isLezione() || utente.praticaStrumentoOStile(ev.getStrumentoStile());
				%>
					<tr>
						<td><%= ev.getData() != null ? fmtData.format(ev.getData()) : "" %></td>
						<td><%= ev.getOra() != null ? fmtOra.format(ev.getOra()) : "" %></td>
						<td><%= ev.getTitolo() %>
							<% if (ev.isLezione() && ev.getStrumentoStile() != null) { %>
								<br><small style="color:#888"><%= ev.getStrumentoStile() %></small>
							<% } %>
						</td>
						<td><%= ev.getTipo() %></td>
						<td>
							<% if (!iscritto) { %>
								<button class="dark" disabled>Iscriviti prima</button>
							<% } else if (confermato) { %>
								<button class="dark" disabled>Presente ✓</button>
							<% } else if (!futuro) { %>
								<button class="dark" disabled>Concluso</button>
							<% } else if (!praticaMateria) { %>
								<button class="dark" disabled title="Non pratichi <%= ev.getStrumentoStile() %>">Non pratichi <%= ev.getStrumentoStile() %></button>
							<% } else { %>
								<form method="post" action="<%= ctx %>/Associazione" style="margin:0">
									<input type="hidden" name="action" value="confirmEvent">
									<input type="hidden" name="idAssociazione" value="<%= idAssoc %>">
									<input type="hidden" name="idEvento" value="<%= ev.getId() %>">
									<button type="submit" class="dark">Presente</button>
								</form>
							<% } %>
						</td>
					</tr>
				<% } %>
			</table>
		</div>

		<!-- ===== ISCRITTI ===== -->
		<div class="section">
			<h3>Iscritti</h3>
			<% for (Iscrizione i : membri) { %>
				<span class="tag"><%= i.getUsername() %><%= i.isPresidente() ? " (Presidente)" : "" %></span>
			<% } %>
		</div>

		<!-- ===== AREA PRESIDENTE ===== -->
		<% if (presidente) { %>
			<div class="section">
				<h3>Area Presidente</h3>

				<h4>Richieste di iscrizione da valutare</h4>
				<% if (richieste.isEmpty()) { %>
					<p style="color:#999">Nessuna richiesta in attesa.</p>
				<% } else { %>
					<table>
						<tr><th>Utente</th><th>Descrizione</th><th>Azioni</th></tr>
						<% for (RichiestaIscrizione r : richieste) { %>
							<tr>
								<td><%= r.getUsername() %></td>
								<td><%= r.getDescrizione() != null ? r.getDescrizione() : "" %></td>
								<td>
									<form method="post" action="<%= ctx %>/Associazione" style="display:inline">
										<input type="hidden" name="action" value="approva">
										<input type="hidden" name="idAssociazione" value="<%= idAssoc %>">
										<input type="hidden" name="targetUser" value="<%= r.getIdUtente() %>">
										<button type="submit" class="dark">Accetta</button>
									</form>
									<form method="post" action="<%= ctx %>/Associazione" style="display:inline">
										<input type="hidden" name="action" value="rifiuta">
										<input type="hidden" name="idAssociazione" value="<%= idAssoc %>">
										<input type="hidden" name="targetUser" value="<%= r.getIdUtente() %>">
										<button type="submit" class="dark">Rifiuta</button>
									</form>
								</td>
							</tr>
						<% } %>
					</table>
				<% } %>

				<h4 style="margin-top:20px">Nomina un Insegnante</h4>
				<p style="color:#999;font-size:0.9em">Solo iscritti che praticano lo strumento/stile, coerente con la tipologia dell'associazione (R30F).</p>
				<form method="post" action="<%= ctx %>/Associazione" class="inline-form">
					<input type="hidden" name="action" value="nominate">
					<input type="hidden" name="idAssociazione" value="<%= idAssoc %>">
					<select name="targetUser" required>
						<% for (Iscrizione i : membri) { %>
							<option value="<%= i.getIdUtente() %>"><%= i.getUsername() %></option>
						<% } %>
					</select>
					<select name="materia" required>
						<% for (String m : materie) { %><option value="<%= m %>"><%= m %></option><% } %>
					</select>
					<button type="submit" class="dark">Nomina</button>
				</form>
			</div>
		<% } %>

		<!-- ===== CHAT (in memoria, senza persistenza) ===== -->
		<div class="section" id="chat">
			<h3>Chat dell'associazione <small style="color:#999">(messaggi non persistiti)</small></h3>
			<div class="chat-box">
				<% if (messaggi.isEmpty()) { %><p style="color:#999">Nessun messaggio. Inizia la conversazione!</p><% } %>
				<% for (Messaggio m : messaggi) { %>
					<div class="chat-msg">
						<span class="who"><%= m.getMittente() %></span>
						<span class="meta">[<%= m.getData() %> <%= m.getOra() %>]</span><br>
						<%= m.getCorpo() %>
					</div>
				<% } %>
			</div>
			<% if (iscritto) { %>
				<form method="post" action="<%= ctx %>/Messaggio" class="inline-form" style="margin-top:10px">
					<input type="hidden" name="idAssociazione" value="<%= idAssoc %>">
					<input type="text" name="corpo" placeholder="Scrivi un messaggio..." required>
					<button type="submit" class="dark">Invia</button>
				</form>
			<% } else { %>
				<p style="color:#999">Iscriviti per partecipare alla chat.</p>
			<% } %>
		</div>
	</div>
</body>
</html>
