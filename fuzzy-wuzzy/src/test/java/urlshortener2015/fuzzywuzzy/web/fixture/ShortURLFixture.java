package urlshortener2015.fuzzywuzzy.web.fixture;

import urlshortener2015.fuzzywuzzy.domain.ShortURL;

public class ShortURLFixture {

	public static ShortURL someUrl() {
		return new ShortURL("someKey", "http://example.com/", null, null, null,
				null, 307, true, null, null, null, "www.unizar.es");
	}
}
