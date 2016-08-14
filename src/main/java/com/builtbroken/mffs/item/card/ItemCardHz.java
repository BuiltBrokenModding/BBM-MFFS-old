package com.builtbroken.mffs.item.card;

import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.google.common.hash.Hashing;
import io.netty.buffer.ByteBuf;
import com.builtbroken.mffs.ModularForceFieldSystem;
import com.builtbroken.mffs.api.card.IItemFrequency;
import com.builtbroken.mffs.item.gui.EnumGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class ItemCardHz extends ItemCard implements IItemFrequency, IPacketReceiver
{
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
    {
        list.add(LanguageUtility.getLocal("info.cardFrequency.freq") + " " + getEncodedFrequency(itemStack));
    }

    public String getEncodedFrequency(ItemStack itemStack)
    {
        return Hashing.md5().hashInt(getFrequency(itemStack)).toString();
    }

    @Override
    public int getFrequency(ItemStack itemStack)
    {
        if (itemStack != null)
        {
            if (itemStack.getTagCompound() == null)
            {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            return itemStack.getTagCompound().getInteger("frequency");
        }
        return 0;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        if (!world.isRemote)
        {
            player.openGui(ModularForceFieldSystem.instance, EnumGui.frequency.ordinal(), world, 0, 0, 0);
        }
        return itemStack;
    }

    @Override
    public void read(ByteBuf buf, EntityPlayer player, PacketType packet)
    {
        setFrequency(buf.readInt(), player.getCurrentEquippedItem());
    }

    @Override
    public void setFrequency(int frequency, ItemStack itemStack)
    {
        if (itemStack != null)
        {
            if (itemStack.getTagCompound() == null)
            {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            itemStack.getTagCompound().setInteger("frequency", frequency);
        }
    }
}
