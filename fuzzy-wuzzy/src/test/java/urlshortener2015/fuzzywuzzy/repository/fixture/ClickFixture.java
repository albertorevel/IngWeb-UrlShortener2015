package urlshortener2015.fuzzywuzzy.repository.fixture;

import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ShortURL;

import java.sql.Date;

public class ClickFixture {

	public static Click click(ShortURL su) {
		return new Click(null, su.getHash(), null, null, null, null, null, null,null,null,0,0);
	}

	public static Click click1(ShortURL su) {
		return new Click(null, su.getHash(), new Date(2016,1,14), null, null, null, "155.210.227.8", "España","Aragon","Zaragoza",10,-10);
	}

	public static Click click2(ShortURL su) {
		return new Click(null, su.getHash(), new Date(2015,12,14), null, null, null, "155.210.227.9", "España","Aragon","Huesca",12,-10);
	}

	public static Click click3(ShortURL su) {
		return new Click(null, su.getHash(), new Date(2015,12,17), null, null, null, "155.210.228.1", "Estados Unidos","California","California",-30,-100);
	}
	public static Click click4(ShortURL su) {
		return new Click(null, su.getHash(), new Date(2015,5,17), null, null, null, "155.210.229.1", "Inglaterra","London","London",100,-10);
	}
	public static Click click5(ShortURL su) {
		return new Click(null, su.getHash(), new Date(2015,7,17), null, null, null, "155.210.222.1", "China","Yin Yang","Xui",137,102);
	}
	public static Click click6(ShortURL su) {
		return new Click(null, su.getHash(), new Date(2015,3,17), null, null, null, "155.210.223.3", "Marruecos","Moroco","Casablanca",-50,-10);
	}
}
