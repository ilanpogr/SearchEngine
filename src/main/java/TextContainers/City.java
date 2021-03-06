package TextContainers;

/**
 * an small assignment to extract from each doc the city that representing the doc
 * CityInfo class updates this class if needed and using this class to be a
 * value in his HashMap.
 */
public class City {

    private String cityName;
    private String countryName;
    private String currency;
    private String population;

    /**
     * Constructor used by CityInfo, straightforward
     * @param cityName : city name
     * @param countryName : country name
     * @param currency : currency
     * @param population : population
     */
    public City(String cityName,String countryName,String currency,String population) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.currency = currency;
        this.population = population;
    }

    @Override
    public String toString(){
        return "Country: " + countryName + ", Population: " + population + ", Currency: " + currency;
    }

    /**
     * Getters
     * @return string
     */
    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPopulation() {
        return population;
    }
}