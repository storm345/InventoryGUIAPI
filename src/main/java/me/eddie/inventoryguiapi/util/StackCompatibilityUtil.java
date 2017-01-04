package me.eddie.inventoryguiapi.util;

import org.bukkit.inventory.ItemStack;

/**
 * Checks if two items are capable of stacking
 */
public class StackCompatibilityUtil {
    public static boolean canStack(ItemStack... items){
        if(items.length < 1){
            return true;
        }
        ItemStack anItem = items[0]; //Check if all items are stackable against this stack
        for(ItemStack item:items){
            if(!item.isSimilar(anItem)){
                return false;
            }
        }
        return true;
    }
}
