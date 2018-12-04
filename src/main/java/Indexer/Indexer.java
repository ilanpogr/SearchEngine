package Indexer;

import Controller.Controller;
import Controller.PropertiesFile;
import Parser.Parse;
import Stemmer.Stemmer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;

import static org.apache.commons.io.FileUtils.sizeOf;
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
    private static final int cacheSlice = (int) getPropertyAsDouble("one.part.to.cache.from");
    private static final double maxIdfForCache = getPropertyAsDouble("max.idf.for.cache");
    private static final String cachePointer = PropertiesFile.getProperty("pointer.to.cache");
    private static final String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static final String fileDelimiter = PropertiesFile.getProperty("file.posting.delimiter");
//    private static ConcurrentLinkedDeque<StringBuilder> tmpDicQueue = new ConcurrentLinkedDeque<>();
    private static AtomicInteger tmpFilesCounter  = new AtomicInteger(0);
//    private static AtomicInteger termCount = new AtomicInteger(0);
    private static AtomicInteger mergedFilesCounter = new AtomicInteger(0);
    private static String targetPath = "C:\\Users\\User\\Documents\\לימודים\\אחזור מידע\\מנוע חיפוש\\tmp-run\\writerDir\\";
    private static final double log2 = StrictMath.log10(2);
    private static BufferedWriter inverter = null;
    private static TreeMap<Integer,String> mostCommonTerms = new TreeMap<>();
    private static TreeMap<Integer,String> leastCommonTerms = new TreeMap<>();


    /**
     * get a Property from properties file and convert it to double.
     * if it can't convert to Double, it will return 1.
     *
     * @param s - the value of the property
     * @return the value of the property
     */
    private static double getPropertyAsDouble(String s) {
        try {
            return Double.parseDouble(PropertiesFile.getProperty(s));
        } catch (Exception e) {
            System.out.println("Properties Weren't Set Right. Default Value is set, Errors Might Occur!");
            return 1;
        }
    }


    public void indexTempFile(TreeMap<String, String> sortedTermsDic) {
        try {
            StringBuilder tmpPostFile = new StringBuilder();
            sortedTermsDic.forEach((k, v) -> tmpPostFile.append(lowerCase(k)).append(termSeperator).append(v).append("\n"));
            tmpPostFile.trimToSize();
//            tmpDicQueue.addLast(tmpPostFile);
            WrieFile.createTempPostingFile(tmpPostFile);
            tmpFilesCounter.incrementAndGet();
        }  catch (OutOfMemoryError om) {     //if Map is too big
            try {
                ArrayList<Map.Entry<String, String>> lines = new ArrayList<>(sortedTermsDic.entrySet());
                lines.sort(new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                        return compareIgnoreCase(o1.getKey(), o2.getKey(), true);
                    }
                });
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

    public void mergePostingTempFiles(String targetDirPath) {
        double startIndexTime = System.currentTimeMillis();
        int last = 0;
        targetPath = targetDirPath;
        int currFileNum = WrieFile.getFileNum();
//        int currFileNum = 31;
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
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic,"others");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic,"ab");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "cd");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "ef");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "ghi");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "jkl");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "mn");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "op");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "qrs");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "tuvwxyz");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "Term Dictionary");
        initMergedDictionaries(mergedFilesCounterDic, mergedFilesDic, "Cache Dictionary");
        TreeMap<String, ArrayList<Integer>> termsSorter = new TreeMap<>();
        int tmpFilesInitialSize = tmpFiles.size();
        double logN = StrictMath.log10(Controller.getDocCount()) / log2;
