package com.builtbroken.mffs.security.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mffs.api.machine.IProjector;
import com.builtbroken.mffs.security.MFFSPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;

public class ItemModuleBroadcast extends ItemModuleDefense
{
    @Override
    public boolean onProject(ItemStack stack, IProjector projector, List<Pos> fields)
    {
        List<Entity> entities = getEntitiesInField(stack, projector);

        //TODO: Add custom broadcast messages
        entities.stream()
                .filter(entity -> entity instanceof EntityPlayer && !projector.hasPermission(((EntityPlayer) entity).getGameProfile(), MFFSPermissions.defense))
                .forEach(entity1 -> ((EntityPlayer) entity1).addChatMessage(new ChatComponentTranslation("message.moduleWarn.warn")));
        return false;
    }
}