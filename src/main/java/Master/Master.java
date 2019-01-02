package Master;

import Controller.PropertiesFile;
import Indexer.Indexer;
import Model.ModelMenu;
import Parser.Parser;
import Ranker.Ranker;
import ReadFile.ReadFile;
import Searcher.*;
import Stemmer.Stemmer;
import TextContainers.Doc;
import TextContainers.LanguagesInfo;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * The Master Controls the whole Process of indexing the corpus
 */
public class Master {
    private static int fileNum = PropertiesFile.getPropertyAsInt("number.of.files");
    private static int tmpFileNum = PropertiesFile.getPropertyAsInt("number.of.temp.files");
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static String termSeparator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static String targetPath;
    private static StringBuilder stringBuilder = new StringBuilder();
    private static TreeMap<String, String> docDic = new TreeMap<>(String::compareToIgnoreCase);
    private static LinkedHashMap<String, String> tmpTermDic = new LinkedHashMap<>();
    private static TreeMap<String, String> termDictionary = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, String> cache = new TreeMap<>(String::compareToIgnoreCase);
    private static TreeMap<String, StringBuilder> cityTags = new TreeMap<>(String::compareToIgnoreCase);
    private static ArrayList<Doc> filesList;
    private static boolean isStemMode = setStemMode();
    private static double avrageDocLength = 0;
    private static DoubleProperty currentStatus = new SimpleDoubleProperty(0);
    // Part B
    private static HashMap<String, ArrayList<String>> semanticDic = null;
    private static String currDocName;


    /**
     * sets the stem mode from properties file
     */
    private static boolean setStemMode() {//doc - changed from returns
        String stem = PropertiesFile.getProperty("stem.mode");
        if (stem.equalsIgnoreCase("0")) {
            isStemMode = false;
        } else {
            isStemMode = true;
        }
        return isStemMode;
    }

    /**
     * is there a need to use stemmer?
     *
     * @return yes or no.
     */
    public static boolean isStemMode() {
        return isStemMode;
    }

    /**
     * set the status of external class
     *
     * @param indexStatus - the status from the indexer
     */
    public static void setCurrentStatus(double indexStatus) {
        if (currentStatus.get() - 1 < indexStatus)
            currentStatus.set(indexStatus + 1);
    }

    /**
     * Makes a Dictionary of <Term , Weight_in_Query> to the given query
     * after Parsing and (maybe) Stemming each term in the query.
     *
     * @param query - the given single query
     * @param ranker -the ranker which is going to rank the query
     * @return the mentioned above dictionary
     */
    public static HashMap<String, Integer> makeQueryDic(QuerySol query, Ranker ranker) {
        Parser parser = new Parser();
        return handleQuery(parser.parse(new String[]{query.getTitle() + (PropertiesFile.getPropertyAsInt("semantic.mode") == 0 ? "" : getSemantics(query,ranker))}),ranker);
    }

    /**
     * gets the semantics to a given query
     *
     * @param query - the query we want semantics for
     * @param ranker -the ranker which is going to rank the query
     * @return String of words with semantic contents to the query words
     */
    private static String getSemantics(QuerySol query, Ranker ranker) {
        if (semanticDic == null)
            semanticDic = new ReadFile().readSemantics("semantic_DB_XXL");
        StringBuilder stringBuilder = new StringBuilder();
        int semantic = PropertiesFile.getPropertyAsInt("semantic.mode");
        String[] q = query.getTitleArray();
        for (int i = 0; i < q.length; i++) {
            ArrayList<String> semantics = semanticDic.get(lowerCase(q[i]));
            for (int j = semantic, k = 0; j >= 0 && semantics!=null && k < semantics.size(); j--, k++) {
                stringBuilder.append(" ").append(semantics.get(k));
            }
        }
        return isEmpty(stringBuilder) ? "" : stringBuilder.toString();
    }

