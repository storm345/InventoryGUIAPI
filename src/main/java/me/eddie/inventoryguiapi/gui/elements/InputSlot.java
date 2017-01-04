package me.eddie.inventoryguiapi.gui.elements;

import me.eddie.inventoryguiapi.gui.events.GUIEvent;
import me.eddie.inventoryguiapi.gui.events.GUIMiscClickEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPickupItemEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPlaceItemEvent;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
import me.eddie.inventoryguiapi.util.StackCompatibilityUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a GUIElement that defines a slot where players can input item.
 */
public class InputSlot extends AbstractGUIElement {
    private String inputSlotID;
    private InputSlot.ActionHandler actionHandler;

    /**
     * Construct a new InputSlot
     * @param uniqueInputSlotID The unique (Within this GUI) input slot ID for this input slot. Used to track what item is currently contained within this input slot.
     * @param slot The desired slot for this GUIElement
     * @param actionHandler The action handler that defines how this input slot should behave when interacted with
     */
    public InputSlot(String uniqueInputSlotID, int slot, InputSlot.ActionHandler actionHandler){
        super(slot);
        if(uniqueInputSlotID == null){
            throw new IllegalArgumentException("Unique Input Slot Id must not be null");
        }
        this.inputSlotID = uniqueInputSlotID;
        if(actionHandler == null){
            throw new IllegalArgumentException("ActionHandler must not be null");
        }
        this.actionHandler = actionHandler;
    }

    /**
     * Construct a new InputSlot
     * @param uniqueInputSlotID The unique (Within this GUI) input slot ID for this input slot. Used to track what item is currently contained within this input slot.
     * @param actionHandler The action handler that defines how this input slot should behave when interacted with
     */
    public InputSlot(String uniqueInputSlotID, InputSlot.ActionHandler actionHandler){
        this(uniqueInputSlotID, AbstractGUIElement.NO_DESIRED_SLOT, actionHandler);
    }

    /**
     * Get the unique (Within this GUI) input slot ID for this input slot. Used to track what item is currently contained within this input slot.
     * @return The input slot ID.
     */
    public String getInputSlotID() {
        return inputSlotID;
    }

    /**
     * Set the unique (Within this GUI) input slot ID for this input slot. Used to track what item is currently contained within this input slot.
     * @param inputSlotID The input slot ID
     */
    public void setInputSlotID(String inputSlotID) {
        this.inputSlotID = inputSlotID;
    }

    /**
     * Get the ActionHandler that defines how this InputSlot behaves
     * @return The ActionHandler
     */
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    /**
     * Set the ActionHandler that defines how this InputSlot behaves
     * @param actionHandler The ActionHandler
     */
    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    /**
     * Represents the action that should happen when this input slot is interacted with
     */
    public static interface ActionHandler {
        /**
         * Whether or not viewers can use Minecraft's auto-insert (Shift click) feature with this slot.
         * If they can then shift-clicking will be able to target this slot. Shift click events won't be passed directly
         * to the GUIElement though, they are instead turned into pickup and place events and those are passed to GUIElements
         * as appropriate.
         * @param viewer The viewer trying to auto insert into the slot
         * @param session The GUISession trying to auto insert into the slot
         * @return True if viewer may auto insert into slot, False if not
         */
        public boolean shouldAllowAutoInsert(Player viewer, GUISession session);
        /**
         * When called should perform the action that should happen when this input slot is clicked
         * @param event The ClickEvent that caused this to be called
         */
        public void onClick(GUIMiscClickEvent event);
        /**
         * When called should perform the action that should happen when this input slot's item is picked up
         * @param event The PickupItemEvent that caused this to be called
         */
        public void onPickupItem(GUIPickupItemEvent event);
        /**
         * When called should perform the action that should happen when this input slot has an item placed into it
         * @param event The PlaceItemEvent that caused this to be called
         */
        public void onPlaceItem(GUIPlaceItemEvent event);

        /**
         * Method called when the item currently in this InputSlot changes.
         * @param guiSession The GUI session that this has happened for
         * @param newItem The item now currently inside this InputSlot
         */
        public void onCurrentItemChanged(GUISession guiSession, ItemStack newItem);
    }

