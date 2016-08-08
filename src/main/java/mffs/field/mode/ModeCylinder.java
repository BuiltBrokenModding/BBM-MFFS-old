package mffs.field.mode;

import com.builtbroken.mc.lib.render.model.ModelCube;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import mffs.api.modules.IProjectorMode;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * A cylinder mode.
 *
 * @author Calclavia, Thutmose
 */
public class ModeCylinder implements IProjectorMode
{
    private int step = 1;
    private int radiusExpansion = 0;

    @Override
    public List<Pos> getExteriorPoints(ItemStack stack, IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();
        int radius = (posScale.xi() + negScale.xi() + posScale.zi() + negScale.zi()) / 2;
        int height = posScale.yi() + negScale.yi();

        for (int x = -radius; x <= radius; x++)
        {
            for (int y = 0; y <= height; y++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    if ((y == 0 || y == height - 1) && (x * x + z * z + radiusExpansion) <= (radius * radius))
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }

                    if ((x * x + z * z + radiusExpansion) <= (radius * radius) && (x * x + z * z + radiusExpansion) >= ((radius - 1) * (radius - 1)))
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                }
            }
        }

        return fieldBlocks;
    }

    @Override
    public List<Pos> getInteriorPoints(ItemStack stack, IFieldMatrix projector)
    {
        Pos translation = projector.getTranslation();
        List<Pos> fieldBlocks = new ArrayList();
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();
        int radius = (posScale.xi() + negScale.xi() + posScale.zi() + negScale.zi()) / 2;
        int height = posScale.yi() + negScale.yi();

        for (int x = -radius; x <= radius; x++)
        {
            for (int y = 0; y <= height; y++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    Pos position = new Pos(x, y, z);

                    if (isInField(stack, projector, position.add(new Pos((TileEntity) projector).add(translation))))
                    {
                        fieldBlocks.add(position);
                    }
                }
            }
        }

        return fieldBlocks;
    }

    @Override
    public boolean isInField(ItemStack stack, IFieldMatrix projector, Pos position)
    {
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();
        int radius = (posScale.xi() + negScale.xi() + posScale.zi() + negScale.zi()) / 2;
        Pos projectorPos = new Pos((TileEntity) projector).add(projector.getTranslation());
        Pos relativePosition = position.clone().subtract(projectorPos);
        relativePosition.transform(new EulerAngle(-projector.getRotationYaw(), -projector.getRotationPitch(), 0));
        if (relativePosition.x() * relativePosition.x() + relativePosition.z() * relativePosition.z() + radiusExpansion <= radius * radius)
        {
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(ItemStack stack, IProjector projector, double x, double y, double z, float f, long ticks)
    {
        float scale = 0.15f;
        float detail = 0.5f;
        GL11.glScalef(scale, scale, scale);

        int radius = (int) (1.5f * detail);

        int i = 0;

        //TODO: Check scale and detail
        for (int renderX = -radius; renderX <= radius; renderX++)
        {
            for (int renderY = 0; renderY <= 255; renderY++)
            {
                for (int renderZ = -radius; renderZ <= radius; renderZ++)
                {
                    {
                        if (((renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius) && (renderX * renderX + renderZ * renderZ + radiusExpansion) >= ((radius - 1) * (radius - 1))) || ((renderY == 0 || renderY == radius - 1) && (renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius)))
                        {
                            if (i % 2 == 0)
                            {
                                Pos vector = new Pos(renderX / detail, renderY / detail, renderZ / detail);
                                GL11.glTranslated(vector.x(), vector.y(), vector.z());
                                ModelCube.INSTNACE.render();
                                GL11.glTranslated(-vector.x(), -vector.y(), -vector.z());
                            }
                            i += 1;
                        }
                    }
                }
            }
        }
    }

    @Override
    public float getFortronCost(ItemStack stack, float amplifier)
    {
        return -1;
    }
}