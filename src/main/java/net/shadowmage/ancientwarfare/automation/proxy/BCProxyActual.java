package net.shadowmage.ancientwarfare.automation.proxy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.shadowmage.ancientwarfare.core.config.AWLog;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueGenerator;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueReceiver;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueTile;
import buildcraft.api.mj.IBatteryIOObject;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.IOMode;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;

public class BCProxyActual extends BCProxy
{

public static class TorqueMJBattery implements IBatteryIOObject
{
ITorqueTile tile;
IOMode mode;
ForgeDirection dir;
public TorqueMJBattery(ITorqueTile tile, IOMode mode, ForgeDirection dir)
  {
  this.tile = tile;
  this.mode = mode;
  this.dir = dir;
  }

@Override
public double getEnergyRequested()
  {
  return tile.getMaxEnergy()-tile.getEnergyStored();
  }

@Override
public double addEnergy(double mj)
  {
  return addEnergy(mj, false);
  }

@Override
public double addEnergy(double mj, boolean ignoreCycleLimit)
  {
  /**
   * also, NOOP the ignoreCycleLimit -- theres fucking limits for a goddamn reason.
   * 
   * ALSO  FUCK THIS STUPID BATTERY BULLSHIT __ IT SHOULD NOT BE THIS HARD TO CREATE A GODDAMN BATTERY INTERFACE
   * WHY THE FUCK DOES BC NOT DO PROPER SIDED ENERGY INPUT/OUTPUT HANDLING?
   */
  if(tile instanceof ITorqueReceiver)
    {
    return ((ITorqueReceiver) tile).addEnergy(dir, mj);
    }
  return 0;
  }

@Override
public double getEnergyStored()
  {
  return tile.getEnergyStored();
  }

@Override
public void setEnergyStored(double mj)
  {
  tile.setEnergy(mj);
  }

@Override
public double maxCapacity()
  {
  return tile.getMaxEnergy();
  }

@Override
public double minimumConsumption()
  {  
  return 0;
  }

@Override
public double maxReceivedPerCycle()
  {
  if(tile instanceof ITorqueReceiver)
    {
    return ((ITorqueReceiver) tile).getMaxInput();
    }
  return 0;
  }

@Override
public IBatteryObject reconfigure(double maxCapacity, double maxReceivedPerCycle, double minimumConsumption)
  {
  //NOOP -- can't fucking reconfigure my batteries @ runtime...F-YOU
  return this;
  }

@Override
public String kind()
  {
  return MjAPI.DEFAULT_POWER_FRAMEWORK;
  }

@Override
public IOMode mode()
  {
  return mode;
  }

@Override
public boolean canSend()
  {
  return mode.canSend;
  }

@Override
public boolean canReceive()
  {
  return mode.canReceive;
  }

}

@Override
public IBatteryObject getBatteryObject(String kind, ITorqueTile tile, ForgeDirection dir)
  {
//  AWLog.logDebug("creating battery object for: "+tile+" of kind: "+kind+" for direction: "+dir);
  boolean send = false, recieve = false;
  if(tile instanceof ITorqueReceiver){recieve = ((ITorqueReceiver) tile).canInput(dir);}
  if(tile instanceof ITorqueGenerator){send = ((ITorqueGenerator) tile).canOutput(dir);}
  IOMode mode = send && recieve ? IOMode.Both : send ? IOMode.Send : recieve ? IOMode.Receive : IOMode.None;
  return new TorqueMJBattery(tile, mode, dir);
  }

@Override
public boolean isPowerPipe(World world, TileEntity te)
  {
  if(te==null){return false;}
  if(te instanceof TileGenericPipe)
    {
    TileGenericPipe tgp = (TileGenericPipe)te;
    if(tgp.pipe!=null && tgp.pipe.transport instanceof PipeTransportPower)
      {
      return true;
      }
    }
  return false;
  }

@Override
public void transferPower(World world, int x, int y, int z, ITorqueGenerator generator)
  {
  if(!(generator instanceof IPowerEmitter)){return;}
  world.theProfiler.startSection("AW-BC-PowerUpdate");
  double[] requestedEnergy = new double[6];
  
  IBatteryObject[] targets = new IBatteryObject[6];
  TileEntity[] tes = generator.getNeighbors();
  TileEntity te;
  
  IBatteryObject target;
  
  double maxOutput = generator.getMaxOutput();
  if(maxOutput>generator.getEnergyStored()){maxOutput = generator.getEnergyStored();}
  if(maxOutput<1)
    {
    world.theProfiler.endSection();
    return;
    }  
  double request;
  double totalRequest = 0;
  
  ForgeDirection d;
  for(int i = 0; i < 6; i++)
    {
    d = ForgeDirection.getOrientation(i);
    if(!generator.canOutput(d)){continue;}
    te = tes[i];//world.getTileEntity(x+d.offsetX, y+d.offsetY, z+d.offsetZ);
    if(te instanceof ITorqueReceiver){continue;}
    target = MjAPI.getMjBattery(te);
    if(target==null){continue;}
    targets[d.ordinal()]=target;  
    request = target.maxReceivedPerCycle();
    if(request +target.getEnergyStored() > target.maxCapacity()){request = target.maxCapacity()-target.getEnergyStored();}
    if(request>0)
      {
      requestedEnergy[d.ordinal()]=request;
      totalRequest += request;          
      } 
    }
  if(totalRequest>0)
    {
    double percentFullfilled = maxOutput / totalRequest;  
    for(int i = 0; i<6; i++)
      {
      if(targets[i]==null){continue;}
      target = targets[i];
      request = requestedEnergy[i];
      request *= percentFullfilled;
      request = target.addEnergy(request);
      generator.setEnergy(generator.getEnergyStored()-request);  
      }
    }
  world.theProfiler.endSection();
  }

}
