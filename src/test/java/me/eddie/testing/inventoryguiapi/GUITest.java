package me.eddie.testing.inventoryguiapi;

import junit.framework.Assert;
import me.eddie.inventoryguiapi.gui.contents.GUIContentsProvider;
import me.eddie.inventoryguiapi.gui.elements.ActionItem;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.events.GUIClickEvent;
import me.eddie.inventoryguiapi.gui.events.GUIEvent;
import me.eddie.inventoryguiapi.gui.guis.GUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.InventoryState;
import me.eddie.inventoryguiapi.plugin.EventCaller;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test written to test GUI
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ItemStack.class, InventoryGUIAPI.class, GUIElementFactory.class,
        Bukkit.class, BukkitScheduler.class, EventCaller.class, InventoryView.class})
public class GUITest {
    /**
     * Tests that GUI is:
     * -Correctly passing events to GUIElements interacted with (Just basic click)
     * -Showing viewer the inventory
     */
    @Test
    public void testGUI(){
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

        try {
            //Mock events that are fired to bukkit, make them do nothing
            TestUtil.mockBukkitEventCalling();
        } catch (Exception e) {
            e.printStackTrace();
        }

        InventoryType[] types = new InventoryType[]{ //Inventory types to test
                InventoryType.CHEST, //The chest inventory type
                InventoryType.DISPENSER}; //A non-standard inventory type

        //The GUI to test; test lots of different (valid) configurations
        for(InventoryType inventoryType:types){
            int[] allowedSizes = inventoryType.equals(InventoryType.CHEST) ?
                    new int[]{9,18,27,36,45,54}
                    : new int[]{inventoryType.getDefaultSize()}; //The different max-sizes we are going to test with
            for(int maxSize:allowedSizes){
                boolean[] dynamicSizeVals = inventoryType.equals(InventoryType.CHEST) ?
                        new boolean[]{true, false}
                        : new boolean[]{false}; //The different values of 'isDynamicSize' to test with
                for(boolean isDynamicSize:dynamicSizeVals){
                    int[] amts = new int[]{maxSize};
                    for(int guiElementAmt:amts) {
                        GUI gui = new GUI(inventoryType, maxSize, isDynamicSize, getContentsProvider("TITLE", guiElementAmt));
                        Player mockPlayer = Mockito.mock(Player.class); //A mock player to test stuff with
                        final Holder<Inventory> inventoryBeingViewed = new Holder<Inventory>(); //The inventory the player currently sees

                        //Update the inventory being viewed when the player is shown an inventory
                        Mockito.doAnswer(new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                inventoryBeingViewed.value = (Inventory) invocation.getArguments()[0];
                                return null;
                            }
                        }).when(mockPlayer).openInventory(Mockito.any(Inventory.class));

                        //Create a fake player inventory to use for this test
                        Inventory playerInventory = Bukkit.createInventory(mockPlayer, InventoryType.PLAYER, "player inventory");

                        gui.open(mockPlayer);
                        //Verify the player was shown an inventory
                        Mockito.verify(mockPlayer).openInventory(Mockito.any(Inventory.class));

                        //Mock the view the player is now seeing (with the inventory)
                        InventoryView openView = Mockito.mock(InventoryView.class);
                        //Make the top inventory the one being viewed
                        Mockito.when(openView.getTopInventory()).thenReturn(inventoryBeingViewed.value);
                        Mockito.when(openView.getBottomInventory()).thenReturn(playerInventory);
                        Mockito.when(openView.getType()).thenReturn(inventoryType);
                        Mockito.when(openView.getPlayer()).thenReturn(mockPlayer);

                        for(int i=0;i<guiElementAmt;i++){
                            //Simulate picking up item in slot
                            InventoryClickEvent pickupGUIElementEvent = new InventoryClickEvent(
                                    openView, InventoryType.SlotType.CONTAINER, i, ClickType.LEFT, InventoryAction.PICKUP_ALL);
                            //Get the GUI to handle the bukkit event
                            //Validation of if correct GUIElement was 'clicked' should happen with the element
                            gui.handleBukkitEvent(pickupGUIElementEvent, GUISession.extractSession(inventoryBeingViewed.value));

                            //Assert that the GUIElement was told about the event
                            GUISession session = GUISession.extractSession(inventoryBeingViewed.value);
                            Assert.assertNotNull(session);
                            InventoryState state = session.getGUIState().getExistingInventoryState(1);
                            Assert.assertNotNull(state);
                            GUIElement clicked = state.getElementInSlot(i);
                            Assert.assertNotNull(clicked);

                            //Asserts that the method was called
                            Mockito.verify(clicked).onEvent(Mockito.any(GUIEvent.class));

                            //Assert that the event was cancelled (always the case with this GUI we've created)
                            Assert.assertTrue(pickupGUIElementEvent.isCancelled());
                        }
                    }
                }
            }
        }
    }

    private GUIContentsProvider getContentsProvider(final String title, final int elementAmount){
        return new GUIContentsProvider() {
            @Override
            public void genContents(Player viewer, int page, GUISession session, Callback<GUIContentsResponse> callback) {
                if(page > 1){
                    callback.call(GUIContentsResponse.create(false, new ArrayList<GUIElement>()));
                }
                //What is in each GUIElement, quantity, desired slots, etc... doesn't matter as already has been tested elsewhere.

                List<GUIElement> result = new ArrayList<GUIElement>();
                for(int i=0;i<elementAmount;i++){
                    final int slotShouldEndUp = i;
                    ItemStack display = new FakeItemStack(Material.WOOD);
                    display.setItemMeta(new FakeItemMeta("Name "+i, new ArrayList<String>(),
                            new HashMap<Enchantment, Integer>(), new ArrayList<ItemFlag>(), false));

                    GUIElement elem = GUIElementFactory.createActionItem(
                            display,
                            new ActionItem.ActionHandler() {
                                @Override
                                public void onClick(GUIClickEvent event) {
                                    //Assert that the correct GUIElement was clicked
                                    Assert.assertEquals(slotShouldEndUp, event.getBukkitEvent().getSlot());
                                }
                            }
                    );
                    elem = PowerMockito.spy(elem);
                    result.add(elem);
                }

                callback.call(GUIContentsResponse.create(false, result));
            }

            @Override
            public void genTitle(Player viewer, int page, GUISession session, Callback<String> callback) {
                callback.call(title); //Irrelevant to test what title is, etc...; tested elsewhere
            }
        };
    }
}
