package TextContainers;

import Controller.PropertiesFile;
import Indexer.Indexer;
import Indexer.WrieFile;
import org.apache.commons.lang3.tuple.MutablePair;
import org.ibex.nestedvm.util.Seekable;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * class representing a doc in file.
 */
public class Doc {

    private static Indexer indexer = new Indexer();
    private static String seperator = PropertiesFile.getProperty("file.posting.delimiter");
    private static int numberOfPersonalNames = PropertiesFile.getPropertyAsInt("number.of.person.names");
    private String fileName;
    private String city;
    private int length;
    private int max_tf;
    private boolean hasCity = false;
    private String language;
    private HashMap<String, String> attributes;
    private TreeMap<Integer,String> entities;
//    private String [] personasNames;
//    private int [] personalsFreqs;


    /**
     * Ctor
     */
    public Doc() {
        this.attributes = new HashMap<>();
        max_tf = -1;
        length = 0;
        entities = new TreeMap<>(Integer::compareTo);
//        personasNames = new String[numberOfPersonalNames];
//        personalsFreqs = new int[numberOfPersonalNames];
    }

    /**
     * Getters          (most of them are not used for now)
     */


    public String getFileName() {
        return fileName;
    }

    public int getMax_tf() {
        return max_tf;
    }

    public int getLength() {
        return length;
    }

    public void getAtributes(String[] tag) {
        for (int i = 0; i < tag.length; i++) {
            tag[i] = attributes.getOrDefault(tag[i], "");
        }
    }

    public String getAttribute(String key) {
        return attributes.getOrDefault(key, "");
    }

    public String getAttributesValues() {
        return attributes.values().toString();
    }

    public String docNum() {
        return attributes.get("DOCNO");
    }

    public String[] text() {
        return new String[]{attributes.getOrDefault("TEXT", "")};
    }

    public String getCity() {
        return city;
    }

    public String getLanguage() {
        return language;
    }

    /**
     * returns all the tags contained in the doc's text in the file.
     *
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

    /**
     * Setters (most of them are not used for now)
     */
    public void setFileName(String fileName) {
        if (this.fileName == null)
            this.fileName = fileName;
    }

    public void setLength(int length) {
        if (this.length==0) this.length=length;
    }

    public void setMax_tf(int max_tf) {
        if (this.max_tf != -1 || max_tf < 0) return;
        this.max_tf = max_tf;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * add an Attributes. if it representing the doc's city name,
     * the doc has a flag that this docs have a city.
     *
     * @param tags: the tags..
     */
    public void addAttributes(String... tags) {
        if (!hasCity && tags.length < 3) {
            if (tags[0].charAt(0) == 'F' || tags[0].charAt(0) == 'f') {
                hasCity = true;
                return;
            }
        }
        for (int i = 0; i < tags.length - 1; i++) {
            attributes.put(tags[i++], tags[i]);
        }
    }

    /**
     * add an Attributes. if it representing the doc's city name,
     * the doc has a flag that this docs have a city.
     *
     * @param tags: the tags.. now as ArrayList
     */
    public void addAttributes(ArrayList<String> tags) {
        if (!hasCity) {
            if (tags.get(0).charAt(0) == 'F' || tags.get(0).charAt(0) == 'f') {
                hasCity = true;
                return;
            }
        }
        for (int i = 0; i < tags.size() - 1; i++) {
            attributes.put(tags.get(i++), tags.get(i));
        }
    }

    /**
     * does this doc has a city mentioned?
     *
     * @return yes or no (true or false)
     */
    public boolean hasCity() {
        return hasCity;
    }

    /**
     * add an Entity to the list
     * @param termKey - name
     * @param freq - frequency
     */
    public void addEntity(String termKey, int freq) {
        if (!LanguagesInfo.getInstance().contains(termKey) && CityInfo.getInstance().getValueFromCitiesDictionary(termKey)==null) {
            if (entities.size() < numberOfPersonalNames) entities.put(freq, termKey);
            if (entities.firstKey() < freq) {
                entities.pollFirstEntry();
                entities.put(freq, termKey);
            }
        }
//        for (int i = 0; i < numberOfPersonalNames; i++) {
//            if (personalsFreqs[i]==0 || personalsFreqs[i]<freq){
//                personasNames[i]=termKey;
//                personalsFreqs[i]=freq;
//                return;
//            }
//        }
    }

    /**
     * appends the entities to a given string builder
     * and writes the entities into a file
     * @param stringBuilder - will append the entities as |<Entity>|<Frequency>|*
     * @return String that represents the pointer to the entity file in the Entities Dictionary (number in radix 36)
     */
    public String appendPersonas(StringBuilder stringBuilder) {
        while (entities.size()>0){
            Map.Entry<Integer,String> toPrint =entities.pollLastEntry();
            stringBuilder.append(toPrint.getValue()).append(seperator).append(toPrint.getKey()).append("\n");
        }
//        for (int i = 0; i < numberOfPersonalNames && personalsFreqs[i]!=0; i++) {
//            stringBuilder.append(personasNames[i]).append(seperator).append(personalsFreqs[i]).append("\n");
//        }
        return Integer.toString(stringBuilder.toString().getBytes().length+ indexer.appendToFile(stringBuilder,"Entities")+1,36);
    }

    public String appendPersonas(){
        return appendPersonas(new StringBuilder());
    }

    public String[] getAttributesToIndex() {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String tag = entry.getKey();
            if (tag.equalsIgnoreCase("DOCNO") || tag.length()<3 || !isAlphanumeric(tag)) continue;
            String val = entry.getValue();
            stringBuilder.append(tag).append(" ").append(val).append(" ");
        }
        return new String[] {stringBuilder.toString()};
    }
}
