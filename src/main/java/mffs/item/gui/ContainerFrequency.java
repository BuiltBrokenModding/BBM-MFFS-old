package mffs.item.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * @author Calclavia
 */
public class ContainerFrequency extends ContainerItem
{

    public ContainerFrequency(EntityPlayer player, ItemStack stack)
    {
        super(player, stack, new CopyInventory(stack, 1));
        //addSlotToContainer(new SlotSpecific(inventory, 0, 81, 101, ItemCardFrequency.class));
    }
}
