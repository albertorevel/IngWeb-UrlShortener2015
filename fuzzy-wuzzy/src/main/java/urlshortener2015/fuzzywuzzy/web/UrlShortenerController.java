package urlshortener2015.fuzzywuzzy.web;

import com.google.common.hash.Hashing;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import urlshortener2015.fuzzywuzzy.domain.Click;
import urlshortener2015.fuzzywuzzy.domain.ShortURL;
import urlshortener2015.fuzzywuzzy.repository.ClickRepository;
import urlshortener2015.fuzzywuzzy.repository.ShortURLRepository;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.UUID;

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

            return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{id:(?!link|index).*}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectTo(@PathVariable String id, HttpServletRequest request) {
        ShortURL l = shortURLRepository.findByKey(id);
        if (l != null) {
            createAndSaveClick(id, extractIP(request));
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
                                              @RequestParam(value = "logo", required = false) String logo,
                                              HttpServletRequest request) {
        logger.info("Requested new short for uri " + url);
        ShortURL su = createAndSaveIfValid(url, sponsor, brand, vCardName, correction, UUID
                .randomUUID().toString(), extractIP(request), logo);
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

    protected void createAndSaveClick(String hash, String ip) {
        Click cl = new Click(null, hash, new Date(System.currentTimeMillis()),
                null, null, null, ip, null);
        cl = clickRepository.save(cl);
        log.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
    }

    protected ShortURL createAndSaveIfValid(String url, String sponsor,
                                            String brand, String vCardName, String correction,
                                            String owner, String ip, String logo) {
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
            QrGenerator qrGenerator = new QrGenerator(150, 150, "UTF-8", correction.charAt(0), uri.toString(), vCardName, 0xFFFFFFF, 0);
//            String qrApi = qrGenerator.getQrApi();
            String qrApi = uri+"/qr";
			String qrDef = (logo != null ? qrGenerator.getEncodedLogoQr(logo) : qrGenerator.getEncodedQr());
//            String qrDef = qrGenerator.getEncodedQr();
//            String qrDef = qrGenerator.getEncodedLogoQr("http://www.gc.cuny.edu/shared/images/shared/LinkedIn_25px_25px.png");
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
}
