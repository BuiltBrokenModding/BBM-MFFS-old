package mffs.slot;

import mffs.api.card.IItemFrequency;
import mffs.api.fortron.FrequencyGridRegistry;
import mffs.base.TileFrequency;
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
                ((IItemFrequency) itemStack.getItem()).setFrequency(((FrequencyGridRegistry.IBlockFrequency) tileEntity).getFrequency(), itemStack);
            }
        }
    }
}
