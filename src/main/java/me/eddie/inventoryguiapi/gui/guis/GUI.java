package me.eddie.inventoryguiapi.gui.guis;

import me.eddie.inventoryguiapi.gui.contents.GUIContentsProvider;
import me.eddie.inventoryguiapi.gui.contents.GUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.events.*;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
import me.eddie.inventoryguiapi.gui.session.InventoryState;
import me.eddie.inventoryguiapi.gui.view.GUIPresenter;
import me.eddie.inventoryguiapi.plugin.EventCaller;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import me.eddie.inventoryguiapi.util.GUISettingValidation;
import me.eddie.inventoryguiapi.util.StackCompatibilityUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a GUI
 * The state of the GUI (GUIState) is local to each session, and when a session ends (GUI closes) is lost
 */
public class GUI implements InventoryGUI {
    public static final int AUTO_SET_SIZE = -1; //An inventory created with this size will automatically have it's size set to the default for it's given type

    protected GUIContentsProvider contentsProvider;
    protected GUIPopulator guiPopulator;
    protected List<GUIActionListener> actionListeners = new ArrayList<GUIActionListener>();
    protected GUIPresenter guiPresenter;
    protected InventoryType inventoryType;
    protected int maxSize;
    protected boolean isDynamicSize;

    /**
     * Create a new GUI
     * @param inventoryType The type of inventory to use in the created GUI
     * @param size The size (maximum if dynamic) to make the created GUI. Non-default sizes only supported with CHEST inventory type (Minecraft limitation)
     * @param isDynamicSize True if inventory should resize to wrap it's contents
     * @param contentsProvider The contents provider that dictates what this GUI should be showing to viewers of it
     * @param guiPopulator The GUIPopulator to use to position the GUIElements within the inventory to display
     * @param guiPresenter The GUIPresenter to use to present the computed inventory to the player
     * @param guiActionListeners Any ActionListeners that you want to specify. These receive GUIEvents before GUIElements do so that you can further customise the GUI's behaviour. They also receive GUIOpenEvent, GUICloseEvent and GUIUpdateEvent which GUIElements do not
     */
    public GUI(InventoryType inventoryType, int size, boolean isDynamicSize, GUIContentsProvider contentsProvider,
               GUIPopulator guiPopulator, GUIPresenter guiPresenter, GUIActionListener... guiActionListeners){
        if(inventoryType == null || !GUISettingValidation.isAllowed(inventoryType)){
            throw new IllegalArgumentException("Inventory type is invalid!");
        }
        if(size == AUTO_SET_SIZE && inventoryType != null){
            size = inventoryType.getDefaultSize();
        }
        if(!GUISettingValidation.isValid(size, isDynamicSize, inventoryType)){
            throw new IllegalArgumentException("Described inventory to display is invalid! Try changing the size or making it a fixed size (Not dynamic)! Specify a size of '"+AUTO_SET_SIZE+"' to automatically use the default size for the specified inventory type!");
        }
        if(contentsProvider == null){
            throw new IllegalArgumentException("Contents Provider must not be null");
        }
        if(guiPopulator == null){
            throw new IllegalArgumentException("GUI Populator must not be null");
        }
        if(guiPresenter == null){
            throw new IllegalArgumentException("GUI Presenter must not be null");
        }
        this.inventoryType = inventoryType;
        this.maxSize = size;
        this.isDynamicSize = isDynamicSize;
        this.contentsProvider = contentsProvider;
        this.guiPopulator = guiPopulator;
        this.guiPresenter = guiPresenter;
        this.actionListeners.clear();
        for(GUIActionListener actionListener:guiActionListeners){
            if(actionListener != null){
                this.actionListeners.add(actionListener);
            }
        }
    }

    /**
     * Create a new GUI
     * @param inventoryType The type of inventory to use in the created GUI
     * @param size The size (maximum if dynamic) to make the created GUI. Non-default sizes only supported with CHEST inventory type (Minecraft limitation)
     * @param isDynamicSize True if inventory should resize to wrap it's contents
     * @param contentsProvider The contents provider that dictates what this GUI should be showing to viewers of it
     * @param guiActionListeners Any ActionListeners that you want to specify. These receive GUIEvents before GUIElements do so that you can further customise the GUI's behaviour
     */
    public GUI(InventoryType inventoryType, int size, boolean isDynamicSize, GUIContentsProvider contentsProvider, GUIActionListener... guiActionListeners){
        this(inventoryType, size, isDynamicSize, contentsProvider, new GUIPopulator(), new GUIPresenter(), guiActionListeners);
    }

