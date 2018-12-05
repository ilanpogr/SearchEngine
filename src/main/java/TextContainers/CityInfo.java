package TextContainers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Singleton class: contains the cities from corpus dictionary.
 * controls  all operations containing dealing with city name, and it's info.
 */
public class CityInfo {

    private static CityInfo cityInfo_instance = null;


    private HashMap<String, City> citiesInfo_map;
    private HashMap<String, String> infoEntries;
    private HashSet<String> citiesNotAPI = new HashSet<>();

    public HashSet<String> getCitiesNotAPI() {
        return citiesNotAPI;
    }

    /**
     * regular singleton
     * @return the cityInfo instance
     */
    public static CityInfo getInstance() {
        if (cityInfo_instance == null) {
            cityInfo_instance = new CityInfo();
        }
        return cityInfo_instance;
    }

    /**
     * NO CLASS NEED AN ACCESS TO THE CITIES FROM CORPUS.
     *
     * @param key: city name
     * @return: the city if map has it. else returning null as map
     * does when get method called.
     */
    public City getValueFromCitiesDictionary(String key){
        return citiesInfo_map.get(key);
    }

    /**
     * Singleton because we want to connect to the api once, and then read from memory all the cities.
     * ALL ENTRIES inserted to HashMap for faster extraction.
     * creating URL and reading input --> taken from Oracle.
     */
    private CityInfo() {
        try {
            citiesInfo_map = new HashMap<>();
            infoEntries = new HashMap<>();
            URL geoByteUrl = new URL("https://restcountries.eu/rest/v2/all?fields=capital;name;population;currencies");
            URLConnection yc = geoByteUrl.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    yc.getInputStream()));
//            String inputLine = in.readLine();
            JsonParser parser = new JsonParser();
            JsonElement entries = parser.parse(in);
            in.close();

            JsonArray fullDetailsArray = entries.getAsJsonArray();
            for (int i = 0; i < fullDetailsArray.size(); i++) {
                String wholeLine = fullDetailsArray.get(i).toString();
                String city = substringBetween(wholeLine, "\"capital\":\"", "\"");
                if (city.equals("")) {
                    continue;
                }
                String country = substringBetween(wholeLine, "}],\"name\":\"", "\"");
                String population = substringBetween(wholeLine, "\"population\":", "}");
                population = parsePopulation(population);
                String currency = substringBetween(wholeLine, "\"code\":\"", "\"");
                infoEntries.put(upperCase(city), country + "," + population + "," + currency);
            }


        } catch (IOException e) {
            System.out.println("ERROR WITH CONNECTING TO API");
        }
    }

    /**
     * Parsing the number of population by the rules applied to the parser. 2 digits after '.' delimiter.
     *
     * @param population : the number we want to parse.
     * @return : population parsed.
     */
    private String parsePopulation(String population) {
        if (population.length() <= 4) { // Thousand
            return population;
        } else if (population.length() <= 6) { // Thousand
            population = substring(population, 0, population.length() - 3) + '.' + truncate(substring(population, population.length() - 3), 2) + 'K';
        } else if (population.length() <= 9) { // Million
            population = population.substring(0, population.length() - 6) + '.' + truncate(population.substring(population.length() - 6), 2) + 'M';
        } else { // more than Million --> represented with B (Billion)
            population = population.substring(0, population.length() - 9) + '.' + truncate(population.substring(population.length() - 9), 2) + 'B';
        }
        return population;
    }


    /**
     * finding the city name in all entries and inserting city in citiesInfo HashMap of all corpus by city name key.
     * problems with Hong Kong.. so We'll put it manually
     * String[] info: [0] - country , [1] - population , [2] - currency
     *
     * @param tag : <<F P=104> tag from files (without the tag) -> inside might be the city name.
     */
    public void setInfo(String tag, Doc doc) {
        if (containsIgnoreCase(tag, "Hong Kong")) {
            doc.setCity("HONG KONG");
            citiesInfo_map.put("HONG KONG", new City("HONG KONG", "Hong Kong", "Hong Kong Dollar", "7.39M"));
            return;
        }
        String cityNameShort = "";
        String cityNameLong = "";
        String[] tokens = split(tag, " ");
        if (tokens.length > 1) {
            cityNameShort = upperCase(tokens[0]);
            cityNameLong = upperCase(tokens[0] + " " + tokens[1]);
        } else {
            cityNameShort = upperCase(tokens[0]);
        }
        if (citiesInfo_map.containsKey(cityNameShort)) {
            doc.setCity(cityNameShort);
        } else if (citiesInfo_map.containsKey(cityNameLong)) {
            doc.setCity(cityNameLong);
        } else {
            String cityName = "";
            boolean found = false;
            if (infoEntries.containsKey(cityNameShort)) {
                cityName = cityNameShort;
                found = true;
            } else if (infoEntries.containsKey(cityNameLong)) {
                cityName = cityNameLong;
                found = true;
            }
            if (found) {
                doc.setCity((cityName));
                String[] parameters = split(infoEntries.get(cityName), ",");
                City city = new City(cityName, parameters[0], parameters[2], parameters[1]);
                citiesInfo_map.put(cityName, city);
            } else {
                doc.setCity(cityNameShort);
                citiesNotAPI.add(cityNameShort);
            }
        }
    }

    /**
     * for debugging.. print all the cities registered from corpus.
     */
    public void printCities() {
        for (String name : citiesInfo_map.keySet()) {
            System.out.println(name + "  -->  " + citiesInfo_map.get(name));
        }
    }


}


