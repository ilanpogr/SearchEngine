package Searcher;

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

    public QuerySol(String query) {
        String[] q = split(query, "|");
        qNum = q[0];
        title = q[1];
        desc = q[2];
        narr = q[3];
        if (postingPointer == 0)
            try {
                postingPointer = Integer.parseInt(q[4], 36);
            } catch (Exception e) {
                QueryDic.getPointer();
            }
        sols = new ArrayList<>();
    }

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
            if (o.title.equals(this.title)) {
                return true;
            }
        }
        return false;
    }

    public void addPosting(String readLine) {
        if (!substringBefore(readLine, ",").equals(qNum))
            return;
        String[] docnums = split(substringAfter(readLine, ","), "|");

        for (int i = 0; i < docnums.length; i++) {
            if (!sols.contains(docnums[i])) {
                sols.add(docnums[i]);
            }
        }
    }

    public void filterSols(Set<String> docs) {
        ArrayList<String> newSols = new ArrayList<>();
        for (String s : sols) {
            if (docs.contains(s)) {
                newSols.add(s);
            }
        }
        sols = newSols;
    }

    public void filterSolsNum(int i) {
        while (sols.size()>i) sols.remove(0);
    }
}
