package TextContainers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.apache.commons.lang3.StringUtils.*;


public class CityInfo {

    private static CityInfo cityInfo_instance = null;

    private JsonElement entries;


    public static CityInfo getInstance() {
        if (cityInfo_instance == null) {
            cityInfo_instance = new CityInfo();
        }
        return cityInfo_instance;
    }

    /**
     * Singleton because we want to connect to the api once, and then read from memory all the cities.
     * creating URL and reading input --> taken from Oracle.
     */
    private CityInfo() {
        try {
            URL geoByteUrl = new URL("https://restcountries.eu/rest/v2/all?fields=capital;name;population;currencies");
            URLConnection yc = geoByteUrl.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    yc.getInputStream()));
//            String inputLine = in.readLine();
            JsonParser parser = new JsonParser();
            entries = parser.parse(in);
            in.close();
        } catch (IOException e) {
            entries = null;
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
     * finding the city name in all entries and returning the info as a 4 cells String array.
     * else.. returning NULL;
     * problems with Hong Kong.. so We'll put it manually
     *
     * @param tag : <<F P=104> tag from files -> inside the city name.
     * @return info: [0] - city name, [1] - country, [2] - population, [3] - currencies
     */
    public String[] getInfo(String tag) {
        if (tag != null && !tag.equals("")) {
            if (containsIgnoreCase(tag,"Hong Kong")){
                return new String[] {"HONG KONG", "Hong Kong", "7.39M", "Hong Kong Dollar"};
            }
            String cityNameShort = "";
            String cityNameLong = "";
            if (tag.length() > 1) {
                String[] tokens = split(tag, " ");
                cityNameShort = Character.toUpperCase(tokens[0].charAt(0)) + substring(tokens[0], 1);
                cityNameLong = Character.toUpperCase(tokens[0].charAt(0)) + substring(lowerCase(tokens[0]), 1) + " " + Character.toUpperCase(tokens[1].charAt(0)) + substring(lowerCase(tokens[1]), 1);
            } else {
                cityNameShort = Character.toUpperCase(tag.charAt(0)) + substring(lowerCase(tag), 1);
            }

            if (entries != null) {
                String[] info = {"", "", "", ""};
                JsonArray fullDetailsArray = entries.getAsJsonArray();
                for (int i = 0; i < fullDetailsArray.size(); i++) {
                    JsonObject citiesInfo = (JsonObject) (fullDetailsArray.get(i));
                    String capitalCity = citiesInfo.get("capital").getAsString();
                    if ((!cityNameShort.equals("") && capitalCity.equals(cityNameShort)) || (!cityNameLong.equals("") && capitalCity.equals(cityNameLong))) {
                        info[0] = upperCase(citiesInfo.get("name").getAsString());
                        info[1] = capitalCity;
                        info[2] = parsePopulation(citiesInfo.get("population").getAsString());
                        JsonArray JE = citiesInfo.getAsJsonArray("currencies");
                        for (Object o : JE) {
                            JsonObject jsonLineItem = (JsonObject) o;
                            JsonElement val = jsonLineItem.get("code");
                            info[3] = val.getAsString();
                        }
                        return info;
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        CityInfo c = getInstance();
        String[] info = c.getInfo("Hong Kong ZHONGGUO TONGXUN SHEO");
        if (info != null) {
            for (String s : info) {
                System.out.println(s);
            }
        }
    }
}
