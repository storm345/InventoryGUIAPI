package me.eddie.inventoryguiapi.gui.guis;

import me.eddie.inventoryguiapi.gui.contents.GUIContentsProvider;
import me.eddie.inventoryguiapi.gui.contents.GUIPopulator;
import me.eddie.inventoryguiapi.gui.contents.PaginatingGUIContentsProvider;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.view.GUIPresenter;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for the constructing an instance of one of the default GUI implementations
 */
public class GUIBuilder {
    private GUIStateBehaviour guiStateBehaviour = null;
    private InventoryType inventoryType = InventoryType.CHEST;
    private int size = GUI.AUTO_SET_SIZE;
    private boolean isDynamicSize = false;
    private GUIContentsProvider contentsProvider = null;
    private GUIPopulator guiPopulator = null;
    private GUIPresenter guiPresenter = null;
    private GUIActionListener[] actionListeners = new GUIActionListener[]{};

    /**
     * Create a new GUIBuilder, used for building a new InventoryGUI that uses the default GUI implementation
     */
    public GUIBuilder(){

    }

    /**
     * Enum that defines how a GUI's GUIState should be handled
     */
    public static enum GUIStateBehaviour {
        /**
         * This value will mean the default {@link GUI} implementation is used,
         * with this implementation the GUIState is lost when the session ends (GUI closes).
         */
        LOCAL_TO_SESSION,
        /**
         * This value will mean the default {@link SharedGUI} implementation is used,
         * with this implementation the GUIState is bound to the GUI and is persistent. This means all viewers will see the
         * same GUIState and it persists beyond the GUI closing.
         */
        BOUND_TO_GUI;
    }

    /**
     * Define how the GUIState should be handled.
     *
     * A value of {@link GUIStateBehaviour#LOCAL_TO_SESSION} will mean the default {@link GUI} implementation is used,
     * with this implementation the GUIState is lost when the session ends (GUI closes).
     *
     * A value of {@link GUIStateBehaviour#BOUND_TO_GUI} will mean the default {@link SharedGUI} implementation is used,
     * with this implementation the GUIState is bound to the GUI and is persistent. This means all viewers will see the
     * same GUIState and it persists beyond the GUI closing.
     *
     * @param behaviour How the GUIState should behave
     * @return Returns self
     */
    public GUIBuilder guiStateBehaviour(GUIStateBehaviour behaviour){
        if(behaviour == null){
            throw new IllegalArgumentException("GUIStateBehaviour must not be null!");
        }
        this.guiStateBehaviour = behaviour;
        return this; //Common Builder pattern thing that allows code calling builders to easily fit into 1 line.
    }

    /**
     * Specify a custom GUIPresenter to use with this GUI
     *
     * A GUIPresenter takes the calculated InventoryState that the GUI should currently be displaying and displays it
     * to the end-user. A custom GUIPresenter will allow you to customize how your GUI is viewed. For almost all use cases
     * it is recommended to use the default GUIPresenter (Don't call this method)
     * @param presenter The custom GUIPresenter to use, or null (or not specified) if you want the default one
     * @return Returns self
     */
    public GUIBuilder presenter(GUIPresenter presenter){
        this.guiPresenter = presenter;
        return this;
    }

    /**
     * Specify a custom GUIPopulator to use with this GUI.
     *
     * A GUIPopulator calculates the final position that each element should be in within the displayed GUI before it
     * is rendered. Specify a custom GUIPopulator to make your GUI use a complex custom layout. (Simple layout changes
     * can be done by manipulating the desired slots of GUIElements)
     * @param populator The custom GUIPopulator to use, or null (Or not specified) if you want the default one
     * @return Returns self
     */
    public GUIBuilder populator(GUIPopulator populator){
        this.guiPopulator = populator;
        return this;
    }

    /**
     * Specify action listeners for this GUI.
     * ActionListeners receive GUI events (Eg. open, close, place item) before GUIElements do (But after they're
     * fired through the Bukkit event system). These are ideal for adding GUI specific event handling. For example
     * you may want to, when a GUI closes, save what is input into an InputSlot.
     * @param actionListeners The ActionListeners that should listen to actions with this GUI
     * @return Returns self
     */
    public GUIBuilder actionListeners(GUIActionListener... actionListeners){
        if(actionListeners == null){
            throw new IllegalArgumentException("GUI ActionListener list must not be null!");
        }
        this.actionListeners = actionListeners;
        return this;
    }

    /**
     * Set the size this GUI should be for each page.
     * With a dynamically re-sizing GUI this is the maximum size
     * @param size The size or maximum size of this GUI
     * @return Returns self
     */
    public GUIBuilder size(int size){
        if(size < 1 && size != GUI.AUTO_SET_SIZE){
            throw new IllegalArgumentException("Size of the GUI must be at least 1! If in doubt, don't specify a size and the default will be used!");
        }
        this.size = size;
        return this;
    }

    /**
     * Set whether or not this inventory should automatically re-size to wrap it's contents. (Up to the maximum size).
     * When automatic re-sizing is enabled, the size specified to the builder is treated as the maximum size of a GUI page.
     * @param resize True if GUI should dynamically resize
     * @return Returns self
     */
    public GUIBuilder dynamicallyResizeToWrapContent(boolean resize){
        if(!inventoryType.equals(InventoryType.CHEST) && resize){
            throw new IllegalArgumentException("GUI Cannot have dynamic size and have a non-chest inventory type!");
        }
        this.isDynamicSize = resize;
        return this;
    }

