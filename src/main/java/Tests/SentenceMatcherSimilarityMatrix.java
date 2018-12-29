package Tests;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.*;

public class SentenceMatcherSimilarityMatrix
{
    private static ILexicalDatabase db = new NictWordNet();

    /**
     * creates a Double 2D array with computing similarity for each word for both sentences
     * @param words1 - first sentence, each word in array's index
     * @param words2 - second sentence, each word in array's index
     * @param rc - the RelatednessCalculator Object
     * @return - 2D array containg the similarity for the sentences (#words1 x #words2)
     */
    private double[][] getSimilarityMatrix( String[] words1, String[] words2, RelatednessCalculator rc )
    {
        double[][] result = new double[words1.length][words2.length];
        for ( int i=0; i<words1.length; i++ ){
            for ( int j=0; j<words2.length; j++ ) {
                double score = rc.calcRelatednessOfWords(words1[i], words2[j]);
                result[i][j] = score;
            }
        }
        return result;
    }

    /**
     * computes sentence similarity as above and each time using different algorithms:
     * WuPalmer, Resnik, JiangConrath, Lin, LeacockChodrow, Path.
     * use this to understand how to use sentenceMatcherSimilarityMatrix
     * @param words1 - first sentence, each word in array's index
     * @param words2 - second sentence, each word in array's index
     */
    private void compute (String[] words1, String[] words2){
        System.out.println("WuPalmer");
        RelatednessCalculator rc1 = new WuPalmer(db);
        {
            double[][] s1 = getSimilarityMatrix(words1, words2,rc1);
            for(int i=0; i<words1.length; i++){
                for(int j=0; j< words2.length; j++){
                    System.out.print(s1[i][j] +"\t" + "|" + "\t");
                }
                System.out.println();
            }}
        System.out.println();
        System.out.println();

        System.out.println("Resnik");
        RelatednessCalculator rc2 = new Resnik(db);
        {
            double[][] s2 = getSimilarityMatrix(words1, words2,rc2);
            for(int i=0; i<words1.length; i++){
                for(int j=0; j< words2.length; j++){
                    System.out.print(s2[i][j] +"\t" + "|" + "\t");
                }
                System.out.println();
            }}
        System.out.println();
        System.out.println();

        System.out.println("JiangConrath");
        RelatednessCalculator rc3 = new JiangConrath(db);
        {
            double[][] s2 = getSimilarityMatrix(words1, words2,rc3);
            for(int i=0; i<words1.length; i++){
                for(int j=0; j< words2.length; j++){
                    System.out.print(s2[i][j] +"\t" + "|" + "\t");
                }
                System.out.println();
            }}
        System.out.println();
        System.out.println();

        System.out.println("Lin");
        RelatednessCalculator rc4 = new Lin(db);
        {
            double[][] s2 = getSimilarityMatrix(words1, words2,rc4);
            for(int i=0; i<words1.length; i++){
                for(int j=0; j< words2.length; j++){
                    System.out.print(s2[i][j] +"\t" + "|" + "\t");
                }
                System.out.println();
            }}
        System.out.println();
        System.out.println();

        System.out.println("LeacockChodrow");
        RelatednessCalculator rc5 = new LeacockChodorow(db);
        {
            double[][] s2 = getSimilarityMatrix(words1, words2,rc5);
            for(int i=0; i<words1.length; i++){
                for(int j=0; j< words2.length; j++){
                    System.out.print(s2[i][j] +"\t" + "|" + "\t");
                }
                System.out.println();
            }}
        System.out.println();
        System.out.println();

        System.out.println("Path");
        RelatednessCalculator rc6 = new Path(db);
        {
            double[][] s2 = getSimilarityMatrix(words1, words2,rc6);
            for(int i=0; i<words1.length; i++){
                for(int j=0; j< words2.length; j++){
                    System.out.print(s2[i][j] +"\t" + "|" + "\t");
                }
                System.out.println();
            }}
        System.out.println();
        System.out.println();

        System.out.println("Lesk");
        RelatednessCalculator rc7 = new Lesk(db);
        {
            double[][] s2 = getSimilarityMatrix(words1, words2,rc7);
            for(int i=0; i<words1.length; i++){
                for(int j=0; j< words2.length; j++){
                    System.out.print(s2[i][j] +"\t" + "|" + "\t");
                }
                System.out.println();
            }}
    }

//    public static void main(String[] args)
//    {
//        String sent1 = "finally a massive hurricane attacked my home";
//        String sent2 = "eventually a huge cyclone hit the enterance of my house";
//
//        String[] words1 = sent1.split(" ");
//        String[] words2 = sent2.split(" ");
//        SentenceMatcherSimilarityMatrix sm1 = new SentenceMatcherSimilarityMatrix();
//        sm1.compute(words1, words2);
//    }
}
