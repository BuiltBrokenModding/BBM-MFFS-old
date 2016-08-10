package com.builtbroken.mffs.security.module;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mffs.base.ItemModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemModuleDefense extends ItemModule
{
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
    {
        info.add("\u00a74" + LanguageUtility.getLocal("info.module.defense"));
        super.addInformation(itemStack, player, info, b);
    }
}