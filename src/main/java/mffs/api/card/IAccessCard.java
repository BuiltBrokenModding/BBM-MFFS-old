package mffs.api.card;

import com.builtbroken.mc.lib.access.AccessUser;
import net.minecraft.item.ItemStack;

/**
 * Applied to Item ID and group cards.
 *
 * @author Calclavia
 */
public interface IAccessCard extends ICard
{
    AccessUser getAccess(ItemStack stack);

    void setAccess(ItemStack stack, AccessUser access);
}
