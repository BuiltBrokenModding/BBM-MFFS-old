package mffs.item.gui;

import com.builtbroken.mc.prefab.inventory.ExternalInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A temporary inventory used by items for copying
 *
 * @author Calclavia
 */

public class CopyInventory extends ExternalInventory {

    private ItemStack copyItem;

    public CopyInventory(ItemStack stack, int slot) {
        super(null, slot);
        copyItem = stack;
    }

    @Override
    public void markDirty() {
        if (copyItem.getTagCompound() != null && getStackInSlot(0) != null)
            getStackInSlot(0).setTagCompound((NBTTagCompound) copyItem.getTagCompound().copy());
    }
}
