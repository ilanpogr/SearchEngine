package Indexer;

import Controller.PropertiesFile;
import Master.Master;
import Parser.Parser;
import Stemmer.Stemmer;
import TextContainers.City;
import TextContainers.CityInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;

import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer {

    private static double totalPostingSizeByKB = 0;
    private static final int minNumberOfTf = (int) getPropertyAsDouble("min.tf.to.save");
    private static final double maxIdfForCache = getPropertyAsDouble("max.idf.for.cache");
    private static final int cacheSlice = (int) getPropertyAsDouble("one.part.to.cache.from");
    private static final String cachePointer = PropertiesFile.getProperty("pointer.to.cache");
    private static final String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static final String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static String targetPath = PropertiesFile.getProperty("save.files.path");
    private static AtomicInteger tmpFilesCounter = new AtomicInteger(0);
    private static AtomicInteger mergedFilesCounter = new AtomicInteger(0);
    private static final double log2 = StrictMath.log10(2);
    private static BufferedWriter inverter = null;
    private static boolean createdFolder = false;
    private static int termCounter = 0;


    /**
     * get a Property from properties file and convert it to double.
     * if it can't convert to Double, it will return 1.
     *
     * @param prop - the value of the property
     * @return the value of the property
     */
    private static double getPropertyAsDouble(String prop) {
        try {
            return Double.parseDouble(PropertiesFile.getProperty(prop));
        } catch (Exception e) {
            System.out.println("Properties Weren't Set Right. Default Value is set, Errors Might Occur!");
            return 1;
        }
    }

    /**
     * creates and writes a temp file
     * @param sortedTermsDic - the dictionary of few Docs we want to write to disk
     */
    public void indexTempFile(TreeMap<String, String> sortedTermsDic) {
        checkOrMakeDir(getFileOrDirName(targetPath + "Dictionaries"));
        try {
            StringBuilder tmpPostFile = new StringBuilder();
            sortedTermsDic.forEach((k, v) -> tmpPostFile.append(lowerCase(k)).append(termSeperator).append(v).append("\n"));
            tmpPostFile.trimToSize();
            WrieFile.createTempPostingFile(tmpPostFile);
            tmpFilesCounter.incrementAndGet();
        } catch (OutOfMemoryError om) {     //if Map is too big
            try {
                ArrayList<Map.Entry<String, String>> lines = new ArrayList<>(sortedTermsDic.entrySet());
                lines.sort((o1, o2) -> compareIgnoreCase(o1.getKey(), o2.getKey(), true));
                indexTempFile(new TreeMap<>(sortedTermsDic.tailMap(lines.get(lines.size() / 2).getKey())));
                indexTempFile(new TreeMap<>(sortedTermsDic.headMap(lines.get(lines.size() / 2).getKey())));
            } catch (Exception e) {
                sortedTermsDic.forEach((term, value) -> {
                    StringBuilder s = new StringBuilder();
                    s.append(term).append(termSeperator).append(value).append("\n");
                    s.trimToSize();
                    WrieFile.createTempPostingFile(s);
                });
            }
        }
    }

    /**
     * the inverter function. takes all temp files and merges them.
     */
    public void mergePostingTempFiles() {
        String targetDirPath = targetPath;
        int currFileNum = WrieFile.getFileNum();
        StringBuilder stringBuilder = new StringBuilder();
        LinkedHashMap<Integer, MutablePair<String, Integer>> termKeys = new LinkedHashMap<>();
        LinkedHashMap<Integer, String> termValues = new LinkedHashMap<>();
        LinkedHashMap<Integer, BufferedReader> tmpFiles = new LinkedHashMap<>();
        for (int i = 1; i <= currFileNum; i++) {
            stringBuilder.setLength(0);
            stringBuilder.append(targetDirPath).append((i)).append(".post").trimToSize();
            checkOrMakeDir(targetDirPath);
            addFileToList(tmpFiles, stringBuilder, i);
            getFirstTerms(tmpFiles, termKeys, termValues, i);
        }
        LinkedHashMap<String, Integer> mergedFilesCounterDic = new LinkedHashMap<>();
        LinkedHashMap<String, BufferedWriter> mergedFilesDic = new LinkedHashMap<>();
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "others");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "ab");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "cd");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "ef");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "ghi");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "jkl");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "mn");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "op");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "qrs");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "tuvwxyz");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, getFileOrDirName("1. Term Dictionary"));
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, getFileOrDirName("2. Cache Dictionary"));
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, getFileOrDirName("4. Cities Dictionary"));
        TreeMap<String, ArrayList<Integer>> termsSorter = new TreeMap<>();
        int tmpFilesInitialSize = tmpFiles.size();
        double logN = StrictMath.log10(Master.getDocCount()) / log2;
        while (mergedFilesCounter.get() < tmpFilesCounter.get()){
            stringBuilder.setLength(0);
            ArrayList<Integer> minTerms = new ArrayList<>();
            for (int i = 1; i <= tmpFilesInitialSize; i++) {
                if (!tmpFiles.containsKey(i)) {
                    continue;
                }
                String k = termKeys.get(i).left;
                if (!termsSorter.containsKey(k)) {
                    termsSorter.put(k, new ArrayList<>());
                    termsSorter.get(k).add(i);
                }
            }
            String minTerm;
            try {
                minTerm = termsSorter.pollFirstEntry().getKey();
            } catch (NullPointerException npe) {
                continue;
            }
            int isUpperCase = 1;
            for (Map.Entry<Integer, MutablePair<String, Integer>> term : termKeys.entrySet()
            ) {
                if (equalsIgnoreCase(minTerm, term.getValue().left)) {
                    isUpperCase = isUpperCase & term.getValue().right;
                    minTerms.add(term.getKey());
                }
            }
            for (Integer integer : minTerms) {
                stringBuilder.append(termValues.get(integer)).append(fileDelimiter);
                getFirstTerms(tmpFiles, termKeys, termValues, integer);
            }
            stringBuilder.trimToSize();
            ArrayList<ImmutablePair<Integer, ImmutablePair<String, String>>> sortedPosting = new ArrayList<>();
            sortPostingByFrequency(sortedPosting, stringBuilder);
            int totalTf = 0;
            stringBuilder.setLength(0);
            for (ImmutablePair<Integer, ImmutablePair<String, String>> d : sortedPosting) {
                totalTf += d.left;
                stringBuilder.append(d.right.left).append(fileDelimiter).append(d.right.right).append(fileDelimiter);
            }
            int df = sortedPosting.size();
            double idf = logN - (StrictMath.log10(df) / log2);

            if (totalTf > minNumberOfTf || minTerm.contains(" ")) {
                String mergedFileName = getFileName(minTerm.charAt(0));
                if (isUpperCase == 1) {
                    minTerm = upperCase(minTerm);
                }
                City city = CityInfo.getInstance().getValueFromCitiesDictionary(minTerm);
                if (maxIdfForCache < idf || df == 1) {
                    WrieFile.addLineToFile(mergedFilesDic, getFileOrDirName("1. Term Dictionary"), minTerm + termSeperator + totalTf + "," + df + "," + mergedFileName + "," + mergedFilesCounterDic.get(mergedFileName) + "\n");
                    if (city!=null) WrieFile.addLineToFile(mergedFilesDic, getFileOrDirName("4. Cities Dictionary"), minTerm + termSeperator + city.getCountryName() + "," + city.getCurrency() + "," + city.getPopulation() + "," + mergedFilesCounterDic.get(mergedFileName) + "\n");
                    WrieFile.addLineToFile(mergedFilesDic, mergedFileName, stringBuilder.append("\n").toString());
                    mergedFilesCounterDic.replace(mergedFileName, mergedFilesCounterDic.get(mergedFileName) + 1);
                } else {
                    String[] cacheSplitedPost = splitToCachePost(stringBuilder);
                    WrieFile.addLineToFile(mergedFilesDic, getFileOrDirName("1. Term Dictionary"), minTerm + termSeperator + totalTf + "," + df + "," + mergedFileName + "," + mergedFilesCounterDic.get(mergedFileName) + "," + cachePointer + "\n");
                    if (city!=null) WrieFile.addLineToFile(mergedFilesDic, getFileOrDirName("4. Cities Dictionary"), minTerm + termSeperator + city.getCountryName() + "," + city.getCurrency() + "," + city.getPopulation() + "," + mergedFilesCounterDic.get(mergedFileName) + "," + cachePointer + "\n");
                    WrieFile.addLineToFile(mergedFilesDic, getFileOrDirName("2. Cache Dictionary"), minTerm + termSeperator + cacheSplitedPost[0] + "," + mergedFilesCounterDic.get(mergedFileName) + "\n");
                    WrieFile.addLineToFile(mergedFilesDic, mergedFileName, cacheSplitedPost[1] + "\n");
                    mergedFilesCounterDic.replace(mergedFileName, mergedFilesCounterDic.get(mergedFileName) + 1);
                }
                termCounter++;
                Master.setCurrentStatus(getIndexStatus(minTerm));
            } else {
                Master.removeFromDictionary(minTerm);
            }
        }
        for (Map.Entry<String, BufferedWriter> mapEntry : mergedFilesDic.entrySet()) {
            BufferedWriter bufferedWriter = mapEntry.getValue();
            try {
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                //stop indexing
            }
        }
        try {
            totalPostingSizeByKB = sizeOf(new File(targetDirPath));
        } catch (Exception e) {
            try {
                for (Map.Entry<String, BufferedWriter> entry : mergedFilesDic.entrySet()) {
                    totalPostingSizeByKB += new File(targetDirPath + entry.getKey() + ".post").length();
                    entry.getValue().flush();
                    entry.getValue().close();
                }
            } catch (Exception e2) {
                totalPostingSizeByKB = Master.getTermCount() * 1024;
            }
        }
        System.out.println("Size of Posting Files: " + (int) (totalPostingSizeByKB / 1024) + " KB");
        createdFolder = false;
    }

    /**
     * gets the letter value that is being indexed
     * @param curr - the current term
     * @return number in [0,1] to indicate the state
     */
    private double getIndexStatus(String curr) {
        double stat = 0;
        switch (curr.charAt(0)){
            case '0': stat = 1; break;
            case '1': stat = 2; break;
            case '2': stat = 3; break;
            case '3': stat = 4; break;
            case '4': stat = 5; break;
            case '5': stat = 6; break;
            case '6': stat = 7; break;
            case '7': stat = 8; break;
            case '8': stat = 9; break;
            case '9': stat = 10; break;
            case 'a': stat = 11; break;
            case 'b': stat = 12; break;
            case 'c': stat = 13; break;
            case 'd': stat = 14; break;
            case 'e': stat = 15; break;
            case 'f': stat = 16; break;
            case 'g': stat = 17; break;
            case 'h': stat = 18; break;
            case 'i': stat = 19; break;
            case 'j': stat = 20; break;
            case 'k': stat = 21; break;
            case 'l': stat = 22; break;
            case 'm': stat = 23; break;
            case 'n': stat = 24; break;
            case 'o': stat = 25; break;
            case 'p': stat = 26; break;
            case 'q': stat = 27; break;
            case 'r': stat = 28; break;
            case 's': stat = 29; break;
            case 't': stat = 30; break;
            case 'u': stat = 31; break;
            case 'v': stat = 32; break;
            case 'w': stat = 33; break;
            case 'x': stat = 34; break;
            case 'y': stat = 35; break;
            case 'z': stat = 36; break;
        }
        return stat/36;

    }

    /**
     * appends with/without stemming to the file/dir name
     *
     * @param fileName the files
     * @return the file's string appended with "with(out)? stemming"
     */
    private String getFileOrDirName(String fileName) {
        return appendIfMissingIgnoreCase(fileName, " with" + (Master.isStemMode() ? "" : "out") + " stemming");
    }

    /**
     * takes a part of the posting and splits it to two parts - one for cache and the other to the posting file
     *
     * @param stringBuilder - the posting of a term
     * @return a two-slots-Array    slot0 - posting for the cache   slot1 - rest of the posting
     */
    private String[] splitToCachePost(StringBuilder stringBuilder) {
        String[] forCache = split(stringBuilder.toString(), fileDelimiter);
        int sliceIndex = forCache.length / cacheSlice;
        if (sliceIndex % 2 == 1) {
            sliceIndex = Integer.max(--sliceIndex, 2);
        }
        return new String[]{join(forCache, fileDelimiter, 0, sliceIndex), join(forCache, fileDelimiter, sliceIndex, forCache.length)};//TODO- Check!!!

    }

    /**
     * get the file name which the term's posting should be written to
     *
     * @param first - the first character of a term
     * @return the file's full name
     */
    private String getFileName(char first) {
        switch (first) {
            case 'a':
                return "ab";
            case 'b':
                return "ab";
            case 'c':
                return "cd";
            case 'd':
                return "cd";
            case 'e':
                return "ef";
            case 'f':
                return "ef";
            case 'g':
                return "ghi";
            case 'h':
                return "ghi";
            case 'i':
                return "ghi";
            case 'j':
                return "jkl";
            case 'k':
                return "jkl";
            case 'l':
                return "jkl";
            case 'm':
                return "mn";
            case 'n':
                return "mn";
            case 'o':
                return "op";
            case 'p':
                return "op";
            case 'q':
                return "qrs";
            case 'r':
                return "qrs";
            case 's':
                return "qrs";
            case 't':
                return "tuvwxyz";
            case 'u':
                return "tuvwxyz";
            case 'v':
                return "tuvwxyz";
            case 'w':
                return "tuvwxyz";
            case 'x':
                return "tuvwxyz";
            case 'y':
                return "tuvwxyz";
            case 'z':
                return "tuvwxyz";
        }
        return "others";
    }

    /**
     * sorts the posting string by the term frequency
     *
     * @param toSort        - an array list holding the tf as a key and the Map's term's record as a value
     * @param stringBuilder - a string given with the term's value
     */
    private void sortPostingByFrequency(ArrayList<ImmutablePair<Integer, ImmutablePair<String, String>>> toSort, StringBuilder stringBuilder) {
        String[] pairs = split(stringBuilder.toString(), fileDelimiter);
        for (int i = 0, frequency; i < pairs.length - 1; i++) {
            frequency = countMatches(pairs[i + 1], Stemmer.getStemDelimiter().charAt(0));
            if (frequency == 0)
                frequency++;
            frequency += countMatches(pairs[i + 1], Parser.getGapDelimiter().charAt(0));
            toSort.add(new ImmutablePair<>(frequency, new ImmutablePair<>(pairs[i++], pairs[i])));
        }
        toSort.sort((o1, o2) -> Integer.compare(o2.left, o1.left));
    }

    /**
     * creates the file that we want to write to
     *
     * @param mergedFilesCounterDic the map that counts for each file how many terms (+1) are inserted - for knowing later the row's number
     * @param mergedFilesDic        - key: the file's name.    value: BufferedWriter that will be assigned to this file.
     * @param fileName              - the name of the created file
     */
    private void initMergedDictionaries(LinkedHashMap<String, Integer> mergedFilesCounterDic, LinkedHashMap<String, BufferedWriter> mergedFilesDic, String fileName) {
        checkOrMakeDir(getFileOrDirName(targetPath + "Dictionaries"));
        StringBuilder stringBuilder = new StringBuilder(targetPath);
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(contains(fileName, " ") ? "" : ".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
    }

    /**
     * adds a BufferedReader for each of the temp posting files
     *
     * @param tmpFiles      - the key is the index (name) of a temporary file and the value is the reader
     * @param stringBuilder - has the path
     * @param i             - posting file index
     */
    private void addFileToList(LinkedHashMap<Integer, BufferedReader> tmpFiles, StringBuilder stringBuilder, int i) {
        try {
            tmpFiles.put(i, new BufferedReader(new FileReader(new File(stringBuilder.toString()))));
        } catch (FileNotFoundException e) {
            System.out.println("couldn't find or open: " + stringBuilder);
            //do nothing
        }
    }

    /**
     * adds a BufferedWriter for each of the temp posting files
     *
     * @param tmpFiles      - the key is the index (name) of a temporary file and the value is the reader
     * @param stringBuilder - has the path
     * @param name          - name of the file
     */
    private void addFileToList(LinkedHashMap<String, BufferedWriter> tmpFiles, StringBuilder stringBuilder, String name) {
        try {
            File file = new File(stringBuilder.toString());
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            tmpFiles.put(name, new BufferedWriter(new FileWriter(file, true)));
        } catch (FileNotFoundException e) {
            System.out.println("couldn't find or open: " + stringBuilder);
            //do nothing
        } catch (IOException e) {
            System.out.println("couldn't create: " + stringBuilder);
            //do nothing
        }
    }

    /**
     * reads the next line of a file (if available) or deletes it from the maps.
     *
     * @param tmpFiles   - the key is the index (name) of a temporary file and the value is the reader
     * @param termKeys   - the keys map of the current lines
     * @param termValues - the values map of the current lines
     * @param i          - index of the temp posting file
     */
    private void getFirstTerms(LinkedHashMap<Integer, BufferedReader> tmpFiles, LinkedHashMap<Integer, MutablePair<String, Integer>> termKeys, LinkedHashMap<Integer, String> termValues, int i) {
        try {
            String line = tmpFiles.get(i).readLine();
            if (!isEmpty(line)) {
                String[] term = split(line, termSeperator, 3);
                termKeys.put(i, new MutablePair<>(term[0], Integer.parseInt(term[1])));
                termValues.put(i, term[2]);
            } else {
                termKeys.remove(i);
                termValues.remove(i);
                tmpFiles.remove(i).close();
                Files.delete(Paths.get(targetPath + i + ".post"));
                mergedFilesCounter.incrementAndGet();
            }
        } catch (IOException e) {
            System.out.println("couldn't find or open: ");
            //do nothing
        }

    }

    /**
     * checks if a given path points to a valid directory. if not - creates the directory
     *
     * @param targetDirPath - the Dir path
     */
    private void checkOrMakeDir(String targetDirPath) {
        try {
            Path path = Paths.get(targetDirPath);
            if (!createdFolder) {
                if (Files.notExists(path))
                    Files.createDirectory(path);
                PropertiesFile.putProperty("save.files.path", targetDirPath + "\\");
                targetPath = PropertiesFile.getProperty("save.files.path");
                createdFolder = true;
            }
            WrieFile.setTargetPath(targetPath);
        } catch (Exception e) {
            System.out.println("couldn't find or open: " + targetDirPath);
            //do nothing
        }
    }

    /**
     * writes a TreeMap to a file
     *
     * @param dic     - the map we want to write
     * @param dicName - the name of the created file
     */
    public void writeToDictionary(TreeMap<String, String> dic, String dicName) {
        try {
            inverter = new BufferedWriter(new FileWriter(new File(targetPath + dicName), true));
            dic.forEach((term, value) -> {
                try {
                    inverter.append(term).append(termSeperator).append(value).append("\n");
                } catch (IOException e) {
                    //do nothing
                }
            });
            try {
                inverter.flush();
                inverter.close();
            } catch (IOException e) {
                //do nothing
            }
        } catch (OutOfMemoryError om) {     //if Map is too big
            try {
                ArrayList<Map.Entry<String, String>> lines = new ArrayList<>(dic.entrySet());
                lines.sort((o1, o2) -> compareIgnoreCase(o1.getKey(), o2.getKey(), true));
                writeToDictionary(new TreeMap<>(dic.tailMap(lines.get(lines.size() / 2).getKey())), dicName);
                writeToDictionary(new TreeMap<>(dic.headMap(lines.get(lines.size() / 2).getKey())), dicName);
            } catch (Exception e) {
                dic.forEach((term, value) -> {
                    StringBuilder s = new StringBuilder();
                    s.append(term).append(termSeperator).append(value).append("\n");
                    s.trimToSize();
                    WrieFile.writeToDictionary(s, dicName);
                });
            }
        } catch (IOException e) {
            //do nothing
        }
    }


    /**
     * get the number of terms
     * @return int - number of terms
     */
    public static int getTermCounter() {
        return termCounter;
    }

    /**
     * deletes all folders and files created by this program
     */
    public static void reset() {
        delete(targetPath + "Dictionaries with stemming");
        delete(targetPath + "Dictionaries without stemming");
    }

    /**
     * deletes the folder of last run (depends on stem mode)
     * @return true if deleted
     */
    public boolean removeAllFiles() {
        return delete(getFileOrDirName(targetPath + "Dictionaries"));
    }

    /**
     * deletes a folder and all it's content recursively
     * @param path - the folder's path
     * @return true if deleted
     */
    private static boolean delete(String path) {
        try {
            File dir = new File(targetPath);
            if (dir.isDirectory()) {
                File file = new File(path);
                if (FileUtils.directoryContains(dir, file)) {
                    return deleteQuietly(file);
                }
            }
        } catch (Exception e) {
            //do nothing
        }
        return false;
    }

    /**
     * clears memory
     */
    public static void clear() {
        totalPostingSizeByKB = 0;
        targetPath = PropertiesFile.getProperty("save.files.path");
        tmpFilesCounter = new AtomicInteger(0);
        mergedFilesCounter = new AtomicInteger(0);
        inverter = null;
        createdFolder = false;
        termCounter = 0;
        WrieFile.clear();
    }
}