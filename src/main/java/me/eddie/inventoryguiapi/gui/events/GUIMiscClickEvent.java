package me.eddie.inventoryguiapi.gui.events;

import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Cancellable event that is fired when a GUIElement has been clicked on but has not yet handled the click
 */
public class GUIMiscClickEvent extends GUIClickEvent implements Cancellable {

    public GUIMiscClickEvent(GUISession session, Player viewer, int inventorySlot, GUIElement interactedElement, InventoryClickEvent bukkitEvent){
        super(session, viewer, inventorySlot, interactedElement, bukkitEvent);
    }

    private static final HandlerList handlers = new HandlerList(); //Required for Bukkit Events, can't just inherit
    @Override
    public HandlerList getHandlers() { //Required for Bukkit Events, can't just inherit
        return handlers;
    }
    public static HandlerList getHandlerList() { //Required for Bukkit Events, can't just inherit
        return handlers;
    }
}
