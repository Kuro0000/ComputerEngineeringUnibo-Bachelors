package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.unibo.tw.web.model.Costanti;
import it.unibo.tw.web.model.Utente;

/**
 * Persistenza "forza bruta" delle competenze di un Utente: strumenti e stili
 * praticati (R7F). Il campo {@code tipo} distingue "Strumento" da "Stile".
 */
public class CompetenzaRepository {

	private DataSource dataSource;

	private static final String TABLE = "competenza";
	private static final String ID = "id";
	private static final String ID_UTENTE = "idUtente";
	private static final String VALORE = "valore";
	private static final String TIPO = "tipo"; // "Strumento" | "Stile"

	public static final String TIPO_STRUMENTO = "Strumento";
	public static final String TIPO_STILE = "Stile";

	private static final String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
			ID + " INT NOT NULL AUTO_INCREMENT, " +
			ID_UTENTE + " INT NOT NULL, " +
			VALORE + " VARCHAR(50) NOT NULL, " +
			TIPO + " VARCHAR(20) NOT NULL, " +
			"PRIMARY KEY (" + ID + "), " +
			"CONSTRAINT uq_competenza UNIQUE (" + ID_UTENTE + ", " + VALORE + "), " +
			"CONSTRAINT fk_competenza_utente FOREIGN KEY (" + ID_UTENTE + ") REFERENCES utente(id) " +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String drop = "DROP TABLE " + TABLE;

	private static final String insert = "INSERT INTO " + TABLE + " ( " +
			ID_UTENTE + ", " + VALORE + ", " + TIPO + " ) VALUES (?,?,?) ";

	private static final String read_by_utente = "SELECT " + VALORE + ", " + TIPO +
			" FROM " + TABLE + " WHERE " + ID_UTENTE + " = ? ";

	private static final String exists_query = "SELECT " + ID + " FROM " + TABLE +
			" WHERE " + ID_UTENTE + " = ? AND " + VALORE + " = ? ";

	public CompetenzaRepository(DataSource dataSource) {
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
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
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

	public boolean exists(int idUtente, String valore) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(exists_query);
			statement.setInt(1, idUtente);
			statement.setString(2, valore);
			ResultSet rs = statement.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}
	}

	/** Aggiunge uno strumento o stile al profilo dell'utente. Il tipo e' dedotto dalle costanti. */
	public void persist(int idUtente, String valore) throws PersistenceException {
		String tipo = Costanti.isStrumentoValido(valore) ? TIPO_STRUMENTO : TIPO_STILE;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(insert);
			statement.setInt(1, idUtente);
			statement.setString(2, valore);
			statement.setString(3, tipo);
			statement.executeUpdate();
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}
	}

	/** Carica strumenti e stili dell'utente, valorizzandone le liste. */
	public void caricaCompetenze(Utente u) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_by_utente);
			statement.setInt(1, u.getId());
			ResultSet rs = statement.executeQuery();
			u.getStrumenti().clear();
			u.getStili().clear();
			while (rs.next()) {
				String valore = rs.getString(VALORE);
				if (TIPO_STRUMENTO.equals(rs.getString(TIPO))) {
					u.getStrumenti().add(valore);
				} else {
					u.getStili().add(valore);
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				throw new PersistenceException(e.getMessage());
			}
		}
	}
}
