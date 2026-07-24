package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistenza "forza bruta" delle conferme di presenza di un Utente agli Eventi (R22F).
 * Il vincolo di unicita' garantisce una sola conferma per evento (R34F).
 */
public class PresenzaRepository {

	private DataSource dataSource;

	private static final String TABLE = "presenza";
	private static final String ID = "id";
	private static final String ID_UTENTE = "idUtente";
	private static final String ID_EVENTO = "idEvento";

	private static final String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
			ID + " INT NOT NULL AUTO_INCREMENT, " +
			ID_UTENTE + " INT NOT NULL, " +
			ID_EVENTO + " INT NOT NULL, " +
			"PRIMARY KEY (" + ID + "), " +
			"CONSTRAINT uq_presenza UNIQUE (" + ID_UTENTE + ", " + ID_EVENTO + "), " +
			"CONSTRAINT fk_pres_utente FOREIGN KEY (" + ID_UTENTE + ") REFERENCES utente(id), " +
			"CONSTRAINT fk_pres_evento FOREIGN KEY (" + ID_EVENTO + ") REFERENCES evento(id) " +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String drop = "DROP TABLE " + TABLE;

	private static final String insert = "INSERT INTO " + TABLE + " ( " +
			ID_UTENTE + ", " + ID_EVENTO + " ) VALUES (?,?) ";

	private static final String exists_query = "SELECT " + ID + " FROM " + TABLE +
			" WHERE " + ID_UTENTE + " = ? AND " + ID_EVENTO + " = ? ";

	private static final String read_eventi_by_utente = "SELECT " + ID_EVENTO + " FROM " + TABLE +
			" WHERE " + ID_UTENTE + " = ? ";

	public PresenzaRepository(DataSource dataSource) {
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

	public boolean exists(int idUtente, int idEvento) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(exists_query);
			statement.setInt(1, idUtente);
			statement.setInt(2, idEvento);
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

	public void persist(int idUtente, int idEvento) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(insert);
			statement.setInt(1, idUtente);
			statement.setInt(2, idEvento);
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

	public List<Integer> readEventiByUtente(int idUtente) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<Integer> result = new ArrayList<>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_eventi_by_utente);
			statement.setInt(1, idUtente);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt(ID_EVENTO));
			}
			return result;
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
