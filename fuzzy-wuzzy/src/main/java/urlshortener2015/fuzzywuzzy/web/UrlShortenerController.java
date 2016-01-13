package urlshortener2015.fuzzywuzzy.web;

import javax.print.attribute.standard.Media;
import javax.servlet.http.HttpServletRequest;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.tomcat.util.codec.binary.Base64;
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
import org.springframework.web.bind.annotation.*;
import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ShortURL;
import urlshortener2015.fuzzywuzzy.repository.ClickRepository;
import urlshortener2015.fuzzywuzzy.repository.ShortURLRepository;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.spec.ECField;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
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

    @RequestMapping(value = "/{id:(?!link|index).*}/qr", method = RequestMethod.GET)
    public ResponseEntity<?> getImage(@PathVariable String id, HttpServletRequest request) {
        ShortURL shortURL = shortURLRepository.findByKey(id);
        if (shortURL != null) {
            String base64 = shortURL.getQrCode();
            byte[] bytes = Base64.decodeBase64(base64);

            //Set headers
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
	@RequestMapping(value = "/final/{id:(?!link|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> getFinal(@PathVariable String id, HttpServletRequest request) {
		ShortURL l = shortURLRepository.findByKey(id);
		HttpHeaders h = new HttpHeaders();
		h.setLocation(URI.create(l.getTarget()));
		return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
	}
	@RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
	public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
		ShortURL l = shortURLRepository.findByKey(id);
		if (l != null) {
			createAndSaveClick(id, extractIP(request));
			ResponseEntity<?> re = createSuccessfulRedirectToResponse(l);
			if(!l.getTiempo().equals("")){
                int tiempoS = Integer.parseInt(l.getTiempo());
                int tiempo = tiempoS*1000;
				String body = "<!doctype html>\n" +
						"<head>\n" +
						"<script type=\"text/javascript\">\n" +
						"function redireccionar(){\n" +
						"location.href=\"final/" + id +"\"\n" +
						"}\n" +
						"setTimeout (\"redireccionar()\"," + tiempo +");\n" +
						"</script>\n" +
						"</head>\n" +
						"<body>\n" +
                        "<div class= \"row\">\n" +
                        "<div align=\"center\">\n" +
                        "<h1> Pagina de publicidad </h1>\n" +
                        "<script type=\"text/javascript\">\n" +
                        "var segundos = "+ tiempoS +";\n" +
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
                        "<div id=\"contador\">Le redireccionaremos automáticamente en "+tiempoS+" segundos</div>\n" +
                        "<center>\n" +
                        "<img src=\"http://www.tiempodepublicidad.com/wp-content/themes/gridthemeresponsiveFull/images/tdp/tiempo-de-publicidad.png\" alt=\"Publicidad\">" +
                        "</center>\n" +
                        "</div>\n" +
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
                                              @RequestParam(value = "vCardName", required = false) String vCardName,
                                              @RequestParam(value = "correction", required = false) String correction,
                                              @RequestParam(value = "logo", required = false) String logo,
											  @RequestParam(value = "tiempo", required = false) String tiempo,
											  @RequestParam(value = "qrSize", required = false) String qrSize,
											  @RequestParam(value = "fgColour", required = false) String fgCol,
											  @RequestParam(value = "bgColour", required = false) String bgCol,
											  @RequestParam(value = "external", required = false) String external,
                                              HttpServletRequest request) {
        logger.info("Requested new short for uri " + url);
        ShortURL su = createAndSaveIfValid(url, sponsor, brand, vCardName, correction, UUID
                .randomUUID().toString(), extractIP(request), logo, tiempo, qrSize, fgCol, bgCol, external);
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
                                            String owner, String ip, String logo, String tiempo,
                                            String qrPSize, String fgPColour, String bgPColour, String external) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http",
                "https"});
        if (urlValidator.isValid(url)) {
            String id = Hashing.murmur3_32()
                    .hashString(url, StandardCharsets.UTF_8).toString();
            URI uri = linkTo(
                    methodOn(urlshortener2015.fuzzywuzzy.web.UrlShortenerController.class).redirectTo(
                            id, null)).toUri();

            int qrSize = 500;
            int bgColour = 0xFFFFFF;
            int fgColour = 0;
            if (correction == null) {
                correction = "L";
            }
            if( tiempo == null){
                tiempo = "";
            }
            if (qrPSize != null) {
               try {
                   qrSize = Integer.parseInt(qrPSize);
               } catch (NumberFormatException e) {
                   qrSize = 500;
               }
            }
            if (bgPColour != null) {
                try {
                    bgColour = Integer.parseInt(bgPColour);
            } catch (NumberFormatException e) {
                bgColour = 0;
            }
            }
            if (fgPColour != null) {
                try {
                    fgColour = Integer.parseInt(fgPColour);
                } catch (NumberFormatException e) {
                    fgColour = 16777215;
                }
            }


            QrGenerator qrGenerator = new QrGenerator(qrSize, qrSize, "UTF-8", correction.charAt(0), uri.toString(), vCardName, bgColour, fgColour);
            String qrApi;
            String qrDef;
            if (external == null) {
                qrApi = qrGenerator.getQrApi();
                qrDef = (logo != null ? qrGenerator.getEncodedLogoQr(logo) : qrGenerator.getEncodedQr());
            } else {
                qrApi = qrGenerator.getGoogleQrApi();
                RestTemplate restTemplate = new RestTemplate();
			    qrDef = encodeBase64String(restTemplate.getForObject(qrApi, byte[].class));
            }

            if (qrDef.length() > 29950) {
                qrDef = "";
            }
            ShortURL su = new ShortURL(id, url,
                    uri, sponsor, new Date(
                    System.currentTimeMillis()), owner,
                    HttpStatus.TEMPORARY_REDIRECT.value(), true, ip, null, qrApi, qrDef, tiempo);

            ShortURL shortURL = shortURLRepository.findByKey(id);
            if (shortURL != null) {
                shortURLRepository.update(su);
                return su;
            } else return shortURLRepository.save(su);
        } else {
            return null;
        }
    }

    protected String extractIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
