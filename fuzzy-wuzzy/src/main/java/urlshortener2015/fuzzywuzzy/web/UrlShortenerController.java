package urlshortener2015.fuzzywuzzy.web;

import com.google.common.hash.Hashing;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
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
        //Si existe esa id/shortURL
        if (l != null) {
            saveClick(request, id);
            ResponseEntity<?> re = createSuccessfulRedirectToResponse(l);
            if (l.getTiempo() != null) {
                int tiempoS = Integer.parseInt(l.getTiempo());
                int tiempo = tiempoS * 1000;
                String body = "<!doctype html>\n" +
                        "<head>\n" +
                        "<script type=\"text/javascript\">\n" +
                        "function redireccionar(){\n" +
                        "location.href=\"final/" + id + "\"\n" +
                        "}\n" +
                        "setTimeout (\"redireccionar()\"," + tiempo + ");\n" +
                        "</script>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div class= \"row\">\n" +
                        "<div align=\"center\">\n" +
                        "<h1> Pagina de publicidad </h1>\n" +
                        "<script type=\"text/javascript\">\n" +
                        "var segundos = " + tiempoS + ";\n" +
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
                        "<div id=\"contador\">Le redireccionaremos automáticamente en " + tiempoS + " segundos</div>\n" +
                        "<center>\n" +
                        "<img src=\"http://www.tiempodepublicidad.com/wp-content/themes/gridthemeresponsiveFull/images/tdp/tiempo-de-publicidad.png\" alt=\"Publicidad\">" +
                        "</center>\n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>";
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.TEXT_HTML);
                return new ResponseEntity<Object>(body, responseHeaders, HttpStatus.OK);
            } else {
                return re;
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Método que a partir de la petición, realiza una petición a una API externa que ofrece información sobre su ip
     * y guarda el "click" en la base de datos.
     * @param request
     * @param id
     */
    private void saveClick(HttpServletRequest request, String id) {
        String ip = extractIP(request);
        //Realizamos la petición a la api externa que nos proporciona los datos: pais, ciudad, comunidad y coordenadas(latitud y longitud)
        RestTemplate restTemplate = new RestTemplate();
        String data = restTemplate.getForObject("http://api.ipinfodb.com/v3/ip-city/" +
                "?key=1a981acd9a7c266e618f658ed1fa0081b5150555443a77596547a318e4baa7de&ip=" + ip, String.class);
        String[] words = data.split(";");
        //Si la api externa funciona correctamente
        if (words[0].equals("OK")) {
            createAndSaveClick(id, ip, words[4], words[5], words[6], Double.parseDouble(words[8]),Double.parseDouble(words[9]));
        }
        //Si la api externa no esta diponible
        else {
            createAndSaveClick(id, ip, null, null, null, 0, 0);
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
                                              HttpServletRequest request) {
        logger.info("Requested new short for uri " + url);
        ShortURL su = createAndSaveIfValid(url, sponsor, brand, vCardName, correction, UUID
                .randomUUID().toString(), extractIP(request), logo, tiempo);
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

    @RequestMapping(value = "/meterDatos", method = RequestMethod.POST)
    public ResponseEntity<String> meterDatos(HttpServletRequest request) {
        logger.info("Metiendo datos.");
        clickRepository.meterDatos();
        shortURLRepository.meterDatos();
        HttpHeaders h = new HttpHeaders();
        return new ResponseEntity<>(h, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public ResponseEntity<JSONArray> infoDate(@RequestParam("patron") String patron,
                                          @RequestParam("group") String group,
                                          @RequestParam("yearSince") String yearSince,
                                          @RequestParam("monthSince") String monthSince,
                                          @RequestParam("daySince") String daySince,
                                          @RequestParam("yearUntil") String yearUntil,
                                          @RequestParam("monthUntil") String monthUntil,
                                          @RequestParam("dayUntil") String dayUntil,

                                          HttpServletRequest request) {
        logger.info("Requested info with patron: " + patron);
        boolean dateSince, dateUntil = false;
        int yearSince2 = 0, monthSince2 = 0,daySince2 = 0,yearUntil2 = 0,monthUntil2 = 0,dayUntil2 = 0;
        List<ClickAgr> list = null;
        List<Click> list1 = null;

        //Comprobamos que las fechas son correctas
        dateSince = testDate(daySince + "/" + monthSince + "/" + yearSince);
        dateUntil = testDate(dayUntil + "/" + monthUntil + "/" + yearUntil);

        //Si hay que acotar entre 2 fechas
        if (dateSince && dateUntil) {
            //Pasamos las fechas a enteros
            yearSince2 = Integer.parseInt(yearSince);
            monthSince2 = Integer.parseInt(monthSince);
            daySince2 = Integer.parseInt(daySince);
            yearUntil2 = Integer.parseInt(yearUntil);
            monthUntil2 = Integer.parseInt(monthUntil);
            dayUntil2 = Integer.parseInt(dayUntil);

            if (yearUntil2 > yearSince2 || (yearUntil2 == yearSince2 && monthUntil2 > monthSince2) ||
                    (yearUntil2 == yearSince2 && monthUntil2 == monthSince2 && dayUntil2 >= daySince2)) {
                list = clickRepository.findByGroupBounded(patron, group, new Date(yearSince2, monthSince2, daySince2),
                        new Date(yearUntil2, monthUntil2, dayUntil2));
                list1 = clickRepository.getCoordenatesBounded(patron, new Date(yearSince2, monthSince2, daySince2),
                        new Date(yearUntil2, monthUntil2, dayUntil2));
            }
        } else if (dateSince) { //Si sólo es desde una fecha
            //Pasamos la fecha a entero
            yearSince2 = Integer.parseInt(yearSince);
            monthSince2 = Integer.parseInt(monthSince);
            daySince2 = Integer.parseInt(daySince);
            list = clickRepository.findByGroupSince(patron, group, new Date(yearSince2, monthSince2, daySince2));
            list1 = clickRepository.getCoordenatesSince(patron, new Date(yearSince2, monthSince2, daySince2));
        } else if (dateUntil) { //Si sólo es hasta una fecha
            //Pasamos la fecha a entero
            yearUntil2 = Integer.parseInt(yearUntil);
            monthUntil2 = Integer.parseInt(monthUntil);
            dayUntil2 = Integer.parseInt(dayUntil);
            list = clickRepository.findByGroupUntil(patron, group, new Date(yearUntil2, monthUntil2, dayUntil2));
            list1 = clickRepository.getCoordenatesUntil(patron, new Date(yearUntil2, monthUntil2, dayUntil2));
        } else{
            list = clickRepository.findByGroup(patron,group);
            list1 = clickRepository.getCoordenatesForGroup(patron);
        }

        if (list != null) {
            HttpHeaders h = new HttpHeaders();
            JSONArray listas = new JSONArray();
            listas.add(convert(list));
            listas.add(convert2(list1));
            return new ResponseEntity<>(listas, h, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/infoArea", method = RequestMethod.POST)
    public ResponseEntity<JSONArray> infoArea(@RequestParam("patron") String patron,
                                          @RequestParam("group") String group,
                                          @RequestParam("latitudSince") double latitudSince,
                                          @RequestParam("latitudUntil") double latitudUntil,
                                          @RequestParam("longitudSince") double longitudSince,
                                          @RequestParam("longitudUntil") double longitudUntil,
                                          HttpServletRequest request) {
        logger.info("Requested info with patron: " + patron);

        List<ClickAgr> list = clickRepository.findByGroupArea(patron, group, latitudSince, latitudUntil,
                longitudSince, longitudUntil);
        List<Click> list1 = clickRepository.getByArea(patron, latitudSince, latitudUntil, longitudSince, longitudUntil);
        if (list != null && list1 != null && latitudSince <= latitudUntil && longitudSince <= longitudUntil) {
            if(list1 != null) {
                HttpHeaders h = new HttpHeaders();
                JSONArray listas = new JSONArray();
                listas.add(convert(list));
                listas.add(convert2(list1));
                return new ResponseEntity<>(listas, h, HttpStatus.CREATED);
            } else{
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Método privado que comprueba si una fecha es válida
     * @param fecha
     * @return
     */
    private boolean testDate(String fecha) {
        try {
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            formatoFecha.setLenient(false);
            formatoFecha.parse(fecha);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }


    protected void createAndSaveClick(String hash, String ip, String country, String comunidad, String city, double latitud, double longitud) {
        Click cl = new Click(null, hash, new Date(System.currentTimeMillis()),
                null, null, null, ip, country, comunidad, city, latitud, longitud);
        cl = clickRepository.save(cl);
        log.info(cl != null ? "[" + hash + "] saved with id [" + cl.getId() + "]" : "[" + hash + "] was not saved");
    }

    /**
     * Método que convierte un objeto List de ClickAgr en una Array en formato JSON y lo devuelve.
     * @param data List de ClickAgr
     * @return JSONArray
     */
    public JSONArray convert(List<ClickAgr> data) {
        JSONArray arr = new JSONArray();
        JSONObject tmp;
        for (int i = 0; i < data.size(); i++) {
            tmp = new JSONObject();
            tmp.put("country", data.get(i).getCountry());
            tmp.put("comunity", data.get(i).getComunity());
            tmp.put("city", data.get(i).getCity());
            tmp.put("target", data.get(i).getTarget());
            tmp.put("count", data.get(i).getCount());
            arr.add(tmp);
        }
        return arr;
    }

    /**
     * Método que convierte un objeto List de Click en una Array en formato JSON y lo devuelve.
     * Sólo convierte la información necesaria.
     * @param data List de ClickAgr
     * @return JSONArray
     */
    public JSONArray convert2(List<Click> data) {
        JSONArray arr = new JSONArray();
        JSONObject tmp;
        for (int i = 0; i < data.size(); i++) {
            tmp = new JSONObject();
            tmp.put("latitud", data.get(i).getLatitud());
            tmp.put("longitud", data.get(i).getLongitud());
            tmp.put("ip", data.get(i).getIp());
            arr.add(tmp);
        }
        return arr;
    }

    protected ShortURL createAndSaveIfValid(String url, String sponsor,
                                            String brand, String vCardName, String correction,
                                            String owner, String ip, String logo, String tiempo) {
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
            QrGenerator qrGenerator = new QrGenerator(150, 150, "UTF-8", correction.charAt(0), uri.toString(), vCardName, 0xFFFFFFF, 0x0000ff);
//            String qrApi = qrGenerator.getQrApi();
            String qrApi = uri + "/qr";
            String qrDef = (logo != null ? qrGenerator.getEncodedLogoQr(logo) : qrGenerator.getEncodedQr());
//            String qrDef = qrGenerator.getEncodedQr();
//            String qrDef = qrGenerator.getEncodedLogoQr("http://www.gc.cuny.edu/shared/images/shared/LinkedIn_25px_25px.png");
            ShortURL su = new ShortURL(id, url,
                    uri, sponsor, new Date(
                    System.currentTimeMillis()), owner,
                    HttpStatus.TEMPORARY_REDIRECT.value(), true, ip, null, qrApi, qrDef, tiempo);
            return shortURLRepository.save(su);
        } else {
            return null;
        }
    }

    protected String extractIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

}
