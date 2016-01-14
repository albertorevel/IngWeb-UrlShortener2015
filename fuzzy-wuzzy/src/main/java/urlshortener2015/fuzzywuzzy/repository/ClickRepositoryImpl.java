package urlshortener2015.fuzzywuzzy.repository;

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

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;


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
					rs.getString("city"),rs.getDouble("latitud"),rs.getDouble("longitud"));
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

	public ClickRepositoryImpl(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
		meterDatos();
	}



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
					ps.setDouble(11, cl.getLatitud());
					ps.setDouble(12, cl.getLongitud());
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
	@Override
	public List<ClickAgr> findByGroup(String er,String group) {

		try {
			String concatenate = "%"+er+"%";
			if(group.equals("city")){
				return jdbc.query("SELECT country, comunity, city, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) " +
						"GROUP BY country, comunity, city, target ORDER BY target, country, comunity, city",
						new Object[]{concatenate},rowMapperGroupCity);

			}
			else if(group.equals("comunity")){
				return jdbc.query("SELECT country, comunity, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) GROUP BY country, comunity, target " +
						"ORDER BY target, country, comunity", new Object[]{concatenate},rowMapperGroupComunity);
			}
			else{
				return jdbc.query("SELECT country, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target," +
						"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) GROUP BY country, target " +
						"ORDER BY target, country", new Object[]{concatenate},rowMapperGroupCountry);
			}

		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}

	/**
	 * Método que devuelve toda la información agregada respecto a un area.
	 * @param er patron
	 * @param group grupo por el que agrupar
	 * @param latitudSince
	 * @param latitudUntil
	 * @param longitudSince
	 * @param longitudUntil
     * @return
     */
	@Override
	public List<ClickAgr> findByGroupArea(String er,String group,double latitudSince, double latitudUntil,
										  double longitudSince, double longitudUntil) {
		try {
			String concatenate = "%"+er+"%";
			if(group.equals("city")){
				return jdbc.query("SELECT country, comunity, city, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
					"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ? AND " +
					"c.latitud >= ? AND c.latitud <= ? AND c.longitud >= ? AND c.longitud <= ?) " +
					"GROUP BY country, comunity, city, target ORDER BY target, country, comunity, city",
					new Object[]{concatenate, latitudSince, latitudUntil, longitudSince, longitudUntil},rowMapperGroupCity);

			}
			else if(group.equals("comunity")){
				return jdbc.query("SELECT country, comunity, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target, " +
					"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ? AND " +
					"c.latitud >= ? AND c.latitud <= ? AND c.longitud >= ? AND c.longitud <= ?) " +
					"GROUP BY country, comunity, target ORDER BY target, country, comunity",
					new Object[]{concatenate, latitudSince, latitudUntil, longitudSince, longitudUntil},rowMapperGroupComunity);
			}
			else{
				return jdbc.query("SELECT country, target, count(*) FROM (SELECT c.country, c.comunity, c.city, s.target," +
					"c.created FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ? AND " +
					"c.latitud >= ? AND c.latitud <= ? AND c.longitud >= ? AND c.longitud <= ?) " +
					"GROUP BY country, target ORDER BY target, country",
					new Object[]{concatenate,latitudSince, latitudUntil, longitudSince, longitudUntil},rowMapperGroupCountry);
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
			log.debug("When select for regurlar expresion with group and date(until)" + er, e);
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

	/**
	 * Método que devuelve todos "clicks" de la base de datos.
	 * @return List<Click>
     */
	public List<Click> getAll() {
		try {
			return jdbc.query("SELECT * FROM Click", rowMapper);
		} catch (Exception e) {
			log.debug("When select clicks.", e);
			return null;
		}
	}

	/**
	 * Método que devuelve todos "clicks" que pertenecen a url que coinciden con el patrón.
	 * @param er
	 * @return
     */
	@Override
	public List<Click> getCoordenatesForGroup(String er) {
		try {
			String concatenate = "%"+er+"%";
			return jdbc.query("SELECT id, hash, created, referrer, browser, platform, ip, country, comunity, city, latitud, longitud" +
							" FROM (SELECT c.id, c.hash, c.created, c.referrer, c.browser, c.platform, c.ip, c.country, c.comunity, " +
							"c.city, c.latitud, c.longitud, s.target FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?)",
					new Object[]{concatenate},rowMapper);
		} catch (Exception e) {
			log.debug("When select for regurlar expresion with area" + er, e);
			return null;
		}
	}

	/**
	 * Método que obtiene los "Clicks" de la base de datos que pertenen a direcciones que coinciden con el patrón y
	 * estan en un area concreto.
	 * @param er patrón
	 * @param latitudSince
	 * @param latitudUntil
	 * @param longitudSince
	 * @param longitudUntil
	 * @return List<Click>
	 */
	@Override
	public List<Click> getByArea(String er,double latitudSince, double latitudUntil, double longitudSince, double longitudUntil) {
		try {
			String concatenate = "%"+er+"%";
			return jdbc.query("SELECT id, hash, created, referrer, browser, platform, ip, country, comunity, city, latitud, longitud" +
							" FROM (SELECT c.id, c.hash, c.created, c.referrer, c.browser, c.platform, c.ip, c.country, c.comunity, " +
							"c.city, c.latitud, c.longitud, s.target FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) "+
							"WHERE latitud >= ? AND latitud <= ? AND longitud >= ? AND longitud <= ?",
					new Object[]{concatenate, latitudSince, latitudUntil, longitudSince, longitudUntil},rowMapper);


		} catch (Exception e) {
			log.debug("When select for regurlar expresion with area" + er, e);
			return null;
		}
	}

	/**
	 * Método que obtiene los "Clicks" de la base de datos que pertenen a direcciones que coinciden con el patrón y
	 * estan en un area concreto y se realizaron despues de una fecha determinada.
	 * @param er patrón
	 * @param since
	 * @return List<Click>
	 */
	@Override
	public List<Click> getCoordenatesSince(String er, Date since) {
		try {
			String concatenate = "%"+er+"%";
			return jdbc.query("SELECT id, hash, created, referrer, browser, platform, ip, country, comunity, city, latitud, longitud" +
							" FROM (SELECT c.id, c.hash, c.created, c.referrer, c.browser, c.platform, c.ip, c.country, c.comunity, " +
							"c.city, c.latitud, c.longitud, s.target FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) " +
							"WHERE created >= ?",
					new Object[]{concatenate, since},rowMapper);


		} catch (Exception e) {
			log.debug("When select for regurlar expresion with area" + er, e);
			return null;
		}
	}

	/**
	 * Método que obtiene los "Clicks" de la base de datos que pertenen a direcciones que coinciden con el patrón y
	 * estan en un area concreto y se realizaron antes de una fecha determinada.
	 * @param er patrón
	 * @param until
	 * @return List<Click>
	 */
	@Override
	public List<Click> getCoordenatesUntil(String er,Date until) {
		try {
			String concatenate = "%"+er+"%";

			return jdbc.query("SELECT id, hash, created, referrer, browser, platform, ip, country, comunity, city, latitud, longitud" +
							" FROM (SELECT c.id, c.hash, c.created, c.referrer, c.browser, c.platform, c.ip, c.country, c.comunity, " +
							"c.city, c.latitud, c.longitud, s.target FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) " +
							"WHERE created <= ?",
					new Object[]{concatenate, until},rowMapper);
		} catch (Exception e) {
			log.debug("When select for regurlar expresion with area" + er, e);
			return null;
		}
	}

	/**
	 * Método que obtiene los "Clicks" de la base de datos que pertenen a direcciones que coinciden con el patrón y
	 * estan en un area concreto y se realizaron durante un periodo de tiempo acotado.
	 * @param er patrón
	 * @param since
     * @param until
     * @return List<Click>
     */
	@Override
	public List<Click> getCoordenatesBounded(String er,Date since, Date until) {
		try {
			String concatenate = "%"+er+"%";

			return jdbc.query("SELECT id, hash, created, referrer, browser, platform, ip, country, comunity, city, latitud, longitud" +
							" FROM (SELECT c.id, c.hash, c.created, c.referrer, c.browser, c.platform, c.ip, c.country, c.comunity, " +
							"c.city, c.latitud, c.longitud, s.target FROM click c, shorturl s WHERE c.hash=s.hash AND target LIKE ?) " +
							"WHERE created >= ? AND created <= ?",
					new Object[]{concatenate, since, until},rowMapper);
		} catch (Exception e) {
			log.debug("When select for regurlar expresion with area: " + er, e);
			return null;
		}
	}


	public void meterDatos() {
		String url = "http://www.unizar.es/";
		String id = Hashing.murmur3_32()
				.hashString(url, StandardCharsets.UTF_8).toString();
		Click data = new Click(null, id, new Date(2015,10,2),
				null, null, null, "74.125.45.100", "España", "Aragón", "Zaragoza", 41.64886959999999, -0.889742100000035);
		save(data);
		data = new Click(null, id, new Date(2015,5,13),
				null, null, null, "74.125.45.102", "United States", "New York", "New York", 37.09024, -92.71289100000001);
		save(data);
		data = new Click(null, id, new Date(2015,9,3),
				null, null, null, "74.125.45.100", "Marruecos", "Tanger", "MarruecosCity", 31.791702, -7.092620000000011);
		save(data);
		data = new Click(null, id, new Date(2015,2,5),
				null, null, null, "74.125.45.100", "España", "Madrid", "Getafe", 40.4167754, -3.7037901999999576);
		save(data);
		data = new Click(null, id, new Date(2015,11,4),
				null, null, null, "74.125.45.107", "United States", "California", "Mountain View", 37.09024, -97.71289100000001);
		save(data);
		data = new Click(null, id, new Date(2015,1,1),
				null, null, null, "74.125.45.100", "España", "Aragón", "Teruel", 40.64886959999999, -0.889742100000035);
		save(data);
		data = new Click(null, id, new Date(2014,12,17),
				null, null, null, "74.125.45.100", "Marruecos", "Gran Casablanca", "Marruecolandia", 31.791702, -6.092620000000011);
		save(data);
		url = "http://www.google.es/";
		id = Hashing.murmur3_32()
				.hashString(url, StandardCharsets.UTF_8).toString();
		data = new Click(null, id, new Date(2015,10,2),
				null, null, null, "74.125.45.100", "España", "Aragón", "Zaragoza", 41.64886959999999, -0.889742100000035);
		save(data);
		data = new Click(null, id, new Date(2014,5,13),
				null, null, null, "74.125.45.108", "United States", "New York", "New York", 37.09024, -98.71289100000001);
		save(data);
		data = new Click(null, id, new Date(2015,9,3),
				null, null, null, "74.125.45.100", "Marruecos", "Tanger", "Khourigba", 32.79170, -7.092620000000011);
		save(data);
		data = new Click(null, id, new Date(2015,2,5),
				null, null, null, "74.125.45.100", "España", "Aragon", "Huesca", 42.131845, -0.40780580000000555);
		save(data);
		data = new Click(null, id, new Date(2014,11,4),
				null, null, null, "74.125.45.106", "United States", "California", "Mountain View", 38.09024, -96.71289100000001);
		save(data);
		data = new Click(null, id, new Date(2015,1,1),
				null, null, null, "74.125.45.100", "España", "Aragon", "Teruel", 40.3456879, -1.1064344999999776);
		save(data);
		data = new Click(null, id, new Date(2014,12,17),
				null, null, null, "74.125.45.100", "Marruecos", "Gran Casablanca", "MarruecosCity", 39.791702, -6.092620000000011);
		save(data);
		url = "http://www.ozanganoo.es/";
		id = Hashing.murmur3_32()
				.hashString(url, StandardCharsets.UTF_8).toString();
		data = new Click(null, id, new Date(2014,10,2),
				null, null, null, "74.125.45.100", "China", "Yong", "Yang", 37.406, -122.079);
		save(data);
		data = new Click(null, id, new Date(2015,5,13),
				null, null, null, "74.125.45.105", "United States", "California", "California", 37.09024, -95.712891);
		save(data);
		data = new Click(null, id, new Date(2014,9,3),
				null, null, null, "74.125.45.100", "Marruecos", "Califan", "MarruecosCity", 33.791702, -8.0926200);
		save(data);
		data = new Click(null, id, new Date(2015,2,5),
				null, null, null, "74.125.45.100", "España", "Madrid", "Getafe", 45.4167754, -3.7037901);
		save(data);
		data = new Click(null, id, new Date(2015,11,4),
				null, null, null, "74.125.45.100", "United States", "California", "Mountain View", 35.861660, 104.195396);
		save(data);
		data = new Click(null, id, new Date(2015,1,1),
				null, null, null, "74.125.45.100", "España", "Aragón", "Teruel", 40.6488695, -0.8897421);
		save(data);
		data = new Click(null, id, new Date(2014,12,17),
				null, null, null, "74.125.45.100", "Marruecos", "Maroco", "Moroco", 31.001702, -7.092620000000011);
		save(data);
	}
}
