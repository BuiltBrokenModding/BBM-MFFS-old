package com.builtbroken.mffs.item.fortron;

import com.builtbroken.mffs.item.card.ItemCard;
import com.builtbroken.mffs.util.FortronUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
public class ItemCardInfinite extends ItemCard implements IFluidContainerItem
{
    //TODO prevent item from being dropped or held by non-creative players
    //TODO configure max amount as a way to reduce drain speed
    public static final FluidStack cachedValue = new FluidStack(FortronUtility.fluidFortron, Integer.MAX_VALUE);

    @Override
    public FluidStack getFluid(ItemStack container)
    {
        return cachedValue.copy();
    }

    @Override
    public int getCapacity(ItemStack container)
    {
        return cachedValue.amount;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill)
    {
        return resource != null && resource.getFluid() == FortronUtility.fluidFortron ? resource.amount : 0;
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
    {
        return new FluidStack(FortronUtility.fluidFortron, maxDrain);
    }
}