    private String getCurrentItemAttributeKey(){ //Return a key that will be used to track the attribute for what item this currently contains
        return "gui.inputSlot.currentItem."+getInputSlotID(); //An arbitrary string that won't conflict with other GUIElements or other slot IDs
    }

    /**
     * Get the item currently being displayed in this input slot
     * @param guiState The GUIState that this InputSlot belongs to
     * @return The ItemStack currently being displayed
     */
    public ItemStack getCurrentItem(GUIState guiState){
        Object item = guiState.getAttribute(getCurrentItemAttributeKey());
        if(item != null && item instanceof ItemStack && !((ItemStack) item).getType().equals(Material.AIR)){ //Should never not be an ItemStack, but it doesn't hurt to check
            return (ItemStack) item;
        }
        return null;
    }

    /**
     * Set the item currently being displayed in this input slot.
     * Note this does NOT update the inventory being displayed, to update the inventory being displayed it will need it's contents refreshing (However if called within a GUIContentsProvider refresh is called for you)
     * @param guiSession The GUISession that is being updated
     * @param item The ItemStack currently being displayed
     */
    public void setCurrentItem(GUISession guiSession, ItemStack item){
        GUIState guiState = guiSession.getGUIState();
        if(item == null){
            guiState.removeAttribute(getCurrentItemAttributeKey());
            getActionHandler().onCurrentItemChanged(guiSession, item); //Notify action handler that the item has now changed
            return;
        }
        guiState.putAttribute(getCurrentItemAttributeKey(), item);
        getActionHandler().onCurrentItemChanged(guiSession, item); //Notify action handler that the item has now changed
    }

