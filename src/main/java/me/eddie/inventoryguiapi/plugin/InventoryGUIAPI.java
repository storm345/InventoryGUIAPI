package me.eddie.inventoryguiapi.plugin;

import me.eddie.inventoryguiapi.language.GUILanguageManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The 'plugin' class. This class is the entrypoint of the plugin and is what defines a 'Plugin' to the Bukkit API
 */
public class InventoryGUIAPI extends JavaPlugin {
    public static InventoryGUIAPI instance; //The instance of the plugin; we can do this because there will only ever be one instance of the plugin object on the server
    public static GUILanguageManager languageManager; //The language manager which defines all the messages the plugin uses

    @Override
    public void onEnable() { //Called when the server starts up and this plugin gets enabled
        instance = this;
        languageManager = new GUILanguageManager();

        getLogger().info(languageManager.getFormattedString("plugin.startup", getName()));
    }

    @Override
    public void onDisable(){ //Called when the server shuts down and this plugin gets disabled
        getLogger().info(languageManager.getFormattedString("plugin.shutdown", getName()));
    }

}
