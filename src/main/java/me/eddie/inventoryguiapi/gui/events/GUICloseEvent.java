package me.eddie.inventoryguiapi.gui.events;

import me.eddie.inventoryguiapi.gui.session.GUISession;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Event that denotes that a GUI has been opened
 */
public class GUICloseEvent extends GUIEvent {
    private GUISession session;
    private Player viewer;

    public GUICloseEvent(GUISession session, Player viewer){
        if(session == null || viewer == null){
            throw new IllegalArgumentException();
        }
        this.session = session;
        this.viewer = viewer;
    }

    /**
     * Get the GUISession associated with the closed GUI
     * @return The GUISession
     */
    public GUISession getSession() {
        return session;
    }

    /**
     * Get the viewer who just closed this GUI
     * @return The viewer
     */
    public Player getViewer() {
        return viewer;
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
