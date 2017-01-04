package me.eddie.inventoryguiapi.plugin;

import me.eddie.inventoryguiapi.examples.CommandInventoryExample;
import me.eddie.inventoryguiapi.examples.TradeInventoryExample;
import me.eddie.inventoryguiapi.gui.contents.PaginatingGUIContentsProvider;
import me.eddie.inventoryguiapi.gui.elements.GUIElement;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.elements.InputSlot;
import me.eddie.inventoryguiapi.gui.events.GUIEvent;
import me.eddie.inventoryguiapi.gui.events.GUIMiscClickEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPickupItemEvent;
import me.eddie.inventoryguiapi.gui.events.GUIPlaceItemEvent;
import me.eddie.inventoryguiapi.gui.guis.GUIActionListener;
import me.eddie.inventoryguiapi.gui.guis.GUIBuilder;
import me.eddie.inventoryguiapi.gui.guis.InventoryGUI;
import me.eddie.inventoryguiapi.gui.session.GUISession;
import me.eddie.inventoryguiapi.util.Callback;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A command to test the GUIs via
 */
public class TestCommandExecutor implements CommandExecutor {

    public TestCommandExecutor(){

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if(sender instanceof Player) {
            if(args.length > 0){
                Player other = Bukkit.getPlayer(args[0]);
                if(other != null){
                    TradeInventoryExample tradeInventoryExample = new TradeInventoryExample((Player) sender, other);
                    tradeInventoryExample.startTrade();
                    return true;
                }
            }
            new CommandInventoryExample().showGUI((Player) sender);
        }
        return true;
    }
}