    @Override
    public void open(final Player player, int page) {
        if(page < 1){
            throw new IllegalArgumentException("Page must be >= 1");
        }
        GUISession session = GUISession.extractSession(player); //Get any existing GUISession
        if(session != null && session.getInventoryGUI().equals(this)){ //Player already is viewing this GUI
            session.setPage(page); //Navigate to the specified page instead
            updateContentsAndView(player);
            return;
        }
        session = createNewSession(player, page);

        final GUISession guiSess = session;
        guiPopulator.populateGUI(session, player, new Callback<Void>() {
            @Override
            public void call(Void param) {
                guiPresenter.updateView(player, guiSess); //Show the player the GUI
                GUIOpenEvent evt = new GUIOpenEvent(guiSess, player);
                EventCaller.fireThroughBukkit(evt);
                fireEventThroughActionListeners(evt);
            }
        });
    }

    protected void fireEventThroughActionListeners(GUIEvent event){
        for(GUIActionListener actionListener:actionListeners){
            actionListener.onEvent(event);
        }
    }

    @Override
    public void open(Player player) {
        open(player, 1);
    }

    protected GUISession createNewSession(Player player, int page){
        return new GUISession(this,page,new GUIState());
    }

    protected void updateContentsAndView(final Player player, final GUISession session){
        if(session == null || !session.getInventoryGUI().equals(this)){
            return; //Session not for this GUI or not present
        }

        guiPopulator.populateGUI(session, player, new Callback<Void>() {
            @Override
            public void call(Void param) {
                updateView(player, session);
            }
        });
    }

    protected void updateView(final Player player, final GUISession session){
        if(session == null || !session.getInventoryGUI().equals(this)){
            return; //Session not for this GUI or not present
        }

        guiPresenter.updateView(player, session); //Update player's view of the GUI
        GUIUpdateEvent evt = new GUIUpdateEvent(session, player);
        Bukkit.getPluginManager().callEvent(evt);
        fireEventThroughActionListeners(evt);
    }

    @Override
    public void updateView(Player player) {
        if(player == null){
            throw new IllegalArgumentException("Player must not be null");
        }
        GUISession session = GUISession.extractSession(player);
        updateView(player, session);
    }

    @Override
    public void updateContentsAndView(Player player) {
        if(player == null){
            throw new IllegalArgumentException("Player must not be null");
        }
        GUISession session = GUISession.extractSession(player);
        updateContentsAndView(player, session);
    }

