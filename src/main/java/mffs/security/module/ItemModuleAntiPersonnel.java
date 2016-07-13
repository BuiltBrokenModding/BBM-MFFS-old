package mffs.security.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.entity.damage.DamageElectrical;
import mffs.api.machine.IProjector;
import mffs.security.MFFSPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

class ItemModuleAntiPersonnel extends ItemModuleDefense
{
    @Override
    public boolean onProject(IProjector projector, List<Pos> fields)
    {
        List<Entity> entities = getEntitiesInField(projector);
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