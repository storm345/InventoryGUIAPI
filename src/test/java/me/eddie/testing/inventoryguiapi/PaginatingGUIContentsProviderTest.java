package me.eddie.testing.inventoryguiapi;

import junit.framework.Assert;
import me.eddie.inventoryguiapi.gui.contents.GUIContentsProvider;
import me.eddie.inventoryguiapi.gui.contents.PaginatingGUIContentsProvider;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
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
 * Test written to test PaginatingGUIContentsProvider
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ItemStack.class, InventoryGUIAPI.class, GUIElementFactory.class})
public class PaginatingGUIContentsProviderTest {
    @Test
    public void testPageTitles(){
        //Setup mocking of classes that we can't instantiate without a minecraft server
        TestUtil.mockGUIElementFactory();
        TestUtil.mockPlugin(); //Uses English locale

        //Args: invSize, numOfElements, page number to check, base title, if include page number, if include page count, the correct title
        //Empty page
        assertTitleEquals(54, 0, 1, "Name", false, false, "Name");
        assertTitleEquals(54, 0, 1, "Name", true, false, "Name page 1");
        assertTitleEquals(54, 0, 1, "Name", true, true, "Name 1/1");
        assertTitleEquals(54, 0, 1, "Name", false, true, "Name 1 pages");

        //First page, not full
        assertTitleEquals(54, 53, 1, "Name", false, false, "Name");
        assertTitleEquals(54, 53, 1, "Name", true, false, "Name page 1");
        assertTitleEquals(54, 53, 1, "Name", true, true, "Name 1/1");
        assertTitleEquals(54, 53, 1, "Name", false, true, "Name 1 pages");

        //First page, full
        assertTitleEquals(54, 54, 1, "Name", true, false, "Name page 1");
        assertTitleEquals(54, 54, 1, "Name", true, true, "Name 1/1");
        assertTitleEquals(54, 54, 1, "Name", false, true, "Name 1 pages");

        //First page, minimum number of elements to have 2 pages
        assertTitleEquals(54, 55, 1, "Name", true, false, "Name page 1");
        assertTitleEquals(54, 55, 1, "Name", true, true, "Name 1/2");
        assertTitleEquals(54, 55, 1, "Name", false, true, "Name 2 pages");

        //Second page, minimum number of elements to have 2 pages
        assertTitleEquals(54, 55, 2, "Name", true, false, "Name page 2");
        assertTitleEquals(54, 55, 2, "Name", true, true, "Name 2/2");
        assertTitleEquals(54, 55, 2, "Name", false, true, "Name 2 pages");

        //Second page, max number of elements to have 2 pages
        assertTitleEquals(54, 104, 2, "Name", true, false, "Name page 2");
        assertTitleEquals(54, 104, 2, "Name", true, true, "Name 2/2");
        assertTitleEquals(54, 104, 2, "Name", false, true, "Name 2 pages");

        //Second page, minimum number of elements to have 3 pages
        assertTitleEquals(54, 105, 2, "Name", true, false, "Name page 2");
        assertTitleEquals(54, 105, 2, "Name", true, true, "Name 2/3");
        assertTitleEquals(54, 105, 2, "Name", false, true, "Name 3 pages");
    }

    @Test
    public void testPageSplitting(){
        //Setup mocking of classes that we can't instantiate without a minecraft server
        TestUtil.mockGUIElementFactory();
        TestUtil.mockPlugin();

        //Tests page buttons for auto paginating inv
        //Args: invSize, numOfElements, page number to check, should there be a page after it, numOfElements on page
        testPageSplitting(54, 0, 1, false, 0); //0 should be 1 page
        testPageSplitting(54, 1, 1, false, 1); //1 should be 1 page
        testPageSplitting(54, 53, 1, false, 53); //53 can fit on 1 page
        testPageSplitting(54, 54, 1, false, 54); //54 can fit on 1 page
        testPageSplitting(54, 55, 1, true, 52); //55 needs more than 1 page
        testPageSplitting(9, 9, 1, false, 9); //9 can fit on 1 page
        testPageSplitting(9, 10, 1, true, 7); //10 needs 2 pages
        testPageSplitting(9, 10, 2, false, 3); //10 can fit on 2 pages
        testPageSplitting(9, 14, 2, false, 7); //14 can fit on 2 pages
        testPageSplitting(9, 15, 2, true, 7); //15 needs a 3rd page
    }

