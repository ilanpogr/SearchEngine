package Ranker;

import Controller.PropertiesFile;
import Parser.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.*;

public class MultiQueriesHandler {

    String[] additionalStopWords = {"etc.", "i.e", "considered", "information", "documents", "document", "discussing", "discuss", "following", "issues", "identify", "find", "must"};
    Parser p = new Parser();
    private String title;
    private String description;
    private String narrative;

    private ArrayList<HashMap<String, Integer>> notRelevantList;
    private ArrayList<HashMap<String, Integer>> relevantList;
    private String queryFile_path = PropertiesFile.getProperty("queries.file.path");

    public MultiQueriesHandler() {
        notRelevantList = new ArrayList<>();
        relevantList = new ArrayList<>();
    }


    public ArrayList<HashMap<String, Integer>> getNotRelevantList() {
        return this.notRelevantList;
    }

    public ArrayList<HashMap<String, Integer>> getRelevantList() {
        return this.relevantList;
    }

    /**
     * remove all stop words we think are not relevant for query terms
     * @param s: the String we want to clean
     * @return: cleaned String
     */
    private String cleanAdditionalStopWords(String s) {
        for (String word : additionalStopWords) {
            s = replaceIgnoreCase(s, word, "");
        }
        return s;
    }

    /**
     * Main function read the queries file from path in properties file.
     * Dividing the whole file in array of Strings that representing an query.
     * Dividing the words that are relevant for the query and those that should NOT appear in the documents
     * into maps and collecting each map inside an ArrayList containing th maps for all the queries.
     *
     * in this class there are two getter for those ArrayLists. use them after running this function.
     *
     */
    public void parseMultiQuery() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(queryFile_path));
            StringBuilder stringBuilder = new StringBuilder();
            Stream<String> s = bufferedReader.lines();
            s.forEach(s1 -> stringBuilder.append(s1 + " "));
            String content = stringBuilder.toString();

            String[] queries = splitByWholeSeparator(content, "</top>");

            for (String query : queries) {
                query = strip(query);
                if (!query.equals("")) {
                    HashMap<String, Integer> notRelevantMap = new HashMap<>();
                    HashMap<String, Integer> relevantMap = new HashMap<>();
                    title = strip(substringBetween(query, "<title>", "<desc>"));
                    description = replace(strip(substringBetween(query, "Description:", "<narr>")), "and/or", "and or");
                    narrative = replace(strip(substringAfter(query, "Narrative:")), "and/or", "and or");

                    handleTitle(relevantMap);
                    handleDescription(relevantMap);
                    handleNarrative(relevantMap, notRelevantMap);
                    relevantList.add(relevantMap);
                    notRelevantList.add(notRelevantMap);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Description section in the query using the Parser class.
     * splitting the sentences to shouldn't have terms and extra terms for the query
     */
    private void handleNarrative(HashMap<String, Integer> relevantMap, HashMap<String, Integer> notRelevantMap) {

        for (String word : additionalStopWords) {
            narrative = replaceIgnoreCase(narrative, word, "");
        }

        narrative = replace(narrative, "\n", " ");
        String[] sentences = splitByWholeSeparator(narrative, ". ");

        for (String sentence : sentences) {
            if (containsIgnoreCase(sentence, "not relevant:")) { // after comes the not relevant stuff
                String notRelevent = substringAfter(sentence, "not relevant:");
                dealWithNotRelevant(notRelevent, notRelevantMap);
                sentence = substringBeforeLast(sentence, "not relevant:");
            }
            if (containsIgnoreCase(sentence, "not relevant") || containsIgnoreCase(sentence, "non-relevant")) { // the same sentence containing what is not relevant
                dealWithNotRelevant(sentence, notRelevantMap);
            }
            if (containsIgnoreCase(sentence, "relevant:")) {
                String relevent = substringAfter(sentence, "relevant:");
                sentence = substringBeforeLast(sentence, "relevant:");
                dealWithRelevant(relevent, relevantMap);
            }
            if (containsIgnoreCase(sentence, "relevant") && !containsIgnoreCase(sentence, "not relevant")) {
                dealWithRelevant(sentence, relevantMap);
            }
        }
        removeQueryWordsFromNotRelevantList(notRelevantMap, relevantMap);
    }

    /**
     * takes care of the sentence that have potential additional relevant terms for the query
     * @param sentence - the relevant sentence
     * @param relevantMap - Map containing the terms that are relevant for the query
     */
    private void dealWithRelevant(String sentence, HashMap<String, Integer> relevantMap) {
        HashMap<String, String> local_relevant = new HashMap<>();
        if (contains(sentence, " - ")) {
            String[] checkPoints = splitByWholeSeparator(sentence, " - ");
            for (String s : checkPoints) {
//                s = cleanAdditionalStopWords(s);
                s = replaceIgnoreCase(s, "relevant", "");
                String[] toParse = {s};
                local_relevant = p.parse(toParse);
            }

        } else { // inside the sentence
            sentence = cleanAdditionalStopWords(sentence);
            sentence = replaceIgnoreCase(sentence, "relevant", "");
            String[] toParse = {sentence};
            local_relevant = p.parse(toParse);
        }

        for (String key : local_relevant.keySet()) {
            addTermToHashMap(local_relevant, relevantMap, key);
        }
    }

    /**
     * takes care of the sentence that have potential NOT relevant terms for the query
     * @param sentence - the sentence containing info that is NOT relevant for the query
     * @param notRelevantMap - Map containing the terms that are NOT relevant for the query
     */
    private void dealWithNotRelevant(String sentence, HashMap<String, Integer> notRelevantMap) {
        HashMap<String, String> local_notRelevant = new HashMap<>();
        if (contains(sentence, " - ")) {
            String[] checkPoints = splitByWholeSeparator(sentence, " - ");
            for (String s : checkPoints) {
                String[] toParse = {s};
                s = replaceIgnoreCase(s, "relevant", "");
                local_notRelevant = p.parse(toParse);
            }

        } else { // inside the sentence
            sentence = cleanAdditionalStopWords(sentence);
            sentence = replaceIgnoreCase(sentence, "relevant", "");
            String[] toParse = {sentence};
            local_notRelevant = p.parse(toParse);
        }

        for (String key : local_notRelevant.keySet()) {
            addTermToHashMap(local_notRelevant, notRelevantMap, key);
        }
    }

    /**
     * if term is in query and in not relevant, remove from not relevant.
     */
    private void removeQueryWordsFromNotRelevantList(HashMap<String, Integer> notRelevantMap, HashMap<String, Integer> relevantMap) {
        for (String key : relevantMap.keySet()) {
            notRelevantMap.remove(key);
        }
    }


    /**
     * Handles the Description section in the query using the Parser class.
     */
    private void handleDescription(HashMap<String, Integer> relevantMap) {
        description = replace(description, "\n", " ");
        for (String s : additionalStopWords) {
            description = replaceIgnoreCase(description, s, "");
        }
        String[] terms = {description};
        HashMap<String, String> res = p.parse(terms);

        // add the description terms and title terms to relevantMap HashMap

        for (String key : res.keySet()) {
            addTermToHashMap(res, relevantMap, key);
        }
    }

    private int countTermAfterParse(String value) {
        return countMatches(value, ':') + 1;
    }

    private void addTermToHashMap(HashMap<String, String> res, HashMap<String, Integer> map, String key) {
        if (map.containsKey(key)) {
            int counter = map.get(key);
            counter += countTermAfterParse(res.get(key));
            map.put(key, counter);
        } else {
            int counter = countTermAfterParse(res.get(key));
            map.put(key, counter);
        }
    }

    /**
     * Handles the Title section in the query using the Parser class.
     */
    private void handleTitle(HashMap<String, Integer> relevantMap) {
        String[] terms = {title};
        HashMap<String, String> res = p.parse(terms);
        for (String key : res.keySet()) {
            addTermToHashMap(res, relevantMap, key);
        }
    }

//    public static void main(String[] args) {
//        MultiQueriesHandler q = new MultiQueriesHandler();
//        q.parseMultiQuery();
//        ArrayList<HashMap<String,Integer>> relevent = q.getRelevantList();
//        ArrayList<HashMap<String,Integer>> notRelevent = q.getNotRelevantList();
//        int counter = 1;
//        while (counter <= relevent.size()) {
//            System.out.println();
//            System.out.println("-----------------------QUERY " + counter + "----------------------");
//            System.out.println();
//            System.out.println("----------------------------------------------------");
//            System.out.println("--------------------NOT RELEVANT--------------------");
//            System.out.println("----------------------------------------------------");
//            printMap(notRelevent.get(counter - 1));
//            System.out.println("----------------------------------------------------");
//            System.out.println();
//            System.out.println("----------------------RELEVANT----------------------");
//            System.out.println("----------------------------------------------------");
//            printMap(relevent.get(counter - 1));
//            System.out.println("----------------------------------------------------");
//            counter++;
//        }
//    }
//
//    private static void printMap(HashMap<String, Integer> stringIntegerHashMap) {
//        for (String key : stringIntegerHashMap.keySet()){
//            System.out.println("            " + key + "-->" + stringIntegerHashMap.get(key));
//        }
//    }
}
