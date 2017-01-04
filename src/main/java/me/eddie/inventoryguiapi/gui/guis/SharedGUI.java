package me.eddie.inventoryguiapi.gui.guis;

import me.eddie.inventoryguiapi.gui.contents.GUIContentsProvider;
import me.eddie.inventoryguiapi.gui.contents.GUIPopulator;
import me.eddie.inventoryguiapi.gui.events.GUIUpdateEvent;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.gui.session.GUIState;
import me.eddie.inventoryguiapi.gui.view.GUIPresenter;
import me.eddie.inventoryguiapi.plugin.EventCaller;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;

/**
 * Default implementation of a GUI designed to be shared between multiple viewers, with a persistent GUIState
 * The state of the GUI (GUIState) is local to the SharedGUI object, all viewers of this GUI will be viewing the
 * same persistent GUIState
 */
public class SharedGUI extends GUI implements SharedInventoryGUI {

    private GUIState guiState;

    /**
     * Create a new SharedGUI - One where all viewers see the same inventory state (So input slots, etc... are shared)
     * @param inventoryType The type of inventory to use in the created GUI
     * @param size The size (maximum if dynamic) to make the created GUI. Non-default sizes only supported with CHEST inventory type (Minecraft limitation)
     * @param isDynamicSize True if inventory should resize to wrap it's contents
     * @param contentsProvider The contents provider that dictates what this GUI should be showing to viewers of it
     * @param guiPopulator The GUIPopulator to use to position the GUIElements within the inventory to display
     * @param guiPresenter The GUIPresenter to use to present the computed inventory to the player
     * @param guiActionListeners Any ActionListeners that you want to specify. These receive GUIEvents before GUIElements do so that you can further customise the GUI's behaviour
     */
    public SharedGUI(InventoryType inventoryType, int size, boolean isDynamicSize, GUIContentsProvider contentsProvider,
                      GUIPopulator guiPopulator, GUIPresenter guiPresenter, GUIActionListener... guiActionListeners){
        super(inventoryType, size, isDynamicSize, contentsProvider, guiPopulator, guiPresenter, guiActionListeners);
        guiState = new GUIState();
    }

    public SharedGUI(InventoryType inventoryType, int size, boolean isDynamicSize, GUIContentsProvider contentsProvider,
                     GUIActionListener... guiActionListeners){
        this(inventoryType, size, isDynamicSize, contentsProvider, new GUIPopulator(), new GUIPresenter(), guiActionListeners);
    }

    @Override
    protected GUISession createNewSession(Player player, int page){
        return new GUISession(this,page,getGUIState());
    }

    @Override
    public GUIState getGUIState() {
        return this.guiState;
    }

    @Override
    protected void updateContentsAndView(final Player player, final GUISession session){
        if(session == null || !session.getInventoryGUI().equals(this)){
            return; //Session not for this GUI or not present
        }

        for(final Player pl:new ArrayList<Player>(Bukkit.getOnlinePlayers())) { //Find all players that are viewing this GUI
            final GUISession playerSession = GUISession.extractSession(pl);
            if (playerSession != null && playerSession.getInventoryGUI().equals(SharedGUI.this)) { //if player viewing this GUI
                guiPopulator.populateGUI(playerSession, pl, new Callback<Void>() {
                    @Override
                    public void call(Void param) {
                        updateView(pl, playerSession);
                    }
                });
            }
        }
    }

    @Override
    protected void updateView(final Player player, final GUISession session){ //Override to update for every viewing player
        if(session == null || !session.getInventoryGUI().equals(this)){
            return; //Session not for this GUI or not present
        }

        for(Player pl:new ArrayList<Player>(Bukkit.getOnlinePlayers())) { //Find all players that are viewing this GUI
            GUISession playerSession = GUISession.extractSession(pl);
            if (playerSession != null && playerSession.getInventoryGUI().equals(SharedGUI.this)) { //if player viewing this GUI
                guiPresenter.updateView(pl, playerSession); //Show the player the updated GUI
                GUIUpdateEvent evt = new GUIUpdateEvent(playerSession, pl);
                EventCaller.fireThroughBukkit(evt);
                fireEventThroughActionListeners(evt);
            }
        }
    }

    @Override
    public void updateContentsAndView() {
        for(Player pl:new ArrayList<Player>(Bukkit.getOnlinePlayers())){ //Find a player that is viewing this GUI
            GUISession session = GUISession.extractSession(pl);
            if(session != null && session.getInventoryGUI().equals(this)){ //if there is a player viewing this GUI
                updateContentsAndView(pl, session); //Call to update the view - this updates for all viewing players not just the one specified
                return;
            }
        }
    }

    @Override
    public void updateView() {
        for(Player pl:new ArrayList<Player>(Bukkit.getOnlinePlayers())){ //Find a player that is viewing this GUI
            GUISession session = GUISession.extractSession(pl);
            if(session != null && session.getInventoryGUI().equals(this)){ //if there is a player viewing this GUI
                updateView(pl, session); //Call to update the view - this updates for all viewing players not just the one specified
                return;
            }
        }
    }
}
