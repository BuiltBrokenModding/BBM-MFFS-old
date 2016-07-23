package mffs.field.mode;

import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import mffs.api.modules.IProjectorMode;
import mffs.base.ItemMFFS;

public abstract class ItemMode extends ItemMFFS implements IProjectorMode
{
    public ItemMode()
    {
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IProjector projector, double x, double y, double z, float f, long ticks)
    {
    }

    @Override
    public boolean isInField(IFieldMatrix projector, Pos position)
    {
        return false;
    }

    @Override
    public float getFortronCost(float amplifier)
    {
        return 8;
    }
}