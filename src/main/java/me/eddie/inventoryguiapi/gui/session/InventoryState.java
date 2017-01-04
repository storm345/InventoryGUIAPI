package me.eddie.inventoryguiapi.gui.session;

import me.eddie.inventoryguiapi.gui.elements.GUIElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the current state of an InventoryGUI page. It keeps track of the currently displayed GUIElements and their positions,
 * the inventory (Bukkit) being currently displayed and it's title and also if there are pages available after this page.
 */
public class InventoryState extends AbstractAttributable {
    private String title;
    private Map<Integer, GUIElement> computedContentsBySlot = new HashMap<Integer, GUIElement>();
    private boolean hasNextPage = false;

    /**
     * Construct a new InventoryState. This will intialize with a null inventory and title and with no next page or contents.
     */
    public InventoryState(){

    }

    /**
     * Retrieve the element currently positioned in a slot
     * @param slot The slot to get the element currently in
     * @return The current element in this slot, or null if none
     */
    public GUIElement getElementInSlot(int slot){
        if(slot < 0){
            throw new IllegalArgumentException("Slot must be greater than or equal to 0");
        }
        synchronized(computedContentsBySlot) {
            return computedContentsBySlot.get(slot);
        }
    }

    /**
     * Get the currently computed contents of this InventoryState by slot
     * @return A copy of the currently computed contents of this InventoryState by slot
     */
    public Map<Integer, GUIElement> getComputedContentsBySlot(){
        synchronized(computedContentsBySlot) { //Synchronized to prevent concurrent modification
            return new HashMap<Integer, GUIElement>(computedContentsBySlot); //Return clone so that their copy isn't modified by ours and vice versa
        }
    }

    public void setComputedContentsBySlot(Map<Integer, GUIElement> computedContentsBySlot){
        synchronized(computedContentsBySlot) { //Synchronized to prevent concurrent modification
            this.computedContentsBySlot.clear();
            this.computedContentsBySlot.putAll(computedContentsBySlot);
        }
    }

    /**
     * Return the title calculated to be used to display this page, or null if none yet exists
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title calculated to be used to display this page, or null if none yet exists
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Whether or not another page exists after this one
     * @return True if another page exists after this one, or False if not
     */
    public boolean hasNextPage() {
        return hasNextPage;
    }

    /**
     * Set whether or not another page exists after this one
     * @param hasNextPage True if another page exists after this one, or False if not
     */
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
}
