package com.builtbroken.mffs.slot;

import com.builtbroken.mffs.api.card.IItemFrequency;
import com.builtbroken.mffs.api.fortron.IBlockFrequency;
import com.builtbroken.mffs.base.TileFrequency;
import net.minecraft.item.ItemStack;

public class SlotCard extends SlotBase
{
    public SlotCard(TileFrequency tileEntity, int id, int par4, int par5)
    {
        super(tileEntity, id, par4, par5);
    }

    @Override
    public void onSlotChanged()
    {
        super.onSlotChanged();
        ItemStack itemStack = this.getStack();

        if (itemStack != null)
        {
            if (itemStack.getItem() instanceof IItemFrequency)
            {
                ((IItemFrequency) itemStack.getItem()).setFrequency(((IBlockFrequency) tileEntity).getFrequency(), itemStack);
            }
        }
    }
}