    @Override
    public void onEvent(GUIEvent event) {
        if(event instanceof GUIMiscClickEvent){
            ((GUIMiscClickEvent) event).getBukkitEvent().setCancelled(true); //Prevent action from doing anything
            //The action handler can do extra handling, or uncancel the bukkit event if wanted
            getActionHandler().onClick((GUIMiscClickEvent) event);
        }
        if(event instanceof GUIPickupItemEvent){
            InventoryClickEvent bukkitEvent = ((GUIPickupItemEvent) event).getBukkitEvent();
            bukkitEvent.setCancelled(true); //Prevent default handling, since Bukkit API makes it ambiguous how many items to be picked up
            //Let the action handler do extra handling, it can even un-cancel the bukkit event
            getActionHandler().onPickupItem((GUIPickupItemEvent) event);
            if(!((GUIPickupItemEvent) event).isCancelled()){
                //They want the item to be picked up
                InventoryAction action = bukkitEvent.getAction();

                //Handle picking up the item ourselves, since Bukkit API makes it ambiguous how many items to be picked up
                int pickupAmt = 0;

                ItemStack prevItem = bukkitEvent.getCurrentItem().clone();
                if(prevItem == null){
                    prevItem = new ItemStack(Material.AIR);
                }

                switch(action){
                    case PICKUP_ALL: {
                        pickupAmt = prevItem.getAmount();
                    }
                    break;
                    case PICKUP_SOME: {
                        pickupAmt = (int) Math.ceil(prevItem.getAmount()/2.0);
                    }
                    break;
                    case PICKUP_HALF: {
                        pickupAmt = (int) Math.ceil(prevItem.getAmount()/2.0);
                    }
                    break;
                    case PICKUP_ONE: {
                        pickupAmt = Math.min(prevItem.getAmount(), 1);
                    }
                    break;
                }

                ItemStack cursor = bukkitEvent.getView().getCursor().clone();
                int cursorAmt = cursor == null || !StackCompatibilityUtil.canStack(cursor, prevItem)
                         ? 0 : cursor.getAmount(); //Get how many of the same item are currently on the cursor
                ItemStack newCursor = prevItem.clone();

                //Update the item in the slot
                int newSlotAmt = prevItem.getAmount() - pickupAmt;
                prevItem.setAmount(newSlotAmt < 1 ? 0 : newSlotAmt); //Update the amount on the item stack, but don't let it be negative
                bukkitEvent.getView().setItem(bukkitEvent.getRawSlot(), newSlotAmt < 1 ? null : prevItem);
                setCurrentItem(((GUIPickupItemEvent) event).getSession(), newSlotAmt < 1 ? null : prevItem);

                //Update the item on the cursor
                int newCursorAmt = cursorAmt + pickupAmt;
                newCursor.setAmount(newCursorAmt);
                bukkitEvent.getView().setCursor(newCursor);

                //Updates view of the GUI for all of it's viewers
                ((GUIPickupItemEvent) event).getSession().getInventoryGUI().updateView(((GUIPickupItemEvent) event).getViewer());
            }
        }
        if(event instanceof GUIPlaceItemEvent){
            InventoryClickEvent bukkitEvent = ((GUIPlaceItemEvent) event).getBukkitEvent();
            bukkitEvent.setCancelled(true); //Prevent default handling, since Bukkit API makes it ambiguous how many items to be picked up
            //Let the action handler do extra handling, it can even un-cancel the bukkit event
            getActionHandler().onPlaceItem((GUIPlaceItemEvent) event);
            if(!((GUIPlaceItemEvent) event).isCancelled()){
                //They want the item to be placed
                InventoryAction action = bukkitEvent.getAction();

                int placeAmt = 0;
                switch(action){
                    case PLACE_ALL: {
                        placeAmt = bukkitEvent.getCursor() == null ? 0 : bukkitEvent.getCursor().getAmount();
                    }
                        break;
                    case PLACE_SOME: {
                        placeAmt = bukkitEvent.getCursor() == null ? 0 : (int)Math.ceil(bukkitEvent.getCursor().getAmount()/2.0);
                    }
                        break;
                    case PLACE_ONE: {
                        placeAmt = bukkitEvent.getCursor() == null ? 0 : 1;
                    }
                        break;
                }

                //Handle placing the item and updating ourselves
                ItemStack cursor = bukkitEvent.getCursor(); //Item on cursor
                ItemStack toPlace = cursor.clone(); //Copy of item on cursor to place
                ItemStack current = bukkitEvent.getCurrentItem(); //Item in slot currently
                int currentAmt = current == null || !StackCompatibilityUtil.canStack(current, cursor)
                        ? 0 : current.getAmount(); //0 if they're different items or the amount if they're the same
                if(cursor == null){
                    return;
                }
                placeAmt = Math.min(cursor.getAmount(), placeAmt);
                if(placeAmt < 1){
                    return;
                }
                if(currentAmt + placeAmt > toPlace.getMaxStackSize()){ //Don't place more than can be stacked
                    placeAmt = toPlace.getMaxStackSize() - currentAmt;
                }
                if(placeAmt < 1){
                    return;
                }

                //Update the cursor
                int newCursorAmt = cursor.getAmount() - placeAmt;
                if(newCursorAmt < 1){
                    bukkitEvent.getView().setCursor(null);
                }
                else {
                    cursor.setAmount(newCursorAmt);
                    bukkitEvent.getView().setCursor(cursor);
                }

                //Update ourselves
                toPlace.setAmount(currentAmt+placeAmt);
                bukkitEvent.getView().setItem(bukkitEvent.getRawSlot(), toPlace.getAmount() < 1 ? null : toPlace);
                setCurrentItem(((GUIPlaceItemEvent) event).getSession(), toPlace.getAmount() < 1 ? null : toPlace);

                //Updates view of the GUI for all of it's viewers
                ((GUIPlaceItemEvent) event).getSession().getInventoryGUI().updateView(((GUIPlaceItemEvent) event).getViewer());
            }
        }
    }

    @Override
    public ItemStack getDisplay(Player viewer, GUISession session) {
        return getCurrentItem(session.getGUIState());
    }

    @Override
    public boolean canAutoInsertIntoSlot(Player viewer, GUISession session) {
        return getActionHandler().shouldAllowAutoInsert(viewer, session);
    }
}
