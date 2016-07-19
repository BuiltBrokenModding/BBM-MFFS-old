package mffs.security.card.gui;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketPlayerItem;
import com.builtbroken.mc.lib.access.AccessUser;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import mffs.item.gui.ContainerItem;
import mffs.security.card.ItemCardIdentification;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * @author Calclavia
 */
class GuiCardID extends GuiAccessCard
{
    public GuiCardID(EntityPlayer player, ItemStack stack)
    {
        super(player, stack, new ContainerItem(player, stack));
    }

    @Override
    public void initGui()
    {
        super.initGui();
        textField.setMaxStringLength(20);

        ItemCardIdentification item = (ItemCardIdentification) stack.getItem();
        AccessUser access = item.getAccess(stack);

        if (access != null)
        {
            textField.setText(access.getName());
        }
    }

    @Override
    public void keyTyped(char c, int id)
    {
        super.keyTyped(c, id);
        Engine.instance.packetHandler.sendToServer(new PacketPlayerItem(player, 1, textField.getText()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y)
    {
        drawStringCentered(LanguageUtility.getLocal("item.mffs:cardIdentification.name"), x, y);
        textField.drawTextBox();
        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
    {
        super.drawGuiContainerBackgroundLayer(f, x, y);
    }
}
