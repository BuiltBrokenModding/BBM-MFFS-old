package mffs.field.gui;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.mojang.realmsclient.gui.ChatFormatting;
import mffs.base.TilePacketType;
import mffs.field.TileElectromagneticProjector;
import mffs.render.button.GuiIcon;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class GuiElectromagneticProjector extends GuiMatrix<TileElectromagneticProjector>
{

    boolean absoluteCache;

    public GuiElectromagneticProjector(EntityPlayer player, TileElectromagneticProjector tile)
    {
        super(new ContainerElectromagneticProjector(player, tile), tile);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.add(new GuiIcon(1, width / 2 - 110, height / 2 - 82, null, new ItemStack(Items.compass)));
        buttonList.add(new GuiButton(2, width / 2 - 73, height / 2 - 20, 45, 20, "Invert"));
        setupTooltips();
        absoluteCache = tile.absoluteDirection;
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        //Caching so we do not need to constantly add.
        if (absoluteCache && !tile.absoluteDirection || !absoluteCache && tile.absoluteDirection)
        {
            ((GuiIcon) buttonList.get(1)).setIndex(!absoluteCache ? 1 : 0);
            absoluteCache = !absoluteCache;
            setupTooltips();
        }
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

        if (guiButton.id == 1)
        {
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.toggleMode4.ordinal()));
        }

        if (guiButton.id == 2)
        {
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.toggleMode2.ordinal()));
        }
    }

}