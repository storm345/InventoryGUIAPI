package me.eddie.testing.inventoryguiapi;

import me.eddie.inventoryguiapi.gui.elements.ActionItem;
import me.eddie.inventoryguiapi.gui.elements.GUIElementFactory;
import me.eddie.inventoryguiapi.gui.elements.InputSlot;
import me.eddie.inventoryguiapi.language.GUILanguageManager;
import me.eddie.inventoryguiapi.plugin.EventCaller;
import me.eddie.inventoryguiapi.plugin.InventoryGUIAPI;
import me.eddie.inventoryguiapi.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import javax.xml.ws.Holder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Utilities to mock classes for use when testing
 */
public class TestUtil {
    public static void mockPlugin() { //Mock our plugin class to behave like it is there and running
        PowerMockito.spy(InventoryGUIAPI.class); //Spy mocks only the methods we request
        InventoryGUIAPI fakePluginClass = Mockito.mock(InventoryGUIAPI.class); //Mock an instance of it
        Mockito.when(InventoryGUIAPI.getInstance()).thenReturn(fakePluginClass); //Alter the static method to return the instance
        GUILanguageManager languageManager = new GUILanguageManager(Locale.ENGLISH);
        Mockito.when(InventoryGUIAPI.getLanguageManager()).thenReturn(languageManager); //Return our instance
    }