    @Override
    public void handleBukkitEvent(Event event, final GUISession session) {
        if(event == null || session == null || !session.getInventoryGUI().equals(this)){
            throw new IllegalArgumentException();
        }

        if(event instanceof InventoryCloseEvent){
            HumanEntity viewer = ((InventoryCloseEvent) event).getPlayer(); //getPlayer() doesn't return Player here because this method is ancient
            if(viewer instanceof Player) { //Check it is a player, which is ALWAYS the case unless some other plugin is doing something funky with fake entities
                GUICloseEvent evt = new GUICloseEvent(session, (Player) viewer);
                EventCaller.fireThroughBukkit(evt);
                fireEventThroughActionListeners(evt);
            }
            return;
        }

        if(!(event instanceof InventoryEvent)){
            return;
        }
        InventoryEvent inventoryEvent = (InventoryEvent) event;
        Inventory topInv = inventoryEvent.getView().getTopInventory();

        if(event instanceof InventoryDragEvent){ //Do not allow dragging of stuff in the GUI, but if it's within one slot treat it like a click
            int minSlotNum = topInv.getSize(); //The largest raw slot number from our GUI inventory + 1
            for(int rawSlotNum:((InventoryDragEvent) event).getRawSlots()){ //Raw slot nums start at top inventory and get larger for bottom inventory
                if(rawSlotNum < minSlotNum){ //If this raw slot is in the top inventory
                    final InventoryDragEvent dragEvent = (InventoryDragEvent) event;
                    dragEvent.setCancelled(true); //Do not let viewers drag around our inventory

                    if(dragEvent.getRawSlots().size() == 1){ //Only 1 slot, so treat this instead as a click event
                        //Handle a click event in this slot instead
                        //Do the click event in the next tick,
                        //because cancelling the drag event resets the cursor afterwards - which is lame
                        final int rawSlot = rawSlotNum;
                        Bukkit.getScheduler().runTaskLater(InventoryGUIAPI.getInstance(), new Runnable(){
                            @Override
                            public void run() {
                                InventoryView view = dragEvent.getView();
                                if(view.getCursor() == null || view.getCursor().getType().equals(Material.AIR)){
                                    //Cursor isn't anything that can be placed, so don't bother
                                    return;
                                }

                                ClickType ct = dragEvent.getType().equals(DragType.EVEN) ? ClickType.LEFT : ClickType.RIGHT; //Get the equivalent click type for the action performed
                                InventoryClickEvent clickEvent = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER,
                                        rawSlot, ct, ct.equals(ClickType.LEFT)?InventoryAction.PLACE_ALL:InventoryAction.PLACE_ONE); //The click event to simulate
                                handleBukkitEvent(clickEvent, session);
                                if(!clickEvent.isCancelled()){ //Not cancelled so GUIElement expects the bukkit event to be happening
                                    ItemStack toPlace = view.getCursor().clone();
                                    if(toPlace != null && !toPlace.getType().equals(Material.AIR)){ //Need to place the item
                                        //Place the item
                                        Inventory inv = dragEvent.getInventory();
                                        ItemStack currentItem = view.getItem(rawSlot); //Get the item in the slot
                                        int currentAmt = currentItem == null || !StackCompatibilityUtil.canStack(toPlace, currentItem) ? 0 : currentItem.getAmount();
                                        int amt = currentAmt + (ct.equals(ClickType.LEFT) ? toPlace.getAmount(): 1); //Add the existing amount and the amount to add
                                        if(amt > toPlace.getMaxStackSize()){ //amt must be at largest the max stack size
                                            amt = toPlace.getMaxStackSize();
                                        }

                                        //Update the cursor
                                        final ItemStack newCursor = toPlace.clone();
                                        newCursor.setAmount(toPlace.getAmount() - (amt - currentAmt)); //Set amount to the remainder
                                        view.setCursor(newCursor.getAmount() < 1 ? null : newCursor);

                                        //Update the inventory
                                        toPlace.setAmount(amt);
                                        view.setItem(rawSlot, toPlace);
                                    }
                                }
                            }
                        }, 1L);
                    }
                    break;
                }
            }
            return;
        }

        if(!(event instanceof InventoryClickEvent)){
            return;
        }

        InventoryClickEvent clickEvent = (InventoryClickEvent) event;
        int slot = clickEvent.getSlot(); //Slot for the INVENTORY, not the InventoryView (raw)
        HumanEntity clicker = clickEvent.getWhoClicked(); //getPlayer() doesn't return Player here because this method is ancient
        if(!(clicker instanceof Player)){ //Check it is a player, which is ALWAYS the case unless some other plugin is doing something funky with fake entities
            return;
        }
        Player player = (Player) clicker;

        GUIState guiState = session.getGUIState();
        InventoryState inventoryState = guiState.getExistingInventoryState(session.getPage());
        if(inventoryState == null){
            throw new RuntimeException("No InventoryState present for GUI interacted with!");
        }

        //Override shift clicking to instead be a pickup and a place event
        if(clickEvent.isShiftClick()){ //Shift clicked on a different inventory - aka auto move into our inventory
            Inventory destInv = !clickEvent.getClickedInventory().equals(topInv) ? topInv : clickEvent.getView().getBottomInventory();
            handleAutoInsertAsIndividualActions(player, inventoryState, session, destInv, clickEvent);
            return;
        }

