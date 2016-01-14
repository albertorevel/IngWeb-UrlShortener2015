package urlshortener2015.fuzzywuzzy.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import urlshortener2015.fuzzywuzzy.domain.ShortURL;

import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;
import static urlshortener2015.fuzzywuzzy.repository.fixture.ShortURLFixture.*;

public class ShortURLRepositoryTests {

	private EmbeddedDatabase db;
	private ShortURLRepository repository;
	private JdbcTemplate jdbc;

	@Before
	public void setup() {
		db = new EmbeddedDatabaseBuilder().setType(HSQL)
				.addScript("schema-hsqldb.sql").build();
		jdbc = new JdbcTemplate(db);
		repository = new ShortURLRepositoryImpl(jdbc);
	}

	@Test
	public void thatSavePersistsTheShortURL() {
		assertNotNull(repository.save(url1()));
		assertSame(jdbc.queryForObject("select count(*) from SHORTURL",
				Integer.class), 4);
	}

	@Test
	public void thatSaveSponsor() {
		assertNotNull(repository.save(urlSponsor()));
		assertSame(repository.findByKey("3").getSponsor(), urlSponsor().getSponsor());
	}

	@Test
	public void thatSaveSafe() {

		int nSafe = jdbc.queryForObject("select count(*) from SHORTURL where safe = true", Integer.class);

		assertNotNull(repository.save(urlSafe()));
		assertSame(
				jdbc.queryForObject("select count(*) from SHORTURL where safe = true", Integer.class),
				nSafe + 1);
		repository.mark(urlSafe(), false);
		assertSame(
				jdbc.queryForObject("select count(*) from SHORTURL where safe = true", Integer.class),
				nSafe);
		repository.mark(urlSafe(), true);
		assertSame(
				jdbc.queryForObject("select count(*) from SHORTURL where safe = true", Integer.class),
				nSafe + 1);
	}

	@Test
	public void thatSaveADuplicateHashIsSafelyIgnored() {
		repository.save(url1());
		assertNotNull(repository.save(url1()));
		assertSame(jdbc.queryForObject("select count(*) from SHORTURL",
				Integer.class), 4);
	}

	@Test
	public void thatErrorsInSaveReturnsNull() {
		assertNull(repository.save(badUrl()));
		assertSame(jdbc.queryForObject("select count(*) from SHORTURL",
				Integer.class), 3);
	}

	@Test
	public void thatFindByKeyReturnsAURL() {
		repository.save(url1());
		repository.save(url2());
		ShortURL su = repository.findByKey(url1().getHash());
		assertNotNull(su);
		assertSame(su.getHash(), url1().getHash());
	}

	@Test
	public void thatFindByKeyReturnsNullWhenFails() {
		repository.save(url1());
		assertNull(repository.findByKey(url2().getHash()));
	}

	@Test
	public void thatFindByTargetReturnsURLs() {
		repository.save(url1());
		repository.save(url2());
		repository.save(url3());
		List<ShortURL> sul = repository.findByTarget(url1().getTarget());
		assertEquals(sul.size(), 3);
		sul = repository.findByTarget(url3().getTarget());
		assertEquals(sul.size(), 2);
		sul = repository.findByTarget("dummy");
		assertEquals(sul.size(), 0);
	}
	
	@Test
	public void thatDeleteDelete() {
		repository.save(url1());
		repository.save(url2());
		repository.delete(url1().getHash());
		assertEquals(repository.count().intValue(), 4);
		repository.delete(url2().getHash());
		assertEquals(repository.count().intValue(), 3);
	}

	@Test
	public void thatUpdateUpdate() {
		repository.save(url1());
		ShortURL su = repository.findByKey(url1().getHash());
		assertEquals(su.getTarget(), "http://www.unizar.es/");
		repository.update(url1modified());
		su = repository.findByKey(url1().getHash());
		assertEquals(su.getTarget(), "http://www.unizar.org/");
	}
	
	@After
	public void shutdown() {
		db.shutdown();
	}

}