//        double logN = StrictMath.log10(472000) / log2;
        while (mergedFilesCounter.get() < tmpFilesCounter.get()) {
//            if (Runtime.getRuntime().freeMemory()<Runtime.getRuntime().totalMemory()/25){
//                Controller.writeToFreeSpace(this);
//            }
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
            if (!contains(minTerm, " ")){
                if (mostCommonTerms.size()<1 || mostCommonTerms.firstKey()==null || mostCommonTerms.firstKey().compareTo(totalTf)<0){
                    if (mostCommonTerms.size()>10) mostCommonTerms.pollFirstEntry();
                    mostCommonTerms.put(totalTf,minTerm+termSeperator+totalTf + "," + df);
                }
                if (leastCommonTerms.size()<1 || leastCommonTerms.lastKey()==null || leastCommonTerms.lastKey().compareTo(totalTf)>0){
                    if (leastCommonTerms.size()>10) leastCommonTerms.pollLastEntry();
                    leastCommonTerms.put(totalTf,minTerm+leastCommonTerms+totalTf + "," + df);
                }
            }
            if (totalTf > minNumberOfTf || minTerm.contains(" ")) {
                String mergedFileName = getFileName(minTerm.charAt(0));
                if (isUpperCase == 1) {
//                    termDictionary.remove(minTerm);
                    minTerm = upperCase(minTerm);
                }
                if (maxIdfForCache < idf || df == 1) {
                    WrieFile.addPostLine(mergedFilesDic, "Term Dictionary", minTerm+termSeperator+totalTf + "," + df + "," + mergedFileName + "," + mergedFilesCounterDic.get(mergedFileName)+"\n");
                    WrieFile.addPostLine(mergedFilesDic, mergedFileName, stringBuilder.append("\n").toString());
                    mergedFilesCounterDic.replace(mergedFileName, mergedFilesCounterDic.get(mergedFileName) + 1);
                } else {
                    String[] cacheSplitedPost = splitToCachePost(stringBuilder);
                    WrieFile.addPostLine(mergedFilesDic, "Term Dictionary", minTerm+termSeperator+totalTf + "," + df + "," + mergedFileName + "," + mergedFilesCounterDic.get(mergedFileName) + "," + cachePointer+"\n");
                    WrieFile.addPostLine(mergedFilesDic, "Cache Dictionary", minTerm+termSeperator+cacheSplitedPost[0] + "," + mergedFilesCounterDic.get(mergedFileName)+"\n");
                    WrieFile.addPostLine(mergedFilesDic, mergedFileName, cacheSplitedPost[1] + "\n");
                    mergedFilesCounterDic.replace(mergedFileName, mergedFilesCounterDic.get(mergedFileName) + 1);
                }
            } else {
                Controller.removeFromDictionary(minTerm);
            }


            int total = (int) ((System.currentTimeMillis() - startIndexTime) / 1000);
            if (last < total) {
                last = total;
                System.out.println("Time until now to index: " + total / 60 + ":" + (total % 60 < 10 ? "0" : "") + total % 60 + " seconds \t\t\tCurrent term: " + minTerm);
            }
        }
        try {
            totalPostingSizeByKB = sizeOf(new File(targetDirPath));
        } catch (Exception e) {
            try {
                for (Map.Entry<String, BufferedWriter> entry : mergedFilesDic.entrySet()) {
                    totalPostingSizeByKB += new File(targetDirPath + entry.getKey() + ".post").length();
                    entry.getValue().close();
                }
            } catch (Exception e2) {
                totalPostingSizeByKB = Controller.getTermCount() * 1024;
            }
        }
        System.out.println("Size of Posting Files: " + totalPostingSizeByKB / 1024);

        writeToExcel();
        System.out.println("\n\nMost Common Terms before stemming: \n");
        mostCommonTerms.forEach(((integer, s) -> System.out.println(s)));
        System.out.println("\n\nLeast Common Terms before stemming: \n");
        leastCommonTerms.forEach(((integer, s) -> System.out.println(s)));

    }

    private void writeToExcel() {

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

    private void initMergedDictionaries(LinkedHashMap<String, Integer> mergedFilesCounterDic, LinkedHashMap<String, BufferedWriter> mergedFilesDic, String fileName) {
        StringBuilder stringBuilder = new StringBuilder(targetPath);
        checkOrMakeDir(stringBuilder.toString());
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
//                Files.delete(Paths.get(targetPath + i + ".post"));
                mergedFilesCounter.incrementAndGet();
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

    public void writeToDictionary(TreeMap<String, String> dic, String dicName) {
        try {
            inverter = new BufferedWriter(new FileWriter(new File(targetPath+dicName),true));
            StringBuilder s = new StringBuilder();
            dic.forEach((term, value) -> {
                try {
                    inverter.append(term).append(termSeperator).append(value).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
//            s.trimToSize();
//            WrieFile.writeToDictionary(s, dicName);
        } catch (OutOfMemoryError om) {     //if Map is too big
            try {
                ArrayList<Map.Entry<String, String>> lines = new ArrayList<>(dic.entrySet());
                lines.sort(new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                        return compareIgnoreCase(o1.getKey(), o2.getKey(), true);
                    }
                });
                writeToDictionary(new TreeMap<>(dic.tailMap(lines.get(lines.size() / 2).getKey())),dicName);
                writeToDictionary(new TreeMap<>(dic.headMap(lines.get(lines.size() / 2).getKey())),dicName);
            } catch (Exception e) {
                dic.forEach((term, value) -> {
                    StringBuilder s = new StringBuilder();
                    s.append(term).append(termSeperator).append(value).append("\n");
                    s.trimToSize();
//                    WrieFile.writeToDictionary(s, dicName);
                });
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static int getTmpFilesCounter() {
        return tmpFilesCounter.get();
    }
}