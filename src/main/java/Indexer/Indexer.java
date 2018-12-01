package Indexer;

import Controller.PropertiesFile;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Indexer {

    private static String termSeperator = PropertiesFile.getProperty("term.to.posting.delimiter");
    private static int tmpFilesCounter = 0;
    private static ConcurrentLinkedDeque<StringBuilder> tmpDicQueue = new ConcurrentLinkedDeque<>();


    public void indexTempFile(TreeMap<String, String> sortedTermsDic) {
        StringBuilder tmpPostFile = new StringBuilder();
        sortedTermsDic.forEach((k, v) ->
                tmpPostFile.append(k).append(termSeperator).append(v).append("\n"));
        tmpPostFile.trimToSize();
        tmpDicQueue.addLast(tmpPostFile);
        WrieFile.createTempPostingFile(tmpPostFile);
    }
}
/*private static void testFileSize(int mb) throws IOException {
        File file = File.createTempFile("test", ".txt");
        file.deleteOnExit();
        char[] chars = new char[1024];
        Arrays.fill(chars, 'A');
        String longLine = new String(chars);
        long start1 = System.nanoTime();
        PrintWriter pw = new PrintWriter(new FileWriter(file));
        for (int i = 0; i < mb * 1024; i++)
            pw.println(longLine);
        pw.close();
        long time1 = System.nanoTime() - start1;
        System.out.printf("Took %.3f seconds to write to a %d MB, file rate: %.1f MB/s%n",
                time1 / 1e9, file.length() >> 20, file.length() * 1000.0 / time1);

        long start2 = System.nanoTime();
        BufferedReader br = new BufferedReader(new FileReader(file));
        for (String line; (line = br.readLine()) != null; ) {
        }
        br.close();
        long time2 = System.nanoTime() - start2;
        System.out.printf("Took %.3f seconds to read to a %d MB file, rate: %.1f MB/s%n",
                time2 / 1e9, file.length() >> 20, file.length() * 1000.0 / time2);
        file.delete();
    }*/