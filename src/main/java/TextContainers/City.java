package TextContainers;

import java.util.ArrayList;

import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.truncate;

public class City {

    private String cityName;
    private String countryName;
    private String currency;
    private ArrayList<String> docNames;
    private ArrayList<Integer> possitions;
    private String population;

    public City(String cityName,String countryName,String currency,String population) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.currency = currency;
        this.population = population;
        docNames = new ArrayList<>();
        possitions = new ArrayList<>();
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public ArrayList<String> getDocName() {
        return docNames;
    }

    public ArrayList<Integer> getPossition() {
        return possitions;
    }

    public void addPossition(int x) {
        possitions.add(x);
    }

    public void addPossitions(int[] a) {
        for (int i : a) {
            possitions.add(i);
        }
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }




}