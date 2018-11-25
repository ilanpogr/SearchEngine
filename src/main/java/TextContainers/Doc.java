package TextContainers;

import java.util.*;
import static org.apache.commons.lang3.StringUtils.split;

public class Doc {

    public final String docNum;
    public final String text;
    public final int length;
    private int max_tf;
    private Map<String, String> attributes;

    public Doc(StringBuilder docNum, StringBuilder text) {
        this.docNum = String.valueOf(docNum);
        this.text = String.valueOf(text);
        this.length = split(String.valueOf(text), ' ').length;
        max_tf=-1;
        this.attributes = new HashMap<>();
    }

    public int getMax_tf() {
        return max_tf;
    }

    public void setMax_tf(int max_tf) {
        if (this.max_tf!=-1 || max_tf<0) return;
        this.max_tf = max_tf;
    }

    public void getAtributes(String[] tag) {
        for (int i = 0; i < tag.length; i++) {
            tag[i] = attributes.getOrDefault(tag[i], "");
        }
    }

    public String[] getAllAttributes() {
        String[] att = new String[attributes.size()];
        int i = 0;
        Set<Map.Entry<String, String>> tmp = attributes.entrySet();
        for (Map.Entry attribute :
                tmp) {
            att[i++] = attribute.getKey() + ":" + attribute.getValue();
        }
        return att;
    }

    public String getAttributesValues() {
        return attributes.values().toString();
    }

    public void addAttributes(String[] tags) {
        for (int i = 0; i < tags.length - 1; i++) {
            attributes.put(tags[i++], tags[i]);
        }
    }

    public void addAttributes(ArrayList<String> tags) {
        for (int i = 0; i < tags.size() - 1; i++) {
            attributes.put(tags.get(i++), tags.get(i));
        }
    }
}
