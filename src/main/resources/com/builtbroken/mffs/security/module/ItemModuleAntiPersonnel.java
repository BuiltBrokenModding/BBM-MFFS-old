package com.builtbroken.mffs.security.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.entity.damage.DamageElectrical;
import com.builtbroken.mffs.api.machine.IProjector;
import com.builtbroken.mffs.security.MFFSPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemModuleAntiPersonnel extends ItemModuleDefense
{
    @Override
    public boolean onProject(ItemStack stack, IProjector projector, List<Pos> fields)
    {
        List<Entity> entities = getEntitiesInField(stack, projector);
        for (Entity entity : entities)
        {
            if (entity instanceof EntityPlayer && !((EntityPlayer) entity).capabilities.disableDamage && !((EntityPlayer) entity).capabilities.isCreativeMode && !projector.hasPermission(((EntityPlayer) entity).getGameProfile(), MFFSPermissions.defense))
            {
                //TODO collect player's items
                entity.attackEntityFrom(new DamageAntiPersonnel(projector), 1000);
            }
        }
        return false;
    }

    public class DamageAntiPersonnel extends DamageElectrical
    {
        IProjector projector;

        public DamageAntiPersonnel(IProjector projector)
        {
            this.projector = projector;
        }
        //TODO custom death message
    }
}