package me.eddie.inventoryguiapi.gui.elements;

import me.eddie.inventoryguiapi.gui.events.GUIEvent;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an element in a GUI and defines it's behaviour.
 */
public interface GUIElement {
    /**
    Returns true if this element has a specific place it wants to be placed in a GUI or false if it doesn't matter. (Insertion order is then used)
     @return True if element has desired display position, False otherwise
     */
    public boolean hasDesiredDisplayPosition();

    /**
    Return the desired display position of this GUI Element in the inventory. If {@link #hasDesiredDisplayPosition()} is false this is ignored,
    otherwise it is used when calculating an inventory to be displayed. The slot number returned should be greater than or equal to 0 and less than
    the maximum size of the inventory it's being inserted into
     @return The desired display position of the GUIElement
     */
    public int getDesiredDisplayPosition();

    /**
     * This method defines the behaviour of this GUIElement. Implementations should handle the event appropriately for the element's behaviour (Including cancelling
     * the bukkit event responsible for the GUIEvent when necessary).
     * GUIElements will only ever receive events that interact with them, eg. click, place and pickup events.
     *
     * This happens in response to an Inventory event, so don't do anything during this call that the Bukkit API docs
     * don't recommend doing at this time.
     * @param event The GUIEvent to handle
     */
    public void onEvent(GUIEvent event);

    /**
     * This method returns the item (Bukkit) to be displayed in the inventory slot for a player. A returned value of null or an item of type AIR denotes that this slot in the inventory should be empty.
     * @param viewer The player viewing the inventory. In the case of SharedGUI's the viewer should be ignored.
     * @param session The GUISession that this GUIElement is being used with
     * @return The itemstack to display to the viewer
     */
    public ItemStack getDisplay(Player viewer, GUISession session);

    /**
     * Whether or not viewers can use Minecraft's auto-insert (Shift click) feature with this slot.
     * If they can then shift-clicking will be able to target this slot. Shift click events won't be passed directly
     * to the GUIElement though, they are instead turned into pickup and place events and those are passed to GUIElements
     * as appropriate.
     * @param viewer The viewer trying to auto insert into the slot
     * @param session The GUISession trying to auto insert into the slot
     * @return True if auto-insert is enabled and False if not
     */
    public boolean canAutoInsertIntoSlot(Player viewer, GUISession session);

}
