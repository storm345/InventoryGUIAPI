package me.eddie.inventoryguiapi.gui.session;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the current state of an InventoryGUI being displayed. It keeps track of all the pages of the GUI and their respective InventoryStates
 */
public class GUIState extends AbstractAttributable {
    private Map<Integer, InventoryState> inventoryStatesByPage = new HashMap<Integer, InventoryState>(); //Map that keeps track of inventory states by page; when a page is open it's inventory state will either be created and put into this map, or if one already exists then it will be re-used (But with the contents recalculated naturally)

    /**
     * Create a new GUIState
     */
    public GUIState(){
        //Empty constructor
    }

    private InventoryState genNewInventoryState(){
        return new InventoryState();
    }

    public Map<Integer, InventoryState> getInventoryStatesByPage(){
        return new HashMap<Integer, InventoryState>(inventoryStatesByPage); //Return a clone of the map so that they cannot modify our version and vice versa
    }

    /**
     * Check if an InventoryState exists for a given page
     * @param page The page to check if an inventory state exists for
     * @return True if one exists or false if not
     */
    public boolean hasInventoryState(int page){
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        return inventoryStatesByPage.containsKey(page);
    }

    /**
     * Get the InventoryState for the given page, creating a new one if none existed
     * @param page The page to get the state of
     * @return The Inventory State
     */
    public InventoryState getOrCreateInventoryState(int page){
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        synchronized (inventoryStatesByPage){ //Synchronized to prevent concurrent threads causing issues
            InventoryState state = getExistingInventoryState(page);
            if(state == null){
                state = genNewInventoryState();
                inventoryStatesByPage.put(page, state); //Put into our map the inventory state since we want callers to this to always receive the same inventory state
            }

            return state;
        }
    }

    /**
     * Remove the inventory state for a given page
     * @param page The page to remove it for
     */
    public void removeInventoryState(int page){
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        synchronized (inventoryStatesByPage) {
            inventoryStatesByPage.remove(page);
        }
    }

    /**
     * Update the inventory state for a given page
     * @param page The page to update the inventory state for
     * @param state The InventoryState of the given page
     */
    public void updateInventoryState(int page, InventoryState state){ //should not ever be necessary since same InventoryState object is given out but allows for a new InventoryState object to replace an existing one
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        if(state == null){
            throw new IllegalArgumentException("InventoryState must not be null");
        }
        synchronized (inventoryStatesByPage){
            inventoryStatesByPage.put(page, state);
        }
    }

    /**
     * Get the InventoryState for the given page, or null if none exists
     * @param page The page to get the state of
     * @return The Inventory State
     */
    public InventoryState getExistingInventoryState(int page){
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        return inventoryStatesByPage.get(page);
    }
}
