package me.eddie.inventoryguiapi.language;

import java.util.Formatter;
import java.util.Locale;
import java.util.PropertyResourceBundle;

/**
 * Created by Edward on 28/12/2016.
 */
public class GUILanguageManager { //Manages all the messages that the plugin uses
    public static final String BUNDLE_NAME = "messages"; //The name of the bundle (.properties file) used for languages.
    private Locale preferredLocale;
    private PropertyResourceBundle resources; //The resources loaded, eg. all the messages the plugin will use

    public GUILanguageManager(Locale locale){
        initResources(locale); //Initialize the locale when constructed
    }

    public GUILanguageManager(){
        this(Locale.getDefault()); //Initialize the default locale when constructed
    }

    /**
     * Initialize the resource bundle to use. Will attempt to find the closest matching available for the given locale.
     * @param locale The locale to attempt to match
     */
    public void initResources(Locale locale){
        preferredLocale = locale;
        resources = (PropertyResourceBundle) PropertyResourceBundle.getBundle(BUNDLE_NAME, locale); //Use java's built-in locale/bundle system for managing localisation
    }

    /**
     * Get the locale this LanguageManager was initialized with and therefore should be used if possible
     * @return The Locale
     */
    public Locale getPreferredLocale(){
        return preferredLocale;
    }

    /**
     * Get the resource bundle being used to determine which messages to display
     * @return The currently used resource bundle
     */
    public PropertyResourceBundle getResources(){
        return resources;
    }

    /**
     * Convenience method that calls to the resource bundle
     * @param key The key to get the value of in the resource bundle
     * @return The value matching the specified key
     */
    public String getString(String key){
        return resources.getString(key);
    }

    /**
     * Get a formatted string. This will get a java-style string to format (Same as used for printf) from the resource bundle and then will format it with the given arguments.
     * @param key The key of the string in the resource bundle
     * @param args The arguments to use to format the string
     * @return The formatted string
     */
    public String getFormattedString(String key, Object... args){
        String str = getString(key);
        Formatter formatter = new Formatter(new StringBuilder(), getPreferredLocale());
        formatter.format(str, args);
        String output = formatter.out().toString();
        formatter.close();;
        return output;
    }
}
