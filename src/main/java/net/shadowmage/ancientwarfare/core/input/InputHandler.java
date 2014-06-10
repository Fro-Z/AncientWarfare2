package net.shadowmage.ancientwarfare.core.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.gui.options.GuiOptions;
import net.shadowmage.ancientwarfare.core.interfaces.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.interfaces.IItemKeyInterface.ItemKey;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketItemInteraction;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.InputEvent.MouseInputEvent;

public class InputHandler
{

public static final String KEY_OPTIONS = "keybind.options";
public static final String KEY_ALT_ITEM_USE_0 = "keybind.alt_item_use_1";
public static final String KEY_ALT_ITEM_USE_1 = "keybind.alt_item_use_2";
public static final String KEY_ALT_ITEM_USE_2 = "keybind.alt_item_use_3";
public static final String KEY_ALT_ITEM_USE_3 = "keybind.alt_item_use_4";
public static final String KEY_ALT_ITEM_USE_4 = "keybind.alt_item_use_5";

public static final String KEY_NPC_ATTACK = "keybind.npc_command.attack";
public static final String KEY_NPC_MOVE = "keybind.npc_command.move";
public static final String KEY_NPC_HOME = "keybind.npc_command.home";
public static final String KEY_NPC_UPKEEP = "keybind.npc_command.upkeep";

private static InputHandler instance = new InputHandler();
public static InputHandler instance(){return instance;}
private InputHandler(){}

private HashMap<String, Keybind> keybindMap = new HashMap<String, Keybind>();
private HashMap<Integer, Set<Keybind>> bindsByKey = new HashMap<Integer, Set<Keybind>>();

Configuration config;
private static final String keybinds = AWCoreStatics.keybinds;

public void loadConfig(Configuration config)
  {
  this.config = config;
  registerKeybind(KEY_OPTIONS, Keyboard.KEY_F7);
  registerKeybind(KEY_ALT_ITEM_USE_0, Keyboard.KEY_Z);
  registerKeybind(KEY_ALT_ITEM_USE_1, Keyboard.KEY_X);
  registerKeybind(KEY_ALT_ITEM_USE_2, Keyboard.KEY_C);
  registerKeybind(KEY_ALT_ITEM_USE_3, Keyboard.KEY_V);
  registerKeybind(KEY_ALT_ITEM_USE_4, Keyboard.KEY_B);  
  InputCallback optionsCB = new InputCallback()
    {
    @Override
    public void onKeyReleased()
      {
     
      }
    @Override
    public void onKeyPressed()
      {
      Minecraft minecraft = Minecraft.getMinecraft();
      if(minecraft==null || minecraft.thePlayer==null || minecraft.currentScreen!=null)
        {       
        return;
        }
      minecraft.displayGuiScreen(new GuiOptions(new ContainerBase(minecraft.thePlayer, 0, 0, 0))); 
      }
    };
  addInputCallback(KEY_OPTIONS, optionsCB);
  
  addInputCallback(KEY_ALT_ITEM_USE_0, new ItemInputCallback(ItemKey.KEY_0));
  addInputCallback(KEY_ALT_ITEM_USE_1, new ItemInputCallback(ItemKey.KEY_1));
  addInputCallback(KEY_ALT_ITEM_USE_2, new ItemInputCallback(ItemKey.KEY_2));
  addInputCallback(KEY_ALT_ITEM_USE_3, new ItemInputCallback(ItemKey.KEY_3));
  addInputCallback(KEY_ALT_ITEM_USE_4, new ItemInputCallback(ItemKey.KEY_4)); 
  }

@SubscribeEvent
public void onMouseInput(MouseInputEvent evt)
  {
  
  }

@SubscribeEvent
public void onKeyInput(KeyInputEvent evt)
  {
  Minecraft minecraft = Minecraft.getMinecraft();
  if(minecraft==null){return;}
  EntityPlayer player = minecraft.thePlayer;
  if(player==null){return;}
  
  int key = Keyboard.getEventKey();
  boolean state = Keyboard.getEventKeyState();
  
  if(bindsByKey.containsKey(key))
    {    
    Set<Keybind> keys = bindsByKey.get(key);
    for(Keybind k : keys)
      {
      if(state)
        {
        k.onKeyPressed();
        }
      else
        {
        k.onKeyReleased();
        }
      }
    }  
  }

public void registerKeybind(String name, int keyCode)
  {
  if(!keybindMap.containsKey(name))
    {    
    int key = config.get(keybinds, name, keyCode).getInt(keyCode);
    Keybind k = new Keybind(name, key);
    keybindMap.put(name, k);
    if(!bindsByKey.containsKey(key))
      {
      bindsByKey.put(key, new HashSet<Keybind>());      
      }
    bindsByKey.get(key).add(k);
    }
  config.save();  
  }

public void reassignKeybind(String name, int newKey)
  {
  Keybind k = keybindMap.get(name);
  if(k==null){return;}
  
  config.get(keybinds, name, 0).set(newKey);
  reassignKeyCode(k, newKey);  
  config.save();  
  }

private void reassignKeyCode(Keybind k, int newKey)
  {
  bindsByKey.get(k.key).remove(k);
  k.key = newKey;
  
  if(!bindsByKey.containsKey(newKey))
    {
    bindsByKey.put(newKey, new HashSet<Keybind>());      
    }
  bindsByKey.get(newKey).add(k);
  }

public void addInputCallback(String name, InputCallback cb)
  {
  keybindMap.get(name).inputHandlers.add(cb);
  }

public Collection<Keybind> getKeybinds()
  {
  return keybindMap.values();
  }

public static final class Keybind
{
List<InputCallback> inputHandlers = new ArrayList<InputCallback>();

private int key;
private String name;

private Keybind(String name, int key)
  {
  this.name = name;
  this.key = key;
  }

public String getName()
  {
  return name;
  }

public int getKeyCode()
  {
  return key;
  }

public void onKeyPressed()
  {
  for(InputCallback c : inputHandlers)
    {
    c.onKeyPressed();
    }
  }

public void onKeyReleased()
  {
  for(InputCallback c : inputHandlers)
    {    
    c.onKeyReleased();
    }
  }

@Override
public String toString()
  {
  return "Keybind ["+key+","+name+"]";
  }
}

public static abstract class InputCallback
{
public abstract void onKeyPressed();
public abstract void onKeyReleased();
}

private static final class ItemInputCallback extends InputCallback
{
ItemKey key;
public ItemInputCallback(ItemKey key)
  {
  this.key = key;
  }

@Override
public void onKeyPressed()
  {
  Minecraft minecraft = Minecraft.getMinecraft();
  if(minecraft==null || minecraft.thePlayer==null || minecraft.currentScreen!=null)
    {       
    return;
    }
  ItemStack stack = minecraft.thePlayer.inventory.getCurrentItem();
  if(stack!=null && stack.getItem() instanceof IItemKeyInterface)
    {
    if(((IItemKeyInterface)stack.getItem()).onKeyActionClient(minecraft.thePlayer, stack, key))
      {
      PacketItemInteraction pkt = new PacketItemInteraction(0, key);
      NetworkHandler.sendToServer(pkt);
      }        
    }
  }

@Override
public void onKeyReleased(){}

}

}
