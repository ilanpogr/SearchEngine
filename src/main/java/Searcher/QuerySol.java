package Searcher;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.apache.commons.lang3.StringUtils.split;

public class QuerySol {
    private String qNum;
    private String title;
    private String desc;
    private String narr;
    private int postingPointer;

    public QuerySol(String query) {
        String [] q = split(query,"|");
        qNum = q[0];
        title = q[1];
        desc = q[2];
        narr = q[3];
    }

    public QuerySol (String query, int pointer){
        this(query);
        postingPointer=pointer;
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

    public String getNar() {
        return narr;
    }

    public int getPostingPointer() {
        return postingPointer;
    }

    public void setPostingPointer(int p) {
        postingPointer+=p;
    }

    public Integer getqNumAsInt() {

        return Integer.parseInt(qNum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuerySol){
            QuerySol o = (QuerySol) obj;
            if (o.title.equals(this.title)){
                return true;
            }
        }
        return false;
    }
}
