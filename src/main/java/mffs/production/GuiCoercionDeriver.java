package mffs.production;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.core.References;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.mojang.realmsclient.gui.ChatFormatting;
import mffs.base.GuiMFFS;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class GuiCoercionDeriver extends GuiMFFS<TileCoercionDeriver>
{

    public GuiCoercionDeriver(EntityPlayer player, TileCoercionDeriver tile)
    {
        super(new ContainerCoercionDeriver(player, tile), tile);
        this.baseTexture = References.GUI_BASE;
    }

    @Override
    public void drawGuiContainerForegroundLayer(int x, int y)
    {
        drawStringCentered(tile.getInventoryName(), this.xSize / 2, 6);
        GL11.glPushMatrix();
        GL11.glRotatef(-90, 0, 0, 1);
        drawTextWithTooltip("upgrade", -95, 140, x, y);
        GL11.glPopMatrix();

        //((GuiButton) buttonList.get(1)).displayString = LanguageUtility.getLocal(tile.isInversed ? "gui.deriver.integrate" : "gui.deriver.derive");

        drawString(ChatFormatting.AQUA + "Energy Requirement:", 8, 20);
        renderUniversalDisplay(8, 30, tile.getPower(), x, y, UnitDisplay.Unit.WATT);

        drawTextWithTooltip("progress", "%1: " + LanguageUtility.getLocal(this.tile.isActive() ? "gui.deriver.running" : "gui.deriver.idle"), 8, 60, x, y);
        drawString("Production: " + ChatFormatting.DARK_GREEN + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getProductionRate() * 20) + "/s", 8, 100);


        drawFortronText(x, y);
        super.drawGuiContainerForegroundLayer(x, y);
    }
}