        GUIElement guiElement = inventoryState.getElementInSlot(slot);
        InventoryAction action = clickEvent.getAction();
        GUIEvent guiEvent = null;
        switch (action){
            case COLLECT_TO_CURSOR: { //Disallow as will grab all of item from the GUI
                clickEvent.setCancelled(true);
                return;
            }
            case NOTHING:
                break;
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE: {
                //Pickup event
                guiEvent = new GUIPickupItemEvent(session, player, slot, guiElement, clickEvent);
            }
                break;
            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR: {
                return; //Don't care about item dropping from cursor
            }
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE: {
                //Place event
                guiEvent = new GUIPlaceItemEvent(session, player, slot, guiElement, clickEvent);
            }
                break;
            case SWAP_WITH_CURSOR: {
                clickEvent.setCancelled(true);
                //Simulate Pickup event and then place event instead
                ItemStack cursor = clickEvent.getView().getCursor().clone(); //Get the item on the cursor
                ItemStack inSlot = clickEvent.getCurrentItem().clone(); //Get the item currently in the slot

                if(inSlot != null && !inSlot.getType().equals(Material.AIR)) {
                    //Simulate a pickup event for the GUI, not bukkit since bukkit events have already decided to allow swap with cursor by it reaching here
                    InventoryClickEvent pickupEvent = new InventoryClickEvent(clickEvent.getView(), clickEvent.getSlotType(), clickEvent.getRawSlot(), ClickType.LEFT, InventoryAction.PICKUP_ALL);
                    handleBukkitEvent(pickupEvent, session);
                    if(!pickupEvent.isCancelled()){ //Not cancelled so it's expected that what would normally happen should happen
                        topInv.setItem(slot, null); //Remove item from the slot as we have now 'picked it up'
                    }
                    if(clickEvent.getView().getItem(clickEvent.getRawSlot()) == inSlot ||
                            (clickEvent.getView().getItem(clickEvent.getRawSlot()) != null
                                    && clickEvent.getView().getItem(clickEvent.getRawSlot()).equals(inSlot))){
                        //If what's in the slot hasn't changed then we know it wasn't picked up
                        return; //Item wasn't picked up so we should stop here
                    }
                }
                clickEvent.getView().setCursor(cursor.clone()); //Re-set the cursor as it may have changed from picking up the item

                //Simulate placing event for the GUI, cursor remains as before so that we're placing with the correct cursor
                InventoryClickEvent placeEvent = new InventoryClickEvent(clickEvent.getView(), clickEvent.getSlotType(), clickEvent.getRawSlot(), ClickType.LEFT, InventoryAction.PLACE_ALL);
                handleBukkitEvent(placeEvent, session);
                //Now handling is done, update the cursor with what we picked up earlier.
                clickEvent.getView().setCursor(inSlot);
                if(placeEvent.isCancelled()){ //We aren't allowed to place
                    return;
                }

                //Update the slot with what was placed if event not cancelled
                topInv.setItem(slot, cursor);
                return;
            }
            default: {
                //Click Event
                guiEvent = new GUIMiscClickEvent(session, player, slot, guiElement, clickEvent);
            }
                break;
        }

        if(!clickEvent.getClickedInventory().equals(topInv)){
            return;
        }

        if(guiEvent == null){
            return;
        }
        //Call the event where needed
        EventCaller.fireThroughBukkit(guiEvent);
        if(guiEvent instanceof Cancellable && ((Cancellable) guiEvent).isCancelled()){ //If event is cancelled don't do any more
            return;
        }
        fireEventThroughActionListeners(guiEvent);
        if(guiEvent instanceof Cancellable && ((Cancellable) guiEvent).isCancelled()){ //If event is cancelled don't do any more
            return;
        }

