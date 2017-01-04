package me.eddie.inventoryguiapi.gui.session;

import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

/**
 * Represents a viewing session for a GUI.
 */
public class GUISession implements InventoryHolder {
    /**
     * Extract the GUISession from the inventory currently being viewed by a player, or null if none exists
     * @param player The player who's open inventory to extract the GUISession from
     * @return The GUISession or null if none exists
     */
    public static GUISession extractSession(Player player){
        if(player == null){
            return null;
        }
        InventoryView oInv = player.getOpenInventory();
        if(oInv == null){
            return null;
        }
        return extractSession(oInv.getTopInventory()); //Get the inventory the player is looking at (Bottom is always their own inventory)
    }

    /**
     * Get the GUISession for a given inventory, or null if none exists for this inventory
     * @param inventory The inventory to get the GUISession from
     * @return The GUISession or null if none exists
     */
    public static GUISession extractSession(Inventory inventory){
        if(inventory == null){
            return null;
        }
        InventoryHolder ih = inventory.getHolder();
        if(ih != null && ih instanceof GUISession){
            return (GUISession) ih;
        }
        return null;
    }

    private InventoryGUI inventoryGUI; //GUI Being viewed
    private int page = 1; //Currently displayed page number
    private GUIState guiState; //The state of the GUI being viewed, eg. what is currently being displayed

    /**
     * Construct a new GUISession
     * @param inventoryGUI The inventory that this a session to view
     * @param page The page currently being viewed
     * @param guiState The state of the GUI being viewed
     */
    public GUISession(InventoryGUI inventoryGUI, int page, GUIState guiState){
        if(inventoryGUI == null){
            throw new IllegalArgumentException("InventoryGUI must not be null");
        }
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        if(guiState == null){
            throw new IllegalArgumentException("GUIState must not be null");
        }

        this.inventoryGUI = inventoryGUI;
        this.page = page;
        this.guiState = guiState;
    }

    /**
     * Get the InventoryGUI being viewed
     * @return The InventoryGUI being viewed
     */
    public InventoryGUI getInventoryGUI() {
        return inventoryGUI;
    }

    /**
     * Get the page currently being viewed
     * @return The page
     */
    public int getPage() {
        return page;
    }

    /**
     * Set the page currently being viewed
     * @param page The page
     */
    public void setPage(int page) {
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        this.page = page;
    }

    /**
     * Get the state of the GUI currently being viewed
     * @return The GUIState of the GUI currently being viewed
     */
    public GUIState getGUIState() {
        return guiState;
    }

    /**
     * Method inherited from Bukkit's InventoryHolder. Will always return null
     * @return Null
     */
    @Override
    public Inventory getInventory() { //Part of InventoryHolder from bukkit
        return null; //doesn't matter at all if null returned
    }
}
