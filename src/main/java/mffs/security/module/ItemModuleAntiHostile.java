package mffs.security.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.entity.damage.DamageSources;
import mffs.api.machine.IProjector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;

import java.util.List;

class ItemModuleAntiHostile extends ItemModuleDefense
{
    @Override
    public boolean onProject(IProjector projector, List<Pos> fields)
    {
        List<Entity> entities = getEntitiesInField(projector);
        entities.stream().filter(entity -> entity instanceof IMob).forEach(entity1 -> entity1.attackEntityFrom(DamageSources.ELECTRIC.getSource(), 100));
        return false;
    }
}