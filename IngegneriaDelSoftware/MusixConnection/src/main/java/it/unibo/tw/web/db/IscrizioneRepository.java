package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.unibo.tw.web.model.Iscrizione;
import it.unibo.tw.web.model.IscrizioneConcreta;
import it.unibo.tw.web.model.Presidente;
import it.unibo.tw.web.model.RichiestaIscrizione;

/**
 * Persistenza "forza bruta" dell'Iscrizione di un Utente ad un'Associazione.
 *
 * <p>Il campo {@code stato} modella il flusso di approvazione (R15F):
 * IN_ATTESA (richiesta presentata), ATTIVA (iscritto a tutti gli effetti),
 * RIFIUTATA. Il flag {@code presidente} indica il ruolo di Presidente (R6F, R01D).</p>
 */
public class IscrizioneRepository {

	private DataSource dataSource;

	public static final String STATO_ATTIVA = "ATTIVA";
	public static final String STATO_IN_ATTESA = "IN_ATTESA";
	public static final String STATO_RIFIUTATA = "RIFIUTATA";

	private static final String TABLE = "iscrizione";
	private static final String ID = "id";
	private static final String ID_UTENTE = "idUtente";
	private static final String ID_ASSOC = "idAssociazione";
	private static final String PRESIDENTE = "presidente";
	private static final String STATO = "stato";
	private static final String DESCRIZIONE = "descrizione";

	private static final String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
			ID + " INT NOT NULL AUTO_INCREMENT, " +
			ID_UTENTE + " INT NOT NULL, " +
			ID_ASSOC + " INT NOT NULL, " +
			PRESIDENTE + " TINYINT(1) NOT NULL DEFAULT 0, " +
			STATO + " VARCHAR(20) NOT NULL DEFAULT '" + STATO_ATTIVA + "', " +
			DESCRIZIONE + " VARCHAR(300), " +
			"PRIMARY KEY (" + ID + "), " +
			"CONSTRAINT uq_iscrizione UNIQUE (" + ID_UTENTE + ", " + ID_ASSOC + "), " +
			"CONSTRAINT fk_iscr_utente FOREIGN KEY (" + ID_UTENTE + ") REFERENCES utente(id), " +
			"CONSTRAINT fk_iscr_assoc FOREIGN KEY (" + ID_ASSOC + ") REFERENCES associazione(id) " +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String drop = "DROP TABLE " + TABLE;

	private static final String insert = "INSERT INTO " + TABLE + " ( " +
			ID_UTENTE + ", " + ID_ASSOC + ", " + PRESIDENTE + ", " + STATO + ", " + DESCRIZIONE +
			" ) VALUES (?,?,?,?,?) ";

	private static final String select_one = "SELECT " + ID + ", " + STATO + ", " + PRESIDENTE +
			" FROM " + TABLE + " WHERE " + ID_UTENTE + " = ? AND " + ID_ASSOC + " = ? ";

	private static final String update_stato = "UPDATE " + TABLE + " SET " + STATO +
			" = ? WHERE " + ID_UTENTE + " = ? AND " + ID_ASSOC + " = ? ";

	private static final String update_stato_descr = "UPDATE " + TABLE + " SET " + STATO +
			" = ?, " + DESCRIZIONE + " = ? WHERE " + ID_UTENTE + " = ? AND " + ID_ASSOC + " = ? ";

	// iscritti (per stato) di un'associazione, con username
	private static final String read_by_assoc_stato =
			"SELECT i." + ID_UTENTE + ", u.username, i." + PRESIDENTE +
			" FROM " + TABLE + " i JOIN utente u ON i." + ID_UTENTE + " = u.id " +
			" WHERE i." + ID_ASSOC + " = ? AND i." + STATO + " = ? ";

	// richieste (per stato) di una specifica associazione
	private static final String read_richieste_assoc =
			"SELECT r." + ID_UTENTE + ", u.username, r." + ID_ASSOC + ", a.nome, r." + DESCRIZIONE +
			" FROM " + TABLE + " r JOIN utente u ON r." + ID_UTENTE + " = u.id " +
			" JOIN associazione a ON r." + ID_ASSOC + " = a.id " +
			" WHERE r." + ID_ASSOC + " = ? AND r." + STATO + " = ? ";

