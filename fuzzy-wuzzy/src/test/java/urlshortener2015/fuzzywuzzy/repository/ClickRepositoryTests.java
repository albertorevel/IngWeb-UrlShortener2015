package urlshortener2015.fuzzywuzzy.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import urlshortener2015.fuzzywuzzy.domain.Click;

import java.sql.Date;

import static org.junit.Assert.*;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;
import static urlshortener2015.fuzzywuzzy.fixture.ShortURLFixture.url4;
import static urlshortener2015.fuzzywuzzy.repository.fixture.ClickFixture.*;
import static urlshortener2015.fuzzywuzzy.repository.fixture.ShortURLFixture.*;

public class ClickRepositoryTests {

	private EmbeddedDatabase db;
	private ClickRepository repository;
	private JdbcTemplate jdbc;


	@Before
	public void setup() {
		db = new EmbeddedDatabaseBuilder().setType(HSQL)
				.addScript("schema-hsqldb.sql").build();
		jdbc = new JdbcTemplate(db);
		ShortURLRepository shortUrlRepository = new ShortURLRepositoryImpl(jdbc);
		shortUrlRepository.save(url1());
		shortUrlRepository.save(url2());
		shortUrlRepository.save(url3());
		shortUrlRepository.save(url4());
		repository = new ClickRepositoryImpl(jdbc);
	}

	@Test
	public void thatSavePersistsTheClickURL() {
		Click click = repository.save(click(url1()));
		assertSame(jdbc.queryForObject("select count(*) from CLICK",
				Integer.class), 1);
		assertNotNull(click);
		assertNotNull(click.getId());
	}

	@Test
	public void thatErrorsInSaveReturnsNull() {
		assertNull(repository.save(click(badUrl())));
		assertSame(jdbc.queryForObject("select count(*) from CLICK",
				Integer.class), 0);
	}

	@Test
	public void thatFindByKeyReturnsAURL() {
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		assertEquals(repository.findByHash(url1().getHash()).size(), 3);
		assertEquals(repository.findByHash(url2().getHash()).size(), 2);
	}

	@Test
	public void thatFindByKeyReturnsEmpty() {
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		assertEquals(repository.findByHash(badUrl().getHash()).size(), 0);
	}

	@Test
	public void thatDeleteDelete() {
		Long id1 = repository.save(click(url1())).getId();
		Long id2 = repository.save(click(url2())).getId();
		repository.delete(id1);
		assertEquals(repository.count().intValue(), 1);
		repository.delete(id2);
		assertEquals(repository.count().intValue(), 0);
	}

	@Test
	public void saveClickWithNewValues() {
		Click click = repository.save(click1(url1()));
		assertSame(jdbc.queryForObject("select count(*) from CLICK",
				Integer.class), 1);
		assertNotNull(click);
		assertEquals(repository.getAll().get(0).getIp(), click.getIp());
		assertEquals(repository.getAll().get(0).getCountry(), click.getCountry());
		assertEquals(repository.getAll().get(0).getComunity(), click.getComunity());
		assertEquals(repository.getAll().get(0).getCity(), click.getCity());
		assertEquals(repository.getAll().get(0).getLatitud(), click.getLatitud(),0.0);
		assertEquals(repository.getAll().get(0).getLongitud(), click.getLongitud(),0.0);
	}

	@Test
	public void findByGroup() {
		repository.save(click1(url2()));
		repository.save(click2(url3()));
		repository.save(click3(url2()));
		repository.save(click4(url3()));
		//Hay 2 agrupamientos que coinciden sobre la url con el patron "z" y cada una es sobre una ciudad
		assertEquals(repository.findByGroup("z","city").size(),2);
		//Ahora sólo hay 1, ya que el otro click guardado es anterior a la fecha
		assertEquals(repository.findByGroupSince("z","comunity", new Date(2016,1,1)).size(),1);
		//Ahora hay 2, ya que los 2 clicks de "google" son anteriores a la fecha y son diferentes comunidades
		assertEquals(repository.findByGroupUntil("oo","comunity", new Date(2016,1,1)).size(),2);
		//Ahora sólo hay 1, ya que sólo uno esta entre las fechas indicadas
		assertEquals(repository.findByGroupBounded("oo","country", new Date(2015,1,1), new Date(2015,12,15)).size(),2);
		//Hay 4 clicks en total
		assertEquals(repository.getAll().size(),4);
	}

	@Test
	public void findByArea() {
		repository.save(click1(url2()));
		repository.save(click2(url3()));
		repository.save(click3(url2()));
		repository.save(click4(url3()));
		repository.save(click5(url4()));
		repository.save(click6(url3()));
		//Hay 1 click entre esas coordenadas y con patron como "unizar", click1
		assertEquals(repository.getByArea("unizar",8,15,-11,-9).size(),1);
		//Hay 2 datos de la agrupacion por paises dentro de esas coordenadas(España y EEUU)
		assertEquals(repository.findByGroupArea("unizar","country",-180,180,-100,-10).size(),2);
		//Ahora hay 2 que coinciden con ese patrón, click2 y click4 y la fecha es posterior a la indicada
		assertEquals(repository.getCoordenatesSince("google",new Date(2015,4,1)).size(),2);
		//Ahora sólo hay 1, click6
		assertEquals(repository.getCoordenatesUntil("google",new Date(2015,4,1)).size(),1);
		//Ahora sólo hay 1, ya que sólo uno esta entre las fechas indicadas
		assertEquals(repository.getCoordenatesBounded("unizar",new Date(2015,1,1),new Date(2015,12,31)).size(),1);
		//Ahora hay 2 clicks que coincida la url con el patron
		assertEquals(repository.getCoordenatesForGroup("unizar").size(),2);
	}

	@After
	public void shutdown() {
		db.shutdown();
	}

}
