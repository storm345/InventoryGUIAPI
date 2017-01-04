package me.eddie.inventoryguiapi.listeners;

import me.eddie.inventoryguiapi.gui.session.GUISession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Listens to Bukkit events and passes them to GUIs when relevant
 */
public class BukkitEventListener implements Listener {
    /**
     * Metadata that if a player has then a GUI will ignore this event
     */
    public static String IGNORE_CLICK_EVENT_META = "InventoryGUIAPI.IgnoreClickEvent";
    /**
     * Metadata that if a player has then a GUI will ignore this event
     */
    public static String IGNORE_DRAG_EVENT_META = "InventoryGUIAPI.IgnoreClickEvent";
    /**
     * Metadata that if a player has then a GUI will ignore this event
     */
    public static String IGNORE_CLOSE_EVENT_META = "InventoryGUIAPI.IgnoreClickEvent";


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST) //Lowest event priority so other plugins handle event first
    public void onInventoryClick(InventoryClickEvent event){ //Click Event also is the pickup and place item events in the Bukkit API
        Entity entity = event.getWhoClicked();
        if(!(entity instanceof Player)){
            return;
        }

        Player player = (Player) entity;
        if(player.hasMetadata(IGNORE_CLICK_EVENT_META)){ //ignore this event
            return;
        }

        InventoryView iv = event.getView();
        if(iv == null || iv.getTopInventory() == null){
            return;
        }

        Inventory inventory = iv.getTopInventory();

        GUISession guiSession = GUISession.extractSession(inventory);
        if(guiSession == null){
            //They didn't click on a GUI
            return; //Ignore
        }
        //They clicked on a GUI
        if(event.getClickedInventory() == null){
            return; //Not an event we need to care about
        }

        if(!event.getClickedInventory().equals(inventory)
                && event.getClick().equals(ClickType.NUMBER_KEY)){
            event.setCancelled(true); //Do not allow using number keys to insert into this inventory
            return;
        }

        guiSession.getInventoryGUI().handleBukkitEvent(event, guiSession);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST) //Lowest event priority so other plugins handle event first
    public void onInventoryDragEvent(InventoryDragEvent event){
        Entity entity = event.getWhoClicked();
        if(!(entity instanceof Player)){
            return;
        }

        Player player = (Player) entity;
        if(player.hasMetadata(IGNORE_DRAG_EVENT_META)){ //ignore this event
            return;
        }

        InventoryView iv = event.getView();
        if(iv == null || iv.getTopInventory() == null){
            return;
        }

        Inventory inventory = iv.getTopInventory();

        GUISession guiSession = GUISession.extractSession(inventory);
        if(guiSession == null){
            //They didn't interact with a GUI
            return; //Ignore
        }
        //They interacted with a GUI

        guiSession.getInventoryGUI().handleBukkitEvent(event, guiSession);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event){
        Entity entity = event.getPlayer();
        if(!(entity instanceof Player)){
            return;
        }

        Player player = (Player) entity;
        if(player.hasMetadata(IGNORE_CLOSE_EVENT_META)){ //ignore this event
            return;
        }

        InventoryView iv = event.getView();
        if(iv == null || iv.getTopInventory() == null){
            return;
        }

        Inventory inventory = iv.getTopInventory();

        GUISession guiSession = GUISession.extractSession(inventory);
        if(guiSession == null){
            //They didn't interact with a GUI
            return; //Ignore
        }
        //They interacted with a GUI

        guiSession.getInventoryGUI().handleBukkitEvent(event, guiSession);
    }
}
