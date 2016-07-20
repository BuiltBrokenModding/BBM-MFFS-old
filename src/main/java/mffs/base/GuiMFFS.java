package mffs.base;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import com.mojang.realmsclient.gui.ChatFormatting;
import mffs.render.button.GuiIcon;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class GuiMFFS<MACHINE extends TileMFFS> extends GuiContainerBase {
    /* TileEntity associated with this tile */
    protected MACHINE tile;

    public GuiMFFS(Container container, MACHINE tile) {
        super(container);
        ySize = 217;
        this.tile = tile;
    }

    public GuiMFFS(Container container) {
        this(container, null);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        //Activation button
        buttonList.add(new GuiIcon(0, width / 2 - 110, height / 2 - 104, new ItemStack(Blocks.torch), new ItemStack(Blocks.redstone_torch)));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();


        if (buttonList.size() > 0 && this.buttonList.get(0) != null) {
            ((GuiIcon) buttonList.get(0)).setIndex(tile.isRedstoneActive ? 1 : 0);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        if (tile != null && button.id == 0) {
            Engine.instance.packetHandler.sendToServer(new PacketTile(tile, TilePacketType.toggleActivation.ordinal()));
        }
    }

    protected void drawFortronText(int x, int y) {
        if (tile instanceof TileFortron) {
            TileFortron fortronTile = (TileFortron) tile;
            drawTextWithTooltip("fortron", ChatFormatting.WHITE + "" + new UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortronEnergy()).symbol() + "/" + new UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortronCapacity()).symbol(), 35, 119, x, y);
        }
    }

    protected void drawFrequencyGui() {
        //Frequency Card
        drawSlot(7, 113);

        if (tile instanceof TileFortron) {
            TileFortron fortronTile = (TileFortron) tile;

            //Fortron Bar
            drawLongBlueBar(30, 115, Math.min((float) fortronTile.getFortronEnergy() / fortronTile.getFortronCapacity(), 1));
        }
    }

}