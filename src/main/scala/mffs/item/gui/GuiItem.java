package mffs.item.gui;

import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/20/2016.
 */
public class GuiItem extends GuiContainerBase
{
    GuiTextField textField;

    public GuiItem(Container container)
    {
        super(container);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        textField = new GuiTextField(fontRendererObj, 50, 30, 80, 15);
    }

    @Override
    public void mouseClicked(int x, int y, int par3)
    {
        super.mouseClicked(x, y, par3);

        if (textField != null)
        {
            textField.mouseClicked(x - this.containerWidth, y - this.containerHeight, par3);
        }
    }

    @Override
    public void keyTyped(char c, int p_73869_2_)
    {
        if (p_73869_2_ == 1 || p_73869_2_ == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            super.keyTyped(c, p_73869_2_);
        }
        textField.textboxKeyTyped(c, p_73869_2_);
    }
}
