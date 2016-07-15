package mffs.field.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.fortron.FrequencyGridRegistry;
import mffs.api.machine.IProjector;
import mffs.base.ItemModule;
import mffs.field.TileElectromagneticProjector;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class ItemModuleFusion extends ItemModule
{
    public ItemModuleFusion()
    {
        setMaxStackSize(1);
        setCost(1f);
    }

    @Override
    public boolean onProject(IProjector projector, List<Pos> field)
    {
        TileEntity tile = (TileEntity) projector;
        List<TileElectromagneticProjector> projectors = FrequencyGridRegistry.SERVER_INSTANCE.getNodes(TileElectromagneticProjector.class, projector.getFrequency());

        //TOOD: Check threading efficiency
        for (TileElectromagneticProjector proj : projectors)
        {
            if (proj.getWorldObj() == tile.getWorldObj() && proj.isActive() && proj.getMode() != null))
            {

                val removeFields = (fieldBlocks.par filter(pos = > checkProjectors
                exists(proj = > proj.getInteriorPoints.contains(pos) || proj.getMode.isInField(proj, pos)))).seq
                fieldBlocks-- = removeFields
            }
        }

        return false;
    }
}