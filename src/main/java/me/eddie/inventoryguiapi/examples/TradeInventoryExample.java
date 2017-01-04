package me.eddie.inventoryguiapi.examples;

import me.eddie.inventoryguiapi.gui.contents.GUIContentsProvider;
import me.eddie.inventoryguiapi.gui.elements.ActionItem;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.elements.InputSlot;
import me.eddie.inventoryguiapi.gui.events.*;
import me.eddie.inventoryguiapi.gui.guis.GUIActionListener;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.SharedInventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * An example of using the API to create a GUI for
 * two players to trade items with each other.
 */
public class TradeInventoryExample implements GUIActionListener,GUIContentsProvider{
    //Track which player is trading on the left
    private Player leftSide;
    //Track which player is trading on the right
    private Player rightSide;

    //Track whether each player has confirmed the trade, default value of false
    private boolean leftPlayerConfirmed = false;
    private boolean rightPlayerConfirmed = false;

    //When the trade is cancelled, this then becomes true
    private boolean cancelled = false;

    /*Track the input slots for the player on the left
      You don't have to do it like this,
      you could instead use the slot ID to determine later if a slot if the left or right player
     */
    private List<InputSlot> leftPlayerInputSlots = new ArrayList<InputSlot>();
    //Track the input slots for the player on the right
    private List<InputSlot> rightPlayerInputSlots = new ArrayList<InputSlot>();

    //The GUI we will be showing the players
    private SharedInventoryGUI gui;

    //Creates a new TradeInventoryExample for the given two players to trade
    public TradeInventoryExample(Player leftSide, Player rightSide){
        this.leftSide = leftSide;
        this.rightSide = rightSide;

        initInputSlots();
    }

    public void startTrade(){
        //Create the GUI to view
        gui = (SharedInventoryGUI) new GUIBuilder()
                /**
                 * GUIState is saved to the GUI, so all viewers will
                 * see the same GUIState (Eg. items that have been input, etc...)
                 */
                .guiStateBehaviour(GUIBuilder.GUIStateBehaviour.BOUND_TO_GUI)
                //Make this GUI use the CHEST layout
                .inventoryType(InventoryType.CHEST)
                //Make the GUI contain 54 slots
                .size(54)
                //Don't auto-resize this GUI
                .dynamicallyResizeToWrapContent(false)
                //Set the contents provider that decides what GUIElements to display
                .contentsProvider(this)
                //Add an action listener that can handle GUIEvents so we can handle when the GUI is opened and closed
                .actionListeners(this)
                .build();

        //Show the GUI to both players
        gui.open(leftSide);
        gui.open(rightSide);
    }

    //Calculate the InputSlots required for the GUI and create them
    protected void initInputSlots(){
        //For each slot in this trade inventory (total of 54)
        for(int i=0;i<54;i++){
            /*
             Divide by 9 and get the remainder to figure out the position in the current row
             */
            int posInRow = i%9;
            if(posInRow == 4){ //If the slot is in the middle of the row ignore it
                continue;
            }
            int row = (int) Math.floor(i/9.0d); //Get the row number by diving by 9 and rounding down, 0 is first row
            if(row == 5 //The last row of the inventory
                    && (posInRow == 3 || posInRow == 8)){ //These slots are reserved for buttons in our GUI
                continue;
            }

            /*
            Arbitrary input slot id unique to this InputSlot within this GUI.
            This is used to track what item is currently inside this input slot
             */
            String uniqueSlotID = "inputSlot"+i;
            if(posInRow < 4){ //InputSlot being created is for the left side
                //Create the input slot, but only allow the player on the left side to modify it
                InputSlot inputSlot = restrictedInputSlot(i, uniqueSlotID, leftSide);
                //Add it to the list of input slots for the left side of the GUI
                leftPlayerInputSlots.add(inputSlot);
            }
            else { //InputSlot being created is for the right side
                //Create the input slot, but only allow the player on the right side to modify it
                InputSlot inputSlot = restrictedInputSlot(i, uniqueSlotID, rightSide);
                //Add it to the list of input slots for the right side of the GUI
                rightPlayerInputSlots.add(inputSlot);
            }
        }
    }

