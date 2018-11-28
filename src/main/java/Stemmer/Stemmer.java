package Stemmer;

import java.util.HashMap;
import java.util.Map;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.upperCase;

/**
 * Stemmer class - stems with "SnowBall" stemmer (PorterStemmer2)
 */
public class Stemmer {

    private static Map<String, String> cache = new HashMap<>();
    private HashMap<String, Integer> stemmed;
    private SnowballStemmer snowballStemmer = new englishStemmer();

    /**
     * clears maps
     */
    private void clear() {
        cache.clear();
        stemmed.clear();
    }

    /**
     * Stem the document given after parsing.
     * @param parsedDic the parsed document's dictionary
     * @return HashMap: key - String, the term from the Doc;
     *                  value - Integer, the term frequency within the given Doc.
     */
    public HashMap<String, Integer> stem(HashMap<String, String> parsedDic) {
        stemmed = new HashMap<>();
        for (Map.Entry<String, String> term : parsedDic.entrySet()
        ) {
            if (term.getValue().endsWith("0")) {    //if there is no need to stem it,
                stemmed.put(term.getKey(), toint(term.getValue())); //add it to the dictionary
                continue;
            }
            boolean isUppercase = false;
            StringBuilder stemmedTerm = new StringBuilder(), currentStemmed = new StringBuilder();
            String s = term.getKey();
            if (Character.isUpperCase(s.charAt(0))) { //if the term is uppercase
                isUppercase = true;
                if (cache.containsKey(s)){  //if we stemmed this term
                    if (cache.containsKey(lowerCase(s))){   //if we stemmed the same word as lowercase
                        s= lowerCase(s);    //convert it to lowercase
                        cache.put(term.getKey(),cache.get(lowerCase(s)));// and add it to cache, from now on the term will be stemmed to lowercase
                    }
                    stemmed.put(cache.get(s),toint(term.getValue()));   //add it to the dictionary
                    continue;
                }
            }
            if (cache.containsKey(s)){  //if we stemmed this term
                stemmed.put(cache.get(s),toint(term.getValue()));//add it to the dictionary
                continue;
            }
            if (s.contains(" ")) {//if the term has more than 1 word
                String[] split = split(s, " "); //stem each part
                for (String token : split
                ) {
                    if (cache.containsKey(token)) { //if we stemmed this part
                        currentStemmed.append(cache.get(token)); //add it to the term
                    } else {    //else- stem it
                        snowballStemmer.setCurrent(token);
                        snowballStemmer.stem();
                        currentStemmed.append(snowballStemmer.getCurrent());
                        if (isUppercase){
                            token = upperCase(token);
                        }
                        cache.put(token, currentStemmed.toString());//store in the cache how to stem it
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