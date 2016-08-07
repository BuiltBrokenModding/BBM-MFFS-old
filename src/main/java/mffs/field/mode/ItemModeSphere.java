package mffs.field.mode;

import com.builtbroken.mc.lib.render.model.ModelCube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ItemModeSphere extends ItemMode
{
    @Override
    public List<Pos> getExteriorPoints(IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        int radius = projector.getModuleCount(ModularForceFieldSystem.moduleScale);
        int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / radius / 2));

        //TODO: This creates MANY duplicates. Each only .01-.10 away from eachother.
        for (int phi_n = 0; phi_n < 2 * steps; phi_n++)
        {
            for (int theta_n = 0; theta_n < steps; theta_n++)
            {
                double phi = Math.PI * 2 / steps * phi_n;
                double theta = Math.PI / steps * theta_n;
                Pos point = new Pos(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi)).multiply(radius);
                fieldBlocks.add(point);
            }
        }
        return fieldBlocks;
    }

    @Override
    public List<Pos> getInteriorPoints(IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        Pos translation = projector.getTranslation();
        int radius = projector.getModuleCount(ModularForceFieldSystem.moduleScale);

        for (int x = -radius; x <= radius; x++)
        {
            for (int y = -radius; y <= radius; y++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    Pos position = new Pos(x, y, z);
                    if (isInField(projector, position.add(new Pos((TileEntity) projector)).add(translation)))
                    {
                        fieldBlocks.add(position);
                    }
                }
            }
        }
        return fieldBlocks;
    }

    @Override
    public boolean isInField(IFieldMatrix projector, Pos position)
    {
        return new Pos((TileEntity) projector).add(projector.getTranslation()).distance(position) < projector.getModuleCount(ModularForceFieldSystem.moduleScale);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(IProjector projector, double x1, double y1, double z1, float f, long ticks)
    {
        //TODO cache shape to improve FPS
        float scale = 0.2f;
        GL11.glScalef(scale, scale, scale);
        float radius = 0.8f;
        int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / radius / 2));

        for (int phi_n = 0; phi_n <= 2 * steps; phi_n++)
        {
            for (int theta_n = 0; theta_n <= 2 * steps; theta_n++)
            {
                double phi = Math.PI * 2 / steps * phi_n;
                double theta = Math.PI / steps * theta_n;

                Pos vector = new Pos(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi)).multiply(radius);
                GL11.glTranslated(vector.x(), vector.y(), vector.z());
                ModelCube.INSTNACE.render();
                GL11.glTranslated(-vector.x(), -vector.y(), -vector.z());
            }
        }
    }
}