    //Create an InputSlot that is restricted to only allow input from a given player
    private InputSlot restrictedInputSlot(int slot, String id, final Player allowed){
        //We specify the slot here, if we didn't it would be automatically placed anywhere available it fits
        return GUIElementFactory.createInputSlot(id, slot, new InputSlot.ActionHandler() {
            @Override
            public boolean shouldAllowAutoInsert(Player viewer, GUISession session) {
                //Only allow the correct player to auto insert into this slot
                return viewer.equals(allowed);
            }

            @Override
            public void onClick(GUIMiscClickEvent event) {
                //Don't want to add custom handling to click events
            }

            @Override
            public void onPickupItem(GUIPickupItemEvent event) {
                //Restrict this action to only the correct player
                if(!event.getViewer().equals(allowed)){
                    //Prevent action by cancelling the GUIEvent
                    event.setCancelled(true);
                }

                //Prevent the slot being edited once the other player has confirmed
                if((allowed.equals(leftSide) && rightPlayerConfirmed)
                        || (allowed.equals(rightSide) && leftPlayerConfirmed)){
                    allowed.sendMessage(ChatColor.RED+"You cannot modify your offering after the other player has accepted! " +
                            "To cancel the trade close the inventory!");
                    //Prevent action by cancelling the GUIEvent
                    event.setCancelled(true);
                }
            }

            @Override
            public void onPlaceItem(GUIPlaceItemEvent event) {
                //Restrict this action to only the correct player
                if(!event.getViewer().equals(allowed)){
                    //Prevent action by cancelling the GUIEvent
                    event.setCancelled(true);
                }

                //Prevent the slot being edited once the other player has confirmed
                if((allowed.equals(leftSide) && rightPlayerConfirmed)
                        || (allowed.equals(rightSide) && leftPlayerConfirmed)){
                    allowed.sendMessage(ChatColor.RED+"You cannot modify your offering after the other player has accepted! " +
                            "To cancel the trade close the inventory!");
                    //Prevent action by cancelling the GUIEvent
                    event.setCancelled(true);
                }
            }

            @Override
            public void onCurrentItemChanged(GUISession guiSession, ItemStack newItem) {
                //Don't need to add custom handling here
            }
        });
    }

    @Override
    public void onEvent(GUIEvent event) {
        if(event instanceof GUICloseEvent){
            //The GUI has been closed by a player, cancel the trade
            Player closedIt = ((GUICloseEvent) event).getViewer();

            //Close the inventory for the other player, since we are cancelling the trade
            final Player otherPlayer = closedIt.equals(leftSide) ? rightSide : leftSide;
            /*Check if other player has inventory open (and close it) next tick.
              If we did it in the same tick it might not yet be closed so we wouldn't be able to tell
             */
            Bukkit.getScheduler().runTaskLater(InventoryGUIAPI.getInstance(), new Runnable(){
                @Override
                public void run() {
                    GUISession otherSession = GUISession.extractSession(otherPlayer);
                    if(otherSession != null && otherSession.getInventoryGUI().equals(gui)){
                        //If the other player is viewing this GUI
                        otherPlayer.closeInventory(); //Close the inventory
                    }
                }
            }, 1L);

            if(leftPlayerConfirmed && rightPlayerConfirmed){
                //If both players have confirmed the trade, it has happened - so don't return any items
                return;
            }

            if(cancelled){ //Trade already cancelled, so don't bother doing it again
                return;
            }
            cancelled = true; //Set that the trade has been cancelled

            //The items to return to each player
            List<ItemStack> leftItems = new ArrayList<ItemStack>();
            List<ItemStack> rightItems = new ArrayList<ItemStack>();
            for(InputSlot inputSlot:leftPlayerInputSlots){
                //Get the item currently inside this slot for this current GUI
                ItemStack inSlot = inputSlot.getCurrentItem(((GUICloseEvent) event).getSession().getGUIState());
                //If the slot had an item in it
                if(inSlot != null && !inSlot.getType().equals(Material.AIR)){
                    //Add it to the list of items to return to the player
                    leftItems.add(inSlot);
                    //Clear the item in this slot
                    inputSlot.setCurrentItem(((GUICloseEvent) event).getSession(), null);
                }
            }
            for(InputSlot inputSlot:rightPlayerInputSlots){
                //Get the item currently inside this slot for this current GUI
                ItemStack inSlot = inputSlot.getCurrentItem(((GUICloseEvent) event).getSession().getGUIState());
                //If the slot had an item in it
                if(inSlot != null && !inSlot.getType().equals(Material.AIR)){
                    //Add it to the list of items to return to the player
                    rightItems.add(inSlot);
                    //Clear the item in this slot
                    inputSlot.setCurrentItem(((GUICloseEvent) event).getSession(), null);
                }
            }

            //Tell the player on the left what happened
            leftSide.sendMessage(ChatColor.RED+"Trade cancelled!");
            //Return them their items (toArray just makes the list into an array, which is what this method expects)
            leftSide.getInventory().addItem(leftItems.toArray(new ItemStack[]{}));

            //Tell the player on the right what happened
            rightSide.sendMessage(ChatColor.RED+"Trade cancelled!");
            //Return them their items (toArray just makes the list into an array, which is what this method expects)
            rightSide.getInventory().addItem(rightItems.toArray(new ItemStack[]{}));
        }
        if(event instanceof GUIOpenEvent){
            //The GUI has been opened by a player
            Player opener = ((GUIOpenEvent) event).getViewer(); //The player who opened the GUI
            if(opener.equals(leftSide)){ //If this player is trading on the left
                //Tell them where to put their items
                opener.sendMessage(ChatColor.YELLOW+"You are trading on the left side of the GUI!");
            }
            else { //If this player is trading on the right
                //Tell them where to put their items
                opener.sendMessage(ChatColor.YELLOW+"You are trading on the right side of the GUI!");
            }
        }
    }

