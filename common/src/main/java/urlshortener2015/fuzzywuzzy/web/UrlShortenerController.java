package urlshortener2015.fuzzywuzzy.web;

import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;
import urlshortener2015.common.domain.Click;
import urlshortener2015.common.domain.ShortURL;
import urlshortener2015.fuzzywuzzy.repository.ClickRepository;
import urlshortener2015.fuzzywuzzy.repository.ShortURLRepository;

import com.google.common.hash.Hashing;

@RestController
public class UrlShortenerController {
    private static final Logger log = LoggerFactory
            .getLogger(UrlShortenerController.class);
    @Autowired
    protected ShortURLRepository shortURLRepository;

    @Autowired
    protected ClickRepository clickRepository;

    @RequestMapping(value = "/{id:(?!link).*}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectTo(@PathVariable String id,
                                        HttpServletRequest request) {
        ShortURL l = shortURLRepository.findByKey(id);
        if (l != null) {
            createAndSaveClick(id, extractIP(request));
            return createSuccessfulRedirectToResponse(l);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    protected void createAndSaveClick(String hash, String ip) {
        Click cl = new Click(null, hash, new Date(System.currentTimeMillis()),
                null, null, null, ip, null);
        cl = clickRepository.save(cl);
        log.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
    }

    protected String extractIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    protected ResponseEntity<?> createSuccessfulRedirectToResponse(ShortURL l) {
        HttpHeaders h = new HttpHeaders();
        if(l.getPublicidad()!=null){
            h.add("Redireccion",l.getTarget());
            h.setLocation(URI.create(l.getPublicidad()));
        }
        else{
            h.setLocation(URI.create(l.getTarget()));
        }
        return new ResponseEntity<>(h, HttpStatus.valueOf(l.getMode()));
    }

//    @RequestMapping(value = "/link", method = RequestMethod.POST)
    public ResponseEntity<ShortURL> shortener(@RequestParam("url") String url,
                                              @RequestParam(value = "sponsor", required = false) String sponsor,
                                              @RequestParam(value = "brand", required = false) String brand,
                                              @RequestParam(value = "vCardName", required = false) String vCard,
                                              @RequestParam(value = "correction", required = false) String correction,
                                              HttpServletRequest request) {
        ShortURL su = createAndSaveIfValid(url, sponsor, brand, vCard, correction, UUID
                .randomUUID().toString(), extractIP(request));
        if (su != null) {
            HttpHeaders h = new HttpHeaders();
            h.setLocation(su.getUri());
            return new ResponseEntity<>(su, h, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
                    methodOn(UrlShortenerController.class).redirectTo(
                            id, null)).toUri();
            String qrApi = createQrQuery(uri, vCardName, correction);
            RestTemplate restTemplate = new RestTemplate();
            String qrDef = encodeBase64String(restTemplate.getForObject(qrApi, byte[].class));
            ShortURL su = new ShortURL(id, url,
                    uri, sponsor, new Date(
                    System.currentTimeMillis()), owner,
                    HttpStatus.TEMPORARY_REDIRECT.value(), true, ip, null, qrApi, qrDef, null);
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
}
