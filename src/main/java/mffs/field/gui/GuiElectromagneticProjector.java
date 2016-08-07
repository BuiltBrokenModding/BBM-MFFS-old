package mffs.field.gui;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.mojang.realmsclient.gui.ChatFormatting;
import mffs.base.TilePacketType;
import mffs.field.TileElectromagneticProjector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

public class GuiElectromagneticProjector extends GuiMatrix<TileElectromagneticProjector>
{
    public GuiElectromagneticProjector(EntityPlayer player, TileElectromagneticProjector tile)
    {
        super(new ContainerElectromagneticProjector(player, tile), tile);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.add(new GuiButton(2, width / 2 - 73, height / 2 - 20, 45, 20, "Invert"));
        setupTooltips();
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        ((GuiButton) buttonList.get(2)).displayString = (tile.isInvertedFilter() ? ChatFormatting.GREEN : ChatFormatting.RED) + "Invert";
    }

    @Override
    public void drawGuiContainerForegroundLayer(int x, int y)
    {
        drawStringCentered(tile.getInventoryName(), this.xSize / 2, 6);
        drawString("Filters", 20, 20);

        drawFortronText(x, y);
        drawString(ChatFormatting.RED + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost() * 20).symbol().toString() + "/s", 120, 119);
        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);
        if (guiButton.id == 2)
        {
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.toggleMode2.ordinal()));
        }
    }

}