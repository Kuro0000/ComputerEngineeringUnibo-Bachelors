<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="it.unibo.tw.web.model.*" %>
<%@ page import="it.unibo.tw.web.db.*" %>
<%
	String ctx = request.getContextPath();
	Utente utente = (Utente) session.getAttribute("utente");
	if (utente == null) {
		response.sendRedirect(ctx + "/pages/login.jsp");
		return;
	}

	List<Associazione> elenco = new ArrayList<Associazione>();
	List<Integer> idAttive = new ArrayList<Integer>();
	List<RichiestaIscrizione> daApprovare = new ArrayList<RichiestaIscrizione>();
	List<RichiestaIscrizione> mieRichieste = new ArrayList<RichiestaIscrizione>();
	Map<Integer,String> statoPerAssoc = new HashMap<Integer,String>();
	try {
		elenco = Associazioni.getInstance().getTutte();
		DataSource ds = new DataSource(DataSource.MYSQL);
		IscrizioneRepository ir = new IscrizioneRepository(ds);
		idAttive = ir.readAssociazioniAttiveIdByUtente(utente.getId());
		daApprovare = ir.readRichiestePerPresidente(utente.getId());
		mieRichieste = ir.readRichiesteUtente(utente.getId());
		for (RichiestaIscrizione r : mieRichieste) { statoPerAssoc.put(r.getIdAssociazione(), r.getStato()); }
	} catch (Exception e) {
		throw new javax.servlet.ServletException("Errore caricamento home", e);
	}

	String creata = request.getParameter("creata");
	String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="it">
<head>
	<meta charset="UTF-8">
	<title>Home - MusixConnection</title>
	<link rel="stylesheet" href="<%= ctx %>/styles/musix.css">
</head>
<body>
	<div class="topbar">
		<span>Ciao, <b><%= utente.getUsername() %></b></span>
		<span>
			<a href="<%= ctx %>/pages/profilo.jsp">Il mio profilo</a> &nbsp;|&nbsp;
			<a href="<%= ctx %>/Logout">Logout</a>
		</span>
	</div>

	<div style="text-align:center">
		<img src="<%= ctx %>/images/logo.png" class="logo" alt="MusixConnection">
	</div>

	<% if (creata != null) { %><div class="msg-ok" style="max-width:760px;margin:10px auto;">Associazione creata! Sei il Presidente.</div><% } %>
	<% if ("approvata".equals(msg)) { %><div class="msg-ok" style="max-width:760px;margin:10px auto;">Richiesta approvata.</div><% } %>
	<% if ("rifiutata".equals(msg)) { %><div class="msg-ok" style="max-width:760px;margin:10px auto;">Richiesta rifiutata.</div><% } %>

	<!-- ===== Cruscotto richieste per il Presidente ===== -->
	<% if (!daApprovare.isEmpty()) { %>
		<div class="card wide">
			<div class="section" style="margin-top:0">
				<h3>Richieste di iscrizione da valutare</h3>
				<table>
					<tr><th>Associazione</th><th>Utente</th><th>Descrizione</th><th>Azioni</th></tr>
					<% for (RichiestaIscrizione r : daApprovare) { %>
						<tr>
							<td><%= r.getNomeAssociazione() %></td>
							<td><%= r.getUsername() %></td>
							<td><%= r.getDescrizione() != null ? r.getDescrizione() : "" %></td>
							<td>
								<form method="post" action="<%= ctx %>/Associazione" style="display:inline">
									<input type="hidden" name="action" value="approva">
									<input type="hidden" name="idAssociazione" value="<%= r.getIdAssociazione() %>">
									<input type="hidden" name="targetUser" value="<%= r.getIdUtente() %>">
									<input type="hidden" name="ritorno" value="home">
									<button type="submit" class="dark">Accetta</button>
								</form>
								<form method="post" action="<%= ctx %>/Associazione" style="display:inline">
									<input type="hidden" name="action" value="rifiuta">
									<input type="hidden" name="idAssociazione" value="<%= r.getIdAssociazione() %>">
									<input type="hidden" name="targetUser" value="<%= r.getIdUtente() %>">
									<input type="hidden" name="ritorno" value="home">
									<button type="submit" class="dark">Rifiuta</button>
								</form>
							</td>
						</tr>
					<% } %>
				</table>
			</div>
		</div>
	<% } %>

	<!-- ===== Le mie associazioni ===== -->
	<h2 style="text-align:center;color:white">Le mie associazioni</h2>
	<div class="grid">
		<%
			boolean almenoUnaMia = false;
			for (Associazione a : elenco) {
				if (idAttive.contains(a.getId())) {
					almenoUnaMia = true;
		%>
				<a class="assoc" href="<%= ctx %>/pages/associazione.jsp?id=<%= a.getId() %>">
					<h3><%= a.getNome() %></h3>
					<p><% for (String t : a.getTipi()) { %><span class="tag"><%= t %></span><% } %></p>
				</a>
		<%	}
			}
			if (!almenoUnaMia) { %>
			<p style="color:white">Non sei ancora iscritto ad alcuna associazione.</p>
		<% } %>
	</div>

	<!-- ===== Altre associazioni ===== -->
	<h2 style="text-align:center;color:white">Altre associazioni</h2>
	<div class="grid">
		<%
			boolean almenoUnAltra = false;
			for (Associazione a : elenco) {
				if (!idAttive.contains(a.getId())) {
					almenoUnAltra = true;
					String st = statoPerAssoc.get(a.getId());
		%>
				<a class="assoc" href="<%= ctx %>/pages/associazione.jsp?id=<%= a.getId() %>">
					<h3><%= a.getNome() %></h3>
					<p><% for (String t : a.getTipi()) { %><span class="tag"><%= t %></span><% } %></p>
					<% if (IscrizioneRepository.STATO_IN_ATTESA.equals(st)) { %>
						<p><span class="tag">Richiesta in attesa</span></p>
					<% } else if (IscrizioneRepository.STATO_RIFIUTATA.equals(st)) { %>
						<p><span class="tag">Richiesta rifiutata</span></p>
					<% } %>
				</a>
		<%	}
			}
			if (!almenoUnAltra) { %>
			<p style="color:white">Nessun'altra associazione disponibile.</p>
		<% } %>
	</div>

	<!-- ===== Le mie richieste (R12F) ===== -->
	<% if (!mieRichieste.isEmpty()) { %>
		<div class="card wide">
			<div class="section" style="margin-top:0">
				<h3>Le mie richieste di iscrizione</h3>
				<table>
					<tr><th>Associazione</th><th>Stato</th></tr>
					<% for (RichiestaIscrizione r : mieRichieste) {
							String label = r.getStato();
							if (IscrizioneRepository.STATO_ATTIVA.equals(r.getStato())) label = "Accettata (iscritto)";
							else if (IscrizioneRepository.STATO_IN_ATTESA.equals(r.getStato())) label = "In attesa";
							else if (IscrizioneRepository.STATO_RIFIUTATA.equals(r.getStato())) label = "Rifiutata";
					%>
						<tr><td><%= r.getNomeAssociazione() %></td><td><%= label %></td></tr>
					<% } %>
				</table>
			</div>
		</div>
	<% } %>

	<div style="text-align:center;margin-top:10px">
		<a class="btn" href="<%= ctx %>/pages/aggiungi_associazione.jsp">+ Aggiungi Associazione</a>
	</div>
</body>
</html>
