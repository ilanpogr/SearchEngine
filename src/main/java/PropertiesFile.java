import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFile {

    private Properties properties;

    /**
     * @param propertiesFileName
     *          The name of the Properties file - relative to the
     *          CLASSPATH - that you want to process using this class.
     */
    public PropertiesFile(String propertiesFileName) {

        properties = new Properties();

        // Load the properties file into the Properties object
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName));
            System.out.println("Properties file '" + propertiesFileName + "' loaded.");
        } catch (IOException e) {
            String message = "Exception while reading properties file '" + propertiesFileName + "':" + e.getLocalizedMessage();
            System.out.println( );
            throw new RuntimeException(message, e);
        }
    }



    /**
     * Fetches a single property whose value will be returned as a String.
     * @param propertyName : The name of the property
     * @return String - the value of the property
     */
    public String getProperty(String propertyName) {
        String ret = "";
        ret = properties.getProperty(propertyName);
        System.out.println("Property value is '" + ret + "'");
        return ret;
    }

    public void resetProperties(String[] propertiesArray){
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
    public void putProperty(String propertyName, String propertyValue) {
        if (properties.getProperty(propertyName) == null) {
            properties.put(propertyName, propertyValue);
            System.out.println("new Property: " + propertyName + " , With value: " + propertyValue);
        } else {
            properties.setProperty(propertyName, propertyValue);
            System.out.println("Property changed: " + propertyName + " , With value: " + propertyValue);
        }
    }

    public void updatePropertiesFile() throws IOException {
        properties.store(new FileOutputStream("xyz.properties"), null);
    }
}
