package urlshortener2015.fuzzywuzzy.repository;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ClickAgr;


@Repository
public class ClickRepositoryImpl implements ClickRepository {

	private static final Logger log = LoggerFactory
			.getLogger(urlshortener2015.fuzzywuzzy.repository.ClickRepositoryImpl.class);

	private static final RowMapper<Click> rowMapper = new RowMapper<Click>() {
		@Override
		public Click mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Click(rs.getLong("id"), rs.getString("hash"),
					rs.getDate("created"), rs.getString("referrer"),
					rs.getString("browser"), rs.getString("platform"),
					rs.getString("ip"), rs.getString("country"),rs.getString("comunity"),
					rs.getString("city"),rs.getString("latitud"),rs.getString("longitud"));
		}
	};

	/*
	 * Mapping para cuando sólo se guardan los campos country, target y count
	 */
	private static final RowMapper<ClickAgr> rowMapperGroupCountry = new RowMapper<ClickAgr>() {
		@Override
		public ClickAgr mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClickAgr clickAgr = new ClickAgr(rs.getString(1), rs.getString(2), rs.getInt(3));
			return clickAgr;
		}
	};

	/*
	 * Mapping para cuando sólo se guardan los campos country, comunity, target y count
	 */
	private static final RowMapper<ClickAgr> rowMapperGroupComunity = new RowMapper<ClickAgr>() {
		@Override
		public ClickAgr mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClickAgr clickAgr = new ClickAgr(rs.getString(1), rs.getString(2),
					rs.getString(3), rs.getInt(4));
			return clickAgr;
		}
	};

	/*
	 * Mapping para cuando sólo se guardan los campos country, comunity, city, target y count
	 */
	private static final RowMapper<ClickAgr> rowMapperGroupCity = new RowMapper<ClickAgr>() {
		@Override
		public ClickAgr mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClickAgr clickAgr = new ClickAgr(rs.getString(1), rs.getString(2),
					rs.getString(3), rs.getString(4), rs.getInt(5));
			return clickAgr;
		}
	};

	@Autowired
	protected JdbcTemplate jdbc;

	public ClickRepositoryImpl() {
	}

	public ClickRepositoryImpl(JdbcTemplate jdbc) {this.jdbc = jdbc;}



	@Override
	public List<Click> findByHash(String hash) {
		try {
			return jdbc.query("SELECT * FROM click WHERE hash=?",
					new Object[] { hash }, rowMapper);
		} catch (Exception e) {
			log.debug("When select for hash " + hash, e);
			return null;
		}
	}

	@Override
	public Click save(final Click cl) {
		try {
			KeyHolder holder = new GeneratedKeyHolder();
			jdbc.update(new PreparedStatementCreator() {

				@Override
				public PreparedStatement createPreparedStatement(Connection conn)
						throws SQLException {
					PreparedStatement ps = conn
							.prepareStatement(
									"INSERT INTO CLICK VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
									Statement.RETURN_GENERATED_KEYS);
					ps.setNull(1, Types.BIGINT);
					ps.setString(2, cl.getHash());
					ps.setDate(3, cl.getCreated());
					ps.setString(4, cl.getReferrer());
					ps.setString(5, cl.getBrowser());
					ps.setString(6, cl.getPlatform());
					ps.setString(7, cl.getIp());
					ps.setString(8, cl.getCountry());
					ps.setString(9, cl.getComunity());
					ps.setString(10, cl.getCity());
					ps.setString(11, cl.getLatitud());
					ps.setString(12, cl.getLongitud());
					return ps;
				}
			}, holder);
			new DirectFieldAccessor(cl).setPropertyValue("id", holder.getKey()
					.longValue());
		} catch (DuplicateKeyException e) {
			log.debug("When insert for click with id " + cl.getId(), e);
			return cl;
		} catch (Exception e) {
			log.debug("When insert a click", e);
			return null;
		}
		return cl;
	}

	@Override
	public void update(Click cl) {
		log.info("ID2: "+cl.getId()+"navegador: "+cl.getBrowser()+" SO: "+cl.getPlatform()+" Date:"+cl.getCreated());
		try {
			jdbc.update(
					"update click set hash=?, created=?, referrer=?, browser=?, platform=?, ip=?, country=?," +
							"comunity=?, city=?, latitud=?, longitud=? where id=?",
					cl.getHash(), cl.getCreated(), cl.getReferrer(),
					cl.getBrowser(), cl.getPlatform(), cl.getIp(),
					cl.getCountry(),cl.getComunity(), cl.getCity(), cl.getLatitud(), cl.getLongitud(), cl.getId());

		} catch (Exception e) {
			log.info("When update for id " + cl.getId(), e);
		}
	}

	@Override
	public void delete(Long id) {
		try {
			jdbc.update("delete from click where id=?", id);
		} catch (Exception e) {
			log.debug("When delete for id " + id, e);
		}
	}

	@Override
	public void deleteAll() {
		try {
			jdbc.update("delete from click");
		} catch (Exception e) {
			log.debug("When delete all", e);
		}
	}

	@Override
	public Long count() {
		try {
			return jdbc
					.queryForObject("select count(*) from click", Long.class);
		} catch (Exception e) {
			log.debug("When counting", e);
		}
		return -1L;
	}

	@Override
	public List<Click> list(Long limit, Long offset) {
		try {
			return jdbc.query("SELECT * FROM click LIMIT ? OFFSET ?",
					new Object[] { limit, offset }, rowMapper);
		} catch (Exception e) {
			log.debug("When select for limit " + limit + " and offset "
					+ offset, e);
			return null;
		}
	}

	@Override
	public Long clicksByHash(String hash) {
		try {
			return jdbc
					.queryForObject("select count(*) from click where hash = ?", new Object[]{hash}, Long.class);
		} catch (Exception e) {
			log.debug("When counting hash "+hash, e);
		}
		return -1L;
	}

	/**
	 * Método que devuelve toda la información agregada
	 * @param er patrón
	 * @param group indica sobre qué parametro se agrega la información (country, comunity o city)
	 * @return Lista de objetos que contiene toda información agregada
	 */
	public List<ClickAgr> findByGroup(String er,String group) {

		try {
			String concatenate = "%"+er+"%";
			if(group.equals("city")){
				return jdbc.query("SELECT country, comunity, city, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) " +
						"GROUP BY country, comunity, city, target ORDER BY target",
						new Object[]{concatenate},rowMapperGroupCity);

			}
			else if(group.equals("comunity")){
				return jdbc.query("SELECT country, comunity, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) GROUP BY country, comunity, target " +
						"ORDER BY target", new Object[]{concatenate},rowMapperGroupComunity);
			}
			else{
				return jdbc.query("SELECT country, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target," +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) GROUP BY country, target " +
						"ORDER BY target", new Object[]{concatenate},rowMapperGroupCountry);
			}

		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}

	/**
	 * Método que devuelve la información agregada desde una fecha
	 * @param er patrón
	 * @param group indica sobre qué parametro se agrega la información (country, comunity o city)
	 * @param date fecha límite
	 * @return Lista de objetos que contiene toda información agregada
	 */
	public List<ClickAgr> findByGroupSince(String er, String group, Date date) {

		try {
			String concatenate = "%"+er+"%";
			if(group.equals("city")){
				return jdbc.query("SELECT country, comunity, city, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) WHERE " +
						"created >= ? GROUP BY country, comunity, city, target ORDER BY target",
						new Object[]{concatenate, date},rowMapperGroupCity);

			}
			else if(group.equals("comunity")){
				return jdbc.query("" +
						"SELECT country, comunity, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) WHERE created >= ? GROUP BY " +
						"country, comunity, target ORDER BY target", new Object[]{concatenate, date},rowMapperGroupComunity);
			}
			else{
				return jdbc.query("SELECT country, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target," +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND s.target LIKE ?) WHERE created >= ? GROUP BY " +
						"country, target ORDER BY target", new Object[]{concatenate, date},rowMapperGroupCountry);
			}

		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}

	/**
	 * Método que devuelve la información agregada hasta una fecha
	 * @param er patrón
	 * @param group indica sobre qué parametro se agrega la información (country, comunity o city)
	 * @param date fecha de inicio
	 * @return Lista de objetos que contiene toda información agregada
	 */
	public List<ClickAgr> findByGroupUntil(String er, String group, Date date) {
		try {
			String concatenate = "%" + er + "%";
			if (group.equals("city")) {
				return jdbc.query("SELECT country, comunity, city, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND s.target LIKE ?) WHERE created <= ? " +
						"GROUP BY country, comunity, city, target ORDER BY target",
						new Object[]{concatenate, date}, rowMapperGroupCity);

			} else if (group.equals("comunity")) {
				return jdbc.query("" +
						"SELECT country, comunity, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) WHERE created <= ? GROUP BY " +
						"country, comunity, target ORDER BY target", new Object[]{concatenate, date}, rowMapperGroupComunity);
			} else {
				return jdbc.query("SELECT country, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target," +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) WHERE created <= ? GROUP BY " +
						"country, target ORDER BY target", new Object[]{concatenate, date}, rowMapperGroupCountry);
			}

		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}

	/**
	 * Método que devuelve la información agregada acotada entre dos fechas
	 * @param er patrón
	 * @param group indica sobre qué parametro se agrega la información (country, comunity o city)
	 * @param dateSince fecha inicio
	 * @param dateUntil fecha fin
     * @return Lista de objetos que contiene toda información agregada
     */
	public List<ClickAgr> findByGroupBounded(String er, String group, Date dateSince, Date dateUntil) {

		try {
			String concatenate = "%" + er + "%";
			if (group.equals("city")) {
				return jdbc.query("SELECT country, comunity, city, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) WHERE " +
						"created >= ? AND created <= ? GROUP BY country, comunity, city, target " +
						"ORDER BY target",
						new Object[]{concatenate, dateSince, dateUntil}, rowMapperGroupCity);

			} else if (group.equals("comunity")) {
				return jdbc.query("" +
						"SELECT country, comunity, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) WHERE " +
						"created >= ? AND created <= ? GROUP BY country, comunity, target " +
						"ORDER BY target", new Object[]{concatenate, dateSince, dateUntil}, rowMapperGroupComunity);
			} else {
				return jdbc.query("SELECT country, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target," +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) WHERE " +
						"created >= ? AND created <= ? GROUP BY country, target " +
						"ORDER BY target", new Object[]{concatenate, dateSince, dateUntil}, rowMapperGroupCountry);
			}

		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}
}