    /**
     * Sets the type of the inventory the GUI displays.
     * This must be type CHEST if you wish to have a dynamically sized GUI
     * @param inventoryType The type of inventory
     * @return Returns self
     */
    public GUIBuilder inventoryType(InventoryType inventoryType){
        if(inventoryType == null){
            throw new IllegalArgumentException("GUI Inventory Type cannot be null!");
        }
        if(!inventoryType.equals(InventoryType.CHEST) && isDynamicSize){
            throw new IllegalArgumentException("GUI Cannot have dynamic size and have a non-chest inventory type!");
        }

        this.inventoryType = inventoryType;
        return this;
    }

    /**
     * Specify what GUIElements this GUI should display and the title of the GUI. This is useful for static GUIs.
     * For GUIs that need to show different GUIElements to different viewers, or that need GUISession information to
     * determine which GUIElements to display a GUIContentsProvider should be used instead.
     * @param title The title of the GUI
     * @param elements The list of elements to display
     * @param paginate Whether or not to automatically split these GUIElements into pages
     * @param showPageNum Whether or not to show the page number in the title, ignored if paginate is False
     * @param showPageCount Whether or not to show the page count in the title, ignored if paginate is False
     * @return Returns self
     */
    public GUIBuilder contents(final String title, final List<GUIElement> elements,
                               boolean paginate, final boolean showPageNum, final boolean showPageCount){
        if(title == null){
            throw new IllegalArgumentException("GUI Title cannot be null!");
        }
        if(title.length() > GUIContentsProvider.MAX_TITLE_LENGTH){
            throw new IllegalArgumentException("GUI Title too long for Minecaft! Max length is "+GUIContentsProvider.MAX_TITLE_LENGTH);
        }
        if(elements == null){
            throw new IllegalArgumentException("GUI list of elements cannot be null!");
        }
        if(paginate){
            return paginatingContentsProvider(new PaginatingGUIContentsProvider() {
                @Override
                public void genContents(Player viewer, GUISession session, Callback<List<GUIElement>> callback) {
                    callback.call(elements);
                }

                @Override
                public void genBaseTitle(Player viewer, GUISession session, Callback<String> callback) {
                    callback.call(title);
                }

                @Override
                public boolean showPageNumberInTitle() {
                    return showPageNum;
                }

                @Override
                public boolean showPageCountInTitle() {
                    return showPageCount;
                }
            });
        }
        return contentsProvider(new GUIContentsProvider() {
            @Override
            public void genContents(Player viewer, int page, GUISession session, Callback<GUIContentsResponse> callback) {
                if(page == 1){
                    callback.call(GUIContentsResponse.create(false, elements));
                    return;
                }
                callback.call(GUIContentsResponse.create(false, new ArrayList<GUIElement>()));
            }

            @Override
            public void genTitle(Player viewer, int page, GUISession session, Callback<String> callback) {
                callback.call(title);
            }
        });
    }

    /**
     * Specify the PaginatingGUIContentsProvider this GUI should use.
     * A contents provider defines what GUIElements should be displayed on each GUI page and for which viewer.
     * This method is the same as calling {@link #contentsProvider(GUIContentsProvider)} with a PaginatingContentsProvider
     *
     * PaginatingContentsProvider is an abstract class that handles splitting a list of GUIElements into pages to display.
     * This is useful if you have a list of GUIElemenets to display and want them automatically sorted into pages for you.
     * To use PaginatingContentsProvider, extend the class and implement the methods marked as abstract.
     *
     * @param contentsProvider The PaginatingContentsProvider for this GUI
     * @return Returns self
     */
    public GUIBuilder paginatingContentsProvider(PaginatingGUIContentsProvider contentsProvider){
        return contentsProvider(contentsProvider);
    }

    /**
     * Specify the GUIContentsProvider this GUI should use.
     * A contents provider defines what GUIElements should be displayed on each GUI page and for which viewer.
     * @param contentsProvider The ContentsProvider for this GUI
     * @return Returns self
     */
    public GUIBuilder contentsProvider(GUIContentsProvider contentsProvider){
        if(contentsProvider == null){
            throw new IllegalArgumentException("GUIContentsProvider must not be null!");
        }
        this.contentsProvider = contentsProvider;
        return this;
    }

    public InventoryGUI build(){
        if(guiStateBehaviour == null){
            throw new IllegalArgumentException("GUIStateBehaviour must not be null! Please specify it before building!");
        }

        if(contentsProvider == null){
            throw new IllegalArgumentException("GUIContentsProvider must not be null! Please specify it before building!");
        }

        if(inventoryType == null){
            throw new IllegalArgumentException("InventoryType must not be null!");
        }

        if(!inventoryType.equals(InventoryType.CHEST) && isDynamicSize){
            throw new IllegalArgumentException("Inventory can only be dynamic size when CHEST InventoryType is used!");
        }

        if(guiPopulator == null){
            guiPopulator = new GUIPopulator(); //The default populator
        }

        if(guiPresenter == null){
            guiPresenter = new GUIPresenter(); //The default presenter
        }

        switch(guiStateBehaviour){
            case LOCAL_TO_SESSION: {
                return new GUI(inventoryType, size, isDynamicSize, contentsProvider, guiPopulator, guiPresenter, actionListeners);
            }
            case BOUND_TO_GUI: {
                return new SharedGUI(inventoryType, size, isDynamicSize, contentsProvider, guiPopulator, guiPresenter, actionListeners);
            }
        }

        return null;
    }
}
