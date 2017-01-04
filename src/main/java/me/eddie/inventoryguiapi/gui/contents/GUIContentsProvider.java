package me.eddie.inventoryguiapi.gui.contents;

import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Determines what contents to show viewers on each page, the title of each page and if there is another page after this one.
 * Once the inventory for a given page has been opened the title won't be able to be changed without closing and re-opening the inventory,
 * this is due to a limitation in the Bukkit API and/or Minecraft.
 */
public interface GUIContentsProvider {

    /**
     * The maximum allowed title length of Minecraft
     */
    public static final int MAX_TITLE_LENGTH = 32;

    /**
     * The calculated result of a call to {@link #genContents(Player, int, GUISession, Callback)}
     * An instance of this object should be passed to {@link me.eddie.inventoryguiapi.util.Callback#call(Object)} on the callback provided with genContents.
     */
    public static class GUIContentsResponse {
        private boolean hasNextPage;
        private List<GUIElement> elements;

        /**
         * Create a new GUIContentsResponse
         * @param hasNextPage True if the GUI should display another page after this, False otherwise
         * @param elements The GUIElements to be displayed on this page of the GUI
         * @return The created GUIContentsResponse
         */
        public static GUIContentsResponse create(boolean hasNextPage, List<GUIElement> elements){ //Convenience method
            return new GUIContentsResponse(hasNextPage, elements);
        }

        /**
         * Create a new GUIContentsResponse
         * @param hasNextPage True if the GUI should display another page after this, False otherwise
         * @param elements The GUIElements to be displayed on this page of the GUI
         */
        public GUIContentsResponse(boolean hasNextPage, List<GUIElement> elements){
            this.hasNextPage = hasNextPage;
            if(elements == null){
                throw new IllegalArgumentException("Elements must not be null");
            }
            this.elements = elements;
        }

        public boolean hasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        public List<GUIElement> getElements() {
            return elements;
        }

        public void setElements(List<GUIElement> elements) {
            if(elements == null){
                throw new IllegalArgumentException("Elements must not be null");
            }
            this.elements = elements;
        }
    }

    /**
     * Calculate the GUIElements to display on this page and if another exists.
     * Once calculated the result (a GUIContentsResponse) should be passed to the provided callback.
     * If the callback isn't called then the GUI will not work properly.
     * @param viewer The viewer of this inventory. In the case of a GUI with multiple viewers (SharedInventoryGUI) this will be whatever viewer caused this method to be called
     * @param page The page to retrieve the contents for. Page 1 is the first page.
     * @param session The GUISession being viewed
     * @param callback The callback to pass the response to. If the callback isn't called then the GUI will not work properly. Use the {@link me.eddie.inventoryguiapi.util.Callback#call(Object)} method of the callback.
     */
    public void genContents(Player viewer, int page, GUISession session, Callback<GUIContentsResponse> callback);
    /**
     * Calculate the title to display for a given page.
     * Once calculated the result (a String) should be passed to the provided callback.
     * If the callback isn't called then the GUI will not work properly.
     * @param viewer The viewer of this inventory. In the case of a GUI with multiple viewers (SharedInventoryGUI) this will be whatever viewer caused this method to be called
     * @param page The page to retrieve the contents for. Page 1 is the first page.
     * @param session The GUISession being viewed
     * @param callback The callback to pass the response to. If the callback isn't called then the GUI will not work properly. Use the {@link me.eddie.inventoryguiapi.util.Callback#call(Object)} method of the callback.
     */
    public void genTitle(Player viewer, int page, GUISession session, Callback<String> callback);
}
