package mffs.item.card;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.vector.Location;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.api.card.ICoordLink;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class ItemCardLink extends ItemCard implements ICoordLink
{
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
    {
        super.addInformation(itemstack, entityplayer, list, flag);

        if (hasLink(itemstack))
        {
            Location vec  = getLink(itemstack);
            Block block = vec.getBlock(entityplayer.worldObj);

            if (block != null)
            {
                list.add(LanguageUtility.getLocal("info.item.linkedWith") + " " + block.getLocalizedName());
            }

            list.add(vec.xi() + ", " + vec.yi() + ", " + vec.zi());
            list.add(LanguageUtility.getLocal("info.item.dimension") + " " + vec.world.provider.getDimensionName());
        }
        else
        {
            list.add(LanguageUtility.getLocal("info.item.notLinked"));
        }
    }

    public boolean hasLink(ItemStack itemStack)
    {
        return getLink(itemStack) != null;
    }

    public Location getLink(ItemStack itemStack)
    {
        if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("link"))
        {
            return null;
        }
        return new Location(itemStack.getTagCompound().getCompoundTag("link"));
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        if (!world.isRemote)
        {
            Location vector = new Location(world, x, y, z);
            this.setLink(itemStack, vector);

            if (vector.getBlock(world) != null)
            {
                player.addChatMessage(new ChatComponentTranslation("info.item.linkedWith", x + ", " + y + ", " + z + " - " + vector.getBlock(world).getLocalizedName()));
            }
        }
        return true;
    }

    public void setLink(ItemStack itemStack, Location vec)
    {
        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setTag("link", vec.toNBT());
    }

    public void clearLink(ItemStack itemStack)
    {
        itemStack.getTagCompound().removeTag("link");
    }
}
