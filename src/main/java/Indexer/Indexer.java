package Indexer;

import Controller.Controller;
import Controller.PropertiesFile;
import Parser.Parse;
import Stemmer.Stemmer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Triple;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Indexer {

    private static double maxIdfForCache = getPropertyAsDouble("max.idf.for.cache");
    private static int cacheSlice = (int) getPropertyAsDouble("one.part.to.cache.from");

    private static double getPropertyAsDouble(String s) {
        try {
            return Double.parseDouble(PropertiesFile.getProperty(s));
        } catch (Exception e) {
            System.out.println("Properties Weren't Set Right. Default Value is set, Errors Might Occur!");
            return 2.5;
        }
    }

    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static int tmpFilesCounter = 0;
    private static int mergedFilesCounter = 0;
    private static ConcurrentLinkedDeque<StringBuilder> tmpDicQueue = new ConcurrentLinkedDeque<>();
    private static String targetPath = null;
    private static ArrayList<ImmutablePair<String, Triple<Integer, Integer, Integer>>> finalTermDic;


    public void indexTempFile(TreeMap<String, String> sortedTermsDic) {
        try {
            StringBuilder tmpPostFile = new StringBuilder();
            sortedTermsDic.forEach((k, v) -> tmpPostFile.append(lowerCase(k)).append(termSeperator).append(v).append("\n"));
            tmpPostFile.trimToSize();
            tmpDicQueue.addLast(tmpPostFile);
            WrieFile.createTempPostingFile(tmpPostFile);
            tmpFilesCounter++;
        } catch (OutOfMemoryError om) {     //if Map is too big
            try {
                indexTempFile(new TreeMap<>(sortedTermsDic.tailMap("m")));
                indexTempFile(new TreeMap<>(sortedTermsDic.headMap("m")));
            } catch (OutOfMemoryError om2) {    //if "M" wasn't good enough
                indexTempFile(new TreeMap<>(sortedTermsDic.tailMap("a")));
                indexTempFile(new TreeMap<>(sortedTermsDic.headMap("a")));
            }
        }

    }

    public void mergePostingTempFiles(String targetDirPath, LinkedHashMap<String, String> termDictionary, LinkedHashMap<String, String> cache) {
        targetPath = targetDirPath;
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
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic);
        TreeMap<String, ArrayList<Integer>> termsSorter = new TreeMap<>();
        stringBuilder.setLength(0);
        while (mergedFilesCounter < tmpFilesCounter) {
            ArrayList<Integer> minTerms = new ArrayList<>();
            for (int i = 1; i <= tmpFiles.size(); i++) {
                if (!tmpFiles.containsKey(i)) continue;
                String k = termKeys.get(i).left;
                if (!termsSorter.containsKey(k)) {
                    termsSorter.put(k, new ArrayList<>());
                }
                termsSorter.get(k).add(i);
            }
            String minTerm = termsSorter.firstKey();
            int isUpperCase = 1;
            double logN = StrictMath.log10(Controller.getTermCount());
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
            double idf = logN - StrictMath.log10(df);
            finalTermDic = new ArrayList<>();
            if (totalTf > 1) {
                String mergedFileName = getFileName(minTerm.charAt(0));
                if (maxIdfForCache < idf || df == 1) {
                    finalTermDic.add(new ImmutablePair<>(minTerm, new ImmutableTriple<>(totalTf, df, mergedFilesCounterDic.get(mergedFileName))));
                    mergedFilesCounterDic.replace(mergedFileName, mergedFilesCounterDic.get(mergedFileName) + 1);
                    WrieFile.addPostLine(mergedFilesDic, mergedFileName, stringBuilder);
                } else {
                    String[] cacheSplitedPost = splitToCachePost(stringBuilder, df);
                }
            }

        }

    }

    private String[] splitToCachePost(StringBuilder stringBuilder, int df) {
        String[] forCache = split(stringBuilder.toString(), fileDelimiter);
        return new String[]{};
    }

    private String getFileName(char first) {
        switch (first) {
            case 'a':
                return "abc";
            case 'd':
                return "defgh";
            case 'i':
                return "ijkl";
            case 'm':
                return "mnop";
            case 'q':
                return "qrst";
            case 'u':
                return "uvwxyz";
        }
        return "others";
    }

    private void sortPostingByFrequency(ArrayList<ImmutablePair<Integer, ImmutablePair<String, String>>> toSort, StringBuilder stringBuilder) {
        String[] pairs = split(stringBuilder.toString(), fileDelimiter);
        for (int i = 0, frequency; i < pairs.length - 1; i++) {
            frequency = countMatches(pairs[i + 1], Stemmer.getStemDelimiter().charAt(0));
            if (frequency == 0)
                frequency++;
            frequency += countMatches(pairs[i + 1], Parse.getGapDelimiter().charAt(0));
            toSort.add(new ImmutablePair<>(frequency, new ImmutablePair<>(pairs[i++], pairs[i])));
        }
        toSort.sort((o1, o2) -> Integer.compare(o2.left, o1.left));
    }

    private void initMergedDictionaries(LinkedHashMap<String, Integer> mergedFilesCounterDic, LinkedHashMap<String, BufferedWriter> mergedFilesDic) {
        StringBuilder stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        String fileName = "others";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "abc";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "defgh";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "ijklm";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "mnop";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "qrst";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "uvwxyz";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
    }

    private void addFileToList(LinkedHashMap<Integer, BufferedReader> tmpFiles, StringBuilder stringBuilder, int i) {
        try {
            tmpFiles.put(i, new BufferedReader(new FileReader(new File(stringBuilder.toString()))));
        } catch (FileNotFoundException e) {
            System.out.println("couldn't find or open: " + stringBuilder);
            e.printStackTrace();
        }
    }

    private void addFileToList(LinkedHashMap<String, BufferedWriter> tmpFiles, StringBuilder stringBuilder, String name) {
        try {
            File file = new File(stringBuilder.toString());
            file.createNewFile();
            tmpFiles.put(name, new BufferedWriter(new FileWriter(file, true)));
        } catch (FileNotFoundException e) {
            System.out.println("couldn't find or open: " + stringBuilder);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("couldn't create: " + stringBuilder);
            e.printStackTrace();
        }
    }

    private void getFirstTerms(LinkedHashMap<Integer, BufferedReader> tmpFiles, LinkedHashMap<Integer, MutablePair<String, Integer>> termKeys, LinkedHashMap<Integer, String> termValues, int i) {
        try {
            BufferedReader file = tmpFiles.get(i);
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
                mergedFilesCounter++;
            }
        } catch (IOException e) {
            System.out.println("couldn't find or open: ");
            e.printStackTrace();
        }

    }

    private void checkOrMakeDir(String targetDirPath) {
        try {
            Path path = Paths.get(targetDirPath);
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }
        } catch (Exception e) {
            System.out.println("couldn't find or open: " + targetDirPath);
            e.printStackTrace();
        }
    }
}