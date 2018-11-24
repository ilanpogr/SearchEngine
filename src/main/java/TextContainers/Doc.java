package TextContainers;

import java.util.*;

public class Doc {

    public final String docNum;
    public final String text;
    public final int length;
    private Map<String, String> attributes;
    private String docBuilder;

    public Doc(String docNum, String text) {
        this.docNum = docNum;
        this.text = text;
        this.length = text.split(" ").length;
        this.attributes = new HashMap<>();
    }

    public Doc(String[] doc) {
        docBuilder = doc[0];
        this.docNum = extractDocNum();
        this.text = "";
        this.length = 0;
        this.attributes = new HashMap<>();
    }

    private String extractDocNum() {
        String s = "";
        String[] s1 = docBuilder.split("</DOCNO>");
        s = s1[0];
        docBuilder = s1[1];
        s1 = s.split("<DOCNO>");
        s=s1[1].trim();
        docBuilder=s1[0]+docBuilder;
        return s;
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
