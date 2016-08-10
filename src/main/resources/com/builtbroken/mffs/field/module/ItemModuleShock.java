package com.builtbroken.mffs.field.module;

import com.builtbroken.mffs.ModularForceFieldSystem;
import com.builtbroken.mffs.base.ItemModule;
import com.builtbroken.mffs.field.TileForceField;
import com.builtbroken.mffs.security.MFFSPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemModuleShock extends ItemModule
{
    @Override
    public boolean onCollideWithForceField(World world, int x, int y, int z, Entity entity, ItemStack moduleStack)
    {
        if (entity instanceof EntityPlayer)
        {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof TileForceField)
            {
                if (((TileForceField) tile).getProjector().hasPermission(entityPlayer.getGameProfile(), MFFSPermissions.forceFieldWarp))
                {
                    return true;
                }
            }

            entity.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, moduleStack.stackSize);
        }
        return true;
    }
}