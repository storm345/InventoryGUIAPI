package me.eddie.testing.inventoryguiapi;

import junit.framework.Assert;
import me.eddie.inventoryguiapi.gui.elements.AbstractGUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.elements.InputSlot;
import me.eddie.inventoryguiapi.gui.events.GUIMiscClickEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPickupItemEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPlaceItemEvent;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
import me.eddie.inventoryguiapi.gui.session.InventoryState;
import me.eddie.inventoryguiapi.gui.view.GUIPresenter;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Test written to test GUIPresenter
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ItemStack.class, InventoryGUIAPI.class, GUIElementFactory.class, Bukkit.class, BukkitScheduler.class})
public class GUIPresenterTest {
    @Test
    public void testPresenter(){
        //Setup mocking of classes that we can't instantiate without a minecraft server
        TestUtil.mockItemStacks();
        TestUtil.mockGUIElementFactory();
        TestUtil.mockPlugin();

        try {
            TestUtil.mockServer();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //Mock the bukkit scheduler so stuff that happens 'next tick' (Eg. close GUI and open new page of GUI) happens instantaneously.
        //Mock bukkit inventory creation so that it can be simulated
        TestUtil.mockBukkitSchedulerAndInvCreation();


        //genInvState args: numOfElements or numOfElements, shouldSetDesiredSlots, desiredSlotsOffset (Desired slot of each element is element num+offset)
        //Args: Inventory Type, maxInvSize, isAutoResize, requiredInvSize, the state to display, the page to display, if should simulate inventory re-use
        //Empty inventory with auto resize inv
        testInvDisplay(InventoryType.CHEST, 54, true, 9, genInvState(0), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 9, genInvState(0), 1, true);

        //Full inventory with auto resize inv
        testInvDisplay(InventoryType.CHEST, 54, true, 54, genInvState(54), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 54, genInvState(54), 1, true);

        //Inventory of exactly 1 line of items
        testInvDisplay(InventoryType.CHEST, 54, true, 9, genInvState(9), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 9, genInvState(9), 1, true);

        //Inventory of exactly 1 more than 1 line of items
        testInvDisplay(InventoryType.CHEST, 54, true, 18, genInvState(10), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 18, genInvState(10), 1, true);

        //Inventory of 1 less than 2 lines of items, inventory should auto re-size
        testInvDisplay(InventoryType.CHEST, 54, true, 18, genInvState(17), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 18, genInvState(17), 1, true);

        //Inventory of exactly 2 lines of items, inventory should auto re-size
        testInvDisplay(InventoryType.CHEST, 54, true, 18, genInvState(18), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 18, genInvState(18), 1, true);

        //Inventory of minimum items for 3 lines, inventory should auto re-size
        testInvDisplay(InventoryType.CHEST, 54, true, 27, genInvState(19), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 27, genInvState(19), 1, true);

        //Inventory of minimum items for 3 lines (auto re-sizing) but who's contents is offset by 9 slots, so should be 4 lines.
        testInvDisplay(InventoryType.CHEST, 54, true, 36, genInvState(19, true, 9), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, true, 36, genInvState(19, true, 9), 1, true);

        //Non-full inventory that should always be maxSize
        testInvDisplay(InventoryType.CHEST, 54, false, 54, genInvState(17), 1, false);
        testInvDisplay(InventoryType.CHEST, 54, false, 54, genInvState(17), 1, true);

        //Check that a different inventory type is being created correctly
        testInvDisplay(InventoryType.DISPENSER, InventoryType.DISPENSER.getDefaultSize(), false, 9, genInvState(9), 1, false);
    }

    //Mock a player having their view updated after viewing (or not viewing) a previously open inv
    private void testInvDisplay(final InventoryType inventoryType, int maxInvSize, boolean autoResize, final int requiredInvSize, final InventoryState toDisplay, int page, boolean shouldReuseInv){
        final Player viewer = Mockito.mock(Player.class); //Mock the player viewing it

        InventoryGUI inventoryGUI = Mockito.mock(InventoryGUI.class); //Mock an inventory GUI
        Mockito.when(inventoryGUI.getMaximumGUISize()).thenReturn(maxInvSize); //Set the size of the GUI
        Mockito.when(inventoryGUI.getInventoryType()).thenReturn(inventoryType); //Set the type of the GUI
        Mockito.when(inventoryGUI.isGUISizeDynamic()).thenReturn(autoResize); //Dynamically re-sizing GUI

        GUIState guiState = new GUIState();
        guiState.updateInventoryState(page, toDisplay); //Set the inventory state to display
        final GUISession session = new GUISession(inventoryGUI, page, guiState); //Create a valid session to show

        final Inventory previouslyOpen; //The inventory the player was looking at before the GUI wanted to be presented
        if(!shouldReuseInv){ //previouslyOpen should be any inventory that isn't from the GUI
            //Simulate having been viewing some other random inventory
            previouslyOpen = Bukkit.createInventory(null, 54, "");
        }
        else { //Make previouslyOpen an inventory from this GUI that can be re-used
            //Simulate having been viewing an inventory from this GUI that can be re-used
            previouslyOpen = inventoryType.equals(InventoryType.CHEST)
                    ? Bukkit.createInventory(session, requiredInvSize, toDisplay.getTitle())
                    : Bukkit.createInventory(session, inventoryType, toDisplay.getTitle());
        }

        InventoryView prevOpenView = Mockito.mock(InventoryView.class); //The previously open inventory view of the player
        Mockito.when(prevOpenView.getTopInventory()).thenReturn(previouslyOpen);
        Mockito.when(viewer.getOpenInventory()).thenReturn(prevOpenView); //Set this as what the player was looking at

        GUIPresenter presenter = new GUIPresenter(); //The object to test

        //check generated inv is correct
        final Callback<Inventory> validateInventory = new Callback<Inventory>() {
            @Override
            public void call(Inventory inv) {
                Assert.assertEquals(requiredInvSize, inv.getSize()); //Assert that inv is the correct size
                Assert.assertEquals(inventoryType, inv.getType()); //Assert that the inv is the correct type

                ItemStack[] content = inv.getContents(); //What is being displayed
                Map<Integer, GUIElement> shouldBeDisplayed = toDisplay.getComputedContentsBySlot(); //What should be displayed
                for(Map.Entry<Integer, GUIElement> display:shouldBeDisplayed.entrySet()){
                    GUIElement elem = display.getValue();
                    ItemStack item = elem.getDisplay(viewer, session); //The item that should be displayed
                    Assert.assertEquals(item, content[display.getKey()]); //Assert that the items are the same
                }
            }
        };
        Mockito.doAnswer(new Answer() { //When open inv, validate it is good
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Inventory inv = (Inventory) invocation.getArguments()[0];
                validateInventory.call(inv);
                return null;
            }
        }).when(viewer).openInventory(Mockito.any(Inventory.class));
        Mockito.doAnswer(new Answer() { //When update inv, validate it is good
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                validateInventory.call(previouslyOpen);
                return null;
            }
        }).when(viewer).updateInventory();

