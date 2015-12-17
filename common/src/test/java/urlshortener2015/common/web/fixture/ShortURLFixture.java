package urlshortener2015.common.web.fixture;

import urlshortener2015.common.domain.ShortURL;

public class ShortURLFixture {

	public static ShortURL someUrl() {
		return new ShortURL("someKey", "http://example.com/", null, null, null,
				null, 307, true, null, null);
	}
}
