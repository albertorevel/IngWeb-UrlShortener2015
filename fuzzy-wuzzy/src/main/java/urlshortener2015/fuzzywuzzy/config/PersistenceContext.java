package urlshortener2015.fuzzywuzzy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import urlshortener2015.fuzzywuzzy.repository.ClickRepository;
import urlshortener2015.fuzzywuzzy.repository.ClickRepositoryImpl;
import urlshortener2015.fuzzywuzzy.repository.ShortURLRepository;
import urlshortener2015.fuzzywuzzy.repository.ShortURLRepositoryImpl;

@Configuration
public class PersistenceContext {

	@Autowired
    protected JdbcTemplate jdbc;

	@Bean
	ShortURLRepository shortURLRepository() {
		return new ShortURLRepositoryImpl(jdbc);
	}
 	
	@Bean
	ClickRepository clickRepository() {
		return new ClickRepositoryImpl(jdbc);
	}
	
}
