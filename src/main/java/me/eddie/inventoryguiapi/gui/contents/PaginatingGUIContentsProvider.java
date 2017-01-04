package me.eddie.inventoryguiapi.gui.contents;

import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of GUIContentsProvider that will handle paginating a provided list of GUIElements. To use this, extend the class
 * Determines what contents to show viewers and the title of each page.
 * Once the inventory for a given page has been opened the title won't be able to be changed without closing and re-opening the inventory,
 * this is due to a limitation in the Bukkit API and/or Minecraft.
 *
 */
public abstract class PaginatingGUIContentsProvider implements GUIContentsProvider {

    private static final String PAGE_COUNT_ATTRIBUTE = "gui.pageCount";

    /**
     * Calculate the GUIElements to display in this GUI.
     * Once calculated the result (a List of GUIElements) should be passed to the provided callback.
     * If the callback isn't called then the GUI will not work properly.
     * @param viewer The viewer of this inventory. In the case of a GUI with multiple viewers (SharedInventoryGUI) this will be whatever viewer caused this method to be called
     * @param session The GUISession being viewed
     * @param callback The callback to pass the response to. If the callback isn't called then the GUI will not work properly. Use the {@link me.eddie.inventoryguiapi.util.Callback#call(Object)} method of the callback.
     */
    public abstract void genContents(Player viewer, GUISession session, Callback<List<GUIElement>> callback);
    /**
     * Calculate the title to display for this GUI. The title may then be extended with the page number.
     * Once calculated the result (a String) should be passed to the provided callback.
     * If the callback isn't called then the GUI will not work properly.
     * @param viewer The viewer of this inventory. In the case of a GUI with multiple viewers (SharedInventoryGUI) this will be whatever viewer caused this method to be called
     * @param session The GUISession being viewed
     * @param callback The callback to pass the response to. If the callback isn't called then the GUI will not work properly. Use the {@link me.eddie.inventoryguiapi.util.Callback#call(Object)} method of the callback.
     */
    public abstract void genBaseTitle(Player viewer, GUISession session, Callback<String> callback);

    /**
     * Whether or not to show the page number in the title of the inventory.
     * @return True if the page number should be shown in the inventory title, False otherwise
     */
    public abstract boolean showPageNumberInTitle();
    /**
     * Whether or not to show the page count in the title of the inventory.
     * @return True if the page count should be shown in the inventory title, False otherwise
     */
    public abstract boolean showPageCountInTitle();

    private void setPageCountAttribute(GUIState guiState, int pageCount){
        if(guiState != null){
            guiState.putAttribute(PAGE_COUNT_ATTRIBUTE, pageCount);
        }
    }

    private int getPageCount(GUIState guiState){
        if(guiState != null){
            Object o = guiState.getAttribute(PAGE_COUNT_ATTRIBUTE);
            if(o == null){
                return -1;
            }
            return (int) o;
        }
        return -1;
    }

    @Override
    public void genContents(Player viewer, final int page, final GUISession session, final Callback<GUIContentsResponse> callback) {
        if(viewer == null || page < 1 || session == null || callback == null){
            throw new IllegalArgumentException("Invalid arguments");
        }
        genContents(viewer, session, new Callback<List<GUIElement>>() {
            @Override
            public void call(List<GUIElement> elements) {
                elements = new ArrayList<GUIElement>(elements); //Use a copy of the list so that the list they're given us is not modified
                InventoryGUI gui = session.getInventoryGUI();
                int maxSize = gui.getMaximumGUISize();
                if(elements.size() <= maxSize){
                    setPageCountAttribute(session.getGUIState(), 1);
                    if(page == 1) {
                        callback.call(GUIContentsResponse.create(false, elements));
                    }
                    else {
                        callback.call(GUIContentsResponse.create(false, new ArrayList<GUIElement>()));
                    }
                    return;
                }
                int pageCount = (int) Math.ceil(elements.size() / ((double)maxSize-2)); //Cast maxSize to double so that the division is correct (Not rounded)
                setPageCountAttribute(session.getGUIState(), pageCount);
                maxSize = maxSize - 2; //Make room for next/prev page buttons
                int startIndex = (page-1) * maxSize; //0 is first element since startIndex is inclusive
                int endIndex = Math.min(startIndex+maxSize, elements.size()); //Exclusive bound

                if(startIndex >= elements.size()){
                    callback.call(GUIContentsResponse.create(false, new ArrayList<GUIElement>()));
                    return;
                }

                List<GUIElement> elementsToDisplay = elements.subList(startIndex, endIndex);
                boolean hasNextPage = endIndex < elements.size(); //If the end of our sublist isn't the end of the complete list
                callback.call(GUIContentsResponse.create(hasNextPage, elementsToDisplay));
            }
        });
    }

    @Override
    public void genTitle(Player viewer, final int page, final GUISession session, final Callback<String> callback) {
        genBaseTitle(viewer, session, new Callback<String>() {
            @Override
            public void call(String base) {
                //Format it with page count and number
                if(!showPageCountInTitle() && !showPageNumberInTitle()){ //No page number or count
                    callback.call(base);
                    return;
                }
                int pageCount = getPageCount(session.getGUIState());
                if(showPageCountInTitle() && !showPageNumberInTitle()){ //Just count
                    callback.call(InventoryGUIAPI.getLanguageManager().getFormattedString("gui.title.withPageCount", base, pageCount));
                    return;
                }
                else if(!showPageCountInTitle() && showPageNumberInTitle()){ //Just page number
                    callback.call(InventoryGUIAPI.getLanguageManager().getFormattedString("gui.title.withPageNumber", base, page));
                    return;
                }
                //Must therefore want both page count and page number
                callback.call(InventoryGUIAPI.getLanguageManager().getFormattedString("gui.title.withPageNumberAndCount", base, page, pageCount));
                return;
            }
        });
    }
}
