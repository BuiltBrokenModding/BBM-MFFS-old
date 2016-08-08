package mffs.api.modules;

import net.minecraft.item.ItemStack;

/**
 * @author Calclavia
 */
public interface IFortronCost
{
    /**
     * The amount of Fortron this module consumes per tick.
     *
     * @return
     */
    float getFortronCost(ItemStack stack, float amplifier);
}
