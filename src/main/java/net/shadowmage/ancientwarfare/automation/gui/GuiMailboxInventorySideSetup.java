package net.shadowmage.ancientwarfare.automation.gui;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StatCollector;
import net.shadowmage.ancientwarfare.automation.container.ContainerMailbox;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RelativeSide;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.block.Direction;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase;
import net.shadowmage.ancientwarfare.core.gui.elements.Button;
import net.shadowmage.ancientwarfare.core.gui.elements.Label;

import org.lwjgl.input.Mouse;

public class GuiMailboxInventorySideSetup extends GuiContainerBase
{

GuiMailboxInventory parent;
ContainerMailbox container;

public GuiMailboxInventorySideSetup(GuiMailboxInventory parent)
  {
  super((ContainerBase) parent.inventorySlots, 240, 108, defaultBackground);
  container = (ContainerMailbox)parent.inventorySlots;
  this.parent = parent;
  }

@Override
public void initElements()
  {

  }

@Override
public void setupElements()
  {
  this.clearElements();
  
  Label label;
  SideButton sideButton;
  RelativeSide accessed;
  int dir;
  
  label = new Label(8, 6, StatCollector.translateToLocal("guistrings.automation.block_side"));  
  addGuiElement(label);
  label = new Label(74, 6, StatCollector.translateToLocal("guistrings.automation.direction"));  
  addGuiElement(label);
  label = new Label(128, 6, StatCollector.translateToLocal("guistrings.automation.inventory_accessed"));  
  addGuiElement(label);
  
  int height = 18;
  for(RelativeSide side : RotationType.FOUR_WAY.getValidSides())
    {
    label = new Label(8, height, StatCollector.translateToLocal(side.getTranslationKey()));
    addGuiElement(label);
      
    dir = RelativeSide.getMCSideToAccess(RotationType.FOUR_WAY, container.worksite.getBlockMetadata(), side);
    label = new Label(74, height, StatCollector.translateToLocal(Direction.getDirectionFor(dir).getTranslationKey()));
    addGuiElement(label);

    accessed = container.sideMap.get(side);  
    sideButton = new SideButton(128, height, side, accessed);
    addGuiElement(sideButton);
        
    height+=14;
    }   
  }

@Override
protected boolean onGuiCloseRequested()
  {  
  container.addSlots();
  int x = Mouse.getX();
  int y = Mouse.getY();
  Minecraft.getMinecraft().displayGuiScreen(parent);
  Mouse.setCursorPosition(x, y);
  return false;
  }

private class SideButton extends Button
{
RelativeSide side;//base side
RelativeSide selection;//accessed side

public SideButton(int topLeftX, int topLeftY, RelativeSide side, RelativeSide selection)
  {
  super(topLeftX, topLeftY, 55, 12, StatCollector.translateToLocal(selection.getTranslationKey()));
  if(side==null)
    {
    throw new IllegalArgumentException("access side may not be null..");
    }
  this.side = side;
  this.selection = selection;
  }

@Override
protected void onPressed()
  {
  int ordinal = selection.ordinal();
  RelativeSide next;
  EnumSet<RelativeSide> validSides = container.worksite.inventory.getValidSides(); 
  for(int i = 0; i < RelativeSide.values().length; i++)
    {
    ordinal++;
    if(ordinal>=RelativeSide.values().length)
      {
      ordinal = 0;
      }
    next = RelativeSide.values()[ordinal];
    if(validSides.contains(next))
      {
      selection = next;
      break;
      }
    }
  container.sideMap.put(side, selection);
  setText(StatCollector.translateToLocal(selection.getTranslationKey()));
  container.sendSlotChange(side, selection);
  }

}

}