    /**
     * cleaning the query after parsing and returns the Query-Dictionary.
     *
     * @param parsed - the map after parsing the query.
     * @param ranker -the ranker which is going to rank the query
     * @return the cleaned dictionary mentioned above.
     */
    private static HashMap<String, Integer> handleQuery(HashMap<String, String> parsed, Ranker ranker) {
        HashMap<String, Integer> queryDic = new HashMap<>();
        setSearchWeights(ranker);
        if (isStemMode) {
            Stemmer stemmer = new Stemmer();
            parsed = stemmer.stem(parsed);
        } else {
            parsed.replaceAll((key, value) -> value = substring(value, 2));
        }
        int freq;
        for (Map.Entry<String, String> term : parsed.entrySet()) {
            String word = term.getKey();
            freq = getFrequencyFromPosting(term);
            queryDic.put(word, freq);
        }
        return queryDic;
    }

    /**
     * Sets accurate weights to the ranker
     * @param ranker - the ranker that will be set
     */
    private static void setSearchWeights(Ranker ranker) {
        boolean semanticMode = PropertiesFile.getPropertyAsInt("semantic.mode") != 0;
        if (setStemMode() && !semanticMode)ranker.setWeights(0.66,0.15,0.06,0.03,0.1);
        else if (isStemMode && semanticMode)ranker.setWeights(0.24,0.31,0.15,0.07,0.23);
        else if (semanticMode) ranker.setWeights(0.16, 0.33, 0.17, 0.09, 0.25);
        else ranker.setWeights(0.46, 0.31, 0.1, 0.05, 0.17);
    }

    /**
     * the Main program of the master.
     * this is where the master manages the other classes and indexes the corpus.
     */
    public void indexCorpus() {
        double tmpFileIndex = 0;
        double i = 0;
        try {
            ModelMenu.setProgress();
            isStemMode = setStemMode();
            String corpusPath = PropertiesFile.getProperty("data.set.path") + "corpus\\";
            targetPath = PropertiesFile.getProperty("save.files.path");
            ReadFile readFile = new ReadFile(corpusPath);
            Indexer indexer = new Indexer();
            fileNum = PropertiesFile.getPropertyAsInt("number.of.files");
            tmpFileNum = PropertiesFile.getPropertyAsInt("number.of.temp.files");
            double tmpChunkSize = Double.min(Integer.max(fileNum / tmpFileNum, 1), fileNum);//doc - function to choose size
            filesList = new ArrayList<>();
            Parser p = new Parser();
            System.out.print("READING, PARSING, ");
            tmpFileIndex++;
            int nextTmpFileIndex = (int) (tmpFileIndex * tmpChunkSize);
            while (ReadFile.hasNextFile()) {
                i++;
                filesList = readFile.getFileList();
                int docCount = 0;
                for (Doc aFilesList : filesList) {
                    currDocName = aFilesList.docNum();
                    HashMap<String, String> map = p.parse(aFilesList.getAttributesToIndex());//doc - indexing whole text (not only text tag)
                    handleFile(map, docCount++);
                }
                currentStatus.set(i / fileNum);
                if ((i == nextTmpFileIndex && tmpFileIndex < tmpFileNum) || i == fileNum) {
                    indexer.appendToFile(Doc.getEntitiesPrinter(), "Entities");//doc - append entities
                    Doc.getEntitiesPrinter().setLength(0);
                    indexer.indexTempFile(new TreeMap<>(tmpTermDic));
                    tmpTermDic.clear();
                    tmpFileIndex++;
                    nextTmpFileIndex = (int) (tmpFileIndex * (tmpChunkSize));
                }
            }
            System.out.println("MERGING");
            indexer.mergePostingTempFiles();
            indexer.writeToDictionary(docDic, "3. Documents Dictionary");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PropertiesFile.putProperty("save.files.path", targetPath);
            writeLanguagesToFile(new Indexer());//doc  -  writing languges to file
            Doc.zeroEntitiesPointer();
            PropertiesFile.putProperty("save.files.path", targetPath);
        }
    }

