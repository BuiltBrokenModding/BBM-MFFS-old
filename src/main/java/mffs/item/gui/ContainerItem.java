package mffs.item.gui;

import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.prefab.gui.ContainerBase;
import com.builtbroken.mc.prefab.inventory.ExternalInventory;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import mffs.slot.SlotDisabled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author Calclavia
 */
public class ContainerItem extends ContainerBase
{
    public ContainerItem(EntityPlayer player, ItemStack itemStack)
    {
        super(new ExternalInventory(null, 1));
        addPlayerInventory(player);
    }

    public ContainerItem(EntityPlayer player, ItemStack itemStack, IInventory inventory)
    {
        super(inventory);
        addPlayerInventory(player);
    }

    @Override
    public void addPlayerInventory(EntityPlayer player)
    {
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, this.xInventoryDisplacement + x * 18, this.yInventoryDisplacement + y * 18));
            }
        }

        for (int x = 0; x < 9; x++)
        {
            if (x == player.inventory.currentItem)
            {
                addSlotToContainer(new SlotDisabled(player.inventory, x, this.xInventoryDisplacement + x * 18, this.yHotBarDisplacement));
            }
            else
            {
                addSlotToContainer(new Slot(player.inventory, x, this.xInventoryDisplacement + x * 18, this.yHotBarDisplacement));
            }
        }

    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    {
        return null;
    }

    /**
     * Drop all inventory contents upon container close.
     */
    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (inventory.getStackInSlot(slot) == null)
            {
                continue;
            }
            InventoryUtility.dropItemStack(player.worldObj, new Location(player), inventory.getStackInSlot(slot));
            inventory.setInventorySlotContents(slot, null);
        }

        super.onContainerClosed(player);
    }
}
