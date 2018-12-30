package TextContainers;

import Controller.PropertiesFile;
import Indexer.Indexer;
import Indexer.WrieFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * class representing a doc in file.
 */
public class Doc {

    private static StringBuilder entitiesPrinter = new StringBuilder();
    private static String seperator = PropertiesFile.getProperty("file.posting.delimiter");
    private static int numberOfPersonalNames = PropertiesFile.getPropertyAsInt("number.of.person.names");
    private String fileName;
    private String city;
    private int length;
    private int max_tf;
    private boolean hasCity = false;
    private String language;
    private HashMap<String, String> attributes;
    private TreeSet<ImmutablePair<String, Integer>> entities;


    /**
     * Ctor
     */
    public Doc() {
        this.attributes = new HashMap<>();
        max_tf = -1;
        length = 0;
        entities = new TreeSet<>(new Comparator<ImmutablePair<String, Integer>>() {
            @Override
            public int compare(ImmutablePair<String, Integer> o1, ImmutablePair<String, Integer> o2) {
                int res = Integer.compare(o1.right, o2.right);
                if (res == 0) {
                    res = StringUtils.compareIgnoreCase(o1.left, o2.left);
                }
                return res;
            }
        });
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

    public static StringBuilder getEntitiesPrinter() {
        return entitiesPrinter;
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
        if (this.length == 0) this.length = length;
    }

    public void setMax_tf(int max_tf) {
        if (this.max_tf != -1 || max_tf < 0) return;
        this.max_tf = max_tf;
    }

    public void setCity(String city) {
        this.city = city;
        if (isEmpty(city)) hasCity = false;
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
     *
     * @param termKey - name
     * @param freq    - frequency
     */
    public void addEntity(String termKey, int freq) {
        if (entities.size() < numberOfPersonalNames) {
            entities.add(new ImmutablePair<>(termKey, freq));
            return;
        }
        if (entities.first().right < freq) {
            entities.pollFirst();
            entities.add(new ImmutablePair<>(termKey, freq));
        }
    }

    /**
     * appends the entities to a given string builder
     * and writes the entities into a file
     *
     * @param stringBuilder - will append the entities as |<Entity>|<Frequency>|*
     * @return String that represents the pointer to the entity file in the Entities Dictionary (number in radix 36)
     */
    public String appendPersonas(StringBuilder stringBuilder) {
        while (entities.size() > 0) {
            ImmutablePair<String, Integer> toPrint = entities.pollLast();
            stringBuilder.append(toPrint.left).append(seperator).append(toPrint.right).append("|");
        }
        entitiesPrinter.append(stringBuilder.append("\n"));

        return Integer.toString(entitiesPrinter.toString().getBytes().length + 1, 36);
    }

    /**
     * appends the entities to a string builder (which wont be saved)
     * and writes the entities into a file
     *
     * @return String that represents the pointer to the entity file in the Entities Dictionary (number in radix 36)
     */
    public String appendPersonas() {
        return appendPersonas(new StringBuilder());
    }

    /**
     * get all tags of this document to index it
     *
     * @return String array to parse
     */
    public String[] getAttributesToIndex() {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String tag = entry.getKey();
            if (tag.equalsIgnoreCase("DOCNO") || tag.equalsIgnoreCase("DOCID") || !isAlphanumeric(tag)) continue;
            String val = entry.getValue();
            stringBuilder.append(val).append(" ");
        }
        if (city != null) stringBuilder.append(city);
        return new String[]{stringBuilder.toString()};
    }
}