	// richieste in attesa per tutte le associazioni di cui l'utente e' Presidente
	private static final String read_richieste_presidente =
			"SELECT r." + ID_UTENTE + ", u.username, r." + ID_ASSOC + ", a.nome, r." + DESCRIZIONE +
			" FROM " + TABLE + " r JOIN utente u ON r." + ID_UTENTE + " = u.id " +
			" JOIN associazione a ON r." + ID_ASSOC + " = a.id " +
			" WHERE r." + STATO + " = '" + STATO_IN_ATTESA + "' AND r." + ID_ASSOC + " IN ( " +
			"   SELECT p." + ID_ASSOC + " FROM " + TABLE + " p WHERE p." + ID_UTENTE + " = ? " +
			"   AND p." + PRESIDENTE + " = 1 AND p." + STATO + " = '" + STATO_ATTIVA + "' ) " +
			" ORDER BY a.nome ";

	// tutte le richieste presentate da un utente (per la sua visualizzazione, R12F)
	private static final String read_richieste_utente =
			"SELECT r." + ID_UTENTE + ", u.username, r." + ID_ASSOC + ", a.nome, r." + DESCRIZIONE + ", r." + STATO +
			" FROM " + TABLE + " r JOIN utente u ON r." + ID_UTENTE + " = u.id " +
			" JOIN associazione a ON r." + ID_ASSOC + " = a.id " +
			" WHERE r." + ID_UTENTE + " = ? AND r." + PRESIDENTE + " = 0 ORDER BY a.nome ";

	// id associazioni a cui l'utente e' iscritto ATTIVO
	private static final String read_attive_by_utente = "SELECT " + ID_ASSOC + " FROM " + TABLE +
			" WHERE " + ID_UTENTE + " = ? AND " + STATO + " = '" + STATO_ATTIVA + "' ";

