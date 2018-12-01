package TextContainers;

import java.util.ArrayList;

public class City {

    private String cityName;
    private String countryName;
    private String currency;
    private ArrayList<String> docNames;
    private ArrayList<Integer> possitions;
    private double population;


    public City(String cityName) {
        this.cityName = cityName;
        //todo- add all values from an API
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

    public void addPossition(int x){
        possitions.add(x);
    }

    public void addPossitions(int [] a){
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

    public double getPopulation() {
        return population;
    }

    public void setPopulation(double population) {
        this.population = population;
    }
}