package com.uniquid.settings;

import com.uniquid.settings.exception.UnknownSettingException;
import com.uniquid.settings.model.Setting;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Properties;

public class TestSettings {

    @Test
    public void testContructor() throws Exception {

        DummySettings dummy = new DummySettings();

        Assert.assertNotNull(dummy);

        Collection<Setting> settings = dummy.getSettings();

        Assert.assertTrue(settings.contains(DummySettings.DUMMY));

        Properties props = dummy.getProperties();

        Assert.assertTrue(props.containsKey(DummySettings.DUMMY.getKey()));

        try {

            dummy.setSetting("unknown", "value");

            Assert.fail();

        } catch (UnknownSettingException ex) {

            Assert.assertTrue(true);
        }

    }

}
