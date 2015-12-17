package urlshortener2015.fuzzywuzzy.web;

import javax.print.attribute.standard.Media;
import javax.servlet.http.HttpServletRequest;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;
import urlshortener2015.fuzzywuzzy.Application;
import urlshortener2015.fuzzywuzzy.repository.ClickRepository;
import urlshortener2015.fuzzywuzzy.repository.ShortURLRepository;
import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ShortURL;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class UrlShortenerController {
	private static final Logger log = LoggerFactory
			.getLogger(urlshortener2015.fuzzywuzzy.web.UrlShortenerController.class);
	@Autowired
	protected ShortURLRepository shortURLRepository;

	@Autowired
	protected ClickRepository clickRepository;
	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class);

	@RequestMapping(value = "/final/{id:(?!link|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> getFinal(@PathVariable String id, HttpServletRequest request) {
		ShortURL l = shortURLRepository.findByKey(id);
		HttpHeaders h = new HttpHeaders();
		h.setLocation(URI.create(l.getTarget()));
		return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
	}
//	@RequestMapping(produces = "application/html", value = "/publicidad", method = RequestMethod.GET)
//	public ResponseEntity<?> getPublicidad(HttpServletRequest request) {
//		String body = "<!doctype html>\n" +
//				"<head>\n" +
//				"<script type=\"text/javascript\">\n" +
//				"function redireccionar(){\n" +
//				"location.href=\"final\"\n" +
//				"}\n" +
//				"setTimeout (\"redireccionar()\", 5000);\n" +
//				"</script>\n" +
//				"</head>\n" +
//				"<body>\n" +
//				"<p> Pagina de publicidad </p>\n" +
//				"</body>\n" +
//				"</html>";
//		HttpHeaders responseHeaders = new HttpHeaders();
//		responseHeaders.setContentType(MediaType.TEXT_HTML);
//		return new ResponseEntity<Object>(body,responseHeaders,HttpStatus.OK);
//	}

	@RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
		ShortURL l = shortURLRepository.findByKey(id);
		if (l != null) {
			createAndSaveClick(id, extractIP(request));
			ResponseEntity<?> re = createSuccessfulRedirectToResponse(l);
			if(l.getTiempo()!= 0){
				String body = "<!doctype html>\n" +
						"<head>\n" +
						"<script type=\"text/javascript\">\n" +
						"function redireccionar(){\n" +
						"location.href=\"final/" + id +"\"\n" +
						"}\n" +
						"setTimeout (\"redireccionar()\"," + l.getTiempo() +");\n" +
						"</script>\n" +
						"</head>\n" +
						"<body>\n" +
						"<p> Pagina de publicidad </p>\n" +
						"</body>\n" +
						"</html>";
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(MediaType.TEXT_HTML);
				return new ResponseEntity<Object>(body,responseHeaders,HttpStatus.OK);
			}
			else {
				return re;
			}
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	protected ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
		HttpHeaders h = new HttpHeaders();
		h.setLocation(URI.create(l.getTarget()));
		return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
	}

	@RequestMapping(value = "/link", method = RequestMethod.POST)
	public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
											  @RequestParam(value = "sponsor", required = false) String sponsor,
											  @RequestParam(value = "brand", required = false) String brand,
											  @RequestParam(value = "vCardName", required = false) String vCard,
											  @RequestParam(value = "correction", required = false) String correction,
											  @RequestParam(value = "tiempo", required = false) int tiempo,
											  HttpServletRequest request) {
		logger.info("Requested new short for uri " + url);
		ShortURL su = createAndSaveIfValid(url, sponsor, brand, vCard, correction, UUID
				.randomUUID().toString(), extractIP(request), tiempo);
		if (su != null) {
			HttpHeaders h = new HttpHeaders();
			h.setLocation(su.getUri());
			return new ResponseEntity<>(su, h, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	protected void createAndSaveClick(String hash, String ip) {
		Click cl = new Click(null, hash, new Date(System.currentTimeMillis()),
				null, null, null, ip, null);
		cl = clickRepository.save(cl);
		log.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
	}

	protected ShortURL createAndSaveIfValid(String url, String sponsor,
											String brand, String vCardName, String correction,
											String owner, String ip, int tiempo) {
		UrlValidator urlValidator = new UrlValidator(new String[]{"http",
				"https"});
		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32()
					.hashString(url, StandardCharsets.UTF_8).toString();
			URI uri = linkTo(
					methodOn(urlshortener2015.fuzzywuzzy.web.UrlShortenerController.class).redirectTo(
							id, null)).toUri();
			String qrApi = createQrQuery(uri, vCardName, correction);
			RestTemplate restTemplate = new RestTemplate();
			String qrDef = encodeBase64String(restTemplate.getForObject(qrApi, byte[].class));
			ShortURL su = new ShortURL(id, url,
					uri, sponsor, new Date(
					System.currentTimeMillis()), owner,
					HttpStatus.TEMPORARY_REDIRECT.value(), true, ip, null, qrApi, qrDef, tiempo);
			return shortURLRepository.save(su);
		} else {
			return null;
		}
	}
	private String createQrQuery(URI uri, String vCardName, String correction) {
		String query = "https://chart.googleapis.com/chart?chs=150x150&cht=qr&choe=UTF-8&chl=";
		if (vCardName == null) {
			query += uri;
		} else {
			try {
				vCardName = URLEncoder.encode(vCardName, "UTF-8");
				query += "BEGIN%3AVCARD%0AVERSION%3A4.0%0AN%3A" + vCardName +
						"%0AURL%3A" + uri + "%0AEND%3AVCARD";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				query = null;
			}

		}
		if (correction != null) {
			query += "&chld=" + correction;
		}
		return query;

	}
	protected String extractIP(HttpServletRequest request) {
		return request.getRemoteAddr();
	}
}
