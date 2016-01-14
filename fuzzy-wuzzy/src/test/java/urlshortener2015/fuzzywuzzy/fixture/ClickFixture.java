package urlshortener2015.fuzzywuzzy.fixture;

import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ShortURL;

import java.sql.Date;

public class ClickFixture {

	public static Click click(ShortURL su) {
		return new Click(null, su.getHash(), null, null, null, null, null,
				null, null, null, 0, 0);
	}

	public static Click click1(ShortURL su) {
		return new Click(null, su.getHash(), new Date(2016,1,14), null, null, null, "155.210.227.8", "España","Aragon","Zaragoza",10,-10);
	}
}
