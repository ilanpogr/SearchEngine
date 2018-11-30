package Controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFile {

    private static Properties properties = initialize();

    private static Properties initialize() {

        properties = new Properties();

        // Load the properties file into the Properties object
        String propertiesFileName = "project.properties";
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName));
            //System.out.println("Properties file '" + propertiesFileName + "' loaded.\n");
        } catch (IOException e) {
            String message = "Exception while reading properties file '" + propertiesFileName + "':" + e.getLocalizedMessage();
            //System.out.println();
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
        String ret = "";
        ret = properties.getProperty(propertyName);
        //System.out.println("Property " + propertyName + " :    value is '" + ret + "'");
        return ret;
    }

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
            //System.out.println("new Property: " + propertyName + " , With value: " + propertyValue);
        } else {
            properties.setProperty(propertyName, propertyValue);
            //System.out.println("Property changed: " + propertyName + " , With value: " + propertyValue);
        }
    }

    public static void updatePropertiesFile() {
        try {
            properties.store(new FileOutputStream("project.properties"), null);
            //System.out.println("Property file updated\n");
        } catch (IOException e) {
            String message = "Exception while reading properties file\n" + e.getLocalizedMessage();
            //System.out.println();
        }
    }
}
