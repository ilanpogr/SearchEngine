package Controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


/**
 * this class is the controller of the properties in this project.
 * all calles to read,write,update the properties, comes from this class
 * static and initialized at the start.
 */
public class PropertiesFile {

    private static Properties properties = initialize();


    /**
     * initialize properties file.
     * @return a properties object.
     */
    private static Properties initialize() {
        properties = new Properties();

        // Load the properties file into the Properties object
        String propertiesFileName = "project.properties";
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName));
        } catch (IOException e) {
            String message = "Exception while reading properties file '" + propertiesFileName + "':" + e.getLocalizedMessage();
            throw new RuntimeException(message, e);
        }
        return properties;
    }



    /**
     * Fetches a single property whose value will be returned as a String.
     * @param propertyName : The name of the property
     * @return String - the value of the property
     */
    public static String getProperty(String propertyName) {
            return  new String(properties.getProperty(propertyName).getBytes(), StandardCharsets.UTF_8);

    }

    /**
     * reseting all the properties that can be controlled by the user!
     * and only them!
     * the set of those properties are saved in ControllerMenu.
     * @param propertiesArray
     */
    public static void resetProperties(String[] propertiesArray){
        for (String property : propertiesArray){
            if (properties.getProperty(property) != null) {
                properties.setProperty(property, "");
            }
        }
    }

    /**
     * Adds a property to this Properties file.
     * @param propertyName : property name
     * @param propertyValue : property value
     */
    public static void putProperty(String propertyName, String propertyValue) {
        if (properties.getProperty(propertyName) == null) {
            properties.put(propertyName, propertyValue);
        } else {
                properties.setProperty(propertyName, new String(propertyValue.getBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * the properties are saved in the heap. if we want to save the current properties in file
     * call this function.
     */
    public static void updatePropertiesFile() {
        try {
            properties.store(new FileOutputStream("project.properties"), null);
        } catch (IOException e) {
            String message = "Exception while reading properties file\n" + e.getLocalizedMessage();
        }
    }
}
