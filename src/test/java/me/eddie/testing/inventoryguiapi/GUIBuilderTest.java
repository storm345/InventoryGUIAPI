package me.eddie.testing.inventoryguiapi;

import junit.framework.Assert;
import me.eddie.inventoryguiapi.gui.contents.GUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.events.GUIEvent;
import me.eddie.inventoryguiapi.gui.guis.*;
import me.eddie.inventoryguiapi.gui.view.GUIPresenter;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

/**
 * Test written to test GUIBuilder
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ItemStack.class, InventoryGUIAPI.class, GUIElementFactory.class,
        Bukkit.class})
public class GUIBuilderTest {
    /**
     * Tests that GUIBuilder is:
     * creating GUIs correctly,
     * validation GUI options correctly
     */
    @Test
    public void testGUIBuilder(){
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

        assertError(new Runnable() { //Assert that can't build a GUI without specifying anything
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.build();
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that null contents provider isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.contentsProvider(null);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that null GUIStateBehaviour isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.guiStateBehaviour(null);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that null action listeners isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                GUIActionListener[] actionListeners = null;
                guiBuilder.actionListeners(actionListeners);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that null inventory type isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.inventoryType(null);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert that null paginating contents provider isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.paginatingContentsProvider(null);
            }
        }, IllegalArgumentException.class);

        assertNoError(new Runnable() { //Assert that null GUIPopulator IS allowed - Makes it use default
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.populator(null);
            }
        });

        assertNoError(new Runnable() { //Assert that null GUIPresenter IS allowed - Makes it use default
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.presenter(null);
            }
        });

        assertError(new Runnable() { //Assert that size < 1 isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.size(0);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert null title isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.contents(null, new ArrayList<GUIElement>(), false, false, false);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert long title isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.contents("asdakhdaksdhajksssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss",
                        new ArrayList<GUIElement>(), false, false, false);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert null content isn't allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.contents("title", null, false, false, false);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert dynamic resize not allowed with non chest
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.inventoryType(InventoryType.ANVIL);
                guiBuilder.dynamicallyResizeToWrapContent(true);
            }
        }, IllegalArgumentException.class);

        assertError(new Runnable() { //Assert dynamic resize not allowed with non chest
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.dynamicallyResizeToWrapContent(true);
                guiBuilder.inventoryType(InventoryType.ANVIL);
            }
        }, IllegalArgumentException.class);

        assertNoError(new Runnable() { //Assert dynamic resize allowed with default type
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.dynamicallyResizeToWrapContent(true);
            }
        });

        assertNoError(new Runnable() { //Assert dynamic resize allowed with chest type
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.inventoryType(InventoryType.CHEST);
                guiBuilder.dynamicallyResizeToWrapContent(true);
            }
        });

        assertNoError(new Runnable() { //Assert no errors building a valid GUI, specifying the minimum allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.contents("Title", new ArrayList<GUIElement>(), false, false, false);
                guiBuilder.guiStateBehaviour(GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION);
                InventoryGUI gui = guiBuilder.build();
                Assert.assertTrue(gui instanceof GUI); //Assert created correct GUI type
            }
        });

        assertNoError(new Runnable() { //Assert no errors building a valid GUI, specifying the maximum allowed
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.contents("Title", new ArrayList<GUIElement>(), false, false, false);
                guiBuilder.guiStateBehaviour(GUIBuilder.GUIStateBehaviour.BOUND_TO_GUI);
                guiBuilder.inventoryType(InventoryType.ANVIL);
                guiBuilder.size(InventoryType.ANVIL.getDefaultSize());
                GUIActionListener[] actionListeners = new GUIActionListener[]{new GUIActionListener() {
                    @Override
                    public void onEvent(GUIEvent event) {

                    }
                }};
                guiBuilder.actionListeners(actionListeners);
                guiBuilder.dynamicallyResizeToWrapContent(false);
                guiBuilder.populator(new GUIPopulator());
                guiBuilder.presenter(new GUIPresenter());
                InventoryGUI gui = guiBuilder.build();
                Assert.assertTrue(gui instanceof SharedGUI); //Assert created correct GUI type
                Assert.assertEquals(InventoryType.ANVIL.getDefaultSize(), gui.getMaximumGUISize());
                Assert.assertEquals(InventoryType.ANVIL, gui.getInventoryType());
            }
        });

        assertError(new Runnable() { //Assert we can't create a GUI with an invalid size
            @Override
            public void run() {
                GUIBuilder guiBuilder = new GUIBuilder();
                guiBuilder.size(3); //Chest invs cannot be size 3
                guiBuilder.build();
            }
        }, IllegalArgumentException.class);
    }

    public void assertNoError(Runnable run){
        try {
            run.run();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void assertError(Runnable run, Class<? extends Exception> exceptionClass){
        Class<? extends Exception> thrownException = null;
        try {
            run.run();
        } catch (Exception e) {
            thrownException = e.getClass();
        }
        Assert.assertNotNull(thrownException);
        Assert.assertTrue(exceptionClass.equals(thrownException) || exceptionClass.isAssignableFrom(thrownException));
    }

}
