package com.builtbroken.mffs.security.card;

import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.access.AccessUser;
import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.access.Permissions;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.NBTUtility;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import com.builtbroken.mffs.ModularForceFieldSystem;
import com.builtbroken.mffs.item.gui.EnumGui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class ItemCardIdentification extends ItemCardAccess implements IPacketIDReceiver
{
    @Override
    public boolean hitEntity(ItemStack itemStack, EntityLivingBase entityLiving, EntityLivingBase par3EntityLiving)
    {
        if (entityLiving instanceof EntityPlayer)
        {
            AccessUser user = new AccessUser(((EntityPlayer) entityLiving));
            setAccess(itemStack, user);
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
    {
        final AccessUser access = getAccess(itemStack);

        if (access != null)
        {
            info.add(LanguageUtility.getLocal("info.cardIdentification.username") + " " + access.getName());
        }
        else
        {
            info.add(LanguageUtility.getLocal("info.cardIdentification.empty"));
        }

    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        if (!world.isRemote)
        {
            if (player.isSneaking())
            {
                setAccess(itemStack, new AccessUser(player));
            }
            else
            {
                player.openGui(ModularForceFieldSystem.instance, EnumGui.cardID.ordinal(), world, 0, 0, 0);
            }
        }

        return itemStack;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        ItemStack itemStack = player.getCurrentEquippedItem();
        AccessUser access = getAccess(itemStack);

        //Permission toggle packet
        if (id == 0)
        {
            Permission perm = Permissions.find(ByteBufUtils.readUTF8String(buf));

            if (access == null)
            {
                access = new AccessUser(player);
            }

            if (perm != null)
            {
                if (access.hasNode(perm))
                {
                    access.removeNode(perm);
                }
                else
                {
                    access.addNode(perm);
                }
            }
            //Update entry in NBT
            setAccess(itemStack, access);
            return true;
        }
        //Username change packet
        else if (id == 1)
        {
            String name = ByteBufUtils.readUTF8String(buf);
            //TODO look up player by name to get UUID for better permission control
            //Update entry in NBT
            setAccess(itemStack, access.copyToNewUser(name));
            return true;
        }
        return false;
    }

    @Override
    public AccessUser getAccess(ItemStack itemStack)
    {
        NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);

        if (nbt != null)
        {
            return AccessUser.loadFromNBT(nbt);
        }
        return null;
    }
}