    //Do the trade
    protected void tradeItems(GUISession session){
        //Close the GUI for both players if it's open
        GUISession leftSession = GUISession.extractSession(leftSide);
        GUISession rightSession = GUISession.extractSession(rightSide);

        //If they are viewing this GUI, then close their inventory
        if(leftSession != null && leftSession.getInventoryGUI().equals(gui)){
            leftSide.closeInventory();
        }

        //If they are viewing this GUI, then close their inventory
        if(rightSession != null && rightSession.getInventoryGUI().equals(gui)){
            rightSide.closeInventory();
        }

        List<ItemStack> leftItems = new ArrayList<ItemStack>();
        List<ItemStack> rightItems = new ArrayList<ItemStack>();
        for(InputSlot inputSlot:leftPlayerInputSlots){
            //Get the item currently inside this slot for this current GUI
            ItemStack inSlot = inputSlot.getCurrentItem(session.getGUIState());
            //If the slot had an item in it
            if(inSlot != null && !inSlot.getType().equals(Material.AIR)){
                //Add it to the list of items to return to the player
                leftItems.add(inSlot);
                //Clear the item in this slot
                inputSlot.setCurrentItem(session, null);
            }
        }
        for(InputSlot inputSlot:rightPlayerInputSlots){
            //Get the item currently inside this slot for this current GUI
            ItemStack inSlot = inputSlot.getCurrentItem(session.getGUIState());
            //If the slot had an item in it
            if(inSlot != null && !inSlot.getType().equals(Material.AIR)){
                //Add it to the list of items to return to the player
                rightItems.add(inSlot);
                //Clear the item in this slot
                inputSlot.setCurrentItem(session, null);
            }
        }

        //Transfer the items

        //Give the player on the left the items from the right
        leftSide.getInventory().addItem(rightItems.toArray(new ItemStack[]{}));

        //Give the player on the right the items from the left
        rightSide.getInventory().addItem(leftItems.toArray(new ItemStack[]{}));

        leftSide.sendMessage(ChatColor.GREEN+"Trade complete!");
        rightSide.sendMessage(ChatColor.GREEN+"Trade complete!");
    }

