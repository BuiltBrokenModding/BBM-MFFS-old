package com.builtbroken.mffs.render.button;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.vector.Point;
import cpw.mods.fml.client.FMLClientHandler;
import com.builtbroken.mffs.Reference;
import com.builtbroken.mffs.base.GuiMFFS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

public class GuiIndexedButton extends GuiButton
{
    /**
     * Stuck determines if the button is should render as pressed or disabled.
     */
    boolean stuck = false;
    /* Offset of this button */
    Point offset;
    /* The parent gui of this element. */
    private GuiMFFS<?> mainGui;

    public GuiIndexedButton(int id, int x, int y, GuiMFFS<?> main, String name, Point offset)
    {
        super(id, x, y, 18, 18, name);
        this.mainGui = main;
        this.offset = offset;
    }

    @Override
    public void drawButton(Minecraft minecraft, int x, int y)
    {
        if (this.visible)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(Reference.guiButtonTexture);
            if (this.stuck)
            {
                GL11.glColor4f(0.6f, 0.6f, 0.6f, 1);
            }
            else if (this.isPointInRegion(this.xPosition, this.yPosition, this.width, this.height, x, y))
            {
                GL11.glColor4f(0.85f, 0.85f, 0.85f, 1);
            }
            else
            {
                GL11.glColor4f(1, 1, 1, 1);
            }
            this.drawTexturedModalRect(this.xPosition, this.yPosition, (int) this.offset.x(), (int) this.offset.y(), this.width, this.height);
            this.mouseDragged(minecraft, x, y);
        }
    }

    @Override
    protected void mouseDragged(Minecraft minecraft, int x, int y)
    {
        if (this.mainGui != null && this.displayString != null && this.displayString.length() > 0)
        {
            if (this.isPointInRegion(this.xPosition, this.yPosition, this.width, this.height, x, y))
            {
                String title = LanguageUtility.getLocal("gui." + this.displayString + ".name");
                this.mainGui.tooltip = LanguageUtility.getLocal("gui." + this.displayString + ".tooltip");
                if (title != null && title.length() > 0)
                {
                    this.mainGui.tooltip = title + ": " + this.mainGui.tooltip;
                }
            }
        }
    }

    protected boolean isPointInRegion(int x, int y, int w, int h, int mouseX, int mouseY)
    {
        return mouseX >= x - 1 && mouseX < x + w + 1 && mouseY >= y - 1 && mouseY < y + h + 1;
    }

}