package me.eddie.testing.inventoryguiapi;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Implementation of ItemStack to use for testing
 */
public class FakeItemStack extends ItemStack{
    public FakeItemStack(){
        super();
    }

    public FakeItemStack(ItemStack stack){
        this(stack.getType(), stack.getAmount(), stack.getDurability(), stack.getData().getData());
        this.im = stack instanceof FakeItemStack ? new FakeItemMeta(stack.getItemMeta()) : new FakeItemMeta();
    }

    public FakeItemStack(Material material){
        super(material);
    }

    public FakeItemStack(Material material, int amount){
        super(material, amount);
    }

    public FakeItemStack(Material material, int amount, short damage){
        super(material, amount, damage);
    }

    public FakeItemStack(Material material, int amount, short damage, Byte data){
        super(material, amount, damage, data);
    }

    private ItemMeta im = new FakeItemMeta();

    @Override
    public boolean hasItemMeta(){
        return true;
    }

    @Override
    public ItemMeta getItemMeta(){
        return this.im.clone();
    }

    @Override
    public boolean setItemMeta(ItemMeta im){
        this.im = im;
        return true;
    }
}
