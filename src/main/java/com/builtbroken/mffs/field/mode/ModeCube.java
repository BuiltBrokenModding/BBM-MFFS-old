package com.builtbroken.mffs.field.mode;

import com.builtbroken.mc.lib.render.model.ModelCube;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.builtbroken.mffs.api.machine.IFieldMatrix;
import com.builtbroken.mffs.api.machine.IProjector;
import com.builtbroken.mffs.api.modules.IProjectorMode;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ModeCube implements IProjectorMode
{
    @Override
    public List<Pos> getExteriorPoints(ItemStack stack, IFieldMatrix projector)
    {
        //TODO redo as we do not need to do x^3 but could just do x * 6
        List<Pos> fieldBlocks = new ArrayList();
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale(); //Positive numbers so do -# when using

        for (int x = -negScale.xi(); x <= posScale.xi(); x += 1)
        {
            for (int y = -negScale.yi(); y <= posScale.yi(); y += 1)
            {
                for (int z = -negScale.zi(); z <= posScale.zi(); z += 1)
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
    public List<Pos> getInteriorPoints(ItemStack stack, IFieldMatrix projector)
    {
        List<Pos> fieldBlocks = new ArrayList();
        Pos posScale = projector.getPositiveScale();
        Pos negScale = projector.getNegativeScale();

        //TODO: Check parallel possibility
        for (int x = -negScale.xi(); x <= posScale.xi(); x += 1)
        {
            for (int y = -negScale.yi(); y <= posScale.yi(); y += 1)
            {
                for (int z = -negScale.zi(); z <= posScale.zi(); z += 1)
                {
                    fieldBlocks.add(new Pos(x, y, z));
                }
            }
        }

        return fieldBlocks;
    }

    @Override
    public boolean isInField(ItemStack stack, IFieldMatrix projector, Pos position)
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
    public void render(ItemStack stack, IProjector projector, double x, double y, double z, float f, long ticks)
    {
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        ModelCube.INSTNACE.render();
    }

    @Override
    public float getFortronCost(ItemStack stack, float amplifier)
    {
        return -1;
    }
}