package me.eddie.testing.inventoryguiapi;

import me.eddie.inventoryguiapi.language.GUILanguageManager;
import org.junit.Test;

import java.util.Locale;

/**
 * Test written to test GUILanguageManager
 */
public class LanguageManagerTest {
    @Test
    public void testLanguageManager(){
        GUILanguageManager languageManager = new GUILanguageManager(Locale.ENGLISH);

        //Assert everything is as it should be
        assert languageManager.getPreferredLocale() != null;
        assert languageManager.getResources() != null;
        assert languageManager.getString("plugin.startup") != null; //Assert a key that we know should exist is there
        String formatted = languageManager.getFormattedString("plugin.startup", "name"); //Assert that a key that we know should exist is there, and that the formatting is working
        assert formatted != null;
        assert formatted.contains("name");
    }
}