    @Override
    public void genContents(Player viewer, int page, GUISession session, Callback<GUIContentsResponse> callback) {
        //Generate the content to display in the GUI and tell the GUI it.

        if(page != 1){ //if the page isn't 1 then we don't want to display anything
            //Tell the GUI there isn't a next page and we don't want to show anything
            callback.call(GUIContentsResponse.create(false, new ArrayList<GUIElement>()));
            return;
        }

        //The list of GUIElements to display in the GUI
        List<GUIElement> toDisplay = new ArrayList<GUIElement>();
        toDisplay.addAll(leftPlayerInputSlots); //Add the input slots for the left player
        toDisplay.addAll(rightPlayerInputSlots); //Add the input slots for the right player

        //For all the slots in the middle where we want to place divider
        for(int i:new int[]{4,13,22,31,40,49}){
            toDisplay.add( //Add an item in the middle of the GUI to divide it
                    GUIElementFactory.createActionItem(
                            GUIElementFactory.formatItem(
                                    new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15), //Black stained glass pane
                                    "Divider"), //Give the item the name divider
                            new Callback<Player>() {
                                @Override
                                public void call(Player param) {
                                    //When clicked we don't want to do anything
                                }
                            }
                    )
            );
        }

        //Add the GUIElement that lets the player on the left confirm the trade
        toDisplay.add(GUIElementFactory.createActionItem(48, //Put this in slot 48
                //The item to be displayed
                GUIElementFactory.formatItem(
                        new ItemStack(Material.WOOL, 1, //Colored wool, if confirmed already then green, if not then yellow
                                (byte) (leftPlayerConfirmed ? DyeColor.GREEN.getWoolData() : DyeColor.YELLOW.getWoolData())),
                        //The name of the item, changes depending on if already confirmed
                        ChatColor.YELLOW + (leftPlayerConfirmed ? "Unconfirm trade" : "Confirm trade"),
                        ChatColor.WHITE + "Click me to",//Line 1 of lore (item description)
                        ChatColor.WHITE + (leftPlayerConfirmed ? "unconfirm trade" : "confirm trade")), //Line 2 of lore
                new ActionItem.ActionHandler() {
                    @Override
                    public void onClick(GUIClickEvent event) {
                        //Only let the player on the left do this
                        if(!event.getViewer().equals(leftSide)){
                            return;
                        }
                        //When clicked alter the confirm status
                        leftPlayerConfirmed = !leftPlayerConfirmed; //Change if we are confirmed to the opposite to now

                        //If both players have confirmed, then do the trade
                        if(leftPlayerConfirmed && rightPlayerConfirmed){
                            tradeItems(event.getSession());
                            return;
                        }

                        //Will update the GUIElements being displayed
                        gui.updateContentsAndView();
                    }
                }
        ));

        //Add the GUIElement that lets the player on the right confirm the trade
        toDisplay.add(GUIElementFactory.createActionItem(48, //Put this in slot 48
                //The item to be displayed
                GUIElementFactory.formatItem(
                        new ItemStack(Material.WOOL, 1, //Colored wool, if confirmed already then green, if not then yellow
                                (byte) (rightPlayerConfirmed ? DyeColor.GREEN.getWoolData() : DyeColor.YELLOW.getWoolData())),
                        //The name of the item, changes depending on if already confirmed
                        ChatColor.YELLOW + (rightPlayerConfirmed ? "Unconfirm trade" : "Confirm trade"),
                        ChatColor.WHITE + "Click me to",//Line 1 of lore (item description)
                        ChatColor.WHITE + (rightPlayerConfirmed ? "unconfirm trade" : "confirm trade")), //Line 2 of lore
                new ActionItem.ActionHandler() {
                    @Override
                    public void onClick(GUIClickEvent event) {
                        //Only let the player on the right do this
                        if(!event.getViewer().equals(rightSide)){
                            return;
                        }
                        //When clicked alter the confirm status
                        rightPlayerConfirmed = !rightPlayerConfirmed; //Change if we are confirmed to the opposite to now

                        //If both players have confirmed, then do the trade
                        if(leftPlayerConfirmed && rightPlayerConfirmed){
                            tradeItems(event.getSession());
                            return;
                        }

                        //Will update the GUIElements being displayed
                        gui.updateContentsAndView();
                    }
                }
        ));

        //Tell the GUI there isn't a next page and what to display
        callback.call(GUIContentsResponse.create(false, toDisplay));
    }

    @Override
    public void genTitle(Player viewer, int page, GUISession session, Callback<String> callback) {
        //Tell the GUI what it should be called
        callback.call("Trade");
    }
}