    public static void mockItemStacks(){ //Mock ItemStack with our own version that doesn't require the server implementation
        try {
            //Mock all constructors of ItemStack to instead return our version
            PowerMockito.whenNew(ItemStack.class).withNoArguments().thenReturn(new FakeItemStack());
            PowerMockito.whenNew(ItemStack.class).withParameterTypes(ItemStack.class)
                    .withArguments(Mockito.isA(ItemStack.class)).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    ItemStack item = (ItemStack) args[0];
                    return new FakeItemStack(item);
                }
            });
            PowerMockito.whenNew(ItemStack.class).withParameterTypes(Material.class)
                    .withArguments(Mockito.isA(Material.class)).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    Material mat = (Material) args[0];
                    return new FakeItemStack(mat);
                }
            });
            PowerMockito.whenNew(ItemStack.class).withArguments(Mockito.any(Material.class), Mockito.anyInt()).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    Material mat = (Material) args[0];
                    int amt = (int) args[1];
                    return new FakeItemStack(mat, amt);
                }
            });
            PowerMockito.whenNew(ItemStack.class).withArguments(Mockito.any(Material.class), Mockito.anyInt(), Mockito.anyShort())
                    .thenAnswer(new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            Object[] args = invocation.getArguments();
                            Material mat = (Material) args[0];
                            int amt = (int) args[1];
                            short durability = (short) args[2];
                            return new FakeItemStack(mat, amt, durability);
                        }
                    });
            PowerMockito.whenNew(ItemStack.class).withArguments(Mockito.any(Material.class), Mockito.anyInt(), Mockito.anyShort(), Mockito.anyByte())
                    .thenAnswer(new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            Object[] args = invocation.getArguments();
                            Material mat = (Material) args[0];
                            int amt = (int) args[1];
                            short durability = (short) args[2];
                            Byte b = (Byte) args[3];
                            return new FakeItemStack(mat, amt, durability, b);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mockBukkitEventCalling() throws Exception {
        PowerMockito.mockStatic(EventCaller.class);
        PowerMockito.doNothing().when(EventCaller.class,"fireThroughBukkit",Mockito.any(Event.class));
    }

    public static void mockGUIElementFactory(){ //Mock format item, but get the other methods to call the real methods
        PowerMockito.mockStatic(GUIElementFactory.class);
        Mockito.when(GUIElementFactory.formatItem(Mockito.any(ItemStack.class), Mockito.anyString(), Mockito.any(String[].class)))
                .thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        //Gets the provided item, turns it into our FakeItemStack and then formats it and returns it
                        Object[] args = invocation.getArguments();
                        ItemStack base = (ItemStack) args[0];
                        String name = (String) args[1];
                        List<String> lore = new ArrayList<String>();
                        for(int i = 2;i<args.length;i++){
                            Object arg = args[i];
                            if(arg instanceof String[]){
                                lore.addAll(Arrays.asList((String[])args[i]));
                            }
                            else {
                                lore.add(arg+"");
                            }
                        }
                        ItemStack result = new FakeItemStack(base);
                        ItemMeta im = result.getItemMeta();
                        im.setDisplayName(name);
                        im.setLore(lore);
                        result.setItemMeta(im);
                        return result;
                    }
                });
        Mockito.when(GUIElementFactory.createActionItem(Mockito.anyInt(), Mockito.any(ItemStack.class), Mockito.any(ActionItem.ActionHandler.class)))
            .thenCallRealMethod();
        Mockito.when(GUIElementFactory.createActionItem(Mockito.anyInt(), Mockito.any(ItemStack.class), Mockito.any(Callback.class)))
                .thenCallRealMethod();
        Mockito.when(GUIElementFactory.createActionItem(Mockito.any(ItemStack.class), Mockito.any(ActionItem.ActionHandler.class)))
                .thenCallRealMethod();
        Mockito.when(GUIElementFactory.createActionItem(Mockito.any(ItemStack.class), Mockito.any(Callback.class)))
                .thenCallRealMethod();

        Mockito.when(GUIElementFactory.createInputSlot(Mockito.anyString(), Mockito.anyInt(), Mockito.any(InputSlot.ActionHandler.class)))
                .thenCallRealMethod();

        Mockito.when(GUIElementFactory.createInputSlot(Mockito.anyString(), Mockito.any(InputSlot.ActionHandler.class)))
                .thenCallRealMethod();
    }

    //Mock the bukkit scheduler so stuff that happens 'next tick' (Eg. close GUI and open new page of GUI) happens instantaneously.
    //Mock bukkit inventory creation so that it can be simulated
    public static void mockBukkitSchedulerAndInvCreation(){
        //Mock the bukkit scheduler so stuff that happens 'next tick' (Eg. close GUI and open new page of GUI) happens instantaneously.
        BukkitScheduler mockScheduler = Mockito.mock(BukkitScheduler.class);
        PowerMockito.mockStatic(Bukkit.class);
        Mockito.when(Bukkit.getScheduler()).thenReturn(mockScheduler);

        //Make stuff that happens later instead happen now
        Mockito.when(mockScheduler.runTaskLater(Mockito.any(Plugin.class), Mockito.any(Runnable.class), Mockito.anyLong()))
                .thenAnswer(new Answer<BukkitTask>() {
                    @Override
                    public BukkitTask answer(InvocationOnMock invocation) throws Throwable {
                        Runnable run = (Runnable) invocation.getArguments()[1]; //Get the action to perform
                        run.run(); //Do it now

                        BukkitTask task = Mockito.mock(BukkitTask.class);
                        return task; //Return a mock bukkit task
                    }
                });

        //Mock inventory creation so that we can use Bukkit.createInventory without issues
        Mockito.when(Bukkit.createInventory(Mockito.any(InventoryHolder.class), Mockito.anyInt(), Mockito.anyString()))
                .thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Inventory result = Mockito.mock(Inventory.class); //Return a mock inventory
                        Object[] args = invocation.getArguments();

                        InventoryHolder ih = (InventoryHolder) args[0];
                        Mockito.when(result.getHolder()).thenReturn(ih); //Set the holder of the mock inventory

                        int size = (int) args[1];
                        Mockito.when(result.getSize()).thenReturn(size); //Set the size of the mock inventory

                        String title = (String) args[2];
                        Mockito.when(result.getTitle()).thenReturn(title); //Set the title of the mock inv

                        Mockito.when(result.getType()).thenReturn(InventoryType.CHEST); //The inventory type of the mock inv

                        //Simulate a contents for this inv
                        final Holder<ItemStack[]> contents = new Holder<ItemStack[]>(new ItemStack[size]);
                        Mockito.when(result.getContents()).thenReturn(contents.value);
                        Mockito.when(result.getItem(Mockito.anyInt())).thenAnswer(new Answer<ItemStack>() {
                            @Override
                            public ItemStack answer(InvocationOnMock invocation) throws Throwable {
                                int slot = (int) invocation.getArguments()[0];

                                return contents.value[slot];
                            }
                        });
                        Mockito.doAnswer(new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                ItemStack[] contentsArr = (ItemStack[]) invocation.getArguments()[0];
                                contents.value = contentsArr;
                                return null;
                            }
                        }).when(result).setContents(Mockito.any(ItemStack[].class));
                        Mockito.doAnswer(new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                int slot = (int) invocation.getArguments()[0];
                                ItemStack item = (ItemStack) invocation.getArguments()[1];
                                contents.value[slot] = item;
                                return null;
                            }
                        }).when(result).setItem(Mockito.anyInt(), Mockito.any(ItemStack.class));
                        return result;
                    }
                });
        Mockito.when(Bukkit.createInventory(Mockito.any(InventoryHolder.class), Mockito.any(InventoryType.class), Mockito.anyString()))
                .thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Inventory result = Mockito.mock(Inventory.class); //Return a mock inventory
                        Object[] args = invocation.getArguments();

                        InventoryHolder ih = (InventoryHolder) args[0];
                        Mockito.when(result.getHolder()).thenReturn(ih); //Set the holder of the mock inventory

                        String title = (String) args[2];
                        Mockito.when(result.getTitle()).thenReturn(title); //Set the title of the mock inv

                        InventoryType type = (InventoryType) args[1];
                        Mockito.when(result.getSize()).thenReturn(type.getDefaultSize()); //Set the size of the mock inventory

                        Mockito.when(result.getType()).thenReturn(type); //The inventory type of the mock inv

                        //Simulate a contents for this inv
                        final Holder<ItemStack[]> contents = new Holder<ItemStack[]>(new ItemStack[type.getDefaultSize()]);
                        Mockito.when(result.getContents()).thenReturn(contents.value);
                        Mockito.when(result.getItem(Mockito.anyInt())).thenAnswer(new Answer<ItemStack>() {
                            @Override
                            public ItemStack answer(InvocationOnMock invocation) throws Throwable {
                                int slot = (int) invocation.getArguments()[0];

                                return contents.value[slot];
                            }
                        });
                        Mockito.doAnswer(new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                ItemStack[] contentsArr = (ItemStack[]) invocation.getArguments()[0];
                                contents.value = contentsArr;
                                return null;
                            }
                        }).when(result).setContents(Mockito.any(ItemStack[].class));
                        Mockito.doAnswer(new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                int slot = (int) invocation.getArguments()[0];
                                ItemStack item = (ItemStack) invocation.getArguments()[1];
                                contents.value[slot] = item;
                                return null;
                            }
                        }).when(result).setItem(Mockito.anyInt(), Mockito.any(ItemStack.class));
                        return result;
                    }
                });
    }

    public static void mockServer() throws NoSuchFieldException, IllegalAccessException {
        Field server = Bukkit.class.getDeclaredField("server");
        server.setAccessible(true);
        Server serverMock = Mockito.mock(Server.class);
        server.set(null, serverMock);
        server.setAccessible(false);
    }
}
