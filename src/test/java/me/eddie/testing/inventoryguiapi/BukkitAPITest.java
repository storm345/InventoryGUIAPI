package me.eddie.testing.inventoryguiapi;

import junit.framework.Assert;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by Edward on 28/12/2016.
 */
@RunWith(PowerMockRunner.class)
public class BukkitAPITest {
    @Test
    public void testPluginValidity(){
        //This asserts that our plugin class is a JavaPlugin. This is an easy way to check that spigot is in the build path correctly
        InventoryGUIAPI pluginClass = PowerMockito.mock(InventoryGUIAPI.class);
        Assert.assertTrue(pluginClass instanceof JavaPlugin);
    }
}
