package org.myorg;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * The properties are enumerated values.
 *
 * Each one has a type- use its getInt(), getBoolean() or getString() method as appropriate.
 * Use the wrong one, and it'll throw an exception.
 */
public enum AppProperty {
    WebPort(PropertyType.IntProperty),
    ApiPort(PropertyType.IntProperty),
    IsClustered(PropertyType.BooleanProperty),
    UseSessions(PropertyType.BooleanProperty),
    UsingStickySessions(PropertyType.BooleanProperty),
    IsHttpSecure(PropertyType.BooleanProperty),
    IsProduction(PropertyType.BooleanProperty),
    UseMySql(PropertyType.BooleanProperty),
    UseDatabase(PropertyType.BooleanProperty),
    MysqlDBHost(PropertyType.StringProperty),
    MysqlDBName(PropertyType.StringProperty),
    MysqlDBUsername(PropertyType.StringProperty),
    MysqlDBPassword(PropertyType.StringProperty),
    MysqlDBPoolSize(PropertyType.IntProperty),
    MysqlDBPort(PropertyType.IntProperty),
    MysqlDBTimeout(PropertyType.IntProperty)
    ;

    final PropertyType valueType;
    AppProperty(PropertyType valueType) {
        this.valueType = valueType;
    }

    /**
     * Returns the value for the numeric property, or zero if there was none.
     */
    public int getInt() {
        if (isBadType(PropertyType.IntProperty)) {
            return 0;
        }
        final String s = getPropertyValue();
        try {
            return Integer.valueOf(s);
        } catch (final Throwable t) {
            final Logger logger = LoggerFactory.getLogger(AppProperty.class);
            logger.error("Value for property "+toString()+" was'"+s+"' not numeric, returning 0", t);
            return 0;
        }
    }


    /**
     * Returns true if the value is "true", else false if the value is not set or "false"
     */
    public boolean getBoolean() {
        if (isBadType(PropertyType.BooleanProperty)) {
            return false;
        }
        final String s = getPropertyValue();
        if (s.isEmpty() || "false".equals(s))
            return false;
        if (s.equals("true"))
            return true;
        final Logger logger = LoggerFactory.getLogger(AppProperty.class);
        logger.error("Value for property "+toString()+" was '"+s+"', not boolean, returning false");
        return false;
    }


    /**
     * Returns the property value, or emptyString if there's none.
     */
    public String getString() {
        if (isBadType(PropertyType.StringProperty)) {
            return "";
        }
        final String s = getPropertyValue();
        return s.equals("<none>") ? "" : s;
    }


    /**
     * This is set when a property is first interrogated.
     * Tests set it first instead of being automatically set from AppConfig.getInstance();
     */
    static AppConfig appConfig; // package-private, for tests

    enum PropertyType {
        IntProperty, StringProperty, BooleanProperty;
    }

    /**
     * It sets appConfig when it first runs, if not set, to allow tests to set it first.
     */
    private String getPropertyValue() {
        if (appConfig == null) {
            appConfig = AppConfig.getInstance();
        }
        final String s = AppConfig.getInstance().getPropertyValue(toString());
        return (s == null) ? "" : s.trim();
    }


    /**
     * Ensures no one calls the wrong getX() call, eg getBoolean() for an int.
     * @return true if the gotType differs from the enum's type, after logging an error.
     */
    private boolean isBadType(PropertyType gotType) {
        if (!valueType.equals(gotType)) {
            final Logger logger = LoggerFactory.getLogger(AppProperty.class);
            logger.error("Software error: property "+toString()+" has type "+valueType+", not "+gotType);
            return true;
        }
        return false; // means there's no problem
    }
}
