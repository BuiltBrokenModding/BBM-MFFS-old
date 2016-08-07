package mffs.field.gui;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.prefab.gui.buttons.GuiIncrementButton;
import com.mojang.realmsclient.gui.ChatFormatting;
import mffs.base.GuiMFFS;
import mffs.base.TilePacketType;
import mffs.field.TileElectromagneticProjector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

public class GuiElectromagneticProjector extends GuiMFFS<TileElectromagneticProjector>
{
    GuiButton invertButton;
    TileElectromagneticProjector tile;

    public GuiElectromagneticProjector(EntityPlayer player, TileElectromagneticProjector tile)
    {
        super(new ContainerElectromagneticProjector(player, tile), tile);
        this.tile = tile;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        invertButton = new GuiButton(2, width / 2 - 73, height / 2 - 20, 45, 20, "Invert");
        buttonList.add(invertButton);

        int cx = width / 2;
        int cy = height / 2;
        //Scale buttons
        for (int i = 0; i < 6; i++)
        {
            buttonList.add(new GuiIncrementButton(3 + i, cx + 12 * i, cy - 78, true));
            buttonList.add(new GuiIncrementButton(4 + i, cx + 12 * i, cy - 60, false));
        }

        //Translation buttons
        for (int i = 0; i < 3; i++)
        {
            buttonList.add(new GuiIncrementButton(11 + i, cx + 12 * i, cy - 38, true));
            buttonList.add(new GuiIncrementButton(12 + i, cx + 12 * i, cy - 20, false));
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        invertButton.displayString = (tile.isInvertedFilter() ? ChatFormatting.GREEN : ChatFormatting.RED) + "Invert";
    }

    @Override
    public void drawGuiContainerForegroundLayer(int x, int y)
    {
        drawStringCentered(tile.getInventoryName(), this.xSize / 2, 6); //TODO translate
        drawString("Filters", 20, 20);

        drawString(tile.scalePoints + "/" + tile.scalePoints + " Scale", (this.xSize / 2), 20); //TODO translate
        drawString(tile.translationPoints + "/" + tile.translationPoints + " Translate", (this.xSize / 2), 60); //TODO translate

        for (int i = 0; i < 6; i++)
        {
            drawStringCentered("" + tile.scale[i], (this.xSize / 2) + (12 * i) + 5, 40);
        }

        drawStringCentered("" + tile.translation.xi(), (this.xSize / 2) + 5, 80);
        drawStringCentered("" + tile.translation.yi(), (this.xSize / 2) + (12 * 1) + 5, 80);
        drawStringCentered("" + tile.translation.zi(), (this.xSize / 2) + (12 * 2) + 5, 80);

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