        if(guiElement != null){
            guiElement.onEvent(guiEvent);
        }
        else {
            clickEvent.setCancelled(true); //Cancel the bukkit event if they clicked in an empty position in the GUI and that click wasn't cancelled.
        }
    }

    //Splits an auto-insert (Shift click) event into pickup and place events that can be handled on a per-slot basis
    //Looks pretty hacky but is required to get around limitations of the Bukkit API - How ambiguous InventoryClickEvent is with placing and picking up items. (Doesn't specify where they're going or how many)
    protected void handleAutoInsertAsIndividualActions(Player viewer, InventoryState inventoryState, GUISession session, Inventory destInv, InventoryClickEvent shiftClickEvent){
        shiftClickEvent.setCancelled(true); //Cancel the event
        boolean destIsTopInv = shiftClickEvent.getView().getTopInventory().equals(destInv);
        //find an empty slot and try and place it there
        ItemStack toMove = shiftClickEvent.getCurrentItem().clone();
        int totalToMove = toMove.getAmount(); //The total amount trying to be moved
        if(toMove == null || toMove.getType().equals(Material.AIR) || toMove.getAmount() < 1){
            return; //No item to move
        }
        int destSlotNum = -1;
        int moveAmount = toMove.getAmount(); //The amount we can move
        for(int i=0;i<destInv.getSize();i++){ //First try and find a slot that we can stack with
            if(!destIsTopInv && i > 35){ //Moving to bottom (Always Player) inventory, slots outside of 35 are not allowed to be auto-inserted into
                break;
            }
            ItemStack it = destInv.getItem(i);
            if(it != null && !it.getType().equals(Material.AIR)){ //Slot isn't empty, let's try and place it here
                GUIElement inSlot = !destIsTopInv ? null : inventoryState.getElementInSlot(i); //The GUIElement in this slot if dest is the GUI
                if(inSlot == null || inSlot.canAutoInsertIntoSlot(viewer, session)) { //Slot is able to be auto inserted into
                    if(it.getAmount() < it.getMaxStackSize() && StackCompatibilityUtil.canStack(it, toMove)){ //They can be stacked together and it's not a full stack
                        destSlotNum = i;
                        moveAmount = Math.min(moveAmount, it.getMaxStackSize() - it.getAmount()); //Reduce the number to move if moving the current amount would overflow the stack
                        break;
                    }
                }
            }
        }
        if(destSlotNum == -1){ //If still not found a slot
            for(int i=0;i<destInv.getSize();i++){ //Second try and find an allowed empty slot to try and place into
                if(!destIsTopInv && i > 35){ //Moving to bottom (Always Player) inventory, slots outside of 35 are not allowed to be auto-inserted into
                    break;
                }
                ItemStack it = destInv.getItem(i);
                if(it == null || it.getType().equals(Material.AIR)){ //Slot is empty, let's try and place it here
                    GUIElement inSlot = !destIsTopInv ? null : inventoryState.getElementInSlot(i); //The GUIElement in this slot if dest is the GUI
                    if(inSlot == null || inSlot.canAutoInsertIntoSlot(viewer, session)) {
                        destSlotNum = i;
                        break;
                    }
                }
            }
        }
        toMove.setAmount(moveAmount);

        if(destSlotNum >= 0) { //If we have found a destination slot
            //Simulate a place event for here
            ItemStack cursor = shiftClickEvent.getView().getCursor();

            if(destIsTopInv) { //If destination is GUI, then simulate placing it
                shiftClickEvent.getView().setCursor(toMove);
                InventoryClickEvent placeEvent = new InventoryClickEvent(shiftClickEvent.getView(), InventoryType.SlotType.CONTAINER, destSlotNum, ClickType.LEFT, InventoryAction.PLACE_ALL);
                handleBukkitEvent(placeEvent, session);
                boolean placed = !placeEvent.isCancelled() || shiftClickEvent.getView().getCursor() == null
                        || shiftClickEvent.getView().getCursor().getType().equals(Material.AIR); //If cursor is now cleared or event not cancelled, then item was/should be moved
                shiftClickEvent.getView().setCursor(cursor); //Reset cursor to how it was before we did the shift click
                if(!placeEvent.isCancelled()){
                    //Move item as it not being cancelled means it's expected to happen
                    destInv.setItem(destSlotNum, toMove); //Make the slot contain the item to move
                }
                if(placed){ //Has been placed into the destination slot, so now clear the source slot of the items we moved
                    int newAmount = totalToMove - moveAmount; //Figure out how many are left
                    ItemStack remainder = toMove.clone(); //Get the item stack that was moved
                    remainder.setAmount(newAmount); //Set the amount to be how many are left
                    shiftClickEvent.getView().setItem(shiftClickEvent.getRawSlot(), newAmount < 1 ? null : remainder); //Update in inventory
                }
            }
            else { //Source is GUI, so simulate picking it up
                shiftClickEvent.getView().setCursor(null); //Set the current cursor to nothing, so we can pickup everything available
                //Pickup all the items in the slot
                InventoryClickEvent pickupEvent = new InventoryClickEvent(shiftClickEvent.getView(), shiftClickEvent.getSlotType(), shiftClickEvent.getRawSlot(), ClickType.LEFT, InventoryAction.PICKUP_ALL);
                handleBukkitEvent(pickupEvent, session);
                boolean pickedUp = !pickupEvent.isCancelled() || (shiftClickEvent.getView().getCursor() != null
                        && !shiftClickEvent.getView().getCursor().getType().equals(Material.AIR)); //If all the items were picked up
                if(!pickupEvent.isCancelled()){ //If not cancelled we actually have to do the action
                    shiftClickEvent.getView().setItem(shiftClickEvent.getRawSlot(), null);
                }
                if(pickedUp){
                    //We picked up too much, so put back any needed
                    int amtPickedUp = shiftClickEvent.getView().getCursor() == null ? 0 : shiftClickEvent.getView().getCursor().getAmount();
                    int toPutBack = amtPickedUp - moveAmount;
                    if(toPutBack > 0){ //Put back the extra
                        ItemStack toReturn = toMove.clone();
                        toReturn.setAmount(toPutBack);
                        shiftClickEvent.getView().setCursor(toReturn);
                        InventoryClickEvent placeEvent = new InventoryClickEvent(shiftClickEvent.getView(), shiftClickEvent.getSlotType(), shiftClickEvent.getRawSlot(), ClickType.LEFT, InventoryAction.PLACE_ALL);
                        handleBukkitEvent(placeEvent, session);
                        if(!placeEvent.isCancelled()){
                            //Change item as it not being cancelled means it's expected to happen
                            shiftClickEvent.getView().setItem(shiftClickEvent.getRawSlot(), toReturn);
                        }
                    }

                    //Put the 'picked up' items into the destination inventory
                    ItemStack currentItem = destInv.getItem(destSlotNum);
                    int currentAmt = currentItem == null || !StackCompatibilityUtil.canStack(toMove, currentItem) ? 0 : currentItem.getAmount();
                    int amt = currentAmt + moveAmount; //Add the existing amount and the amount to add
                    toMove.setAmount(amt);
                    destInv.setItem(destSlotNum, toMove);
                }
                shiftClickEvent.getView().setCursor(cursor); //Reset cursor to how it was before we did the shift click
            }

            if(totalToMove > moveAmount){ //If not all of the stack was moved into the GUI, call recursively to try and move the remainder
                InventoryClickEvent newShiftClickEvent = new InventoryClickEvent(shiftClickEvent.getView(), shiftClickEvent.getSlotType(),
                        shiftClickEvent.getRawSlot(), shiftClickEvent.getClick(), InventoryAction.MOVE_TO_OTHER_INVENTORY);
                //Call self recursively to move the remainder somewhere
                handleAutoInsertAsIndividualActions(viewer, inventoryState, session, destInv, newShiftClickEvent);
            }
        }
        return;
    }

    /**
     * Get the ContentsProvider associated with calculating what GUIElements to display in this GUI
     * @return The ContentsProvider
     */
    public GUIContentsProvider getContentsProvider() {
        return contentsProvider;
    }

    /**
     * Get the GUIPopulator associated with calculating the layout of GUIElements to display in this GUI
     * @return The GUIPopulator
     */
    public GUIPopulator getGuiPopulator() {
        return guiPopulator;
    }

    /**
     * Get the GUIPresenter associated with displaying this GUI
     * @return The GUIPresenter
     */
    public GUIPresenter getGuiPresenter() {
        return guiPresenter;
    }

    @Override
    public InventoryType getInventoryType() {
        return this.inventoryType;
    }

    @Override
    public int getMaximumGUISize() {
        return this.maxSize;
    }

    @Override
    public boolean isGUISizeDynamic() {
        return this.isDynamicSize;
    }
}
