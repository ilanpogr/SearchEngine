

public class City {

    private String cityName;
    private String countryName;
    private String currency;
    private String docName;
    private int possition;
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

    public String getDocName() {
        return docName;
    }

    public int getPossition() {
        return possition;
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