    /**
     * Writes all the languages from an indexed corpus into a file
     *
     * @param indexer - the indexer which indexed the last corpus
     */
    private void writeLanguagesToFile(Indexer indexer) {
        ArrayList<String> langs = LanguagesInfo.getInstance().getLanguagesAsList();
        StringBuilder langsContent = new StringBuilder(join(langs, "\n"));
        indexer.writeLanguages(langsContent, "Languages");
    }

    /**
     * handles each document,(if checked) Stemmers it and Merges it.
     *
     * @param parsedDic - A Dictionary of a single parsed document
     * @param docCount
     */
    private static void handleFile(HashMap<String, String> parsedDic, int docCount) {
        if (isStemMode) {
            Stemmer stemmer = new Stemmer();
            HashMap<String, String> stemmed = stemmer.stem(parsedDic);
            mergeDicts(stemmed, docCount);
        } else {
            parsedDic.replaceAll((key, value) -> value = substring(value, 2));
            mergeDicts(parsedDic, docCount);
        }
    }

    /**
     * takes a term from a map and returns the frequency of it by the positions in the value
     *
     * @param term - the counted term from the map
     * @return the number of times the term appears
     */
    private static int getFrequencyFromPosting(Map.Entry<String, String> term) {
        return getFrequencyFromPosting(term.getValue());
    }


    /**
     * calculates the amount of time of the term appearing from the posting
     * @param positions - string of positions with gaps
     * @return number of occurrences (frequency)
     */
    public static int getFrequencyFromPosting(String positions) {
        int termFrequency = countMatches(positions, Stemmer.getStemDelimiter().charAt(0));
        if (termFrequency == 0)
            termFrequency++;
        termFrequency += countMatches(positions, Parser.getGapDelimiter().charAt(0));
        return termFrequency;
    }

    /**
     * Merging the Dictionary of a single Document into the Main Dictionaries
     *
     * @param map      - the Dictionary that will be merged
     * @param docCount - counter to iterate on filesList
     */
    private static void mergeDicts(HashMap<String, String> map, int docCount) {
        int maxTf = 0, length = 0;
        Doc doc = filesList.get(docCount);
        for (Map.Entry<String, String> term : map.entrySet()
        ) {
            stringBuilder.setLength(0);

            if (term.getKey().length() < 1) continue;

            String termKey = lowerCase(term.getKey());
            boolean isUpperCase = false;
            int termFrequency = getFrequencyFromPosting(term);
            if (Character.isUpperCase(term.getKey().charAt(0)) || Character.isUpperCase(term.getKey().charAt(termKey.length() - 1))) {
                isUpperCase = true;
            }
            if (tmpTermDic.containsKey(termKey)) {
                if (!isUpperCase) {
                    tmpTermDic.replace(termKey, "0" + substring(tmpTermDic.get(termKey), 1));
                }
                stringBuilder.append(tmpTermDic.get(termKey)).append(fileDelimiter);
            } else {
                stringBuilder.append(isUpperCase ? "1" : "0").append(termSeparator);
            }
            stringBuilder.append(currDocName).append(fileDelimiter).append(term.getValue());
            tmpTermDic.put(termKey, stringBuilder.toString());
            if (isUpperCase && isAlpha(termKey))
                doc.addEntity(upperCase(termKey), termFrequency);//doc - append entities
            maxTf = Integer.max(termFrequency, maxTf);
            length += termFrequency;
        }

        doc.setMax_tf(maxTf);
        doc.setLength(length);
        stringBuilder.setLength(0);
        stringBuilder.append(maxTf).append(",").append(length);
        stringBuilder.append(",").append(doc.appendEntities());
        if (doc.hasCity()) stringBuilder.append(",*").append(doc.getCity());//doc - append cities
        docDic.put(currDocName, stringBuilder.toString());
        map.clear();
    }

