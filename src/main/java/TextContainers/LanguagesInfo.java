package TextContainers;


import java.util.HashSet;

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

    private void cleanToken(String[] token) {
        while (token[0].length() > 0 && specialCharSet.contains(token[0].charAt(0))) {
            token[0] = token[0].substring(1);
        }
        while (token[0].length() >= 1 && specialCharSet.contains(token[0].charAt(token[0].length() - 1))) {
            token[0] = token[0].substring(0, token[0].length() - 1);
        }
    }

    private LanguagesInfo() {
        languageInfo_map = new HashSet<>();
        specialCharSet = initSpecialSet();
    }

    public void addLanguageToList(String language) {
        String[] tokens = split(language, " ");
        cleanToken(tokens);
        if (isAlpha(tokens[0]))
            languageInfo_map.add(tokens[0]);
    }

    public void printLanguages() {
        for (String s : languageInfo_map) {
            System.out.println(s);
        }
    }
}
