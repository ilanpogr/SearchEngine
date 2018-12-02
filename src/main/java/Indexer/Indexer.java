package Indexer;

import Controller.Controller;
import Controller.PropertiesFile;
import Parser.Parse;
import Stemmer.Stemmer;
import org.apache.commons.io.FileUtils;
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

    private static double totalPostingSizeByKB = 0;
    private static int minNumberOfTf = (int) getPropertyAsDouble("min.tf.to.save");
    private static int cacheSlice = (int) getPropertyAsDouble("one.part.to.cache.from");
    private static double maxIdfForCache = getPropertyAsDouble("max.idf.for.cache");
    private static String cachePointer = PropertiesFile.getProperty("pointer.to.cache");

    private static double getPropertyAsDouble(String s) {
        try {
            return Double.parseDouble(PropertiesFile.getProperty(s));
        } catch (Exception e) {
            System.out.println("Properties Weren't Set Right. Default Value is set, Errors Might Occur!");
            return 1;
        }
    }

    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
    private static int tmpFilesCounter = 0;
    private static int mergedFilesCounter = 0;
    private static ConcurrentLinkedDeque<StringBuilder> tmpDicQueue = new ConcurrentLinkedDeque<>();
    private static String targetPath = null;


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
        int tmpFilesInitialSize =tmpFiles.size();
        int othersFilesCounter = 1;
        while (mergedFilesCounter < tmpFilesCounter) {
            stringBuilder.setLength(0);
            ArrayList<Integer> minTerms = new ArrayList<>();
            for (int i = 1; i <= tmpFilesInitialSize; i++) {
                if (!tmpFiles.containsKey(i)){
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
            } catch (NullPointerException npe){
                continue;
            }
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
            if (totalTf > minNumberOfTf) {
                String mergedFileName = getFileName(minTerm.charAt(0));
                if (maxIdfForCache < idf || df == 1) {
                    termDictionary.put(minTerm, totalTf + "," + df + "," + mergedFileName + "," + mergedFilesCounterDic.get(mergedFileName) + "," + cachePointer);
                    WrieFile.addPostLine(mergedFilesDic, mergedFileName, stringBuilder.append("\n").toString());
                    mergedFilesCounterDic.replace(mergedFileName, mergedFilesCounterDic.get(mergedFileName) + 1);
                } else {
                    String[] cacheSplitedPost = splitToCachePost(stringBuilder);
                    cache.put(minTerm, cacheSplitedPost[0] + "," + mergedFilesCounterDic.get(mergedFileName));
                    termDictionary.put(minTerm, totalTf + "," + df + "," + mergedFileName + "," + mergedFilesCounterDic.get(mergedFileName));
                    WrieFile.addPostLine(mergedFilesDic, mergedFileName, cacheSplitedPost[1] + "\n");
                    mergedFilesCounterDic.replace(mergedFileName, mergedFilesCounterDic.get(mergedFileName) + 1);
                }
            } else {
                termDictionary.remove(minTerm);
            }
//            if (mergedFilesCounterDic.get("others")>10000){
//                FileUtils.getFile(targetPath+"others.post").renameTo(new File(targetPath + "others" + othersFilesCounter + ".post"));
//                mergedFilesCounterDic.replace("others",1);
//            }
        }
        try {
            for (Map.Entry<String, BufferedWriter> entry : mergedFilesDic.entrySet()) {
                totalPostingSizeByKB += new File(targetDirPath + entry.getKey() + ".post").length();
                entry.getValue().close();
            }
        } catch (Exception e) {
            totalPostingSizeByKB = FileUtils.sizeOf(new File(targetDirPath));
        }
        System.out.println("Size of Posting Files: " + totalPostingSizeByKB/1024);

    }

    private String[] splitToCachePost(StringBuilder stringBuilder) {
        String[] forCache = split(stringBuilder.toString(), fileDelimiter);
        int sliceIndex = forCache.length / cacheSlice;
        if (sliceIndex % 2 == 1) {
            sliceIndex = Integer.max(--sliceIndex, 2);
        }
        return new String[]{join(forCache, fileDelimiter, 0, sliceIndex), join(forCache, fileDelimiter, sliceIndex, forCache.length)};//TODO- Check!!!

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
            case '0':
                return "0_1";
            case '2':
                return "2_3_4";
            case '5':
                return "5_6_7";
            case '8':
                return "8_9";
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
        fileName = "ijkl";
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
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "0_1";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "2_3_4";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "5_6_7";
        mergedFilesCounterDic.put(fileName, 1);
        stringBuilder.append(fileName).append(".post");
        addFileToList(mergedFilesDic, stringBuilder, fileName);
        stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
        fileName = "8_9";
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