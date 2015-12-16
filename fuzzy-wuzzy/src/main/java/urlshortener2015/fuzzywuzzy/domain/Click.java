package urlshortener2015.fuzzywuzzy.domain;

import java.sql.Date;

public class Click{

	private Long id;
	private String hash;
	private Date created;
	private String referrer;
	private String browser;
	private String platform;
	private String ip;
	private String country;
	private String comunity;
	private String city;
	private String latitud;
	private String longitud;

	public Click(Long id, String hash, Date created, String referrer,
				 String browser, String platform, String ip, String country,
				 String comunity, String city, String latitud, String longitud) {
		this.id = id;
		this.hash = hash;
		this.created = created;
		this.referrer = referrer;
		this.browser = browser;
		this.platform = platform;
		this.ip = ip;
		this.country = country;
		this.comunity = comunity;
		this.city = city;
		this.latitud = latitud;
		this.longitud = longitud;
	}

	public Long getId() {
		return id;
	}

	public String getHash() {
		return hash;
	}

	public Date getCreated() {
		return created;
	}

	public String getReferrer() {
		return referrer;
	}

	public String getBrowser() {
		return browser;
	}

	public String getPlatform() {
		return platform;
	}

	public String getIp() {
		return ip;
	}

	public String getCountry() {
		return country;
	}

	public String getComunity() {return comunity;}

	public void setComunity(String comunity) {this.comunity = comunity;}

	public String getCity() {return city;}

	public void setCity(String city) {this.city = city;}

	public String getLatitud() {return latitud;}

	public void setLatitud(String latitud) {this.latitud = latitud;}

	public String getLongitud() {return longitud;}

	public void setLongitud(String longitud) {this.longitud = longitud;}
}
