package me.eddie.testing.inventoryguiapi;

import me.eddie.inventoryguiapi.language.GUILanguageManager;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Locale;

/**
 * Test written to test GUILanguageManager
 */
public class LanguageManagerTest {
    @Test
    public void testLanguageManager(){
        GUILanguageManager languageManager = new GUILanguageManager(Locale.ENGLISH);

        //Asset everything is as it should be
        assert languageManager.getPreferredLocale() != null;
        assert languageManager.getResources() != null;
        assert languageManager.getString("plugin.startup") != null; //Asset a key that we know should exist is there
        String formatted = languageManager.getFormattedString("plugin.startup", "name"); //Asset that a key that we know should exist is there, and that the formatting is working
        assert formatted != null;
        assert formatted.contains("name");
    }
}
