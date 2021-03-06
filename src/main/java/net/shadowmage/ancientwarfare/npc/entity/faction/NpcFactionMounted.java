package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIRideHorseFaction;

public abstract class NpcFactionMounted extends NpcFaction
{

protected NpcAIRideHorseFaction horseAI;

public NpcFactionMounted(World par1World)
  {
  super(par1World);
  }

@Override
public void onDeath(DamageSource source)
  {
  if(!worldObj.isRemote)
    {
    if(horseAI!=null)
      {
      horseAI.onKilled();
      }
    }  
  super.onDeath(source);  
  }

@Override
public void readEntityFromNBT(NBTTagCompound tag)
  {
  super.readEntityFromNBT(tag);
  if(tag.hasKey("horseAI")){horseAI.readFromNBT(tag.getCompoundTag("horseAI"));}
  }

@Override
public void writeEntityToNBT(NBTTagCompound tag)
  {
  super.writeEntityToNBT(tag);
  if(horseAI!=null){tag.setTag("horseAI", horseAI.writeToNBT(new NBTTagCompound()));}
  }

@Override
protected void onRepack()
  {
  if(ridingEntity instanceof EntityHorse)
    {
    mountEntity(null);
    EntityHorse horse = (EntityHorse)ridingEntity;
    horse.setDead();    
    }
  }

}
