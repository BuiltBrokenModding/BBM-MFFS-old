package mffs.field.mode;

import com.builtbroken.mc.api.tile.IRotation;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import mffs.render.model.ModelPlane;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ItemModeTube extends ItemModeCube
{
    @Override
    public List<Pos> getExteriorPoints(IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        ForgeDirection direction = projector instanceof IRotation ? ((IRotation)projector).getDirection() : ForgeDirection.NORTH; //TODO maybe remove rotation based on projector facing direction
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();

        for (int x = -negScale.xi(); x <= posScale.xi(); x++)
        {
            for (int y = -negScale.yi(); y <= posScale.yi(); y++)
            {
                for (int z = -negScale.zi(); z <= posScale.zi(); z++)
                {
                    if (!(direction == ForgeDirection.UP || direction == ForgeDirection.DOWN) && (y == -negScale.yi() || y == posScale.yi()))
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                    else if (!(direction == ForgeDirection.NORTH || direction == ForgeDirection.SOUTH) && (z == -negScale.zi() || z == posScale.zi()))
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                    else if (!(direction == ForgeDirection.WEST || direction == ForgeDirection.EAST) && (x == -negScale.xi() || x == posScale.xi()))
                    {
                        fieldBlocks.add(new Pos(x, y, z));
                    }
                }
            }
        }

        return fieldBlocks;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(IProjector projector, double x, double y, double z, float f, long ticks)
    {
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        GL11.glTranslatef(-0.5f, 0, 0);
        ModelPlane.INSTNACE.render();
        GL11.glTranslatef(1f, 0, 0);
        ModelPlane.INSTNACE.render();
        GL11.glTranslatef(-0.5f, 0f, 0);
        GL11.glRotatef(90, 0, 1, 0);
        GL11.glTranslatef(0.5f, 0f, 0f);
        ModelPlane.INSTNACE.render();
        GL11.glTranslatef(-1f, 0f, 0f);
        ModelPlane.INSTNACE.render();
    }
}