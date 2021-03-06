package net.shadowmage.ancientwarfare.core.gamedata;

import java.util.HashMap;

import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;
import net.shadowmage.ancientwarfare.core.config.AWLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class AWGameData
{

public static final AWGameData INSTANCE = new AWGameData();

private HashMap<String, Class <? extends WorldSavedData>> dataClasses = new HashMap<String, Class <? extends WorldSavedData>>();

@SubscribeEvent
public void onWorldLoad(WorldEvent.Load evt)
  {
  World world = evt.world;
  if(world.isRemote){return;}
  for(String name : dataClasses.keySet())
    {
    world.mapStorage.loadData(dataClasses.get(name), name);
    }
  }

public void registerSaveData(String name, Class <? extends WorldSavedData> clz)
  {
  dataClasses.put(name, clz);
  }

@SuppressWarnings("unchecked")
public <T extends WorldSavedData> T getData(String name, World world, Class <T> clz)
  {
  if(!WorldSavedData.class.isAssignableFrom(clz)){return null;}
  if(!dataClasses.containsKey(name))
    {
    AWLog.logError("Attempt to load unregistered data class: "+clz +" for name: "+name);
    return null;
    }  
  T data = (T) world.mapStorage.loadData(clz, name);
  if(data==null)
    {
    try
      {
      data = (T) clz.newInstance();
      world.mapStorage.setData(name, (WorldSavedData) data);
      return data;
      } 
    catch (InstantiationException e)
      {
      AWLog.logError("Attempt to load data class: "+clz +" for name: "+name +" failed because class needs a no-param constructor!");
      e.printStackTrace();
      return null;
      } 
    catch (IllegalAccessException e)
      {
      e.printStackTrace();
      return null;
      }
    }
  return data;
  }

}
