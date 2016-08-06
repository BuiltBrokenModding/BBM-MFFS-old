package mffs.field.mode;

import com.builtbroken.mc.lib.render.model.ModelCube;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ItemModeCube extends ItemMode
{
    private int step = 1;

    @Override
    public List<Pos> getExteriorPoints(IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale(); //Positive numbers so do -# when using

        for (int x = -negScale.xi(); x < posScale.xi(); x += step)
        {
            for (int y = -negScale.yi(); y < posScale.yi(); y += step)
            {
                for (int z = -negScale.zi(); z < posScale.zi(); z += step)
                {
                    if (y == -negScale.yi() || y == posScale.yi() || x == -negScale.xi() || x == posScale.xi() || z == -negScale.zi() || z == posScale.zi())
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                }
            }
        }
        return fieldBlocks;
    }

    @Override
    public List<Pos> getInteriorPoints(IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();

        //TODO: Check parallel possibility
        for (int x = negScale.xi(); x < posScale.xi(); x += step)
        {
            for (int y = negScale.yi(); y < posScale.yi(); y += step)
            {
                for (int z = negScale.zi(); z < posScale.zi(); z += step)
                {
                    fieldBlocks.add(new Pos(x, y, z));
                }
            }
        }

        return fieldBlocks;
    }

    @Override
    public boolean isInField(IFieldMatrix projector, Pos position)
    {
        Pos projectorPos = new Pos((TileEntity) projector);
        projectorPos = projectorPos.add(projector.getTranslation());
        Pos relativePosition = position.clone().subtract(projectorPos);
        relativePosition.transform(new EulerAngle(-projector.getRotationYaw(), -projector.getRotationPitch(), 0));
        Cube region = new Cube(projector.getNegativeScale().multiply(-1), projector.getPositiveScale());
        return region.intersects(relativePosition);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IProjector projector, double x, double y, double z, float f, long ticks)
    {
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        ModelCube.INSTNACE.render();
    }
}