package Stemmer;

import java.util.HashMap;
import java.util.Map;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.upperCase;

public class Stemmer {

    private static Map<String, String> cache = new HashMap<>();
    private HashMap<String, Integer> stemmed;
    private SnowballStemmer snowballStemmer = new englishStemmer();

    private void clear() {
        cache.clear();
        stemmed.clear();
    }

    public HashMap<String, Integer> stem(HashMap<String, String> parsedDic) {
        stemmed = new HashMap<>();
        for (Map.Entry<String, String> term : parsedDic.entrySet()
        ) {
            if (term.getValue().endsWith("0")) {
                stemmed.put(term.getKey(), toint(term.getValue()));
                continue;
            }

            //todo - to lower - stem - to upper case
            boolean isUppercase = false;
            StringBuilder stemmedTerm = new StringBuilder(), currentStemmed = new StringBuilder();
            String s = term.getKey();
            if (Character.isUpperCase(s.charAt(0))) {
                isUppercase = true;
                if (cache.containsKey(s)){
                    if (cache.containsKey(lowerCase(s))){
                        s= lowerCase(s);
                        cache.replace(term.getKey(),cache.get(lowerCase(s)));
                    }
                    stemmed.put(cache.get(s),toint(term.getValue()));
                    continue;
                }
            }
            if (cache.containsKey(s)){
                stemmed.put(cache.get(s),toint(term.getValue()));
                continue;
            }
            if (s.contains(" ")) {//if has more than 1 word
                String[] split = split(s, " ");
                for (String token : split
                ) {
                    if (cache.containsKey(token)) {
                        currentStemmed.append(cache.get(token));
                    } else {
                        snowballStemmer.setCurrent(token);
                        snowballStemmer.stem();
                        currentStemmed.append(snowballStemmer.getCurrent());
                        if (isUppercase){
                            token = upperCase(token);
                        }
                        cache.put(token, currentStemmed.toString());
                    }
                    stemmedTerm.append(currentStemmed).append(" ");
                    currentStemmed.setLength(0);
                }
                trim(stemmedTerm.toString());
            } else {
                snowballStemmer.setCurrent(s);
                snowballStemmer.stem();
                stemmedTerm.append(isUppercase?upperCase(snowballStemmer.getCurrent()):snowballStemmer.getCurrent());
            }
            if (stemmed.containsKey(stemmedTerm.toString())) {
                currentStemmed.append(stemmed.get(stemmedTerm.toString()));
                sumFrequency(stemmed, stemmedTerm, currentStemmed, term.getValue());
            } else {
                stemmed.put(isUppercase?upperCase(stemmedTerm.toString()):stemmedTerm.toString(), toint(term.getValue()));
            }
            cache.put(isUppercase?upperCase(s):s, stemmedTerm.toString());

        }
        return stemmed;
    }

    private Integer toint(String value) {
        int num = 1;
        try {
            num = Integer.parseInt(split(value, ",")[0]);
        }
        catch (Exception e){

        }
        return num;
    }

    private void sumFrequency(Map<String, Integer> stemmed, StringBuilder stemmedTerm, StringBuilder currentStemmed, String value) {
        try {
            int x = Integer.parseInt(currentStemmed.toString());
            int y = Integer.parseInt(split(value, ",")[0]);
            currentStemmed.setLength(0);
//            currentStemmed.append(x).append(",1");
            stemmed.replace(stemmedTerm.toString(), y, x+y);
        } catch (Exception e) {

        }
    }
}