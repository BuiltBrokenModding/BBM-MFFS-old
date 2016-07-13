package mffs.security.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.machine.IProjector;
import mffs.security.MFFSPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;

class ItemModuleBroadcast extends ItemModuleDefense
{
    @Override
    public boolean onProject(IProjector projector, List<Pos> fields)
    {
        List<Entity> entities = getEntitiesInField(projector);

        //TODO: Add custom broadcast messages
        entities.stream()
                .filter(entity -> entity instanceof EntityPlayer && !projector.hasPermission(((EntityPlayer) entity).getGameProfile(), MFFSPermissions.defense))
                .forEach(entity1 -> ((EntityPlayer) entity1).addChatMessage(new ChatComponentTranslation("message.moduleWarn.warn")));
        return false;
    }
}