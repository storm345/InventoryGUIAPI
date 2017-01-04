package me.eddie.inventoryguiapi.gui.contents;

import me.eddie.inventoryguiapi.gui.elements.ActionItem;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.events.GUIClickEvent;
import me.eddie.inventoryguiapi.gui.guis.GUI;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.InventoryState;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for Calculating the positions that GUIElements need to go into in the displayed inventory, adding page changing buttons if necessary and then
 * updating the current inventory state. To customize an inventory layout beyond what this is capable of, simply extend this class
 * and make your GUI use your extended version of GUIPopulator.
 */
public class GUIPopulator {

    /**
     * Gets, via GUI's contents provider, the items a player should be viewing; figures out what slots they should be
     * displayed in within the inventory; updates the current InventoryState with this and then calls back to the callback.
     * @param session The GUISession of the GUI being populated
     * @param viewer The viewer of the GUISession
     * @param callback Callback to be called back to on completion
     */
    public void populateGUI(final GUISession session, final Player viewer, final Callback<Void> callback){
        if(session == null || viewer == null){
            throw new IllegalArgumentException();
        }
        InventoryGUI iGui = session.getInventoryGUI();
        if(!(iGui instanceof GUI)){
            throw new IllegalArgumentException("The default GUIPopulator can only be used with the default GUI implementation");
        }
        final GUI gui = (GUI) iGui;
        final int page = session.getPage();
        final GUIContentsProvider contentsProvider = gui.getContentsProvider();
        contentsProvider.genContents(viewer, page, session, new Callback<GUIContentsProvider.GUIContentsResponse>() {
            @Override
            public void call(GUIContentsProvider.GUIContentsResponse contents) {
                Map<Integer, GUIElement> positions = new HashMap<Integer, GUIElement>();
                int maxSize = gui.getMaximumGUISize();
                if(page > 1 || contents.hasNextPage()){ //If this GUI should have page-changing controls
                    ActionItem prevPageButton = genPrevPageButton(gui, session);
                    prevPageButton.setDesiredDisplayPosition(maxSize-2);

                    ActionItem nextPageButton = genNextPageButton(gui, session);
                    nextPageButton.setDesiredDisplayPosition(maxSize-1);

                    positions.put(prevPageButton.getDesiredDisplayPosition(), prevPageButton);
                    positions.put(nextPageButton.getDesiredDisplayPosition(), nextPageButton);
                    maxSize -= 2; //Make room for next/prev page buttons
                }
                List<GUIElement> elements = new ArrayList<GUIElement>(contents.getElements()); //Clone as we will be removing items from this list as we go

                //Place first the GUIElements that has desired positions
                for(GUIElement elem:new ArrayList<GUIElement>(elements)){ //Iterate over cloned list since we are modifying it during the iteration (And not doing this would cause a concurrent modification exception)
                    if(elem.hasDesiredDisplayPosition() && elem.getDesiredDisplayPosition() < maxSize
                            && !positions.containsKey(elem.getDesiredDisplayPosition())){ //If this element has a desired position and it isn't taken
                        positions.put(elem.getDesiredDisplayPosition(), elem); //Place it into it's desired position
                        elements.remove(elem); //Remove element from list so that list only contains unplaced elements
                    }
                }

                //Place all the other GUIElements
                int vacantSlot = 0;
                topLoop:for(GUIElement element:new ArrayList<GUIElement>(elements)){ //Iterate over cloned list since we are modifying it during the iteration (And not doing this would cause a concurrent modification exception)
                    while(positions.containsKey(vacantSlot)){
                        vacantSlot++;
                        if(vacantSlot >= maxSize){
                            break topLoop; //Impossible to fit all the remaining GUIElements onto the page, so give up
                        }
                    }
                    positions.put(vacantSlot, element); //Place this element in the vacant slot
                    vacantSlot++; //Not strictly necessary but skips the containsKey check next iteration
                    elements.remove(element);
                }

                if(elements.size() > 0){
                    InventoryGUIAPI.getInstance().getLogger().warning(elements.size()+" GUIElements were unable to be placed into a GUI. The following stack trace should help you find what went wrong");
                    new Exception().printStackTrace();
                }

                final InventoryState inventoryState = session.getGUIState().getOrCreateInventoryState(page);
                inventoryState.setComputedContentsBySlot(positions);
                inventoryState.setHasNextPage(contents.hasNextPage());
                contentsProvider.genTitle(viewer, page, session, new Callback<String>() {
                    @Override
                    public void call(String title) {
                        if(title.length() > GUIContentsProvider.MAX_TITLE_LENGTH){ //If the title is too long
                            InventoryGUIAPI.getInstance().getLogger().warning("GUI title longer than maximum length ("+ GUIContentsProvider.MAX_TITLE_LENGTH+"):"+title+"! It has been truncated!");
                            title = title.substring(0, GUIContentsProvider.MAX_TITLE_LENGTH);
                        }
                        inventoryState.setTitle(title);
                        session.getGUIState().updateInventoryState(page, inventoryState);
                        callback.call(null);
                    }
                });
            }
        });
    }

    /**
     * Generate a new previous page button
     * @param gui The GUI to generate it for
     * @param session The Session to generate it for
     * @return An ActionItem, without desired position, that when clicked will go back a page in the GUI
     */
    public ActionItem genPrevPageButton(final InventoryGUI gui, final GUISession session){
        //Create back button display item
        ItemStack prevPageDisplay = GUIElementFactory.formatItem(
                new ItemStack(Material.PAPER),
                InventoryGUIAPI.getLanguageManager().getString("gui.button.prevPage.name"),
                InventoryGUIAPI.getLanguageManager().getString("gui.button.prevPage.lore"));

        return new ActionItem(prevPageDisplay, new ActionItem.ActionHandler() {
            @Override
            public void onClick(GUIClickEvent event) {
                int newPage = event.getSession().getPage() -1;
                if(newPage > 0) { //if a previous page exists
                    event.getSession().setPage(newPage); //Change the page being viewed
                    gui.updateContentsAndView(event.getViewer()); //Update the view
                }
            }
        });
    }

    /**
     * Generate a new next page button
     * @param gui The GUI to generate it for
     * @param session The Session to generate it for
     * @return An ActionItem, without desired position, that when clicked will go forward a page in the GUI
     */
    public ActionItem genNextPageButton(final InventoryGUI gui, final GUISession session){
        //Create next button display item
        ItemStack nextPageDisplay = GUIElementFactory.formatItem(
                new ItemStack(Material.PAPER),
                InventoryGUIAPI.getLanguageManager().getString("gui.button.nextPage.name"),
                InventoryGUIAPI.getLanguageManager().getString("gui.button.nextPage.lore"));

        return new ActionItem(nextPageDisplay, new ActionItem.ActionHandler() {
            @Override
            public void onClick(GUIClickEvent event) {
                int page = event.getSession().getPage();
                InventoryState inventoryState = event.getSession().getGUIState().getExistingInventoryState(page);
                if(inventoryState != null && inventoryState.hasNextPage()) { //If a next page exists
                    int newPage = page + 1;
                    event.getSession().setPage(newPage); //Change the page being viewed
                    gui.updateContentsAndView(event.getViewer()); //Update the view
                }
            }
        });
    }
}
