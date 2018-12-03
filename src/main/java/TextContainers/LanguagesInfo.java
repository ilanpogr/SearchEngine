package TextContainers;


import java.util.HashSet;

public class LanguagesInfo {

    private static LanguagesInfo languagesInfo_instance = null;


    private HashSet<String> citiesInfo_map;

    /**
     * regular singleton
     * @return the LanguagesInfo instance
     */
    public static LanguagesInfo getInstance() {
        if (languagesInfo_instance == null) {
            languagesInfo_instance = new LanguagesInfo();
        }
        return languagesInfo_instance;
    }

    private LanguagesInfo(){
        citiesInfo_map = new HashSet<>();
    }

    public void addLanguageToList(String language){
        citiesInfo_map.add(language);
    }

    public void printLanguages(){
        for (String s : citiesInfo_map) {
            System.out.println(s);
        }
    }
}
