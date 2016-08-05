package mffs.field.mode;

import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ItemModePyramid extends ItemMode
{
    private int step = 1;

    @Override
    public List<Pos> getExteriorPoints(IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();

        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();

        int xSize = Math.max(posScale.xi(), Math.abs(negScale.xi()));
        int zSize = Math.max(posScale.zi(), Math.abs(negScale.zi()));
        int ySize = Math.max(posScale.yi(), Math.abs(negScale.yi()));

        int initX = xSize;
        int initZ = xSize;

        int xDecr = xSize / (ySize * 2);
        int zDecr = zSize / (ySize * 2);

        //Create pyramid
        for (int y = -ySize; y <= ySize; y++)
        {
            for (int x = -initX; x <= initX; x++)
            {
                for (int z = -initZ; z <= initZ; z++)
                {
                    if (Math.abs(x) == Math.round(xSize) && Math.abs(z) <= Math.round(zSize))
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                    else if (Math.abs(z) == Math.round(zSize) && Math.abs(x) <= Math.round(xSize))
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                    else if (y == -ySize)
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                }
            }

            xSize -= xDecr;
            zSize -= zDecr;
        }

        return fieldBlocks;
    }

    @Override
    public List<Pos> getInteriorPoints(IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();
        int xStretch = posScale.xi() + negScale.xi();
        int yStretch = posScale.yi() + negScale.yi();
        int zStretch = posScale.zi() + negScale.zi();
        Pos translation = new Pos(0, -0.4, 0);

        for (int y = -yStretch; y <= yStretch; y++)
        {
            for (int x = -xStretch; x <= xStretch; x++)
            {
                for (int z = -zStretch; z <= zStretch; z++)
                {
                    Pos position = new Pos(x, y, z).add(translation);

                    if (isInField(projector, position.add(new Pos((TileEntity) projector))))
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
        Pos posScale = projector.getPositiveScale().clone();
        Pos negScale = projector.getNegativeScale().clone();
        int xStretch = posScale.xi() + negScale.xi();
        int yStretch = posScale.yi() + negScale.yi();
        int zStretch = posScale.zi() + negScale.zi();
        Pos projectorPos = new Pos((TileEntity) projector);
        projectorPos.add(projector.getTranslation());
        projectorPos.add(new Pos(0, -negScale.yi() + 1, 0));
        Pos relativePosition = position.clone().subtract(projectorPos);
        relativePosition.transform(new EulerAngle(-projector.getRotationYaw(), -projector.getRotationPitch(), 0));
        Cube region = new Cube(negScale.multiply(-1), posScale);

        if (region.intersects(relativePosition) && relativePosition.y() > 0)
        {
            if ((1 - (Math.abs(relativePosition.x()) / xStretch) - (Math.abs(relativePosition.z()) / zStretch) > relativePosition.y() / yStretch))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IProjector projector, double x, double y, double z, float f, long ticks)
    {
        Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glRotatef(180, 0, 0, 1);
        float height = 0.5f;
        float width = 0.3f;
        int uvMaxX = 2;
        int uvMaxY = 2;
        Pos translation = new Pos(0, -0.4, 0);
        tessellator.startDrawing(6);
        tessellator.setColorRGBA(72, 198, 255, 255);
        tessellator.addVertexWithUV(0 + translation.x(), 0 + translation.y(), 0 + translation.z(), 0, 0);
        tessellator.addVertexWithUV(-width + translation.x(), height + translation.y(), -width + translation.z(), -uvMaxX, -uvMaxY);
        tessellator.addVertexWithUV(-width + translation.x(), height + translation.y(), width + translation.z(), -uvMaxX, uvMaxY);
        tessellator.addVertexWithUV(width + translation.x(), height + translation.y(), width + translation.z(), uvMaxX, uvMaxY);
        tessellator.addVertexWithUV(width + translation.x(), height + translation.y(), -width + translation.z(), uvMaxX, -uvMaxY);
        tessellator.addVertexWithUV(-width + translation.x(), height + translation.y(), -width + translation.z(), -uvMaxX, -uvMaxY);
        tessellator.draw();
        GL11.glPopMatrix();
    }
}