package me.eddie.inventoryguiapi.gui.guis;

import me.eddie.inventoryguiapi.gui.session.GUIState;

/**
 * Represents an InventoryGUI for players to view and interact with that's GUIState is shared between viewers.
 */
public interface SharedInventoryGUI extends InventoryGUI {
    /**
     * Get the shared GUIState that is shared by all viewers of this GUI
     * @return The shared GUIState
     */
    public GUIState getGUIState();
    /**
     * Recalculates the GUIElements to show the viewers (and what their display itemstacks are) and will update what the viewers see - if anybody is viewing this GUI.
     * If no players are viewing this GUI then this method will silently fail
     */
    public void updateContentsAndView();

    /**
     * Will update what the viewers of the GUI see to match the GUI's state
     */
    public void updateView();
}
