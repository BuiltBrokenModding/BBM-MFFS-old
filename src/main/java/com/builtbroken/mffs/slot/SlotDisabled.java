package com.builtbroken.mffs.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class SlotDisabled extends Slot
{
    public SlotDisabled(IInventory inventory, int id, int par4, int par5)
    {
        super(inventory, id, par4, par5);
    }

    @Override
    public boolean isItemValid(ItemStack itemStack)
    {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player)
    {
        return false;
    }
}
