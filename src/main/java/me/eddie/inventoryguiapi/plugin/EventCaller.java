package me.eddie.inventoryguiapi.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

/**
 * Fires an event through bukkit.
 * This one liner is in a separate class as it makes it easier to mock when testing
 */
public class EventCaller {
    /**
     * Fires an event through bukkit
     * @param event The event to fire through bukkit
     */
    public static void fireThroughBukkit(Event event){
        Bukkit.getPluginManager().callEvent(event);
    }
}
