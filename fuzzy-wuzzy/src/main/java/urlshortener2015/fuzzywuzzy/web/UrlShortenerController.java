package urlshortener2015.fuzzywuzzy.web;

import com.google.common.hash.Hashing;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ClickAgr;
import urlshortener2015.fuzzywuzzy.domain.ShortURL;
import urlshortener2015.fuzzywuzzy.repository.ClickRepository;
import urlshortener2015.fuzzywuzzy.repository.ShortURLRepository;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@RestController
public class UrlShortenerController {
	private static final Logger log = LoggerFactory
			.getLogger(urlshortener2015.fuzzywuzzy.web.UrlShortenerController.class);

	private String content2;
	private byte[] byteArray;

	@Autowired
	protected ShortURLRepository shortURLRepository;

	@Autowired
	protected ClickRepository clickRepository;
	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class);


	@RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
		ShortURL l = shortURLRepository.findByKey(id);
		//Si existe esa id/shortURL
		if (l != null) {
			String ip = extractIP(request);
			//Realizamos la petición a la api externa que nos proporciona los datos: pais, ciudad, comunidad y coordenadas(latitud y longitud)
			RestTemplate restTemplate = new RestTemplate();
			String data = restTemplate.getForObject("http://api.ipinfodb.com/v3/ip-city/" +
					"?key=1a981acd9a7c266e618f658ed1fa0081b5150555443a77596547a318e4baa7de&ip=" + ip, String.class);
			String[] words = data.split(";");
			//Si la api externa funciona correctamente
			if(words[0].equals("OK")){
				createAndSaveClick(id, ip, words[4], words[5], words[6], words[8], words[9]);
			}
			//Si la api externa no esta diponible
			else{
				createAndSaveClick(id, ip, null, null, null, null, null);
			}


			return createSuccessfulRedirectToResponse(l);
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
											  @RequestParam(value = "vCardName", required = false) String vCardName,
											  @RequestParam(value = "correction", required = false) String correction,
											  HttpServletRequest request) {
		logger.info("Requested new short for uri " + url);
		ShortURL su = createAndSaveIfValid(url, sponsor, brand, vCardName, correction, UUID
				.randomUUID().toString(), extractIP(request));
//		ShortURL su = createAndSaveIfValid(url, sponsor, brand, "test me", correction, UUID
//				.randomUUID().toString(), extractIP(request));
		if (su != null) {
			HttpHeaders h = new HttpHeaders();
			h.setLocation(su.getUri());
			return new ResponseEntity<>(su, h, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/info", method = RequestMethod.POST)
	public ResponseEntity<JSONArray> info(@RequestParam("patron") String patron,
										 @RequestParam("group") String group,
											  HttpServletRequest request) {
		logger.info("Requested info" + patron);
		List<ClickAgr> list = clickRepository.findByGroup(patron, group);
		if (list != null) {
			HttpHeaders h = new HttpHeaders();
			return new ResponseEntity<>(convert(list), h, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/map", method = RequestMethod.GET)
	public ResponseEntity<?> getMap(HttpServletRequest request) {
		List<Click> list = clickRepository.getAll();
		//Si hay datos
		if (list != null) {
			HttpHeaders h = new HttpHeaders();
			return new ResponseEntity<>(convert2(list), h, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	protected void createAndSaveClick(String hash, String ip, String country, String comunidad, String city, String latitud, String longitud) {
		Click cl = new Click(null, hash, new Date(System.currentTimeMillis()),
				null, null, null, ip, country, comunidad, city, latitud, longitud);
		cl = clickRepository.save(cl);
		log.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
	}

	protected ShortURL createAndSaveIfValid(String url, String sponsor,
											String brand, String vCardName, String correction,
											String owner, String ip) {
		UrlValidator urlValidator = new UrlValidator(new String[]{"http",
				"https"});
		if (urlValidator.isValid(url)) {
			String id = Hashing.murmur3_32()
					.hashString(url, StandardCharsets.UTF_8).toString();
			URI uri = linkTo(
					methodOn(urlshortener2015.fuzzywuzzy.web.UrlShortenerController.class).redirectTo(
							id, null)).toUri();

//			RestTemplate restTemplate = new RestTemplate();
//			String qrDef = encodeBase64String(restTemplate.getForObject(qrApi, byte[].class));
			if (correction == null) {
				correction = "L";
			}
			QrGenerator qrGenerator = new QrGenerator(150,150,"UTF-8",correction.charAt(0),uri.toString(),vCardName,0xFFFFFFF,0);
			String qrApi = qrGenerator.getQrApi();
			String qrDef = qrGenerator.getEncodedQr();
			ShortURL su = new ShortURL(id, url,
					uri, sponsor, new Date(
					System.currentTimeMillis()), owner,
					HttpStatus.TEMPORARY_REDIRECT.value(), true, ip, null, qrApi, qrDef);
			return shortURLRepository.save(su);
		} else {
			return null;
		}
	}

	protected String extractIP(HttpServletRequest request) {
		return request.getRemoteAddr();
	}

	public JSONArray  convert(List<ClickAgr> data) {
		JSONArray arr = new JSONArray();
		JSONObject tmp;
		for(int i = 0; i < data.size(); i++) {
			tmp = new JSONObject();
			tmp.put("country",data.get(i).getCountry());
			tmp.put("comunity",data.get(i).getComunity());
			tmp.put("city",data.get(i).getCity());
			tmp.put("target",data.get(i).getTarget());
			tmp.put("count",data.get(i).getCount());
			arr.add(tmp);
		}
		return arr;
	}

	public JSONArray  convert2(List<Click> data) {
		JSONArray arr = new JSONArray();
		JSONObject tmp;
		for(int i = 0; i < data.size(); i++) {
			tmp = new JSONObject();
			tmp.put("latitud",data.get(i).getLatitud());
			tmp.put("longitud",data.get(i).getLongitud());
			tmp.put("ip",data.get(i).getIp());
			arr.add(tmp);
		}
		return arr;
	}
}
