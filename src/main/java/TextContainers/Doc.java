package TextContainers;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * class representing a doc in file.
 */
public class Doc {

    private String city;
    private int length;
    private int max_tf;
    private boolean hasCity=false;
    private String language;
    private Map<String, String> attributes;


    public Doc(){
        this.attributes = new HashMap<>();
        max_tf=-1;
        length=0;
    }

    public int getMax_tf() {
        return max_tf;
    }

    public int getLength() {
        return length;
    }

    public void setLength() {
        if (length==0) {
            for (Map.Entry<String, String> att : attributes.entrySet()
            ) {
                length+=split(att.getValue()," ").length+1;
            }
        }
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

    /**
     * returns all the tags contained in the doc's text in the file.
     * @return: an String array containing the tags.
     */
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

    /**
     * add an Attributes. if it representing the doc's city name,
     * the doc has a flag that this docs have a city.
     * @param tags: the tags..
     */
    public void addAttributes(String... tags) {
        if (!hasCity && tags.length<3){
            if (tags[0].charAt(0)=='F' || tags[0].charAt(0)=='f'){
                hasCity = true;
                return;
            }
        }
        for (int i = 0; i < tags.length - 1; i++) {
            attributes.put(tags[i++], tags[i]);
        }
    }

    public void addAttributes(ArrayList<String> tags) {
        if (!hasCity){
            if (tags.get(0).charAt(0) == 'F' || tags.get(0).charAt(0) == 'f'){
                hasCity = true;
                return;
            }
        }
        for (int i = 0; i < tags.size() - 1; i++) {
            attributes.put(tags.get(i++), tags.get(i));
        }
    }

    public String getAttribute(String key){
            return attributes.getOrDefault(key,"");
    }

    public String docNum() {
        return attributes.get("DOCNO");
    }

    public String [] text() {
        return new String[]{attributes.getOrDefault("TEXT","")};
    }

    public boolean hasCity() {
        return hasCity;
    }

    private static void validateArgs(final String[] argument) {
                if (argument == null) {
            throw new NullPointerException("Args must not be null");
        }

        if (argument.length == 0) {
            throw new IllegalArgumentException(
                    "At least one Args must be defined");
        }

        for (String extension : argument) {
            if (extension == null) {
                throw new NullPointerException(
                        "Args must not be null");
            }

            if (extension.isEmpty()) {
                throw new IllegalArgumentException(
                        "Args must not be empty");
            }
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
