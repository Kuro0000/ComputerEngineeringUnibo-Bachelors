package it.unibo.tw.web.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.unibo.tw.web.model.Evento;

/**
 * Persistenza "forza bruta" degli Eventi (Esibizioni/Lezioni) pubblicati sulla
 * bacheca di un'associazione (R19F/R21F).
 */
public class EventoRepository {

	private DataSource dataSource;

	private static final String TABLE = "evento";
	private static final String ID = "id";
	private static final String ID_ASSOC = "idAssociazione";
	private static final String TIPO = "tipo";
	private static final String TITOLO = "titolo";
	private static final String DESCRIZIONE = "descrizione";
	private static final String LUOGO = "luogo";
	private static final String DATA = "data";
	private static final String ORA = "ora";
	private static final String STRUMENTO_STILE = "strumentoStile";

	private static final String create = "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
			ID + " INT NOT NULL AUTO_INCREMENT, " +
			ID_ASSOC + " INT NOT NULL, " +
			TIPO + " VARCHAR(20) NOT NULL, " +
			TITOLO + " VARCHAR(150) NOT NULL, " +
			DESCRIZIONE + " VARCHAR(500), " +
			LUOGO + " VARCHAR(200), " +
			DATA + " DATE NOT NULL, " +
			ORA + " TIME, " +
			STRUMENTO_STILE + " VARCHAR(50), " +
			"PRIMARY KEY (" + ID + "), " +
			"CONSTRAINT fk_evento_assoc FOREIGN KEY (" + ID_ASSOC + ") REFERENCES associazione(id) " +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ";

	private static final String drop = "DROP TABLE " + TABLE;

	private static final String insert = "INSERT INTO " + TABLE + " ( " +
			ID_ASSOC + ", " + TIPO + ", " + TITOLO + ", " + DESCRIZIONE + ", " + LUOGO + ", " +
			DATA + ", " + ORA + ", " + STRUMENTO_STILE + " ) VALUES (?,?,?,?,?,?,?,?) ";

	private static final String read_by_assoc = "SELECT * FROM " + TABLE +
			" WHERE " + ID_ASSOC + " = ? ORDER BY " + DATA + ", " + ORA;

	private static final String read_by_id = "SELECT * FROM " + TABLE + " WHERE " + ID + " = ? ";

	private static final String count_assoc = "SELECT COUNT(*) FROM " + TABLE + " WHERE " + ID_ASSOC + " = ? ";

	public EventoRepository(DataSource dataSource) {
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

	public void persist(Evento e) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(insert);
			statement.setInt(1, e.getIdAssociazione());
			statement.setString(2, e.getTipo());
			statement.setString(3, e.getTitolo());
			statement.setString(4, e.getDescrizione());
			statement.setString(5, e.getLuogo());
			statement.setDate(6, e.getData());
			statement.setTime(7, e.getOra());
			statement.setString(8, e.getStrumentoStile());
			statement.executeUpdate();
		} catch (SQLException ex) {
			throw new PersistenceException(ex.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException ex) {
				throw new PersistenceException(ex.getMessage());
			}
		}
	}

	public int countByAssociazione(int idAssociazione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(count_assoc);
			statement.setInt(1, idAssociazione);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException ex) {
			throw new PersistenceException(ex.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException ex) {
				throw new PersistenceException(ex.getMessage());
			}
		}
	}

	private Evento leggi(ResultSet rs) throws SQLException {
		Evento e = new Evento();
		e.setId(rs.getInt(ID));
		e.setIdAssociazione(rs.getInt(ID_ASSOC));
		e.setTipo(rs.getString(TIPO));
		e.setTitolo(rs.getString(TITOLO));
		e.setDescrizione(rs.getString(DESCRIZIONE));
		e.setLuogo(rs.getString(LUOGO));
		e.setData(rs.getDate(DATA));
		e.setOra(rs.getTime(ORA));
		e.setStrumentoStile(rs.getString(STRUMENTO_STILE));
		return e;
	}

	public List<Evento> readByAssociazione(int idAssociazione) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		List<Evento> result = new ArrayList<>();
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_by_assoc);
			statement.setInt(1, idAssociazione);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(leggi(rs));
			}
			return result;
		} catch (SQLException ex) {
			throw new PersistenceException(ex.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException ex) {
				throw new PersistenceException(ex.getMessage());
			}
		}
	}

	public Evento getById(int id) throws PersistenceException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.dataSource.getConnection();
			statement = connection.prepareStatement(read_by_id);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return leggi(rs);
			}
			return null;
		} catch (SQLException ex) {
			throw new PersistenceException(ex.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException ex) {
				throw new PersistenceException(ex.getMessage());
			}
		}
	}
}
