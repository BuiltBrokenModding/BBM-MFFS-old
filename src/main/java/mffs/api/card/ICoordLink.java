package mffs.api.card;

import com.builtbroken.mc.lib.transform.vector.Location;
import net.minecraft.item.ItemStack;

public interface ICoordLink
{
    public void setLink(ItemStack itemStack, Location position);

    public Location getLink(ItemStack itemStack);
}
