package me.eddie.testing.inventoryguiapi;

import junit.framework.Assert;
import me.eddie.inventoryguiapi.gui.contents.GUIContentsProvider;
import me.eddie.inventoryguiapi.gui.contents.GUIPopulator;
import me.eddie.inventoryguiapi.gui.elements.AbstractGUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
import me.eddie.inventoryguiapi.gui.session.InventoryState;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Test written to test GUIPopulator
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ItemStack.class, InventoryGUIAPI.class, GUIElementFactory.class})
public class GUIPopulatorTest {
    @Test
    public void testElementPlacementAndButtonPlacement(){
        //Setup mocking of classes that we can't instantiate without a minecraft server
        TestUtil.mockGUIElementFactory();
        TestUtil.mockPlugin();

        //Tests page buttons for auto paginating inv
        //Args: invSize, numOfElements, if should put elems in desired slots, offset for desired slots if they are being used, if there should be page buttons

        //No elements, not caring about slots; no page buttons
        testElementPlacementAndButtonPlacement(54, 0, false, 0, false);
        //1 element, not caring about slots, no page buttons
        testElementPlacementAndButtonPlacement(54, 1, false, 0, false);
        //54 elements, not caring about slots, no page buttons
        testElementPlacementAndButtonPlacement(54, 1, false, 0, false);
        //1 element, with desired slot offset 9 from natural position, no page buttons
        testElementPlacementAndButtonPlacement(54, 1, true, 9, false);
        //30 elements, with desired slot offset 9 from natural position, no page buttons
        testElementPlacementAndButtonPlacement(54, 30, true, 9, false);
        //54 elements, with desired slot offset 9 from natural position, no page buttons
        testElementPlacementAndButtonPlacement(54, 54, true, 9, false);

        //No elements, not caring about slots; page buttons
        testElementPlacementAndButtonPlacement(54, 0, false, 0, true);
        //1 element, not caring about slots, page buttons
        testElementPlacementAndButtonPlacement(54, 1, false, 0, true);
        //54 elements, not caring about slots, page buttons
        testElementPlacementAndButtonPlacement(54, 1, false, 0, true);
        //1 element, with desired slot offset 9 from natural position, page buttons
        testElementPlacementAndButtonPlacement(54, 1, true, 9, true);
        //30 elements, with desired slot offset 9 from natural position, page buttons
        testElementPlacementAndButtonPlacement(54, 30, true, 9, true);
        //52 elements, with desired slot offset 9 from natural position, page buttons
        testElementPlacementAndButtonPlacement(54, 52, true, 9, true);
    }

    private void testElementPlacementAndButtonPlacement(final int invSize, final int elemAmt, final boolean setDesiredSlots, final int desiredSlotOffsets, final boolean shouldHaveNextPage){ //Test if page buttons were generated correctly for given size inv and num of elems
        GUIPopulator populator = new GUIPopulator(); //Default GUIPopulator
        GUIBuilder guiBuilder = new GUIBuilder().guiStateBehaviour(GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION)
                .size(invSize) //Any valid size
                .dynamicallyResizeToWrapContent(false); //Doesn't affect placement whether enabled or disabled

        //Generate a list of elements to test displaying
        //Args: num of elements, if they should have desired slots set, the offset from 'natural' position to set the slots to, exclusive upper bound for slots
        final List<GUIElement> elems = genElementList(elemAmt, setDesiredSlots, desiredSlotOffsets, shouldHaveNextPage?invSize-2:invSize);
        guiBuilder.contentsProvider(new GUIContentsProvider() {
            @Override
            public void genContents(Player viewer, int page, GUISession session, Callback<GUIContentsResponse> callback) {
                callback.call(GUIContentsResponse.create(shouldHaveNextPage, elems));
            }

            @Override
            public void genTitle(Player viewer, int page, GUISession session, Callback<String> callback) {
                callback.call("Title");
            }
        });

        InventoryGUI gui = guiBuilder.build();

        final GUIState guiState = new GUIState();
        final GUISession session = new GUISession(gui, 1, guiState);

        final Player viewer = Mockito.mock(Player.class);

        populator.populateGUI(session, viewer, new Callback<Void>() {
            @Override
            public void call(Void param) {
                InventoryState state = guiState.getExistingInventoryState(1);
                Assert.assertNotNull(state); //Assert that there is now an inventory state for page

                //Test next and prev buttons are there
                if(!shouldHaveNextPage) {
                    Assert.assertEquals(elemAmt, state.getComputedContentsBySlot().size()); //Assert that the number of elements is correct
                }
                else {
                    Assert.assertEquals(elemAmt+2, state.getComputedContentsBySlot().size()); //Assert that the number of elements is correct

                    GUIElement backButton = state.getElementInSlot(invSize-2); //Get what should be the back and forward buttons
                    GUIElement forwardButton = state.getElementInSlot(invSize-1);

                    Assert.assertNotNull(backButton); //Check they exist
                    Assert.assertNotNull(forwardButton);

                    ItemStack backItem = backButton.getDisplay(viewer, session); //Get what they are to be displayed as
                    ItemStack forwardItem = forwardButton.getDisplay(viewer, session);
                    Assert.assertNotNull(backItem); //Check they will be displaying an item
                    Assert.assertNotNull(forwardItem);
                    Assert.assertEquals(Material.PAPER, backItem.getType()); //Check that the displayed item is the correct type
                    Assert.assertEquals(Material.PAPER, forwardItem.getType());

                    String backText = InventoryGUIAPI.getLanguageManager().getString("gui.button.prevPage.name");
                    String forwardText = InventoryGUIAPI.getLanguageManager().getString("gui.button.nextPage.name");
                    Assert.assertEquals(backText, backItem.getItemMeta().getDisplayName()); //Check that the items are correctly formatted
                    Assert.assertEquals(forwardText, forwardItem.getItemMeta().getDisplayName());
                }

                //Check every GUIElement is in the correct place
                int i =0;
                for(GUIElement elem:elems){
                    int correctSlot = elem.hasDesiredDisplayPosition() ?
                            elem.getDesiredDisplayPosition() : i;
                    GUIElement inSlot = state.getElementInSlot(correctSlot);
                    Assert.assertEquals(elem, inSlot); //Check this GUIElement is the same as what's in the slot
                    i++;
                }
            }
        });
    }

    private List<GUIElement> genElementList(int amount, boolean setDesiredSlots, int desiredSlotOffset, int maxSize){
        List<GUIElement> res = new ArrayList<GUIElement>();
        for(int i =0;i<amount;i++){
            int slot = setDesiredSlots ? i+desiredSlotOffset : AbstractGUIElement.NO_DESIRED_SLOT;
            if(slot >= maxSize){ //If too big, wrap back around
                slot -= maxSize;
            }
            ItemStack it = new ItemStack(Material.WOOD);
            GUIElement elem = GUIElementFactory.createActionItem(slot,
                    GUIElementFactory.formatItem(it, "Name", "Some lore"),
                    new Callback<Player>() {
                        @Override
                        public void call(Player param) {
                            //Do a thing
                        }
                    }
            );

            res.add(elem);
        }
        return res;
    }
}
