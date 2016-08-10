package com.builtbroken.mffs.render.button;

import com.builtbroken.mc.lib.transform.vector.Point;
import com.builtbroken.mffs.base.GuiMFFS;
import com.builtbroken.mffs.production.TileFortronCapacitor;

public class GuiTransferModeButton extends GuiIndexedButton
{
    /* We need to store the capacitor here. */
    private TileFortronCapacitor mode;

    public GuiTransferModeButton(int id, int x, int y, GuiMFFS<?> mainGui, TileFortronCapacitor tile)
    {
        super(id, x, y, mainGui, "", new Point());
        this.mode = tile;
        displayString = "transferMode." + mode.getTransferMode().name();
        offset = new Point(offset.x(), 18 * mode.getTransferMode().ordinal());
    }
}