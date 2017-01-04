package me.eddie.inventoryguiapi.gui.elements;

import me.eddie.inventoryguiapi.gui.events.GUIClickEvent;
import me.eddie.inventoryguiapi.gui.events.GUIEvent;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a GUIElement that when clicked performs an action
 */
public class ActionItem extends AbstractGUIElement {

    private ItemStack display;
    private ActionHandler actionHandler;

    /**
     * Construct a new ActionItem with a desired slot
     * @param slot The slot that this ActionItem should be placed into
     * @param display The display ItemStack
     * @param actionHandler The action for when this is clicked
     */
    public ActionItem(int slot, ItemStack display, ActionHandler actionHandler){
        super(slot);
        if(display == null){
            throw new IllegalArgumentException("Display item must not be null");
        }
        this.display = display;
        this.actionHandler = actionHandler;
        if(actionHandler == null){
            throw new IllegalArgumentException("ActionHandler must not be null");
        }
    }

    /**
     * Construct a new ActionItem with no desired slot
     * @param display The display ItemStack
     * @param actionHandler The action for when this is clicked
     */
    public ActionItem(ItemStack display, ActionHandler actionHandler){
        this(AbstractGUIElement.NO_DESIRED_SLOT, display, actionHandler);
    }

    /**
     * Represents the action that should happen when this action item is clicked
     */
    public static interface ActionHandler {
        /**
         * When called should perform the action that should happen when this action item is clicked.
         * If the GUI should close, call player.closeInventory() in the next tick (using bukkit scheduler).
         * Do not close the inventory in the same tick as this because it will cause issues. (Due to bukkit's handling
         * of inventory events)
         * @param event The ClickEvent that caused this to be called
         */
        public void onClick(GUIClickEvent event);
    }

    /**
     * Get the ActionHandler that defines how this ActionItem behaves
     * @return The ActionHandler
     */
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    /**
     * Set the ActionHandler that should define how this ActionItem behaves
     * @param actionHandler The ActionHandler
     */
    public void setActionHandler(ActionHandler actionHandler) {
        if(actionHandler == null){
            throw new IllegalArgumentException("ActionHandler must not be null");
        }
        this.actionHandler = actionHandler;
    }

    public ItemStack getDisplayItem() {
        return display;
    }

    public void setDisplayItem(ItemStack display) {
        this.display = display;
    }

    @Override
    public void onEvent(GUIEvent event) {
        if(event instanceof GUIClickEvent){
            GUIClickEvent e = (GUIClickEvent) event;
            e.getBukkitEvent().setCancelled(true);
            getActionHandler().onClick(e);
        }
    }

    @Override
    public ItemStack getDisplay(Player viewer, GUISession session) {
        return getDisplayItem();
    }

    @Override
    public boolean canAutoInsertIntoSlot(Player Viewer, GUISession session) {
        return false;
    }
}
