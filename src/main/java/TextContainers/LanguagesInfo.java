package TextContainers;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.split;

public class LanguagesInfo {

    private static LanguagesInfo languagesInfo_instance = null;

    private static HashSet<Character> specialCharSet;

    private HashSet<String> languageInfo_map;

    /**
     * regular singleton
     *
     * @return the LanguagesInfo instance
     */
    public static LanguagesInfo getInstance() {
        if (languagesInfo_instance == null) {
            languagesInfo_instance = new LanguagesInfo();
        }
        return languagesInfo_instance;
    }

    /**
     * Ctor
     *
     */
    private LanguagesInfo() {
        languageInfo_map = new HashSet<>();
        specialCharSet = initSpecialSet();
    }

    /**
     * init all chars we want to clean from token.
     *
     * @return : the char set after initialize.
     */
    private static HashSet<Character> initSpecialSet() {
        specialCharSet = new HashSet<>();
        Character[] characters = new Character[]{'[', '(', '{', '`', ')', '<', '|', '&', '~', '+', '^', '@', '*', '?', '.', '>', ';', '_', '\'', ':', ']', '/', '\\', '}', '!', '=', '#', ',', '"', '-'};

        for (int i = 0; i < characters.length; ++i) {
            specialCharSet.add(characters[i]);
        }
        return specialCharSet;
    }

    /**
     * Cleans the words from special chars
     *
     * @param words - the words that will be clean
     */
    private void cleanToken(String[] words) {
        for (; words[0].length() > 0 && specialCharSet.contains(words[0].charAt(0));words[0] = words[0].substring(1));
        for (; words[0].length() >= 1 && specialCharSet.contains(words[0].charAt(words[0].length() - 1));words[0] = words[0].substring(0, words[0].length() - 1));
    }

    /**
     * adds a language to the Languages List
     * @param language - the added language
     */
    public void addLanguageToList(String language) {
        String[] tokens = split(language, " ");
        cleanToken(tokens);
        if (isAlpha(tokens[0]))
            languageInfo_map.add(tokens[0]);
    }

    /**
     * prints all languages
     */
    public void printLanguages() {
        for (String s : languageInfo_map) {
            System.out.println(s);
        }
    }

    /**
     * get a list of all the languages
     * @return ArrayList of Languages (strings)
     */
    public ArrayList<String> getLanguagesAsList() {
        return new ArrayList<>(languageInfo_map);
    }

    public boolean contains(String lan){
        return languageInfo_map.contains(lan);
    }
}
