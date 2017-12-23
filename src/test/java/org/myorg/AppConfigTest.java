package org.myorg;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppConfigTest {

    @Before
    public void beforeTest() {
        final Properties props = new Properties();
        props.setProperty(AppProperty.WebPort.toString(), "8000");
        props.setProperty(AppProperty.ApiPort.toString(), "7010");
        props.setProperty(AppProperty.IsClustered.toString(), "false");
        props.setProperty(AppProperty.IsHttpSecure.toString(), "false");
        props.setProperty(AppProperty.IsProduction.toString(), "false");
        props.setProperty(AppProperty.UseMySql.toString(), "false");
        props.setProperty(AppProperty.UseDatabase.toString(), "false");

        // quick check that all defaults are set
        for (final AppProperty prop: AppProperty.values()) {
            if (props.getProperty(prop.toString()) == null)
                throw new RuntimeException("AppConfig.makeDefaultProperties() didn't set: "+prop.toString());
        }
        AppConfig.properties = props;
    }

    @Test
    public void testWebPortIs8000() {
        AppConfig.createInstance("test.properties");
        final int port = AppProperty.WebPort.getInt();
        Assert.assertEquals("Expect default WebPort property is 8000", 8000, port);
    }

    @Test
    public void testApiPortIs7010() {
        AppConfig.createInstance("test.properties");
        final int port = AppProperty.ApiPort.getInt();
        Assert.assertEquals("Expect default WebPort property is 7010", 7010, port);
    }

    @Test
    public void testIsClusteredIsFalse() {
        AppConfig.createInstance("test.properties");
        final boolean is = AppProperty.IsClustered.getBoolean();
        Assert.assertFalse("Expect IsClustered to be false", is);
    }


}
