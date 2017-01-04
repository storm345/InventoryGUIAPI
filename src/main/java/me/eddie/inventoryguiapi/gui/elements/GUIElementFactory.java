package me.eddie.inventoryguiapi.gui.elements;

import me.eddie.inventoryguiapi.gui.events.GUIClickEvent;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Some helpful methods for creating common GUIElements
 */
public class GUIElementFactory {
    /**
     * Will take a given ItemStack and change it's lore and display name. The edited ItemStack is then returned.
     * This is a convenience method that saves you from the work of manipulating ItemMeta.
     * @param base The base ItemStack to modify
     * @param displayName The display name to set, can be colored (Using Bukkit's ChatColor)
     * @param lore The lore to set, canbe colored (Using Bukkit'S ChatColor)
     * @return The formatted item
     */
    public static ItemStack formatItem(ItemStack base, String displayName, String... lore){
        if(base == null || displayName == null || lore == null){
            throw new IllegalArgumentException("No arguments to formatItem can be null!");
        }
        ItemMeta im = base.getItemMeta(); //This method returns a clone of the ItemMeta
        im.setDisplayName(displayName);
        im.setLore(Arrays.asList(lore));
        base.setItemMeta(im); //Update the ItemMeta on the item as our 'im' was just a clone of it
        return base;
    }

    /**
     * Create an InputSlot - A GUIElement that accepts user input
     * This InputSlot will have no preference for which slot it should be displayed in. If you wish to specify a slot, use {@link #createInputSlot(String, int, InputSlot.ActionHandler)}
     * Use {@link InputSlot#setCurrentItem(me.eddie.inventoryguiapi.gui.session.GUISession, ItemStack)} to change the item currently input post-creation. Note that this change will not be visible to viewers until their view has been updated. (If done within a ContentsProvider then the view is updated for you)
     * @param uniqueInputSlotID The unique ID that references this input slot. This is used for tracking what item is input into slot throughout the GUISession.
     * @param actionHandler ActionHandler that lets you manipulate the behaviour of this input slot. Eg. prevent certain items being input into the slot (By cancelling GUIEvent and bukkit event within the handler)
     * @return The created InputSlot
     */
    public static InputSlot createInputSlot(String uniqueInputSlotID, InputSlot.ActionHandler actionHandler){
        return createInputSlot(uniqueInputSlotID, AbstractGUIElement.NO_DESIRED_SLOT, actionHandler);
    }

    /**
     * Create an InputSlot - A GUIElement that accepts user input
     * Use {@link InputSlot#setCurrentItem(me.eddie.inventoryguiapi.gui.session.GUISession, ItemStack)} to change the item currently input post-creation. Note that this change will not be visible to viewers until their view has been updated. (If done within a ContentsProvider then the view is updated for you)
     * @param uniqueInputSlotID The unique ID that references this input slot. This is used for tracking what item is input into slot throughout the GUISession.
     * @param desiredSlot The slot you wish for this GUIElement to be placed into in the GUI. Use {@link #createInputSlot(String, InputSlot.ActionHandler)} if you do not care about the placement position of this item.
     * @param actionHandler ActionHandler that lets you manipulate the behaviour of this input slot. Eg. prevent certain items being input into the slot (By cancelling GUIEvent and bukkit event within the handler)
     * @return The created InputSlot
     */
    public static InputSlot createInputSlot(String uniqueInputSlotID, int desiredSlot, InputSlot.ActionHandler actionHandler){
        //Args validated within constructor
        return new InputSlot(uniqueInputSlotID, desiredSlot, actionHandler);
    }

    /**
     * Create an ActionItem - A GUIElement that when clicked performs an action.
     * This ActionItem will have no preference for which slot it should be displayed in. If you wish to specify a slot, use {@link #createActionItem(int, ItemStack, ActionItem.ActionHandler)}
     * @param displayItem The item to display in the slot that this GUIElement occupies.
     * @param actionHandler ActionHandler that defines what this ActionItem does when clicked. To re-calculate the contents to be displayed, call to {@link me.eddie.inventoryguiapi.gui.guis.InventoryGUI#updateContentsAndView(Player)}
     * @return The created ActionItem
     */
    public static ActionItem createActionItem(ItemStack displayItem, ActionItem.ActionHandler actionHandler){
        return createActionItem(AbstractGUIElement.NO_DESIRED_SLOT, displayItem, actionHandler);
    }

    /**
     * Create an ActionItem - A GUIElement that when clicked performs an action.
     * This ActionItem will have no preference for which slot it should be displayed in. If you wish to specify a slot, use {@link #createActionItem(int, ItemStack, ActionItem.ActionHandler)}
     * @param displayItem The item to display in the slot that this GUIElement occupies.
     * @param onClick Runnable that is ran when item clicked. To re-calculate the contents to be displayed, call to {@link me.eddie.inventoryguiapi.gui.guis.InventoryGUI#updateContentsAndView(Player)}
     * @return The created ActionItem
     */
    public static ActionItem createActionItem(ItemStack displayItem, Callback<Player> onClick){
        return createActionItem(AbstractGUIElement.NO_DESIRED_SLOT, displayItem, onClick);
    }

    /**
     * Create an ActionItem - A GUIElement that when clicked performs an action
     * @param desiredSlot The slot you wish for this GUIElement to be placed into in the GUI. Use {@link #createActionItem(ItemStack, ActionItem.ActionHandler)} if you do not care about the placement position of this item.
     * @param displayItem The item to display in the slot that this GUIElement occupies.
     * @param onClick Runnable that is ran when item clicked. To re-calculate the contents to be displayed, call to {@link me.eddie.inventoryguiapi.gui.guis.InventoryGUI#updateContentsAndView(Player)}
     * @return The created ActionItem
     */
    public static ActionItem createActionItem(int desiredSlot, ItemStack displayItem, final Callback<Player> onClick){
        if(onClick == null){
            throw new IllegalArgumentException("Click task must not be null!");
        }
        return createActionItem(desiredSlot, displayItem, new ActionItem.ActionHandler() {
            @Override
            public void onClick(GUIClickEvent event) { //An ActionHandler that just runs the given runnable
                event.getBukkitEvent().setCancelled(true);
                onClick.call(event.getViewer()); //Runnable doesn't care about the click event (Whether it was left/right click, etc...) - it just wants to run!
            }
        });
    }

    /**
     * Create an ActionItem - A GUIElement that when clicked performs an action
     * @param desiredSlot The slot you wish for this GUIElement to be placed into in the GUI. Use {@link #createActionItem(ItemStack, ActionItem.ActionHandler)} if you do not care about the placement position of this item.
     * @param displayItem The item to display in the slot that this GUIElement occupies.
     * @param actionHandler ActionHandler that defines what this ActionItem does when clicked. To re-calculate the contents to be displayed, call to {@link me.eddie.inventoryguiapi.gui.guis.InventoryGUI#updateContentsAndView(Player)}
     * @return The created ActionItem
     */
    public static ActionItem createActionItem(int desiredSlot, ItemStack displayItem, ActionItem.ActionHandler actionHandler){
        return new ActionItem(desiredSlot, displayItem, actionHandler);
    }
}
