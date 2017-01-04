package me.eddie.inventoryguiapi.gui.view;

import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.guis.SharedInventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
import me.eddie.inventoryguiapi.gui.session.InventoryState;
import me.eddie.inventoryguiapi.listeners.BukkitEventListener;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

/**
 * Class that takes the generated contents of an InventoryGUI and shows it to a player
 * To customize an inventory view beyond what this is capable of, simply extend this class
 * and make your GUI use your extended version of GUIPresenter.
 */
public class GUIPresenter {
    /**
     * Will update the viewer's view of the given GUISession. This will not recalculate what should be displayed though,
     * instead use {@link me.eddie.inventoryguiapi.gui.guis.InventoryGUI#updateContentsAndView(Player)} if this is what you require.
     * This method will also only do it for the provided viewer, in the case of shared inventories use
     * {@link SharedInventoryGUI#updateContentsAndView()} to update the view for all viewers
     * @param viewer The viewer of the GUI. Or if it's a GUI with multiple viewers, any viewer of the GUI.
     * @param session The GUISession that determines what should be displayed
     */
    public synchronized void updateView(final Player viewer, GUISession session){ //Synchronized so doesn't happen concurrently
        if(viewer == null || session == null){
            throw new IllegalArgumentException();
        }

        GUIState guiState = session.getGUIState();
        int page = session.getPage();

        InventoryState inventoryState = guiState.getExistingInventoryState(page); //Get the state to display
        while(inventoryState == null){ //Attempt to find a page that does exist nearby, since the state to display isn't there.
            page--;
            if(page < 0){
                throw new RuntimeException("Attempted to update the view of a GUI that doesn't have any state!"); //Output what went wrong
            }
            inventoryState = guiState.getExistingInventoryState(page);
        }

        InventoryGUI gui = session.getInventoryGUI();
        InventoryType inventoryType = gui.getInventoryType();
        int invMaxSize = gui.getMaximumGUISize();
        boolean isSizeDynamic = gui.isGUISizeDynamic();
        Map<Integer, GUIElement> guiElements = inventoryState.getComputedContentsBySlot();

        //Calculate the needed size of this GUI
        int requiredSize = 0;
        for(Map.Entry<Integer, GUIElement> entry:guiElements.entrySet()){
            if(entry.getKey() >= requiredSize){ //If slot bigger than or equal to requiredSize
                requiredSize = entry.getKey()+1; //Make the requiredSize the slot + 1
            }
        }
        if(requiredSize > invMaxSize || !isSizeDynamic){ //If we should be using the max size for this inventory
            requiredSize = invMaxSize;
        }
        else {
            requiredSize = requiredSize == 0 ? 9 : (int) (9*Math.ceil(requiredSize / 9.0d)); //Only chests support dynamic resizing, and they require inventories of a size that is a multiple of 9 (and larger than 0)
        }

        Inventory inventory = viewer.getOpenInventory() == null ? null : viewer.getOpenInventory().getTopInventory(); //Get inventory currently being viewed
        if(inventory != null &&
                (GUISession.extractSession(inventory) == null || !GUISession.extractSession(inventory).equals(session)
                        || inventory.getSize() != requiredSize
                        || !inventory.getTitle().equals(inventoryState.getTitle()))){ //If the existing inventory is unusable
            inventory = null;
        }

        if(inventory == null){ //Creating a new inventory, instead of re-using one
            inventory = inventoryType.equals(InventoryType.CHEST) ?
                    Bukkit.createInventory(session, requiredSize, inventoryState.getTitle())
                    : Bukkit.createInventory(session, inventoryType, inventoryState.getTitle()); //Create the inventory
        }

        //Fill the inventory with the correct items
        for(int i=0;i<inventory.getSize();i++){
            GUIElement elem = inventoryState.getElementInSlot(i);
            ItemStack display = elem == null ? null : elem.getDisplay(viewer, session);
            if(display == null || display.getType().equals(Material.AIR)){
                display = null;
            }
            inventory.setItem(i, display); //Place into the inventory the given item
        }

        InventoryView openInv = viewer.getOpenInventory();
        if(openInv != null && openInv.getTopInventory() != null && openInv.getTopInventory().equals(inventory)){
            viewer.updateInventory(); //Update what the player sees
        }
        else {
            //Close currently open inventory, specifying to ignore the event if for the same GUI, and then open the correct GUI
            Inventory open = openInv == null || openInv.getTopInventory() == null ? null : openInv.getTopInventory();
            if(open == null || open.getType().equals(InventoryType.CRAFTING) || open.getType().equals(InventoryType.CREATIVE)){ //For some reason top inventory can be their own inventory sometimes
                open = null; //They are not viewing an inventory, just their own player inventory
            }

            if(open == null){ //Not currently viewing an inventory
                viewer.openInventory(inventory); //So just open this one
                return;
            }

            GUISession otherInvSession = GUISession.extractSession(open); //Get any GUISessions for the inventory we are going to have to close
            boolean ignoreCloseEvent = otherInvSession != null && otherInvSession.getInventoryGUI().equals(gui); //If it's the same GUI as this one, don't want to fire the GUICloseEvent
            if(ignoreCloseEvent){
                //Use metadta to tell event listener to ignore inventory close events for this player
                viewer.setMetadata(BukkitEventListener.IGNORE_CLOSE_EVENT_META, new FixedMetadataValue(InventoryGUIAPI.getInstance(), "some non null value"));
                viewer.closeInventory();
                viewer.removeMetadata(BukkitEventListener.IGNORE_CLOSE_EVENT_META, InventoryGUIAPI.getInstance());
            }
            else {
                viewer.closeInventory();
            }
            final Inventory toOpen = inventory;
            //Open this inventory in a later tick than we closed the last one, as the Bukkit documentation suggests
            Bukkit.getScheduler().runTaskLater(InventoryGUIAPI.getInstance(), new Runnable(){
                @Override
                public void run() {
                    if(viewer.getOpenInventory() == null
                            || viewer.getOpenInventory().getTopInventory() == null
                            || !viewer.getOpenInventory().getTopInventory().equals(toOpen)) {
                        viewer.openInventory(toOpen);
                    }
                }
            }, 2L);
        }
    }
}
