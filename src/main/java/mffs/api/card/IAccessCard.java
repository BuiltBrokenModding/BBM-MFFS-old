package mffs.api.card;

import com.builtbroken.mc.lib.access.AccessProfile;
import net.minecraft.item.ItemStack;

/**
 * Applied to Item ID and group cards.
 *
 * @author Calclavia
 */
public interface IAccessCard extends ICard
{
	AccessProfile getAccess(ItemStack stack);

	void setAccess(ItemStack stack, AccessProfile access);
}
