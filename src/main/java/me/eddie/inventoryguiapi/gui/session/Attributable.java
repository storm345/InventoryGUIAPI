package me.eddie.inventoryguiapi.gui.session;

/**
 * Represents an object that attributes can be added to and remove from.
 */
public interface Attributable {
    /**
     * Get the value of an attribute
     * @param key The key of the attribute
     * @return The value of the attribute, or null if no value can be found for the given key
     */
    public Object getAttribute(Object key);

    /**
     * Check if a given attribute exists
     * @param key The key of the attribute
     * @return True if the attribute exists, or False if not
     */
    public boolean hasAttribute(Object key);

    /**
     * Set the value of an attribute key/value pair
     * @param key The key of the attribute key/value pair
     * @param value The value of the attribute
     */
    public void putAttribute(Object key, Object value);

    /**
     * Remove an attribute for the given key
     * @param key The key to remove the attribute for
     * @return The previous value for this attribute (before removal) or null
     */
    public Object removeAttribute(Object key);
}
