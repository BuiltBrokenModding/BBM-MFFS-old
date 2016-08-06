package mffs.field.gui;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.mojang.realmsclient.gui.ChatFormatting;
import mffs.base.TilePacketType;
import mffs.field.mobilize.TileForceMobilizer;
import mffs.render.button.GuiIcon;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GuiForceMobilizer extends GuiMatrix<TileForceMobilizer>
{
    boolean absoluteCache;


    public GuiForceMobilizer(EntityPlayer player, TileForceMobilizer tile)
    {
        super(new ContainerMatrix(player, tile), tile);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.add(new GuiIcon(1, width / 2 - 110, height / 2 - 16, new ItemStack(Items.clock)));
        buttonList.add(new GuiIcon(2, width / 2 - 110, height / 2 - 82, null, new ItemStack(Items.redstone), new ItemStack(Blocks.redstone_block)));
        buttonList.add(new GuiIcon(3, width / 2 - 110, height / 2 - 60, null, new ItemStack(Blocks.anvil)));
        buttonList.add(new GuiIcon(4, width / 2 - 110, height / 2 - 38, null, new ItemStack(Items.compass)));
        absoluteCache = tile.absoluteDirection;
        setupTooltips();
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        ((GuiIcon) buttonList.get(2)).setIndex(tile.previewMode);
        ((GuiIcon) buttonList.get(3)).setIndex(tile.doAnchor ? 1 : 0);

        //Caching so we do not need to constantly add.
        if (absoluteCache && !tile.absoluteDirection || !absoluteCache && tile.absoluteDirection)
        {
            ((GuiIcon) buttonList.get(4)).setIndex(!absoluteCache ? 1 : 0);
            absoluteCache = !absoluteCache;
            setupTooltips();
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);
        if (guiButton.id == 1)
        {

            Engine.instance.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMoe.ordinal()));
        }
        else if (guiButton.id == 2)
        {
            Engine.instance.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMode2.ordinal()));
        }
        else if (guiButton.id == 3)
        {
            Engine.instance.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMode3.ordinal()));
        }
        else if (guiButton.id == 4)
        {
            Engine.instance.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.toggleMode4.ordinal()));
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(int x, int y)
    {

        drawStringCentered(tile.getInventoryName(), this.xSize / 2, 6);

        drawString(ChatFormatting.DARK_AQUA + LanguageUtility.getLocal("gui.mobilizer.anchor") + ":", 8, 20);
        drawString(tile.anchor.getX() + ", " + tile.anchor.getY() + ", " + tile.anchor.getZ(), 8, 32);

        drawString(ChatFormatting.DARK_AQUA + LanguageUtility.getLocal("gui.direction") + ":", 8, 48);
        drawString(tile.getDirection().name(), 8, 60);

        drawString(ChatFormatting.DARK_AQUA + LanguageUtility.getLocal("gui.mobilizer.time") + ":", 8, 75);
        drawString((tile.clientMoveTime / 20) + "s", 8, 87);

        drawTextWithTooltip("fortron", ChatFormatting.DARK_RED + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost() * 20).symbol().toString() + "/s", 8, 100, x, y);
        drawFortronText(x, y);
        super.drawGuiContainerForegroundLayer(x, y);
    }
}