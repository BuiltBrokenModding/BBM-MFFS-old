package mffs.field.gui;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.gui.buttons.GuiIncrementButton;
import com.mojang.realmsclient.gui.ChatFormatting;
import mffs.base.GuiMFFS;
import mffs.base.TilePacketType;
import mffs.field.TileElectromagneticProjector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import java.awt.*;

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
            buttonList.add(new GuiIncrementButton(3 + i, cx + 12 * i, cy - 78, true)); // 3 4 5 6 7 8
            buttonList.add(new GuiIncrementButton(9 + i, cx + 12 * i, cy - 60, false)); // 9 10 11 12 13 14
        }

        //Translation buttons
        for (int i = 0; i < 3; i++)
        {
            buttonList.add(new GuiIncrementButton(15 + i, cx + 12 * i, cy - 28, true)); // 15 16 17
            buttonList.add(new GuiIncrementButton(18 + i, cx + 12 * i, cy - 10, false)); //18 19 20
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        invertButton.displayString = (tile.isInvertedFilter() ? ChatFormatting.GREEN : ChatFormatting.RED) + "Invert";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
    {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        int cx = guiLeft + 88;
        int cy = guiTop + 40;

        int color = new Color(68, 69, 86).getRGB();
        //Left line
        //drawRect(cx - 3, cy - 25, cx - 2, cy + 25, color);
        //Right line
        //drawRect(cx + 71, cy - 25, cx + 72, cy + 25, color);
        //Top
        //drawRect(cx - 3, cy - 25, cx + 72, cy - 24, color);
        //Bottom
        //drawRect(cx - 3, cy + 25, cx + 72, cy + 24, color);

        drawRect(cx - 3, cy - 25, cx + 72, cy + 25, color);
        drawRect(cx - 1, cy - 23, cx + 70, cy + 23, new Color(31, 153, 160).getRGB());

        drawRect(cx - 3, cy + 27, cx + 72, cy + 75, color);
        drawRect(cx - 1, cy + 29, cx + 70, cy + 73, new Color(31, 153, 160).getRGB());

        for (int i = 0; i < 6; i++)
        {
            drawRect(cx + (12 * i), cy, cx + 9 + (12 * i), cy + 9, Color.gray.getRGB());
        }

        cy += 50;
        for (int i = 0; i < 3; i++)
        {
            drawRect(cx + (12 * i), cy, cx + 9 + (12 * i), cy + 9, Color.gray.getRGB());
        }
        inventorySlots.inventorySlots.stream().forEach(s -> drawSlot((Slot) s));
    }

    @Override
    public void drawGuiContainerForegroundLayer(int x, int y)
    {
        drawStringCentered("Projector", this.xSize / 2, 6); //TODO translate
        drawString("Filters", 8, 20, Color.white.getRGB());  //TODO translate
        drawString("Mode", 48, 30, Color.white.getRGB());  //TODO translate

        drawString(tile.scalePoints + "/" + tile.scalePoints + " Scale", (this.xSize / 2), 20, Color.white.getRGB()); //TODO translate
        drawString(tile.translationPoints + "/" + tile.translationPoints + " Translate", (this.xSize / 2), 70, Color.white.getRGB()); //TODO translate

        int cx = this.xSize / 2;
        for (int i = 0; i < 6; i++)
        {
            drawStringCentered("" + tile.scale[i], cx + (12 * i) + 5, 41, Color.white.getRGB());
        }

        drawStringCentered("" + tile.translation.xi(), (this.xSize / 2) + 5, 91, Color.white.getRGB());
        drawStringCentered("" + tile.translation.yi(), (this.xSize / 2) + (12 * 1) + 5, 91, Color.white.getRGB());
        drawStringCentered("" + tile.translation.zi(), (this.xSize / 2) + (12 * 2) + 5, 91, Color.white.getRGB());

        drawFortronText(x, y);
        drawString(ChatFormatting.RED + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost() * 20).symbol().toString() + "/s", 120, 119);
        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);
        final int id = guiButton.id;
        if (id == 2)
        {
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.toggleMode2.ordinal()));
        }
        else if (id >= 3 && id <= 8)
        {
            //TODO block increase when at max
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.increase_scale.ordinal(), guiButton.id - 3));
        }
        else if (id >= 9 && id <= 14)
        {
            int direction = guiButton.id - 9;
            if (tile.scale[direction] > 0)
            {
                Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.decrease_scale.ordinal(), direction));
            }
        }
        else if (id >= 15 && id <= 17)
        {
            Pos pos;
            switch (id)
            {
                case 15:
                    pos = tile.translation.add(1, 0, 0);
                    break;
                case 16:
                    pos = tile.translation.add(0, 1, 0);
                    break;
                default:
                    pos = tile.translation.add(0, 0, 1);
                    break;
            }
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.translate.ordinal(), pos));
        }
        else if (id >= 18 && id <= 20)
        {
            Pos pos;
            switch (id)
            {
                case 15:
                    pos = tile.translation.sub(1, 0, 0);
                    break;
                case 16:
                    pos = tile.translation.sub(0, 1, 0);
                    break;
                default:
                    pos = tile.translation.sub(0, 0, 1);
                    break;
            }
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.translate.ordinal(), pos));
        }
    }
}