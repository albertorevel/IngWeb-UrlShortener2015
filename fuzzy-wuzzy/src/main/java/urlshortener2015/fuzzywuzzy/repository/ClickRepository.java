package urlshortener2015.fuzzywuzzy.repository;

import java.sql.Date;
import java.util.List;

import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ClickAgr;

public interface ClickRepository {

	List<Click> findByHash(String hash);

	Long clicksByHash(String hash);

	Click save(Click cl);

	void update(Click cl);

	void delete(Long id);

	void deleteAll();

	Long count();

	List<Click> list(Long limit, Long offset);

	List<ClickAgr> findByGroup(String er, String group);

	List<ClickAgr> findByGroupSince(String er, String group, Date since);

	List<ClickAgr> findByGroupUntil(String er, String group, Date since);

	List<ClickAgr> findByGroupBounded(String er, String group, Date since, Date until);

	List<Click> getAll();
}
