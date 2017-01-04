package me.eddie.testing.inventoryguiapi;

import junit.framework.Assert;
import me.eddie.inventoryguiapi.gui.elements.ActionItem;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.elements.InputSlot;
import me.eddie.inventoryguiapi.gui.events.GUIClickEvent;
import me.eddie.inventoryguiapi.gui.events.GUIMiscClickEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPickupItemEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPlaceItemEvent;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test written to test GUIElementFactory
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ItemStack.class, InventoryGUIAPI.class,
        Bukkit.class})
public class GUIElementFactoryTest {
    /**
     * Tests that GUIElementFactory is:
     * creating GUIElements,
     * validation of params correctly
     */
    @Test
    public void testElementFactory(){
        //Setup mocking of classes that we can't instantiate without a minecraft server
        TestUtil.mockItemStacks();
        TestUtil.mockPlugin();

        try {
            TestUtil.mockServer();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        assertNoError(new Runnable() { //Assert we can format and item and it work correct
            @Override
            public void run() {
                ItemStack item = new FakeItemStack(Material.WOOL);
                ItemStack formatted = GUIElementFactory.formatItem(item, "Name", "Lore");
                Assert.assertNotNull(formatted);
                Assert.assertEquals("Name", formatted.getItemMeta().getDisplayName());
                Assert.assertTrue(formatted.getItemMeta().getLore().get(0).equals("Lore"));
            }
        });

        assertError(new Runnable() { //Assert that we can't format a null item
            @Override
            public void run() {
                GUIElementFactory.formatItem(null, "", "");
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't format with a null name
            @Override
            public void run() {
                ItemStack item = new FakeItemStack(Material.WOOL);
                GUIElementFactory.formatItem(item, null, "");
            }
        }, IllegalArgumentException.class);

        //Tests for creating GUIElements only tested on most overloaded factory method, as the other methods call to this one

        assertError(new Runnable() { //Assert that we can't create input slot with null id
            @Override
            public void run() {
                GUIElementFactory.createInputSlot(null, 5, new InputSlot.ActionHandler() {
                    @Override
                    public boolean shouldAllowAutoInsert(Player viewer, GUISession session) {
                        return false;
                    }

                    @Override
                    public void onClick(GUIMiscClickEvent event) {

                    }

                    @Override
                    public void onPickupItem(GUIPickupItemEvent event) {

                    }

                    @Override
                    public void onPlaceItem(GUIPlaceItemEvent event) {

                    }

                    @Override
                    public void onCurrentItemChanged(GUISession guiSession, ItemStack newItem) {

                    }
                });
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't create input slot with invalid desired slot
            @Override
            public void run() {
                GUIElementFactory.createInputSlot("slotid", -5, new InputSlot.ActionHandler() {
                    @Override
                    public boolean shouldAllowAutoInsert(Player viewer, GUISession session) {
                        return false;
                    }

                    @Override
                    public void onClick(GUIMiscClickEvent event) {

                    }

                    @Override
                    public void onPickupItem(GUIPickupItemEvent event) {

                    }

                    @Override
                    public void onPlaceItem(GUIPlaceItemEvent event) {

                    }

                    @Override
                    public void onCurrentItemChanged(GUISession guiSession, ItemStack newItem) {

                    }
                });
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't create input slot with null action handler
            @Override
            public void run() {
                GUIElementFactory.createInputSlot("slotid", 5, null);
            }
        }, IllegalArgumentException.class);

        assertNoError(new Runnable() { //Assert that we can create an input slot with no errors
            @Override
            public void run() {
                GUIElementFactory.createInputSlot("slotid", 5, new InputSlot.ActionHandler() {
                    @Override
                    public boolean shouldAllowAutoInsert(Player viewer, GUISession session) {
                        return false;
                    }

                    @Override
                    public void onClick(GUIMiscClickEvent event) {

                    }

                    @Override
                    public void onPickupItem(GUIPickupItemEvent event) {

                    }

                    @Override
                    public void onPlaceItem(GUIPlaceItemEvent event) {

                    }

                    @Override
                    public void onCurrentItemChanged(GUISession guiSession, ItemStack newItem) {

                    }
                });
            }
        });

        assertError(new Runnable() { //Assert that we can't create a bad desired slot
            @Override
            public void run() {
                GUIElementFactory.createActionItem(-5, new FakeItemStack(), new Callback<Player>() {
                    @Override
                    public void call(Player param) {

                    }
                });
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't create a bad desired slot
            @Override
            public void run() {
                GUIElementFactory.createActionItem(-5, new FakeItemStack(), new ActionItem.ActionHandler() {
                    @Override
                    public void onClick(GUIClickEvent event) {

                    }
                });
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't create an action item with null display item
            @Override
            public void run() {
                GUIElementFactory.createActionItem(5, null, new Callback<Player>() {
                    @Override
                    public void call(Player param) {

                    }
                });
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't create an action item with null display item
            @Override
            public void run() {
                GUIElementFactory.createActionItem(5, null, new ActionItem.ActionHandler() {
                    @Override
                    public void onClick(GUIClickEvent event) {

                    }
                });
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't create an action with a null action handler
            @Override
            public void run() {
                GUIElementFactory.createActionItem(5, new FakeItemStack(), (ActionItem.ActionHandler)null);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that we can't create an action with a null click task
            @Override
            public void run() {
                GUIElementFactory.createActionItem(5, new FakeItemStack(), (Callback<Player>)null);
            }
        }, IllegalArgumentException.class);

        assertNoError(new Runnable() { //Assert that we can create an action item with valid params
            @Override
            public void run() {
                GUIElementFactory.createActionItem(5, new FakeItemStack(), new Callback<Player>() {
                    @Override
                    public void call(Player param) {

                    }
                });
            }
        });

        assertNoError(new Runnable() { //Assert that we can create an action item with valid params
            @Override
            public void run() {
                GUIElementFactory.createActionItem(5, new FakeItemStack(), new ActionItem.ActionHandler() {
                    @Override
                    public void onClick(GUIClickEvent event) {

                    }
                });
            }
        });
    }

    public void assertNoError(Runnable run){ //Runs the given runnable and asserts that no errors occured
        try {
            run.run();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    //Runs the given runnable and asserts that the given error occured
    public void assertError(Runnable run, Class<? extends Exception> exceptionClass){
        Class<? extends Exception> thrownException = null;
        try {
            run.run();
        } catch (Exception e) {
            thrownException = e.getClass();
        }
        Assert.assertNotNull(thrownException);
        //Check that error that occured is the same as, or a subclass of the desired error
        Assert.assertTrue(exceptionClass.equals(thrownException) || exceptionClass.isAssignableFrom(thrownException));
    }

}
