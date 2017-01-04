package me.eddie.inventoryguiapi.examples;

import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * An example of using the API to create a static inventory full
 * of items that when clicked execute commands.
 * This GUI is static in that it's contents never changes.
 */
public class CommandInventoryExample {
    private InventoryGUI gui; //The GUI we want to display

    public CommandInventoryExample(){
        createGUI(); //When object is created, create the GUI
    }

    //Initialize the GUI we want to display to people
    protected void createGUI(){
        gui = new GUIBuilder() //Construct a new GUIBuilder
                .guiStateBehaviour(GUIBuilder.GUIStateBehaviour.LOCAL_TO_SESSION) //The GUI's state is unique for each viewer
                .inventoryType(InventoryType.CHEST) //make this GUI use the CHEST layout
                .dynamicallyResizeToWrapContent(true) //Set the GUI to auto-resize the inventory to fit the contents
                .size(54) //Set the maximum size for each page to 54 slots
                .contents("Commands", //The title of the inventory
                        genContents(), //The contents to display in this inventory
                        true, //Whether or not to automatically paginate the contents
                        true, //Whether or not to show the page number in the inventory title
                        true //Whether ot not to show the page count in the inventory title
                    )
                .build();
    }

    public void showGUI(Player player){
        gui.open(player);
    }

    protected List<GUIElement> genContents(){ //Generate the list of GUIElements to display
        List<GUIElement> contents = new ArrayList<GUIElement>();
        //Add an item that when clicked sets time to day
        contents.add(createCommandElement("time set day", Material.GLOWSTONE,
                ChatColor.BLUE+"Make it day", ChatColor.WHITE+"Click to", ChatColor.WHITE+"make it day!"));
        //Add an item that when clicked sets time to night
        contents.add(createCommandElement("time set 15000", Material.COAL_BLOCK,
                ChatColor.BLUE+"Make it night", ChatColor.WHITE+"Click to", ChatColor.WHITE+"make it night!"));
        return contents;
    }

    protected GUIElement createCommandElement(final String command, Material material, String name, String... lore){
        return GUIElementFactory.createActionItem(
                //The item stack to display in this slot
                GUIElementFactory.formatItem(
                        new ItemStack(material), name, lore),
                //What to do when this GUIElement is clicked
                new Callback<Player>() {
                    @Override
                    public void call(final Player player) {
                                /*Close the inventory the player is viewing (The GUI).
                                * This is called in response to a click event, and according to bukkit docs we
                                * therefore should close the inventory being viewed in the next tick.
                                */
                        Bukkit.getScheduler().runTaskLater(InventoryGUIAPI.getInstance(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        player.closeInventory();
                                        //Run the command, now the inventory is closed
                                        player.performCommand(command);
                                    }
                                }, 1L);
                    }
                }
        );
    }
}
