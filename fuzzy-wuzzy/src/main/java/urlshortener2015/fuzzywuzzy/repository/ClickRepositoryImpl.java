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
import urlshortener2015.fuzzywuzzy.domain.ShortURL;
import urlshortener2015.fuzzywuzzy.repository.ClickRepository;


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

	private static final RowMapper<ShortURL> rowMapper2 = new RowMapper<ShortURL>() {
		@Override
		public ShortURL mapRow(ResultSet rs, int rowNum) throws SQLException {
			ShortURL shortURL = new ShortURL(rs.getString("hash"), rs.getString("target"),
					null, rs.getString("sponsor"), rs.getDate("created"),
					rs.getString("owner"), rs.getInt("mode"),
					rs.getBoolean("safe"), rs.getString("ip"),
					rs.getString("country"), rs.getString("qrApi"),
					rs.getString("qrCode"));
			return shortURL;
		}
	};

	@Autowired
	protected JdbcTemplate jdbc;

	public ClickRepositoryImpl() {
	}

	public ClickRepositoryImpl(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
		//DATOS INTRODUCIDOS PARA PROBARLO
		String url = "http://www.unizar.es/";
		String id = Hashing.murmur3_32()
				.hashString(url, StandardCharsets.UTF_8).toString();
		Click data = new Click(null, id, new Date(2015,10,2),
				null, null, null, "74.125.45.100", "España", "Aragón", "Zaragoza", "37.406", "-122.079");
		save(data);
		data = new Click(null, id, new Date(2015,1,1),
				null, null, null, "74.125.45.100", "España", "Aragón", "Teruel", "37.406", "-122.079");
		save(data);
		data = new Click(null, id, new Date(2015,2,5),
				null, null, null, "74.125.45.100", "España", "Madrid", "Getafe", "37.406", "-122.079");
		save(data);
		data = new Click(null, id, new Date(2015,9,3),
				null, null, null, "74.125.45.100", "Marruecos", "Nose", "MarruecosCity", "370.406", "-32.079");
		save(data);
		data = new Click(null, id, new Date(2014,12,17),
				null, null, null, "74.125.45.100", "Marruecos", "Nose", "MarruecosCity", "370.406", "-32.079");
		save(data);
		data = new Click(null, id, new Date(2015,11,4),
				null, null, null, "74.125.45.100", "United States", "California", "Mountain View", "63.406", "-963.079");
		save(data);
		data = new Click(null, id, new Date(2015,5,13),
				null, null, null, "74.125.45.100", "United States", "New York", "New York", "1636.406", "1969.079");
		save(data);
		List<Click> list = findByGroup("iza");
		List<Click> list2 = findByGroupSince("iza", new Date(2015,1,1));
		List<Click> list3 = findByGroupUntil("iza", new Date(2015,1,1));
		List<Click> list4 = findByGroupBounded("iza", new Date(2015,1,1),new Date(2015,6,1));
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

	public List<Click> findByGroup(String er) {

		try {
			String concatenate = "%"+er+"%";
			return jdbc.query("SELECT c.* FROM shorturl s, click c WHERE (s.target LIKE ? AND c.hash=s.hash)",
					new Object[]{concatenate}, rowMapper);
		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}

	public List<Click> findByGroupSince(String er, Date date) {

		try {
			String concatenate = "%"+er+"%";
			return jdbc.query("SELECT c.* FROM shorturl s, click c WHERE (s.target LIKE ? AND c.hash=s.hash AND c.created >= ?)",
					new Object[]{concatenate, date}, rowMapper);
		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}

	public List<Click> findByGroupUntil(String er, Date date) {

		try {
			String concatenate = "%"+er+"%";
			return jdbc.query("SELECT c.* FROM shorturl s, click c WHERE (s.target LIKE ? AND c.hash=s.hash AND c.created <= ?)",
					new Object[]{concatenate, date}, rowMapper);
		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}

	public List<Click> findByGroupBounded(String er, Date dateSince, Date dateUntil) {

		try {
			String concatenate = "%"+er+"%";
			return jdbc.query("SELECT c.* FROM shorturl s, click c WHERE (s.target LIKE ? AND" +
					" c.hash=s.hash AND c.created >= ? AND c.created <= ?)",
					new Object[]{concatenate, dateSince, dateUntil}, rowMapper);
		} catch (Exception e) {
			log.debug("When select for regurlar expresion " + er, e);
			return null;
		}
	}
}
