package net.shadowmage.ancientwarfare.structure.tile;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.shadowmage.ancientwarfare.core.api.AWBlocks;
import net.shadowmage.ancientwarfare.core.api.ModuleStatus;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.interfaces.ITorque.ITorqueReceiver;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.interfaces.IWorker;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.util.BlockPosition;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilderTicked;

public class TileStructureBuilder extends TileEntity implements IWorkSite, ITorqueReceiver
{

protected String owningPlayer;

StructureBuilderTicked builder;
private boolean shouldRemove = false;
public boolean isStarted = false;
int workDelay = 20;

public TileStructureBuilder()
  {
  
  }

double maxEnergyStored = 1600;
double maxInput = 100;
private double storedEnergy;

@Override
public int getBoundsMaxWidth(){return 0;}

@Override
public int getBoundsMaxHeight(){return 0;}

@Override
public EnumSet<WorksiteUpgrade> getUpgrades(){return EnumSet.noneOf(WorksiteUpgrade.class);}//NOOP

@Override
public EnumSet<WorksiteUpgrade> getValidUpgrades(){return EnumSet.noneOf(WorksiteUpgrade.class);}//NOOP

@Override
public void addUpgrade(WorksiteUpgrade upgrade){}//NOOP

@Override
public void removeUpgrade(WorksiteUpgrade upgrade){}//NOOP

@Override
public void setBounds(BlockPosition p1, BlockPosition p2){}//NOOP

@Override
public void setWorkBoundsMax(BlockPosition max){}//NOOP

@Override
public void setWorkBoundsMin(BlockPosition min){}//NOOP

@Override
public void onBoundsAdjusted(){}//NOOP

@Override
public boolean userAdjustableBlocks(){return false;}//NOOP

@Override
public void setEnergy(double energy)
  {
  this.storedEnergy = energy;
  }

@Override
public double getEnergyDrainFactor()
  {
  return 1;
  }

@Override
public ForgeDirection getPrimaryFacing()
  {
  return ForgeDirection.getOrientation(getBlockMetadata());
  }

@Override
public double addEnergy(ForgeDirection from, double energy)
  {
  if(canInput(from))
    {
    if(energy+getEnergyStored()>getMaxEnergy())
      {
      energy = getMaxEnergy()-getEnergyStored();
      }
    if(energy>getMaxInput())
      {
      energy = getMaxInput();
      }
    storedEnergy+=energy;
    return energy;    
    }
  return 0;
  }

@Override
public double getMaxEnergy()
  {
  return maxEnergyStored;
  }

@Override
public double getEnergyStored()
  {
  return storedEnergy;
  }

@Override
public double getMaxInput()
  {
  return maxInput;
  }

@Override
public boolean canInput(ForgeDirection from)
  {
  return true;
  }

@Override
public boolean canUpdate()
  {
  return true;
  }

@Override
public void updateEntity()
  {
  super.updateEntity();
  if(worldObj.isRemote){return;}
  if(builder==null || builder.invalid || builder.isFinished())
    {
    shouldRemove = true;
    }
  if(builder.getWorld()==null){builder.setWorld(worldObj);}
  if(shouldRemove)
    {
    worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.air);
    return;
    }
  if(ModuleStatus.automationLoaded || ModuleStatus.npcsLoaded)
    {
    if(storedEnergy>=maxEnergyStored)
      {
      storedEnergy-=maxEnergyStored;
      processWork();
      }
    return;
    }
  if(workDelay>0)
    {
    workDelay--;
    }
  if(workDelay<=0)
    {
    processWork();
    workDelay=20;
    }
  }

public void processWork()
  {
  isStarted = true;
  builder.tick();
  }

/**
 * should be called immediately after the tile-entity is set into the world
 * from the ItemBlockStructureBuilder item onBlockPlaced code
 * @param builder
 */
public void setOwnerName(String name)
  {
  this.owningPlayer = name;
  }

/**
 * should be called immediately after the tile-entity is set into the world
 * from the ItemBlockStructureBuilder item onBlockPlaced code<br>
 * the passed in builder must be valid (have a valid structure), and must not
 * be null
 * @param builder
 */
public void setBuilder(StructureBuilderTicked builder)
  {
  this.builder = builder;
  }

public void onBlockBroken()
  {
  if(!worldObj.isRemote && !isStarted && builder!=null && builder.getTemplate()!=null)
    {
    isStarted = true;//to prevent further drops
    ItemStack item = new ItemStack(AWBlocks.builderBlock);
    item.setTagInfo("structureName", new NBTTagString(builder.getTemplate().name));
    }
  }

@Override
public void readFromNBT(NBTTagCompound tag)
  {  
  super.readFromNBT(tag);  
  if(tag.hasKey("builder"))
    {
    builder = new StructureBuilderTicked();    
    builder.readFromNBT(tag.getCompoundTag("builder"));    
    }
  else
    {
    this.shouldRemove = true;
    }
  this.isStarted = tag.getBoolean("started");
  this.storedEnergy = tag.getDouble("storedEnergy");
  }

@Override
public void writeToNBT(NBTTagCompound tag)
  {
  super.writeToNBT(tag);
  if(builder!=null)
    {
    NBTTagCompound builderTag = new NBTTagCompound();
    builder.writeToNBT(builderTag);  
    tag.setTag("builder", builderTag);    
    }
  tag.setBoolean("started", isStarted);
  tag.setDouble("storedEnergy", storedEnergy);
  }

//*******************************************WORKSITE************************************************//
@Override
public boolean hasWork()
  {
  return storedEnergy<maxEnergyStored;
  }

@Override
public WorkType getWorkType()
  {
  return WorkType.CRAFTING;
  }

@Override
public final Team getTeam()
  {  
  if(owningPlayer!=null)
    {
    worldObj.getScoreboard().getPlayersTeam(owningPlayer);
    }
  return null;
  }

@Override
public BlockPosition getWorkBoundsMin()
  {
  return null;
  }

@Override
public BlockPosition getWorkBoundsMax()
  {
  return null;
  }

@Override
public boolean hasWorkBounds()
  {
  return false;
  }

@Override
public void addEnergyFromWorker(IWorker worker)
  {
  storedEnergy += AWCoreStatics.energyPerWorkUnit * worker.getWorkEffectiveness(getWorkType());
  if(storedEnergy>getMaxEnergy()){storedEnergy = getMaxEnergy();}
  }

@Override
public void addEnergyFromPlayer(EntityPlayer player)
  {
  storedEnergy+=AWCoreStatics.energyPerWorkUnit;
  if(storedEnergy>getMaxEnergy()){storedEnergy=getMaxEnergy();}
  }

}
