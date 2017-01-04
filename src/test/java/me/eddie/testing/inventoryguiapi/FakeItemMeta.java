package me.eddie.testing.inventoryguiapi;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Implementation of ItemMeta to use for testing (As the API doesn't provide an implementation)
 */
public class FakeItemMeta implements ItemMeta {
    private String displayName = null;
    private List<String> lore = new ArrayList<String>();
    private Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
    private List<ItemFlag> itemFlags = new ArrayList<ItemFlag>();
    private boolean unbreakable = false;

    public FakeItemMeta(){

    }

    public FakeItemMeta(ItemMeta im){
        this.displayName = im.getDisplayName();
        this.lore = new ArrayList<String>();
        if(im.hasLore()){
            this.lore.addAll(im.getLore());
        }
        this.enchants = new HashMap<Enchantment, Integer>();
        if(im.hasEnchants()){
            this.enchants.putAll(im.getEnchants());
        }
        this.itemFlags = new ArrayList<ItemFlag>();
        this.itemFlags.addAll(im.getItemFlags());
        this.unbreakable = unbreakable;
    }

    public FakeItemMeta(String displayName, List<String> lore, Map<Enchantment, Integer> enchants, List<ItemFlag> flags, boolean unbreakable){
        this.displayName = displayName;
        this.lore = new ArrayList<String>(lore);
        this.enchants = new HashMap<Enchantment, Integer>(enchants);
        this.itemFlags = new ArrayList<ItemFlag>(flags);
        this.unbreakable = unbreakable;
    }

    @Override
    public boolean hasDisplayName() {
        return displayName != null;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    @Override
    public boolean hasLore() {
        return this.lore != null && this.lore.size() > 0;
    }

    @Override
    public List<String> getLore() {
        return Collections.unmodifiableList(this.lore);
    }

    @Override
    public void setLore(List<String> lore) {
        this.lore = new ArrayList<String>(lore);
    }

    @Override
    public boolean hasEnchants() {
        return enchants.size() > 0;
    }

    @Override
    public boolean hasEnchant(Enchantment ench) {
        return enchants.containsKey(ench);
    }

    @Override
    public int getEnchantLevel(Enchantment ench) {
        if(!hasEnchant(ench)){
            return 0;
        }
        return enchants.get(ench);
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return new HashMap<Enchantment, Integer>(enchants);
    }

    @Override
    public boolean addEnchant(Enchantment ench, int level, boolean ignoreLevelRestriction) {
        enchants.put(ench, level);
        return true;
    }

    @Override
    public boolean removeEnchant(Enchantment ench) {
        enchants.remove(ench);
        return true;
    }

    @Override
    public boolean hasConflictingEnchant(Enchantment ench) {
        return false;
    }

    @Override
    public void addItemFlags(ItemFlag... itemFlags) {
        this.itemFlags.addAll(Arrays.asList(itemFlags));
    }

    @Override
    public void removeItemFlags(ItemFlag... itemFlags) {
        this.itemFlags.removeAll(Arrays.asList(itemFlags));
    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        return new HashSet<ItemFlag>(itemFlags);
    }

    @Override
    public boolean hasItemFlag(ItemFlag flag) {
        for(ItemFlag itemFlag:itemFlags){
            if(itemFlag.equals(flag)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public ItemMeta clone() {
        return new FakeItemMeta(displayName, lore, enchants, itemFlags, unbreakable);
    }

    @Override
    public Spigot spigot() {
        return new ItemMeta.Spigot(){
        };
    }

    @Override
    public Map<String, Object> serialize() {
        //Not used anywhere, so no point implementing
        return null;
    }
}
