import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.*;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class ws4j_example {

    /**
     * EXAMPLES THAT DIDNT UNDERSTAND:
     *  https://www.programcreek.com/java-api-examples/?api=edu.cmu.lti.ws4j.RelatednessCalculator
     *  DEMO: http://ws4jdemo.appspot.com/?mode=w&s1=&w1=car&s2=&w2=car
     *
     */

    private static ILexicalDatabase db = new NictWordNet();
//    available options of metrics
    private static RelatednessCalculator[] rcs = {new WuPalmer(db),
            new Resnik(db), new JiangConrath(db), new Lin(db)};

    // names:
    private static String[] names = {"WuPalmer","Resnik","JiangConrath", "Lin"};

    private static double compute(String word1, String word2, RelatednessCalculator rc) {
        WS4JConfiguration.getInstance().setMFS(true);
//        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return rc.calcRelatednessOfWords(word1, word2);
    }

    public static void main(String[] args) {
        String[] words = {"add", "get", "filter", "remove", "check", "find", "collect", "create", "hurricane", "cyclone", "home", "house", "car", "vehicle","cat","dog","pet","puppy"};
        long start;
        long finish;
        for (int j = 0; j < rcs.length; j++) {
            System.out.println("--------------------------------------------------");
            System.out.println();
            System.out.println("-------"+ names[j].toUpperCase() + "-------");
            System.out.println();
            start = System.currentTimeMillis();
            for (int i = 0; i < words.length - 1; i++) {
                double distance = compute(words[i], words[i + 1],rcs[j]);
                System.out.println(words[i] + " -  " + words[i + 1] + " = " + distance);
            }
            finish = System.currentTimeMillis();
            System.out.println();
            System.out.println("Time took computing: " + (finish - start) + " millis");
        }
    }

}
