package mffs.field.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.machine.IProjector;
import mffs.base.ItemModule;
import mffs.field.TileElectromagneticProjector;
import mffs.security.MFFSPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemModuleRepulsion extends ItemModule
{
    public ItemModuleRepulsion()
    {
        setCost(8);
    }

    @Override
    public boolean onProject(ItemStack stack, IProjector projector, List<Pos> fields)
    {
        double repulsionVelocity = Math.max(projector.getModuleCount(this) / 20, 1.2);
        List<Entity> entities = getEntitiesInField(stack, projector);

        //TODO: Check parallel
        entities.stream()
                .filter(
                        entity ->
                        {
                            if (fields.contains(new Pos(entity).floor()) || projector.getMode().isInField(stack, projector, new Pos(entity)))
                            {
                                if (entity instanceof EntityPlayer)
                                {
                                    EntityPlayer entityPlayer = (EntityPlayer) entity;
                                    return entityPlayer.capabilities.isCreativeMode || projector.hasPermission(entityPlayer.getGameProfile(), MFFSPermissions.forceFieldWarp);
                                }
                                return true;
                            }

                            return false;
                        })
                .forEach(
                        entity ->
                        {
                            Pos repelDirection = new Pos(entity).sub((new Pos(entity).floor().add(0.5)).normalize());
                            entity.motionX = repelDirection.x() * Math.max(repulsionVelocity, Math.abs(entity.motionX));
                            entity.motionY = repelDirection.y() * Math.max(repulsionVelocity, Math.abs(entity.motionY));
                            entity.motionZ = repelDirection.z() * Math.max(repulsionVelocity, Math.abs(entity.motionZ));
                            //TODO: May NOT be thread safe!
                            entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ);
                            entity.onGround = true;

                            if (entity instanceof EntityPlayerMP)
                            {
                                ((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
                            }
                        });

        return true;
    }

    @Override
    public boolean onDestroy(ItemStack stack, IProjector projector, List<Pos> field)
    {
        ((TileElectromagneticProjector) projector).sendFieldToClient();
        return false;
    }

    @Override
    public boolean requireTicks(ItemStack moduleStack)
    {
        return true;
    }
}