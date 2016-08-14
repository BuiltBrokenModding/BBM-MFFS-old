package com.builtbroken.mffs.field.gui;

import com.builtbroken.mc.core.References;
import com.builtbroken.mc.lib.transform.region.Rectangle;
import com.builtbroken.mc.lib.transform.vector.Point;
import cpw.mods.fml.common.registry.LanguageRegistry;
import com.builtbroken.mffs.base.GuiMFFS;
import com.builtbroken.mffs.base.TileFieldMatrix;

/**
 * Anything that has a field matrix within it.
 *
 * @author Calclavia
 */
public abstract class GuiMatrix<T extends TileFieldMatrix> extends GuiMFFS<T>
{

    /* This should really not be needed, try static of container class */
    Point center = null;

    public GuiMatrix(ContainerMatrix matrix, T tile)
    {
        super(matrix, tile);
        this.center = matrix.matrixCenter;
        this.baseTexture = References.GUI_BASE;
    }

    public void setupTooltips()
    {
        String north = LanguageRegistry.instance().getStringLocalization("gui.projector.north");
        String south = LanguageRegistry.instance().getStringLocalization("gui.projector.south");
        String west = LanguageRegistry.instance().getStringLocalization("gui.projector.west");
        String east = LanguageRegistry.instance().getStringLocalization("gui.projector.east");
        String up = LanguageRegistry.instance().getStringLocalization("gui.projector.up");
        String down = LanguageRegistry.instance().getStringLocalization("gui.projector.down");

        for (int i = 1; i <= 2; i++)
        {
            tooltips.put(new Rectangle(new Point(center.x(), center.y() - 18 * i), 18), north);
        }

        for (int i = 1; i <= 2; i++)
        {
            tooltips.put(new Rectangle(new Point(center.x(), center.y() + 18 * i), 18), south);
        }

        for (int i = 1; i <= 2; i++)
        {
            tooltips.put(new Rectangle(new Point(center.x() + 18 * i, center.y()), 18), east);
        }

        for (int i = 1; i <= 2; i++)
        {
            tooltips.put(new Rectangle(new Point(center.x() - 18 * i, center.y()), 18), west);
        }

        this.tooltips.put(new Rectangle(center, 18), LanguageRegistry.instance().getStringLocalization("gui.projector.mode"));

        tooltips.put(new Rectangle(new Point(center.x() - 18, center.y() - 18), 18), up);
        tooltips.put(new Rectangle(new Point(center.x() + 18, center.y() - 18), 18), up);

        tooltips.put(new Rectangle(new Point(center.x() - 18, center.y() + 18), 18), down);
        tooltips.put(new Rectangle(new Point(center.x() + 18, center.y() + 18), 18), down);
    }
}
