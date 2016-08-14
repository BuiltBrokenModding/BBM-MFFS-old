package com.builtbroken.mffs.security.card;

import com.builtbroken.mc.lib.access.AccessUser;
import com.builtbroken.mffs.api.card.IAccessCard;
import com.builtbroken.mffs.item.card.ItemCard;
import net.minecraft.item.ItemStack;

/**
 * @author Calclavia
 */
public abstract class ItemCardAccess extends ItemCard implements IAccessCard
{
    @Override
    public void setAccess(ItemStack itemStack, AccessUser access)
    {
        itemStack.setTagCompound(access.userData());
    }
}
