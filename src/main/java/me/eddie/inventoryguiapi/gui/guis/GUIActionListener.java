package me.eddie.inventoryguiapi.gui.guis;

import me.eddie.inventoryguiapi.gui.events.GUIEvent;

/**
 * An interface that can be used to define handling for GUIEvents for a given GUI. This interface handles events before
 * the events are given to GUIElements for handling.
 */
public interface GUIActionListener {
    /**
     * Method is called when an event happens to this GUI. Cancelling a GUIEvent will prevent further handling of it
     * by the API, but will not cancel the Bukkit event that caused it. To cancel the Bukkit event causing this GUIEvent,
     * use the GUIEvent to find the Bukkit event that caused it and cancel this.
     * @param event The event to be handled.
     */
    public void onEvent(GUIEvent event);
}
