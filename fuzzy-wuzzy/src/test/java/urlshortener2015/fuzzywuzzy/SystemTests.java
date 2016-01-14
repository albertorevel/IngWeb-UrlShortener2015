package urlshortener2015.fuzzywuzzy;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
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

import java.net.URI;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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

	@Test
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
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?&cht=qr&chs=500x500&choeUTF-8&chld=L&chl=BEGIN%3AVCARD%0AVERSION%3A4.0%0AN%3AExample+page%0AURL%3Ahttp%3A%2F%2Flocalhost%3A"+this.port+"%2Ff684a3c4%0AEND%3AVCARD"));
	}

	@Test
	public void testCreateLCorrectionExternalQrCode() throws Exception {
		String[][] params = new String[][]{{"external",""},{"correction","L"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?&cht=qr&chs=500x500&choeUTF-8&chld=L&chl=http%3A%2F%2Flocalhost%3A" + this.port + "%2Ff684a3c4"));
	}
	@Test
	public void testCreateMCorrectionExternalQrCode() throws Exception {
		String[][] params = new String[][]{{"external",""},{"correction","M"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?&cht=qr&chs=500x500&choeUTF-8&chld=M&chl=http%3A%2F%2Flocalhost%3A" + this.port + "%2Ff684a3c4"));
	}
	@Test
	public void testCreateHCorrectionExternalQrCode() throws Exception {
		String[][] params = new String[][]{{"external",""},{"correction","H"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?&cht=qr&chs=500x500&choeUTF-8&chld=H&chl=http%3A%2F%2Flocalhost%3A" + this.port + "%2Ff684a3c4"));
	}
	@Test
	public void testCreateQCorrectionExternalQrCode() throws Exception {
		String[][] params = new String[][]{{"external",""},{"correction","Q"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.qrApi"), Matchers.<Object>is("https://chart.googleapis.com/chart?&cht=qr&chs=500x500&choeUTF-8&chld=Q&chl=http%3A%2F%2Flocalhost%3A" + this.port + "%2Ff684a3c4"));
	}
	@Test
	public void testPonertiempo() throws Exception {
		String[][] params = new String[][]{{"tiempo","5"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		assertThat(entity.getHeaders().getLocation(), is(new URI("http://localhost:"+ this.port+"/f684a3c4")));
		assertThat(entity.getHeaders().getContentType(), is(new MediaType("application", "json", Charset.forName("UTF-8"))));
		ReadContext rc = JsonPath.parse(entity.getBody());
		assertThat(rc.read("$.hash"), Matchers.<Object>is("f684a3c4"));
		assertThat(rc.read("$.uri"), Matchers.<Object>is("http://localhost:"+ this.port+"/f684a3c4"));
		assertThat(rc.read("$.target"), Matchers.<Object>is("http://example.com/"));
		assertThat(rc.read("$.tiempo"), Matchers.<Object>is("5"));
	}

	@Test
	public void testPubliRedirection() throws Exception {
		String[][] params = new String[][]{{"tiempo","5"}};
		postLink("http://example.com/",params);
		ResponseEntity<String> entity = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port
						+ "/f684a3c4", String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		assertThat(entity.getHeaders().getContentType(), Matchers.<MediaType>is(MediaType.valueOf("text/html;charset=UTF-8")));
		assertThat(entity.getBody(), Matchers.is("<!doctype html>\n" +
				"<head>\n" +
				"<script type=\"text/javascript\">\n" +
				"function redireccionar(){\n" +
				"location.href=\"final/f684a3c4\"\n" +
				"}\n" +
				"setTimeout (\"redireccionar()\",5000);\n" +
				"</script>\n" +
				"</head>\n" +
				"<body>\n" +
				"<div class= \"row\">\n" +
				"<div align=\"center\">\n" +
				"<h1> Pagina de publicidad </h1>\n" +
				"<script type=\"text/javascript\">\n" +
				"var segundos = 5;\n" +
				"function contar(){\n" +
				"if(segundos <= 0){\n" +
				"document.getElementById(\"contador\").innerHTML = \"Redireccionando ...\";\n" +
				"} else {\n" +
				"segundos--;\n" +
				"document.getElementById(\"contador\").innerHTML = \"Le redireccionaremos automáticamente en \" + segundos  + \" segundos.\";\n" +
				"}\n" +
				"}\n" +
				"setInterval(\"contar()\",1000);\n" +
				"</script>\n" +
				"<div id=\"contador\">Le redireccionaremos automáticamente en 5 segundos</div>\n" +
				"<center>\n" +
				"<img src=\"http://www.tiempodepublicidad.com/wp-content/themes/gridthemeresponsiveFull/images/tdp/tiempo-de-publicidad.png\" alt=\"Publicidad\"></center>\n" +
				"</div>\n" +
				"</body>\n" +
				"</html>"));
	}
	@Test
	public void redireccionTarget() throws Exception {
		String[][] params = new String[][]{{"tiempo","5"}};
		ResponseEntity<String> entity = postLink("http://example.com/",params);
		assertThat(entity.getStatusCode(), is(HttpStatus.CREATED));
		ResponseEntity<String> entityRedirect = new TestRestTemplate().getForEntity(
				"http://localhost:" + this.port
						+ "/final/f684a3c4", String.class);
		assertThat(entityRedirect.getStatusCode(), is(HttpStatus.TEMPORARY_REDIRECT));
		assertThat(entityRedirect.getHeaders().getLocation(), Matchers.<URI>is(URI.create("http://example.com/")));
	}
	@Test
	public void testGetInfo() throws Exception {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("patron", "unizar");
		parts.add("group", "country");
		parts.add("yearSince", "");
		parts.add("monthSince", "");
		parts.add("daySince", "");
		parts.add("yearUntil", "");
		parts.add("monthUntil", "");
		parts.add("dayUntil", "");
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity(
				"http://localhost:" + this.port+"/info", parts, String.class);
		ReadContext rc = JsonPath.parse(entity.getBody());
		JSONArray list = rc.read("$.[0]");
		//Se comprueba que el tamaño de la primera lista(información agregada) sea 3, ya que hay 3 paises.
		assertEquals(list.size(),3);
		//Se comprueba que el tamaño de la segunda lista(coordenadas e ip de cada click) sea 7.
		JSONArray list1 = rc.read("$.[1]");
		assertEquals(list1.size(),7);
	}

	@Test
	public void testGetInfoByDateBounded() throws Exception {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("patron", "google");
		parts.add("group", "comunity");
		parts.add("yearSince", 2014);
		parts.add("monthSince", 1);
		parts.add("daySince", 1);
		parts.add("yearUntil", 2015);
		parts.add("monthUntil", 7);
		parts.add("dayUntil", 1);
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity(
				"http://localhost:" + this.port+"/info", parts, String.class);
		ReadContext rc = JsonPath.parse(entity.getBody());
		JSONArray list = rc.read("$.[0]");
		assertEquals(list.size(),4);
		JSONArray list1 = rc.read("$.[1]");
		assertEquals(list1.size(),5);
	}

	@Test
	public void testGetInfoByDateSince() throws Exception {
		//Realizamos una peticion "/infoDate" sobre urls con "google" y agrupandola por comunity
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("patron", "google");
		parts.add("group", "comunity");
		parts.add("yearSince", 2015);
		parts.add("monthSince", 7);
		parts.add("daySince", 1);
		parts.add("yearUntil", 0);
		parts.add("monthUntil", 0);
		parts.add("dayUntil", 0);
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity(
				"http://localhost:" + this.port+"/info", parts, String.class);
		ReadContext rc = JsonPath.parse(entity.getBody());
		JSONArray list = rc.read("$.[0]");
		//Se comprueba que el tamaño de la primera lista(información agregada) sea 2, en este caso
		// cada click es de una ciudad diferente.
		assertEquals(list.size(),2);
		//Se comprueba que el tamaño de la segunda lista(coordenadas e ip de cada click) sea 2.
		JSONArray list1 = rc.read("$.[1]");
		assertEquals(list1.size(),2);
	}

	@Test
	public void testGetInfoByDateUntil() throws Exception {

		//Realizamos una peticion "/infoDate" sobre urls con "google" y agrupandola por comunity
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("patron", "google");
		parts.add("group", "comunity");
		parts.add("yearSince", 0);
		parts.add("monthSince", 0);
		parts.add("daySince", 0);
		parts.add("yearUntil", 2015);
		parts.add("monthUntil", 9);
		parts.add("dayUntil", 4);
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity(
				"http://localhost:" + this.port+"/info", parts, String.class);
		ReadContext rc = JsonPath.parse(entity.getBody());
		JSONArray list = rc.read("$.[0]");
		assertEquals(list.size(),5);
		JSONArray list1 = rc.read("$.[1]");
		assertEquals(list1.size(),6);
	}

	@Test
	public void testGetInfoByDArea() throws Exception {
		//Realizamos una peticion "/infoArea" sobre urls con "oo" y agrupandola por country
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("patron", "oo");
		parts.add("group", "country");
		parts.add("latitudSince", 0);
		parts.add("latitudUntil", 50);
		parts.add("longitudSince", -10);
		parts.add("longitudUntil", 0);
		ResponseEntity<String> entity = new TestRestTemplate().postForEntity(
				"http://localhost:" + this.port+"/infoArea", parts, String.class);
		ReadContext rc = JsonPath.parse(entity.getBody());
		JSONArray list = rc.read("$.[0]");
		assertEquals(list.size(),4);
		JSONArray list1 = rc.read("$.[1]");
		assertEquals(list1.size(),9);
	}
}

