package com.builtbroken.mffs.api.card;

import com.builtbroken.mc.lib.transform.vector.Location;
import net.minecraft.item.ItemStack;

public interface ICoordLink
{
    void setLink(ItemStack itemStack, Location position);

    Location getLink(ItemStack itemStack);
}
