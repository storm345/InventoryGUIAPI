package me.eddie.inventoryguiapi.util;

import org.bukkit.event.inventory.InventoryType;

import java.util.Arrays;
import java.util.List;

/**
 * Internally used to validate if GUI settings are valid
 */
public class GUISettingValidation {
    private static List<InventoryType> disallowedTypes = Arrays.asList(new InventoryType[]{
        InventoryType.CRAFTING, InventoryType.CREATIVE, InventoryType.PLAYER
    });

    /**
     * Check if an InventoryType is allowed to be used in a GUI
     * @param inventoryType The InventoryType wanting to be used
     * @return True if can be used, False otherwise
     */
    public static boolean isAllowed(InventoryType inventoryType){
        return !disallowedTypes.contains(inventoryType);
    }

    /**
     * Check if the provided GUI Settings are valid
     * @param maxSize The maxSize of the GUI
     * @param autoResize Whether or not the GUI wishes to auto-resize
     * @param inventoryType The type of inventory to be used with the GUI
     * @return True if can be used, False otherwise
     */
    public static boolean isValid(int maxSize, boolean autoResize, InventoryType inventoryType){
        switch(inventoryType){
            case CHEST: {
                return maxSize > 0 && maxSize % 9 == 0 && maxSize <= 54;
            }
            case DISPENSER: {
                return maxSize == 9 && !autoResize;
            }
            case DROPPER: {
                return maxSize == 9 && !autoResize;
            }
            case FURNACE: {
                return maxSize == 3 && !autoResize;
            }
            case WORKBENCH: {
                return maxSize == 10 && !autoResize;
            }
            case CRAFTING: {
                return maxSize == 5 && !autoResize;
            }
            case ENCHANTING: {
                return maxSize == 2 && !autoResize;
            }
            case BREWING: {
                return maxSize == 4 && !autoResize;
            }
            case PLAYER: {
                return maxSize == 41 && !autoResize;
            }
            case CREATIVE: {
                return maxSize == 5 && !autoResize;
            }
            case MERCHANT: {
                return maxSize == 3 && !autoResize;
            }
            case ENDER_CHEST: {
                return maxSize == 27 && !autoResize;
            }
            case ANVIL: {
                return maxSize == 3 && !autoResize;
            }
            case BEACON: {
                return maxSize == 1 && !autoResize;
            }
            case HOPPER: {
                return maxSize == 5 && !autoResize;
            }
        }
        return true;
    }
}
