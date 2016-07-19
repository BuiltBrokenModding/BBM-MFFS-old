package mffs.render.button;

import ic2.core.util.Vector2;
import mffs.base.GuiMFFS
import mffs.production.TileFortronCapacitor;
import net.minecraft.client.Minecraft;

public class GuiTransferModeButton extends GuiIndexedButton
{

    /* We need to store the capacitor here. */
    private TileFortronCapacitor mode;

    public GuiTransferModeButton(int id, int x, int y, GuiMFFS mainGui, TileFortronCapacitor tile) {
            super(id, x, y, mainGui, "", new Vector2());
        }

    @Override
  public void drawButton(Minecraft minecraft, int x, int y)
  {
    displayString = "transferMode." + mode.getTransferMode().toString();
    offset.y = 18 * mode.getTransferMode().id;
    super.drawButton(minecraft, x, y);
  }
}