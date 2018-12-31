package Searcher;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.*;

public class QuerySol {
    private String qNum;
    private String title;
    private String desc;
    private String narr;
    private int postingPointer;
    private ArrayList<String> sols;
    private MutablePair<Integer, Double> evaluation;

    public QuerySol(QuerySol other) {
        this.qNum = other.qNum;
        this.title = other.title;
        this.desc = other.desc;
        this.narr = other.narr;
        this.sols = other.sols;
        this.postingPointer = -1;
    }

    /**
     * Returns a Mutable pair of the last evaluated query where key is QueryNum and value is rank
     *
     * @return the evaluation pair
     */
    public MutablePair<Integer, Double> getEvaluation() {
        return evaluation;
    }

    public double getEvaluationRank(){
        if (evaluation==null) return -1;
        else return evaluation.right;
    }

    /**
     * Get the number of the most similar query to this one
     * @return number of other query
     */
    public int getEvaluationOtherQueryNum(){
        if (evaluation==null) return -1;
        else return evaluation.left;
    }

    /**
     * Set the Evaluation result.
     * @param otherQNum - the number of the most similar query to this one
     * @param rank - the evaluated rank
     */
    public void setEvaluation(int otherQNum, double rank) {
        if (evaluation == null) evaluation = new MutablePair<>(otherQNum, rank);
        else {
            evaluation.left = otherQNum;
            evaluation.right = rank;
        }
    }

    /**
     * Ctor
     * @param query - the query string as read from QueryDic class
     */
    public QuerySol(String query) {
        String[] q = split(query, "|");
        qNum = q[0];
        title = q[1];
        desc = remove(q[2],";");
        narr = remove(q[3],";");
        if (postingPointer == 0)
            try {
                postingPointer = Integer.parseInt(q[4], 36);
            } catch (Exception e) {
                QueryDic.getPointer();
            }
        sols = new ArrayList<>();
    }

    /**
     * Ctor with specified pointer
     * @param query - the query string as read from QueryDic class
     * @param pointer - the specified pointer
     */
    public QuerySol(String query, int pointer) {
        this(query);
        postingPointer = pointer;
    }

    /**
     * do not use the original list
     *
     * @return docnums
     */
    public ArrayList<String> getSols() {
        return new ArrayList<>(sols);
    }

    /**
     * Copy Ctor
     * @param other the copied query object
     */
    public void copySols(QuerySol other){
        StringBuilder stringBuilder = new StringBuilder(qNum);
        stringBuilder.append(",").append(join(other.sols,"|")).append("|");
        addPosting(stringBuilder.toString());
    }

    public String getqNum() {
        return qNum;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getNarr() {
        return narr;
    }

    public int getPostingPointer() {
        return postingPointer;
    }

    public void setPostingPointer(int p) {
        postingPointer += p;
    }

    public Integer getqNumAsInt() {

        return Integer.parseInt(qNum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuerySol) {
            QuerySol o = (QuerySol) obj;
            if (o.title.equalsIgnoreCase(this.title)) {
                return true;
            }
        }
        return false;
    }

    public void addPosting(String post) {
        if (!substringBefore(post, ",").equals(qNum))
            return;
        String[] docnums = split(substringAfter(post, ","), "|");

        for (int i = 0; i < docnums.length; i++) {
            if (!sols.contains(docnums[i])) {
                sols.add(docnums[i]);
            }
        }
    }

    /**
     * Filter the solutions in this query's solution list by DocNums.
     *      needs a set of documents that are valid to keep
     * @param docs - the documents we want to keep
     */
    public void filterSols(Set<String> docs) {
        if (docs == null || docs.size() == 0) return;
        ArrayList<String> newSols = new ArrayList<>();
        for (String s : sols) {
            if (docs.contains(s)) {
                newSols.add(s);
            }
        }
        sols = newSols;
    }

    /**
     * Trim the size of the solution list
     * @param i - the size after filtering
     */
    public void filterSolsNum(int i) {
        while (sols.size() > i) sols.remove(0);
    }

    /**
     * add a single Document number to the solution list
     * @param docNum - the added Document number
     */
    public void addSingleDoc(String docNum) {
        if (postingPointer == -1 && !sols.contains(docNum)) sols.add(docNum);
    }

    /**
     * get the size of the solution list
     * @return
     */
    public int getSolSize() {
        return sols.size();
    }

    /**
     * get the query as an array
     * @return String array of the query words
     */
    public String[] getTitleArray() {
        return split(title, " .,-/");
    }

    /**
     * get the evaluated rank of a single doc
     * @param doc - the number of the document
     * @return double - rank       Range: [0,1]
     */
    public Double getSolRank(String doc) {
        return sols.contains(doc)? getEvaluationRank(): 0;
    }
}
