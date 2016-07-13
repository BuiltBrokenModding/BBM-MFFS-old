package mffs.base;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import mffs.api.modules.IModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class ItemModule extends ItemMFFS implements IModule
{
    private float fortronCost = 0.5f;

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
    {
        info.add(LanguageUtility.getLocal("info.item.fortron") + " " + new UnitDisplay(UnitDisplay.Unit.LITER, getFortronCost(1) * 20) + "/s");
        super.addInformation(itemStack, player, info, b);
    }

    @Override
    public float getFortronCost(float amplifier)
    {
        return this.fortronCost;
    }

    public ItemModule setCost(float cost)
    {
        this.fortronCost = cost;
        return this;
    }

    public ItemModule setMaxStackSize(int par1)
    {
        super.setMaxStackSize(par1);
        return this;
    }

    @Override
    public boolean requireTicks(ItemStack moduleStack)
    {
        return false;
    }
}
