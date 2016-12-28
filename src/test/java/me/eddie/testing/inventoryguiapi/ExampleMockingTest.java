package me.eddie.testing.inventoryguiapi;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by Edward on 28/12/2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PlayerJoinEvent.class)
public class ExampleMockingTest {

    @Test
    public void testOnPlayerJoin(){
        PlayerJoinEvent mockEvent = PowerMockito.mock(PlayerJoinEvent.class); //Use PowerMockito as getPlayer is a final method
        Player mockPlayer = Mockito.mock(Player.class);

        Mockito.when(mockPlayer.getName()).thenReturn("Pandarr"); //Edit the name of the mockPlayer returned
        Mockito.when(mockEvent.getPlayer()).thenReturn(mockPlayer); //Make this player part of the event

        //Do call to code to test
        mockEvent.getPlayer().sendMessage("HEY THERE");

        //Verify stuff
        Mockito.verify(mockPlayer).sendMessage(Mockito.anyString()); //Verify that any string was sent to the mock player

        //Use vertify(mockPlayer, Mockito.never()) to verify it DIDNT happen
    }
}
