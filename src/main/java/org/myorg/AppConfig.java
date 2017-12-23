package org.myorg;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.myorg.backend.ApiServer;
import org.myorg.frontend.StaticServer;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * This is a singleton, an API to configuration properties.
 *<br>Use AppProperty.<prop>.getInt/Boolean/String()
 *
 * <p>See: http://vertx.io/docs/vertx-web/java/
 */
public class AppConfig {
    static final String PROPERTYFILE = "my-vert-app.properties";

    /**
     * For convenience, this launches all the servers in a single VM.
     * @param args - arguments are not used.
     */
    public static void main(String args[]) {
        StaticServer.main(args);
        ApiServer.main(args);
    }

    /**
     * Returns the singleton instance
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = createInstance(PROPERTYFILE);
        }
        return instance;
    }


    /**
     * If the instance is already created, it is returned.
     * If the filename isn't null or empty, it is loaded as a property file
     */
    static synchronized AppConfig createInstance(String filename) {
        if (instance != null)
            return instance;
        properties = new Properties();
        if ((filename != null) && !filename.isEmpty()) {
            final File file = new File(filename);
            if (!file.exists()) {
                throw new RuntimeException("Property file doesn't exist: "+file.getAbsolutePath());
            } else if (!file.isFile()) {
                throw new RuntimeException("Property file isn't a normal file: "+file.getAbsolutePath());
            } else if (!file.canRead()) {
                throw new RuntimeException("Property file isn't readable: "+file.getAbsolutePath());
            }
            try {
                // Standard char sets are "US-ASCII", "UTF-8", "ISO-8859-1"...
                final Reader reader = new InputStreamReader(new FileInputStream(filename), "US-ASCII");
                properties.load(reader);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        // check that the property file set all properties
        int numSet = 0;
        final int numExpected = AppProperty.values().length;
        for (final AppProperty prop: AppProperty.values()) {
            if (properties.getProperty(prop.toString()) == null)
                logger.error("Property file "+filename+" didnt set property: "+prop.toString());
            else
                numSet++;
        }
        if (numSet != numExpected) {
            throw new RuntimeException("Property file "+filename+" only set "+numSet+"/"+numExpected+" properties");
        }
        logger.info("Property file "+filename+", "+numSet+" properties were found");

        instance = new AppConfig();
        return instance;
    }


    /**
     * Use AppProperty.<Name>.getString(), or getBoolean(), or getInt().
     */
    String getPropertyValue(String propertyName) {
        if (properties == null)
            return null;
        return properties.getProperty(propertyName);
    }

    static Logger logger = LoggerFactory.getLogger(AppConfig.class);
    static Properties properties;
    static AppConfig instance;


    /**
     * Tell what log level is enabled for the passed class
     */
    static public void sayLogLevel(Class<? extends Object> clazz) {
        final Logger logger = LoggerFactory.getLogger(clazz);
        if (logger.isTraceEnabled())
            logger.trace("Trace is enabled for class "+clazz.getName());
        else if (logger.isDebugEnabled())
            logger.debug("Trace is enabled for class "+clazz.getName());
        else if (logger.isInfoEnabled())
            logger.info("Trace is enabled for class "+clazz.getName());
        else
            logger.warn("Only levels Warn and Error are enabled for class "+clazz.getName());
    }
}
