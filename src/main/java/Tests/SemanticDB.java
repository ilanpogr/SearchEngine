package Tests;

import Stemmer.Stemmer;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class SemanticDB {

    private static String filePath = "C:\\Users\\User\\Documents\\Java Projects\\SearchEngine\\src\\main\\resources\\semanticDB\\ppdb-2.0-xxl-all";
    private static String semanticSeperator = "|";
    private static String keySeperator = "~";
    private static String stemSeperaot = "^";
    private static Stemmer stemmer = new Stemmer();

    /**
     * Computes similarity for two words by using a received RelatednessCalculator object
     * @param word1 - first word
     * @param word2 - second word
     * @param rc - RelatednessCalculator object
     * @return - Double representing the similarity
     */
    private static double compute(String word1, String word2, RelatednessCalculator rc) {
        WS4JConfiguration.getInstance().setMFS(true);
        return rc.calcRelatednessOfWords(word1, word2);
    }

    /**
     * writes the map to project folder with given file name
     * @param map - the map we wish to write into file
     * @param fileName - the wished fileName
     * @throws IOException - exception from using FileWriter
     */
    private static void writeToFile(TreeMap<String,String> map, String fileName) throws IOException {
        // write to file
        FileWriter fstreamToText;
        BufferedWriter out;

        fstreamToText = new FileWriter(fileName, true);
        out = new BufferedWriter(fstreamToText);

        while (!map.isEmpty()) {
            Map.Entry<String, String> pairs = map.pollFirstEntry();
            out.write(pairs.getKey() + keySeperator + pairs.getValue() + "\n");
        }
        out.close();
    }

    /**
     * using the filePath from the class Object, opening a DB, reading it
     * after filtering only one word terms from each side,
     * creates a Map with key term and all the semantics for this word.
     * @throws IOException - using Files
     */
    private static void createSemanticHashMapFiles() throws IOException {
        TreeMap<String, String> map = new TreeMap<>(String::compareToIgnoreCase);
        FileInputStream fstream = new FileInputStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;

        while ((strLine = br.readLine()) != null) {
            String[] s = splitByWholeSeparatorPreserveAllTokens(strLine, "|||");
            s[1] = trim(s[1]);
            s[2] = trim(s[2]);
            if (contains(s[1], "www.") || contains(s[1], " ") || contains(s[2], " ") || s[1].equals("")) {
                continue;
            }
            if (map.get(s[1]) == null) {
                map.put(s[1], s[2]);
            } else {
                String value = map.get(s[1]);
                value += semanticSeperator + s[2];
                map.put(s[1], value);
            }
        }
        br.close();

        map = arrangeValueByWeight(map);
        writeToFile(map, "semantic_DB_XXL");
    }

    /**
     * for each term in the TreeMap takes the String value (writen as posting),
     * separates the words and ordering them by weight computed with Compute function above.
     * returnes the value inside the TreeMap.
     * @param map - key: term, value: words with same semantic meaning separated with delimiter defined by: semanticSeperator.
     * @return : new TreeMap after rearranged by weights
     */
    private static TreeMap<String, String> arrangeValueByWeight(TreeMap<String, String> map) {
        ILexicalDatabase db = new NictWordNet();
        RelatednessCalculator rc = new WuPalmer(db);
        TreeMap<String, String> mapArranged = new TreeMap<>(String::compareToIgnoreCase);
        TreeMap<Double, String> wordsMap = new TreeMap<>();
        for (String key : map.keySet()) {
            String values = map.get(key);
            String[] words = split(values, semanticSeperator);

            // check duplicated words
            HashMap<String, String> checkDuplicate = new HashMap<>();
            for (String s : words) {
                checkDuplicate.put(s, "");
            }
            words = new String[checkDuplicate.size()];
            int counterWords = 0;
            for (String s : checkDuplicate.keySet()) {
                words[counterWords] = s;
                counterWords++;
            }

            // rank words
            for (String s : words) {
                double rank = compute(key, s, rc);
                if (!wordsMap.containsKey(rank))
                    wordsMap.put(rank, s);
                else {
                    rank += 0.0000000000000001;
                    wordsMap.put(rank, s);
                }
            }

            StringBuilder valueSorted = new StringBuilder();
            while (!wordsMap.isEmpty()) {
                if (wordsMap.size() == 1) {
                    valueSorted.append(wordsMap.pollLastEntry().getValue());
                } else {
                    valueSorted.append(wordsMap.pollLastEntry().getValue()).append(semanticSeperator);
                }
            }
            mapArranged.put(key, valueSorted.toString());
        }
        return mapArranged;
    }

    /**
     * reads the Semantic file, stemming the terms and the words inside the map key's value.
     * after stemming different terms can be represented the same, so merging the values with a separator defined by: stemSeperaot.
     * saving the new file by the name ending with Stem
     * @throws IOException - usuing Files
     */
    private static void stemSemanticFile() throws IOException {
        FileInputStream fstream = new FileInputStream("semantic_DB_XXL");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        HashMap<String, String> singleWord = new HashMap<>();
        TreeMap<String, String> stemmedSemanticDB = new TreeMap<>(String::compareToIgnoreCase);
        while ((strLine = br.readLine()) != null) {
            String[] content = split(strLine, keySeperator);
            String key = content[0];
            singleWord.put(key,"");
            key = stemWord(singleWord);
            String[] values = split(content[1], semanticSeperator);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                singleWord.clear();
                singleWord.put(values[i], "");
                values[i] = stemWord(singleWord);
                if (stringBuilder.length() == 0) {
                    stringBuilder.append(values[i]);
                } else {
                    stringBuilder.append(semanticSeperator).append(values[i]);
                }
            }
            String value = stringBuilder.toString();
            if (stemmedSemanticDB.containsKey(key)) {
                value += stemSeperaot + stemmedSemanticDB.get(key);
                stemmedSemanticDB.replace(key, value);
            } else {
                stemmedSemanticDB.put(key, value);
            }
        }
        br.close();

        writeToFile(stemmedSemanticDB, "semantic_DB_XXL_stem");
    }

    /**
     * stemming the word.
     * @param singleWord - HashMap containing only one word
     *                   the Stemmer uses HashMap so created as Stemmer wants.
     * @return - the word as String after stemming
     */
    private static String stemWord(HashMap<String, String> singleWord) {
        singleWord = stemmer.stem(singleWord);
        String res = "";
        for (String s : singleWord.keySet()) {
            res = s;
        }
        return res;
    }

}