    /**
     * get how many terms are currently in the Dictionary
     *
     * @return the size of the dictionary
     */
    public static int getTermCount() {
        return Indexer.getTermCounter();
    }

    /**
     * removes a single term from the dictionary
     *
     * @param term
     */
    public static void removeFromDictionary(String term) {
        termDictionary.remove(term);
    }

    /**
     * get the number of documents
     *
     * @return int - number of documents
     */
    public static int getDocCount() {
        return docDic.size();
    }

    /**
     * add a term to cache
     *
     * @param term                      - the term
     * @param mostRelevantPartOfPosting - the Most Relevant Part Of the term's Posting
     */
    public static void addToCacheDictionary(String term, String mostRelevantPartOfPosting) {
        cache.put(term, mostRelevantPartOfPosting);
    }

    /**
     * add a term to the dictionary
     *
     * @param term    - the term
     * @param details - the term's details
     */
    public static void addToFinalTermDictionary(String term, String details) {
        termDictionary.put(term, details);
    }

    /**
     * removes All files associated with the current stem mode
     *
     * @return true iff deleted the directory
     */
    public boolean removeDicsDir() {
        clear();
        return new Indexer().removeDicsDir();
    }

    /**
     * deletes all of the saved files by the Master
     */
    public void reset() { // TODO: 01/01/2019 if somewhere else files are deleted. check and double check
        clear();
        Indexer.reset();
    }

    /**
     * get the status Property from the master
     *
     * @return Status as DoubleProperty
     */
    public static DoubleProperty getProgress() {
        return currentStatus;
    }

    /**
     * Read Dictionaries to RAM
     *
     * @param dicPath - dictionaries' path
     * @return true if was able to read
     */
    public static boolean readDictionaries(String dicPath) {
        TreeMap<Character, TreeMap<String, String>> treeMaps = ReadFile.readDictionaries(dicPath, termSeparator);
        termDictionary = treeMaps.remove('1');
        cache = treeMaps.remove('2');
        docDic = treeMaps.remove('3');
        setAverageDocLength();//doc
        getCitiesFromDocDic();//doc
        QueryDic.getInstance();
        return (termDictionary != null && cache != null && docDic != null);
    }

    /**
     * creates the dictionary "cityTags" where the key is a city name and the value is a StringBuilder
     * which describes a list of Documents.
     */
    private static void getCitiesFromDocDic() {//doc
        for (Map.Entry<String, String> entry : docDic.entrySet()) {
            String doc = entry.getKey();
            String post = entry.getValue();
            String city = substringAfterLast(post, ",*");
            if (!isEmpty(city)) {
                if (cityTags.containsKey(city)) {
                    cityTags.get(city).append(doc).append("|");
                } else {
                    cityTags.put(city, new StringBuilder(doc));
                }
            }
        }
    }

    /**
     * Search multiple queries (in the format as given in the Report)
     *
     * @param querySols - list of solved queries
     * @param cities    - list of cities to filter the documents by
     */
    public void multiSearch(ArrayList<QuerySol> querySols, ArrayList<String> cities) {//doc
        Searcher searcher = new Searcher();
        if (cities.size() > 0) {
            searcher.multiSearch(querySols, termDictionary, cache, createFilteredDocDic(cities), PropertiesFile.getPropertyAsInt("total.rickall") != 0);
        } else {
            searcher.multiSearch(querySols, termDictionary, cache, docDic, PropertiesFile.getPropertyAsInt("total.rickall") != 0);
        }
    }