        presenter.updateView(viewer, session); //Call to update the view

        //Verify that what should have been called was called
        if(!shouldReuseInv){
            Mockito.verify(viewer).openInventory(Mockito.any(Inventory.class)); //Verify an inventory was opened
        }
        else {
            Mockito.verify(viewer, Mockito.never()).openInventory(Mockito.any(Inventory.class)); //Verify no new inv opened
            Mockito.verify(viewer).updateInventory(); //Verify inv was updated
        }
    }

    private InventoryState genInvState(int contentsAmount){ //An inv state full of GUIElements that don't care where they go
        return genInvState(contentsAmount, false, 0);
    }

    //Generates an inventory state full with the given amount of GUIElements
    private InventoryState genInvState(int contentsAmount, boolean setDesiredSlots, int desiredSlotoffset){
        Map<Integer, GUIElement> contents = new HashMap<Integer, GUIElement>();
        for(int i =0;i<contentsAmount;i++){
            GUIElement elem;
            if(i % 2 == 0) { //For even values of i
                elem = GUIElementFactory.createActionItem(!setDesiredSlots ? AbstractGUIElement.NO_DESIRED_SLOT : i+desiredSlotoffset, //The desired slot if wanted
                        GUIElementFactory.formatItem(new ItemStack(Material.WOOD), "item " + i, "Some lore"),
                        new Callback<Player>() {
                            @Override
                            public void call(Player param) {
                                //Do a thing
                            }
                        }
                );
            }
            else { //For odd values of i use input slots instead. Shouldn't make any difference, but why not?
                elem = GUIElementFactory.createInputSlot(i + "inputSlot",
                        !setDesiredSlots ? AbstractGUIElement.NO_DESIRED_SLOT : i+desiredSlotoffset, //The desired slot if wanted
                        new InputSlot.ActionHandler() {
                            @Override
                            public boolean shouldAllowAutoInsert(Player viewer, GUISession session) {
                                return true;
                            }

                            @Override
                            public void onClick(GUIMiscClickEvent event) {
                                //Do nothing
                            }

                            @Override
                            public void onPickupItem(GUIPickupItemEvent event) {
                                //Do nothing
                            }

                            @Override
                            public void onPlaceItem(GUIPlaceItemEvent event) {
                                //Do nothing
                            }

                            @Override
                            public void onCurrentItemChanged(GUISession guiSession, ItemStack newItem) {
                                //Do nothing
                            }
                        });
            }

            //Put this GUIElement into the map of positions in the correct position
            contents.put(elem.hasDesiredDisplayPosition()?elem.getDesiredDisplayPosition():i, elem);
        }

        InventoryState toDisplay = new InventoryState();
        toDisplay.setTitle("TITLE");
        toDisplay.setHasNextPage(false);
        toDisplay.setComputedContentsBySlot(contents);
        return toDisplay;
    }
}