    private void testPageSplitting(final int invSize, final int elemAmt, final int page, final boolean shouldHaveNextPage,
                                                final int amtOnPage){ //Test if page buttons were generated correctly for given size inv and num of elems
        PaginatingGUIContentsProvider contentsProvider = new PaginatingGUIContentsProvider() { //The object to test
            @Override
            public void genContents(Player viewer, GUISession session, Callback<List<GUIElement>> callback) {
                callback.call(genElementList(elemAmt));
            }

            @Override
            public void genBaseTitle(Player viewer, GUISession session, Callback<String> callback) {
                callback.call("A title"); //Irrelevant to test
            }

            @Override
            public boolean showPageNumberInTitle() {
                return false; //Irrelevant to test
            }

            @Override
            public boolean showPageCountInTitle() {
                return false; //Irrelevant to test
            }
        };

        final GUIState guiState = new GUIState();
        InventoryGUI gui = Mockito.mock(InventoryGUI.class);
        Mockito.when(gui.getMaximumGUISize()).thenReturn(invSize);

        final GUISession session = new GUISession(gui, page, guiState);

        final Player viewer = Mockito.mock(Player.class);

        contentsProvider.genContents(viewer, page, session, new Callback<GUIContentsProvider.GUIContentsResponse>() {
            @Override
            public void call(GUIContentsProvider.GUIContentsResponse result) {
                Assert.assertNotNull(result);

                List<GUIElement> content = result.getElements();

                Assert.assertNotNull(content); //Make sure returned contents isn't null

                boolean nextPage = result.hasNextPage();

                Assert.assertEquals(shouldHaveNextPage, nextPage); //Check if the page splitting was done correctly

                Assert.assertEquals(amtOnPage, content.size()); //Check if the quantity of returned elements is right
            }
        });
    }

    private void assertTitleEquals(final int invSize, final int elemAmt, final int page,
                                 final String baseTitle, final boolean pageNum, final boolean pageCount, final String correctTitle){
        PaginatingGUIContentsProvider contentsProvider = new PaginatingGUIContentsProvider() { //The object to test
            @Override
            public void genContents(Player viewer, GUISession session, Callback<List<GUIElement>> callback) {
                callback.call(genElementList(elemAmt));
            }

            @Override
            public void genBaseTitle(Player viewer, GUISession session, Callback<String> callback) {
                callback.call(baseTitle);
            }

            @Override
            public boolean showPageNumberInTitle() {
                return pageNum;
            }

            @Override
            public boolean showPageCountInTitle() {
                return pageCount;
            }
        };

        final GUIState guiState = new GUIState();
        InventoryGUI gui = Mockito.mock(InventoryGUI.class);
        Mockito.when(gui.getMaximumGUISize()).thenReturn(invSize);

        final GUISession session = new GUISession(gui, page, guiState);

        final Player viewer = Mockito.mock(Player.class);

        //Firest generate contents as it allows calculation of page count
        contentsProvider.genContents(viewer, page, session, new Callback<GUIContentsProvider.GUIContentsResponse>() {
            @Override
            public void call(GUIContentsProvider.GUIContentsResponse param) {
                //Do nothing
            }
        });

        //Then generate the title
        contentsProvider.genTitle(viewer, page, session, new Callback<String>() {
            @Override
            public void call(String title) { //Check if the title matches what it should
                Assert.assertNotNull(title);
                Assert.assertEquals(correctTitle, title);
            }
        });
    }

    private List<GUIElement> genElementList(int amount){
        List<GUIElement> res = new ArrayList<GUIElement>();
        for(int i =0;i<amount;i++){
            ItemStack it = new ItemStack(Material.WOOD);
            GUIElement elem = GUIElementFactory.createActionItem(
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