    /**
     * creates a mini Documents Dictionary.
     * meaning: all the Documents which contain either one of the given cities will be added to the
     * mini Dictionary.
     *
     * @param cities - a list of cities we want to filter by
     * @return mini Document Dictionary
     */
    private TreeMap<String, String> createFilteredDocDic(ArrayList<String> cities) {//doc
        TreeMap<String, String> tmpDocDic = new TreeMap<>(String::compareToIgnoreCase);
        String skip = "";
        for (int i = 0; i < cities.size(); i++) {
            String city = cities.get(i);
            StringBuilder docList = cityTags.getOrDefault(city, new StringBuilder());
            if (docList.length() > 0) {
                String posting = termDictionary.get(city);
                if (posting != null) {
                    posting = substringAfterLast(posting, ",");
                    if (posting.equalsIgnoreCase("*")) {
                        skip = appendDocsOfCities(split(cache.get(city), "|"), docList);
                    } else {
                        skip = appendDocsOfCities(split(ReadFile.getTermLine(new StringBuilder(PropertiesFile.getProperty("save.files.path")), city, posting), "|"), docList);
                    }
                    while (!isEmpty(skip))
                        skip = appendDocsOfCities(split(ReadFile.getTermLine(new StringBuilder(PropertiesFile.getProperty("save.files.path")), city, skip), "|"), docList);
                }
                String[] docsWithCity = split(docList.toString(), "|");
                for (int j = 0; j < docsWithCity.length; j++) {
                    tmpDocDic.putIfAbsent(docsWithCity[j], docDic.get(docsWithCity[j]));
                }
            }
        }
        return tmpDocDic;
    }

    /**
     * appends to a string builder the docNums which contains the cities given to
     * the calling function
     *
     * @param citiesFromPost - String array which the values in even places are docNums
     * @param docList        - StringBuilder to append to.
     * @return the pointer to the posting line in the file (or "" if empty)
     */
    private String appendDocsOfCities(String[] citiesFromPost, StringBuilder docList) {
        for (int j = 0; j < citiesFromPost.length; j += 2) {
            docList.append(citiesFromPost[j]).append("|");
        }
        return substringAfterLast(citiesFromPost[citiesFromPost.length - 1], ",");
    }

    /**
     * get the List of cities from the Corpus "F" tag's attribute "p=104"
     *
     * @return list of strings, city names
     */
    public ArrayList<String> getCitiesList() {
        return new ArrayList<>(cityTags.keySet());
    }

    /**
     * get the average document length
     *
     * @return the average Document length
     */
    public static double getAverageDocLength() {
        return avrageDocLength;
    }

    public static void setAverageDocLength() {//doc
        if (docDic == null) avrageDocLength = 0;
        try {
            ArrayList<String> vals = new ArrayList<>(docDic.values());
            double lenSum = 0;
            int i = 0;
            for (; i < vals.size(); i++) {
                lenSum += Integer.parseInt(split(vals.get(i), ",")[1]);
            }
            avrageDocLength = lenSum / i;
        } catch (Exception e) {
            //nothing
        }
    }

    /**
     * return the pointer in Entities file of given key from 'docDic'
     * @param docNum: key in docDic - docID
     * @return: entities pointer of the given key (doc ID)
     */
    public String getEntitiesPointerFromDocNum(String docNum) {
        //value example: LA123190-0133~5,221,kzfs7
        String value = docDic.get(docNum);
        String[] splitted = split(value, ',');
        return splitted[2];
    }

    /**
     * clears memory. returns all states back to the beginning. (except stemmer cache..)
     */
    public void clear() {
        fileNum = PropertiesFile.getPropertyAsInt("number.of.files");
        tmpFileNum = PropertiesFile.getPropertyAsInt("number.of.temp.files");
        termDictionary = new TreeMap<>(String::compareToIgnoreCase);
        cache = new TreeMap<>(String::compareToIgnoreCase);
        cityTags = new TreeMap<>(String::compareToIgnoreCase);
        semanticDic = new HashMap<>();
        stringBuilder = new StringBuilder();
        docDic = new TreeMap<>(String::compareToIgnoreCase);
        tmpTermDic = new LinkedHashMap<>();
        isStemMode = setStemMode();
        currentStatus.set(0);
        avrageDocLength=0;
        Doc.getEntitiesPrinter().setLength(0);
        Doc.zeroEntitiesPointer();
        Indexer.clear();
        ReadFile.clear();
    }
}