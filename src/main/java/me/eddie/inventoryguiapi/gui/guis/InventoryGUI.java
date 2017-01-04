package me.eddie.inventoryguiapi.gui.guis;

import me.eddie.inventoryguiapi.gui.session.GUISession;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;

/**
 * Represents an InvenntoryGUI that players can view and interact with that's GUIState (Contents, etc...) is unique to each viewer
 */
public interface InventoryGUI {
    /**
     * Shows the player provided the GUI, and starts with the player viewing the given page
     * @param player The player who should be shown the GUI
     * @param page The page to open the GUI on
     */
    public void open(Player player, int page);

    /**
     * Shows the player provided the GUI, will show page 1. This method is the same as calling open(Player, 1)
     * @param player The player who should be shown the GUI
     */
    public void open(Player player);

    /**
     * Recalculates the GUIElements to show the player (and what their display itemstacks are) and will update what the player sees - if they are viewing this GUI.
     * If the given player is not viewing this GUI then this method will silently fail
     * @param player The player to use to recalculate the GUIElements being displayed
     */
    public void updateContentsAndView(Player player);

    /**
     * Will update what the viewer(s) see to match the GUI's state - if they are viewing this GUI.
     * If the given player is not viewing this GUI then this method will silently fail
     * @param player Viewer
     */
    public void updateView(Player player);

    /**
     * Handle the given Bukkit event, for example a ClickEvent
     * @param event The event to handle
     * @param session The GUI Session that should respond to the event
     */
    public void handleBukkitEvent(Event event, GUISession session);

    /*public GUIContentsProvider getContentsProvider();
    public GUIPresenter getGUIPresenter();
    public GUIPopulator getGUIPopulator();*/

    /**
     * Get the inventory type used by this GUI to display items to viewers
     * @return The InventoryType
     */
    public InventoryType getInventoryType();

    /**
     * Get the maximum size that this GUI can be before extra elements will flow onto new pages.
     * Note, this size has to be compatible with the possible sizes of the InventoryType specified. Eg. for chests it has to be a multiple of 9.
     * @return The maximum size that this GUI can be.
     */
    public int getMaximumGUISize();

    /**
     * Whether or not this GUI should re-scale it's size to wrap it's current contents or if it should always be the maximum size
     * @return True if the GUI should re-scale to wrap it's current contents, False otherwise
     */
    public boolean isGUISizeDynamic();
}
