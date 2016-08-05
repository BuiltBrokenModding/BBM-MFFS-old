package mffs.render.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/**
 *
 */
public class GuiIcon extends GuiButton
{

    /* Renderer assigned to this button */
    private RenderItem itemRenderer = new RenderItem();

    /* Stack of icons that are assigned to this button */
    private ItemStack[] icons;

    /* Current index of the icons array */
    private byte index = 0;

    public GuiIcon(int id, int x, int y, ItemStack... icons)
    {
        super(id, x, y, 20, 20, "");
        this.icons = icons;
    }

    /**
     * @param ind
     */
    public void setIndex(int ind)
    {
        if (ind >= 0 && ind < icons.length)
        {
            this.index = (byte) ind;
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY)
    {
        super.drawButton(minecraft, mouseX, mouseY);

        if (visible && icons[index] != null)
        {
            drawItemStack(icons[index], xPosition, yPosition);
        }
    }

    protected void drawItemStack(ItemStack itemStack, int x, int y)
    {
        int renderX = x + 2;
        int renderY = y + 1;
        Minecraft mc = Minecraft.getMinecraft();
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        zLevel = 500.0F;
        itemRenderer.zLevel = 500.0F;
        itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemStack, renderX, renderY);
        itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemStack, renderX, renderY);
        zLevel = 0.0F;
        itemRenderer.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
    }
}