package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIAlertFaction;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIFollowPlayer;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIMoveHome;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIWander;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;

public abstract class NpcFactionTrader extends NpcFaction
{

public NpcFactionTrader(World par1World)
  {
  super(par1World);
//  setCurrentItemOrArmor(0, new ItemStack(Items.book));
  
  this.tasks.addTask(0, new EntityAISwimming(this));
  this.tasks.addTask(0, new EntityAIRestrictOpenDoor(this));
  this.tasks.addTask(0, new EntityAIOpenDoor(this, true));
  this.tasks.addTask(1, (alertAI = new NpcAIAlertFaction(this)));
  this.tasks.addTask(1, new NpcAIFollowPlayer(this));
  this.tasks.addTask(2, new NpcAIMoveHome(this, 50.f, 5.f, 30.f, 5.f)); 
  
  this.tasks.addTask(101, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
  this.tasks.addTask(102, new NpcAIWander(this, 0.625D));
  this.tasks.addTask(103, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F)); 
  }

@Override
protected boolean interact(EntityPlayer player)
  {
  boolean baton = player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemCommandBaton;
  if(!player.worldObj.isRemote && !baton)
    {
    NetworkHandler.INSTANCE.openGui(player, NetworkHandler.GUI_NPC_TRADE, getEntityId(), 0, 0);
    }
  return false;
  }

@Override
public boolean isHostileTowards(Entity e)
  {
  return false;
  }

@Override
public boolean canTarget(Entity e)
  {
  return false;
  }

}
