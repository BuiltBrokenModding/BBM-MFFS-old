package mffs.field.gui;

import com.builtbroken.mc.lib.transform.region.Rectangle;
import com.builtbroken.mc.lib.transform.vector.Point;
import com.builtbroken.mc.prefab.gui.GuiSlotType;
import cpw.mods.fml.common.registry.LanguageRegistry;
import mffs.base.GuiMFFS;
import mffs.base.TileFieldMatrix;

/**
 * Anything that has a field matrix within it.
 *
 * @author Calclavia
 */
public abstract class GuiMatrix<T extends TileFieldMatrix> extends GuiMFFS<T> {

    /* This should really not be needed, try static of container class */
    Point center = null;

    public GuiMatrix(ContainerMatrix matrix, T tile) {
        super(matrix, tile);
        this.center = matrix.matrixCenter;
    }

    /**
     * Creates the tooltips to be used.
     */
    public void setupTooltips() {
        String north = LanguageRegistry.instance().getStringLocalization("gui.projector." + (tile.absoluteDirection ? "north" : "front"));
        String south = LanguageRegistry.instance().getStringLocalization("gui.projector." + (tile.absoluteDirection ? "south" : "back"));
        String west = LanguageRegistry.instance().getStringLocalization("gui.projector." + (tile.absoluteDirection ? "west" : "left"));
        String east = LanguageRegistry.instance().getStringLocalization("gui.projector" + (tile.absoluteDirection ? "east" : "right"));
        String up = LanguageRegistry.instance().getStringLocalization("gui.projector.up");
        String down = LanguageRegistry.instance().getStringLocalization("gui.projector.down");

        for (int i = 1; i <= 2; i++)
            tooltips.put(new Rectangle(new Point(center.x(), center.y() - 18 * i), 18), north);

        for (int i = 1; i <= 2; i++)
            tooltips.put(new Rectangle(new Point(center.x(), center.y() + 18 * i), 18), south);

        for (int i = 1; i <= 2; i++)
            tooltips.put(new Rectangle(new Point(center.x() + 18 * i, center.y()), 18), east);

        for (int i = 1; i <= 2; i++)
            tooltips.put(new Rectangle(new Point(center.x() - 18 * i, center.y()), 18), west);

        this.tooltips.put(new Rectangle(center, 18), LanguageRegistry.instance().getStringLocalization("gui.projector.mode"));

        tooltips.put(new Rectangle(new Point(center.x() - 18, center.y() - 18), 18), up);
        tooltips.put(new Rectangle(new Point(center.x() + 18, center.y() - 18), 18), up);

        tooltips.put(new Rectangle(new Point(center.x() - 18, center.y() + 18), 18), down);
        tooltips.put(new Rectangle(new Point(center.x() + 18, center.y() + 18), 18), down);
    }

    public void drawMatrix() {

        drawSlot((int) center.x(), (int) center.y(), GuiSlotType.NONE, 1, 0.4F, 0.4F);

        for (int i = 1; i <= 2; i++)
            drawSlot((int) center.x(), (int) center.y() - 18 * i, GuiSlotType.ARR_UP);

        for (int i = 1; i <= 2; i++)
            drawSlot((int) center.x(), (int) center.y() + 18 * i, GuiSlotType.ARR_DOWN);

        for (int i = 1; i <= 2; i++)
            drawSlot((int) center.x() + 18 * i, (int) center.y(), GuiSlotType.ARR_RIGHT);

        for (int i = 1; i <= 2; i++)
            drawSlot((int) center.x() - 18 * i, (int) center.y(), GuiSlotType.ARR_LEFT);

        //UP
        drawSlot((int) center.x() - 18, (int) center.y() - 18, GuiSlotType.ARR_UP_LEFT);
        drawSlot((int) center.x() + 18, (int) center.y() - 18, GuiSlotType.ARR_UP_RIGHT);
        //DOWN
        drawSlot((int) center.x() - 18, (int) center.y() + 18, GuiSlotType.ARR_DOWN_LEFT);
        drawSlot((int) center.x() + 18, (int) center.y() + 18, GuiSlotType.ARR_DOWN_RIGHT);

        for (int i = -2; i <= 2; i++)
            for (int i2 = -2; i2 <= 2; i2++)
                if (Math.sqrt(i * i + i2 * i2) > 2)
                    drawSlot((int) center.x() + 18 * i, (int) center.y() + 18 * i2);
    }
}
