package mffs.item.gui;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketPlayerItem;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import mffs.Settings;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import scala.util.Random;


/**
 * @author Calclavia
 */
public class GuiFrequency extends GuiItem
{

    ItemStack item;
    EntityPlayer player;

    public GuiFrequency(EntityPlayer player, ItemStack stack)
    {
        super(stack, new ContainerFrequency(player, stack));
        this.player = player;
        this.item = stack;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y)
    {
        drawStringCentered(LanguageUtility.getLocal("item.mffs:cardFrequency.name"), this.xSize / 2, 0);
        drawStringCentered("" + ((ItemCardFrequency) item.getItem()).getEncodedFrequency(item), this.xSize / 2, 20);
        textField.drawTextBox();
        drawStringCentered(LanguageUtility.getLocal("gui.makecopy"), this.xSize / 2, 80);

        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
    {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        drawSlot(80, 100);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);

        if (guiButton.id == 1)
        {
            int ranFreq = new Random().nextInt((int) Math.pow(10, (Settings.maxFrequencyDigits - 1)));
            textField.setText(ranFreq + "");
            ((ItemCardFrequency) item.getItem()).setFrequency(ranFreq, item);
        }
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.add(new GuiButton(1, width / 2 - 50, height / 2 - 60, 110, 20, LanguageUtility.getLocal("gui.frequency.random")));
        textField.setMaxStringLength(Settings.maxFrequencyDigits);
    }

    @Override
    public void keyTyped(char key, int p_73869_2_)
    {
        super.keyTyped(key, p_73869_2_);

        try
        {
            //TODO: Disallow any special characters and etc.
            int newFreq = Math.abs(Integer.parseInt(textField.getText()));
            Engine.instance.packetHandler.sendToServer(new PacketPlayerItem(player, newFreq));
            ((ItemCardFrequency) item.getItem()).setFrequency(newFreq, item);
        }
        catch (NumberFormatException ex)
        {
            ex.printStackTrace();
        }
    }

}
