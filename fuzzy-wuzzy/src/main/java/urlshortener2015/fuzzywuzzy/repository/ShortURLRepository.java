package urlshortener2015.fuzzywuzzy.repository;

import java.util.List;

import urlshortener2015.fuzzywuzzy.domain.ShortURL;

public interface ShortURLRepository {

	ShortURL findByKey(String id);

	List<ShortURL> findByTarget(String target);

	ShortURL save(ShortURL su);

	ShortURL mark(ShortURL urlSafe, boolean safeness);

	void update(ShortURL su);

	void delete(String id);

	Long count();

	List<ShortURL> list(Long limit, Long offset);

}
