package Stemmer;

import java.util.HashMap;
import java.util.Map;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trim;
//import

public class Stemmer {

    private static Map<String,String> cache = new HashMap<>();
    private Map<String,String> stemmed;
    private SnowballStemmer snowballStemmer = new englishStemmer();

    public void clear(){
        cache.clear();
        stemmed.clear();
    }

    public Map<String,String> stem(Map<String,String> parsedDic){
        stemmed = new HashMap<>();
        for (Map.Entry<String,String> term : parsedDic.entrySet()
             ) {
            if (term.getValue().endsWith("0")){
                stemmed.put(term.getKey(),term.getValue());
                continue;
            } else{
                if (cache.containsKey(term.getKey())){//if we stemmed it
                    stemmed.put(cache.get(term.getKey()),term.getValue());//add it
                    continue;
                }
                StringBuilder stemmedTerm = new StringBuilder(),currentStemmed = new StringBuilder();;
                if (term.getKey().contains(" ")){//if has more than 1 word
                    String [] split = split(term.getKey()," ");
                    for (String token:split
                         ) {
                        if (cache.containsKey(token)){
                            currentStemmed.append(cache.get(token));
                        }else{
                            snowballStemmer.setCurrent(token);
                            snowballStemmer.stem();
                            currentStemmed.append(snowballStemmer.getCurrent());
                            cache.put(token, currentStemmed.toString());
                        }
                        stemmedTerm.append(currentStemmed).append(" ");
                        currentStemmed.setLength(0);
                    }
                    trim(stemmedTerm.toString());
                } else {
                    snowballStemmer.setCurrent(term.getKey());
                    snowballStemmer.stem();
                    stemmedTerm.append(snowballStemmer.getCurrent());
                } if (stemmed.containsKey(stemmedTerm.toString())){
                    currentStemmed.append(split(stemmed.get(stemmedTerm.toString()),",")[0]);
                    sumFrequency(stemmed,stemmedTerm,currentStemmed,term.getValue());
                } else {
                    stemmed.put(stemmedTerm.toString(),term.getValue());
                }
                cache.put(term.getKey(),stemmedTerm.toString());
            }
        }
        return stemmed;
    }

    private void sumFrequency(Map<String, String> stemmed, StringBuilder stemmedTerm, StringBuilder currentStemmed, String value) {
        try{
            int x = Integer.parseInt(currentStemmed.toString());
            x+= Integer.parseInt(split(value,",")[0]);
            currentStemmed.setLength(0);
            currentStemmed.append(x).append(",0");
            stemmed.replace(stemmedTerm.toString(),value,currentStemmed.toString());
        }catch (Exception e){

        }
    }
}