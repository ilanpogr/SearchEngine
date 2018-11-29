package Stemmer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Stemmer class - stems with "SnowBall" stemmer (PorterStemmer2)
 */
public class Stemmer {

    private static Map<String, String> cache = new HashMap<>();
    private HashMap<String, MutablePair<Integer,String>> stemmed;
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
     *
     * @param parsedDic the parsed document's dictionary
     * @return HashMap: key - String, the term from the Doc;
     * value - Integer, the term frequency within the given Doc.
     */
    public HashMap<String, MutablePair<Integer, String>> stem(HashMap<String, String> parsedDic) {
        stemmed = new HashMap<>();
        for (Map.Entry<String, String> term : parsedDic.entrySet()
        ) {
            if (term.getValue().endsWith("0")) {    //if there is no need to stem it,
                stemmed.put(term.getKey(), toPair(term.getValue())); //add it to the dictionary
                continue;
            }
            boolean isUppercase = false;
            StringBuilder stemmedTerm = new StringBuilder(), currentStemmed = new StringBuilder();
            String s = term.getKey();
            if (Character.isUpperCase(s.charAt(0))) { //if the term is uppercase
                isUppercase = true;
                if (cache.containsKey(s)) {  //if we stemmed this term
                    if (cache.containsKey(lowerCase(s))) {   //if we stemmed the same word as lowercase
                        s = lowerCase(s);    //convert it to lowercase
                        cache.put(term.getKey(), cache.get(lowerCase(s)));// and add it to cache, from now on the term will be stemmed to lowercase
                    }
                    stemmed.put(cache.get(s), toPair(term.getValue()));   //add it to the dictionary
                    continue;
                }
            }
            if (cache.containsKey(s)) {  //if we stemmed this term
                stemmed.put(cache.get(s), toPair(term.getValue()));//add it to the dictionary
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
                        if (isUppercase) {
                            token = upperCase(token);
                        }
                        cache.put(token, currentStemmed.toString());    //store in the cache how to stem it
                    }
                    stemmedTerm.append(currentStemmed).append(" ");
                    currentStemmed.setLength(0);
                }
                trim(stemmedTerm.toString());
            } else {    //else - the term is a single word
                snowballStemmer.setCurrent(s);
                snowballStemmer.stem();
                stemmedTerm.append(isUppercase ? upperCase(snowballStemmer.getCurrent()) : snowballStemmer.getCurrent());
            }
            if (stemmed.containsKey(stemmedTerm.toString())) {// if we stemmed the word in this doc
                sumFrequency(stemmed, stemmedTerm, toPair(replaceChars(stemmed.get(stemmedTerm.toString()).toString(),"()","")), toPair(term.getValue()));//sum the values
            } else {
                stemmed.put(isUppercase ? upperCase(stemmedTerm.toString()) : stemmedTerm.toString(), toPair(term.getValue()));
            }
            cache.put(isUppercase ? upperCase(s) : s, stemmedTerm.toString());

        }
        return stemmed;
    }

    /**
     * converts the term value to an Integer after the stemming
     *
     * @param value - the term's value in the dictionary
     * @return the Integer value
     */
    private MutablePair<Integer,String> toPair(String value) {
        int num = 1;
        String positions ="";
        try {
            String [] values = split(value, ",");
            num = Integer.parseInt(values[0]);
            positions = substringAfter(join(values,","),",");
        } catch (Exception e) {
            e.printStackTrace();
            positions = substringAfter(value,",");
        }
        return new MutablePair<>(num,positions);
    }

    /**
     * adds the given value to the term's value in the dictionary
     * @param stemmed        - the Map after stem.
     * @param stemmedTerm    - the stemmed term to sum it's value.
     * @param currentStemmedValue - the value that need to be added.
     * @param value          - the value that need to be added to.
     */
    private void sumFrequency(HashMap<String, MutablePair<Integer, String>> stemmed, StringBuilder stemmedTerm, MutablePair<Integer, String> currentStemmedValue, Pair<Integer,String> value) {
        try {
            currentStemmedValue.setLeft(currentStemmedValue.getLeft()+value.getLeft());
            stemmed.put(stemmedTerm.toString(), currentStemmedValue );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}