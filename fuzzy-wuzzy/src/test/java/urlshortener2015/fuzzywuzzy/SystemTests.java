package urlshortener2015.fuzzywuzzy;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import urlshortener2015.fuzzywuzzy.Application;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import java.net.URI;
import java.nio.charset.Charset;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
@DirtiesContext
public class SystemTests {

	@Value("${local.server.port}")
	private int port = 0;

	@Test
	public void testHome() throws Exception {
		ResponseEntity<String> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port, String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("text", "html", Charset.forName("UTF-8"))));
		assertThat(entity.getBody(), containsString("<title>URL"));
	}

	@Test
	public void testCss() throws Exception {
		ResponseEntity<String> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port
						+ "/webjars/bootstrap/3.3.5/css/bootstrap.min.css", String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		assertThat(entity.getHeaders().getContentType(), is(MediaType.valueOf("text/css;charset=UTF-8")));
		assertThat(entity.getBody(), containsString("body"));
	}

	@Test
	public void testCreateLink() throws Exception {
		ResponseEntity<String> entity = postLink("http://example.com/");
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.sponsor"), is(nullValue()));
	
	
	}

	@Test
	public void testRedirection() throws Exception {
		postLink("http://example.com/");
		ResponseEntity<String> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port
						+ "/f684a3c4", String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.TEMPORARY_REDIRECT));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://example.com/")));
	}

	private ResponseEntity<String> postLink(String url) {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("url", url);
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity(
				"http://localhost:" + this.port+"/link", parts, String.class);
		return entity;
	}

	/**
	 * Class prepared in order to do more complex request, with more than the
	 * url argument.
	 * @param args arguments to include in post request. They should be like:
	 *             args[i][0] name of the param i
	 *             args[i][1] value of the param i
	 */
	private ResponseEntity<String> postLink(String url, String[][] args) {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("url", url);
		for (int i = 0; i < args.length; i++) {
			parts.add(args[i][0],args[i][1]);
		}
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity(
				"http://localhost:" + this.port+"/link", parts, String.class);
		return entity;
	}

	@Test
	public void testCreateOwnQr() throws Exception {
		ResponseEntity<String> entity = postLink("http://example.com/");
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4/qr"));
	}

	@Test
	public void testCreateExternalQr() throws Exception {
		String[][] params = new String[][]{{"external",""}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?&cht=qr&chs=500x500&choeUTF-8&chld=L&chl=http%3A%2F%2Flocalhost%3A"+ this.port+"%2Ff684a3c4"));
	}

	public void testCreateVCardQrExternalCode() throws Exception {
		String[][] params = new String[][]{{"external",""},{"vCardName","Example page"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?chs=150x150&cht=qr&choe=UTF-8&chl=BEGIN%3AVCARD%0AVERSION%3A4.0%0AN%3AExample+page%0AURL%3Ahttp://localhost:" + this.port + "/bf19bedb%0AEND%3AVCARD"));
	}

	public void testCreateCorrectionQrCode() throws Exception {
		String[][] params = new String[][]{{"external",""},{"correction","L"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?chs=150x150&cht=qr&choe=UTF-8&chl=http://localhost:" + this.port + "/f684a3c4&chld=L"));
	}
}

