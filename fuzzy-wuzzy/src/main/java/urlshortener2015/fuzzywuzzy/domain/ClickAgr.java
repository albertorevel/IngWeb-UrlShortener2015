package urlshortener2015.fuzzywuzzy.domain;

/**
 * Created by diego on 17/12/15.
 */
public class ClickAgr {

    private String country;
    private String comunity;
    private String city;
    private String target;
    private int count;

    public ClickAgr (String country, String comunity, String city, String target, int count){
        this.country = country;
        this.comunity = comunity;
        this.city = city;
        this.target = target;
        this.count = count;
    }

    public ClickAgr (String country, String comunity, String target, int count){
        this.country = country;
        this.comunity = comunity;
        this.target = target;
        this.count = count;
    }

    public ClickAgr (String country, String target, int count){
        this.country = country;
        this.target = target;
        this.count = count;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getComunity() {
        return comunity;
    }

    public void setComunity(String comunity) {
        this.comunity = comunity;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