	public IscrizioneRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void dropTable() throws PersistenceException {
		Connection conn = this.dataSource.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(drop);
		} catch (SQLException e) {
			// la tabella non esiste
		} finally {
			close(stmt, conn);
		}
	}

	public void createTable() throws PersistenceException {
		Connection connection = this.dataSource.getConnection();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.executeUpdate(create);
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Stato dell'iscrizione (ATTIVA/IN_ATTESA/RIFIUTATA), oppure null se assente. */
	public String getStato(int idUtente, int idAssociazione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(select_one);
			statement.setInt(1, idUtente);
			statement.setInt(2, idAssociazione);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(STATO);
			}
			return null;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Inserisce una iscrizione con stato e ruolo espliciti (usata per creazione/seed). */
	public void persist(int idUtente, int idAssociazione, boolean presidente, String stato)
			throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(insert);
			statement.setInt(1, idUtente);
			statement.setInt(2, idAssociazione);
			statement.setInt(3, presidente ? 1 : 0);
			statement.setString(4, stato);
			statement.setString(5, null);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/**
	 * Presenta una richiesta di iscrizione (R10F): inserisce una nuova richiesta
	 * IN_ATTESA, oppure ripristina a IN_ATTESA una richiesta precedentemente
	 * RIFIUTATA. Non fa nulla se l'utente e' gia' iscritto o ha gia' una richiesta in attesa.
	 */
	public void richiedi(int idUtente, int idAssociazione, String descrizione) throws PersistenceException {
		String stato = getStato(idUtente, idAssociazione);
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			if (stato == null) {
				statement = connection.prepareStatement(insert);
				statement.setInt(1, idUtente);
				statement.setInt(2, idAssociazione);
				statement.setInt(3, 0);
				statement.setString(4, STATO_IN_ATTESA);
				statement.setString(5, descrizione);
				statement.executeUpdate();
			} else if (STATO_RIFIUTATA.equals(stato)) {
				statement = connection.prepareStatement(update_stato_descr);
				statement.setString(1, STATO_IN_ATTESA);
				statement.setString(2, descrizione);
				statement.setInt(3, idUtente);
				statement.setInt(4, idAssociazione);
				statement.executeUpdate();
			}
			// altrimenti (IN_ATTESA o ATTIVA): nessuna azione
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Cambia lo stato di una iscrizione (approvazione/rifiuto da parte del Presidente). */
	public void setStato(int idUtente, int idAssociazione, String stato) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(update_stato);
			statement.setString(1, stato);
			statement.setInt(2, idUtente);
			statement.setInt(3, idAssociazione);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Id dell'iscrizione ATTIVA (per il legame con gli insegnamenti), -1 se assente. */
	public int getIdAttiva(int idUtente, int idAssociazione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(select_one);
			statement.setInt(1, idUtente);
			statement.setInt(2, idAssociazione);
			ResultSet rs = statement.executeQuery();
			if (rs.next() && STATO_ATTIVA.equals(rs.getString(STATO))) {
				return rs.getInt(ID);
			}
			return -1;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	public boolean isMembroAttivo(int idUtente, int idAssociazione) throws PersistenceException {
		return STATO_ATTIVA.equals(getStato(idUtente, idAssociazione));
	}

	public boolean isPresidente(int idUtente, int idAssociazione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(select_one);
			statement.setInt(1, idUtente);
			statement.setInt(2, idAssociazione);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getInt(PRESIDENTE) == 1 && STATO_ATTIVA.equals(rs.getString(STATO));
			}
			return false;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Iscritti ATTIVI di un'associazione (con eventuale decoratore Presidente). */
	public List<Iscrizione> readMembri(int idAssociazione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<Iscrizione> result = new ArrayList<Iscrizione>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_by_assoc_stato);
			statement.setInt(1, idAssociazione);
			statement.setString(2, STATO_ATTIVA);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Iscrizione i = new IscrizioneConcreta(rs.getInt(ID_UTENTE), rs.getString("username"), idAssociazione);
				if (rs.getInt(PRESIDENTE) == 1) {
					i = new Presidente(i);
				}
				result.add(i);
			}
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	private RichiestaIscrizione leggiRichiesta(ResultSet rs, boolean conStato) throws SQLException {
		return new RichiestaIscrizione(
				rs.getInt(ID_UTENTE), rs.getString("username"), rs.getInt(ID_ASSOC),
				rs.getString("nome"), rs.getString(DESCRIZIONE),
				conStato ? rs.getString(STATO) : STATO_IN_ATTESA);
	}

	/** Richieste in attesa per una specifica associazione. */
	public List<RichiestaIscrizione> readRichiestePendenti(int idAssociazione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<RichiestaIscrizione> result = new ArrayList<RichiestaIscrizione>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_richieste_assoc);
			statement.setInt(1, idAssociazione);
			statement.setString(2, STATO_IN_ATTESA);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(leggiRichiesta(rs, false));
			}
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Richieste in attesa per tutte le associazioni di cui l'utente e' Presidente. */
	public List<RichiestaIscrizione> readRichiestePerPresidente(int idPresidente) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<RichiestaIscrizione> result = new ArrayList<RichiestaIscrizione>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_richieste_presidente);
			statement.setInt(1, idPresidente);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(leggiRichiesta(rs, false));
			}
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Tutte le richieste presentate da un utente, con il relativo stato (R12F). */
	public List<RichiestaIscrizione> readRichiesteUtente(int idUtente) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<RichiestaIscrizione> result = new ArrayList<RichiestaIscrizione>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_richieste_utente);
			statement.setInt(1, idUtente);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(leggiRichiesta(rs, true));
			}
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	/** Id delle associazioni a cui l'utente e' iscritto ATTIVO. */
	public List<Integer> readAssociazioniAttiveIdByUtente(int idUtente) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<Integer> result = new ArrayList<Integer>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_attive_by_utente);
			statement.setInt(1, idUtente);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt(ID_ASSOC));
			}
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			close(statement, connection);
		}
	}

	private void close(Statement statement, Connection connection) throws PersistenceException {
		try {
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
	}
}
