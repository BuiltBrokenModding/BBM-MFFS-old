package com.builtbroken.mffs.api.card;

import net.minecraft.item.ItemStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 7/22/2016.
 */
public interface IItemFrequency
{
    //TODO find original interface
    int getFrequency(ItemStack itemStack);

    void setFrequency(int frequency, ItemStack itemStack);
}
