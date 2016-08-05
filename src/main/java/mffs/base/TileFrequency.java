package mffs.base;

import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;
import mffs.Reference;
import mffs.api.card.ICoordLink;
import mffs.api.fortron.FrequencyGrid;
import mffs.item.card.ItemCardFrequency;
import mffs.security.MFFSPermissions;
import mffs.security.TileBiometricIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public abstract class TileFrequency extends TileMFFSInventory implements FrequencyGrid.IBlockFrequency
{
    public int frequencySlot = 0;

    public TileFrequency(String name)
    {
        super(name);
    }


    @Override
    public void validate()
    {
        FrequencyGrid.instance().add(this);
        super.validate();
    }

    @Override
    public void invalidate()
    {
        FrequencyGrid.instance().remove(this);
        super.invalidate();
    }

    public boolean hasPermission(GameProfile profile, Permission... permissions)
    {
        for (Permission perm : permissions)
        {
            if (!hasPermission(profile, permissions))
            {
                return false;
            }
        }
        return true;
    }

    public boolean hasPermission(GameProfile profile, Permission permission)
    {
        if (isActive())
        {
            for (TileBiometricIdentifier identifier : getBiometricIdentifiers())
            {
                if (identifier.hasPermission(profile, permission))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public List<TileBiometricIdentifier> getBiometricIdentifiers()
    {
        List<TileBiometricIdentifier> tiles = new ArrayList();
        for(ItemStack stack : getCards())
        {
            if(stack != null && stack.getItem() instanceof ICoordLink)
            {
                Location location = ((ICoordLink) stack.getItem()).getLink(stack);
                if(location != null)
                {
                    TileEntity tile = location.getTileEntity();
                    if(tile instanceof TileBiometricIdentifier)
                    {
                        tiles.add((TileBiometricIdentifier) tile);
                    }
                }
            }
        }
        return tiles;
    }

    @Override
    public int getFrequency()
    {
        ItemStack frequencyCard = getFrequencyCard();

        if (frequencyCard != null && frequencyCard.getItem() instanceof ItemCardFrequency)
        {
            return ((ItemCardFrequency) frequencyCard.getItem()).getFrequency(frequencyCard);
        }

        return 0;
    }

    @Override
    public void setFrequency(int frequency)
    {

    }

    public ItemStack getFrequencyCard()
    {
        ItemStack stack = getStackInSlot(frequencySlot);

        if (stack != null && stack.getItem() instanceof ItemCardFrequency)
        {
            return stack;
        }

        return null;
    }

    /**
     * Gets the first linked biometric identifier, based on the card slots and frequency.
     */
    public TileBiometricIdentifier getBiometricIdentifier()
    {
        if (getBiometricIdentifiers().size() > 0)
        {
            return getBiometricIdentifiers().get(0);
        }
        return null;
    }

    @Override
    public boolean onPlayerRightClickWrench(EntityPlayer player, int side, Pos hit)
    {
        if (!hasPermission(player.getGameProfile(), MFFSPermissions.configure))
        {
            player.addChatMessage(new ChatComponentText("[" + Reference.name + "]" + " Access denied!"));
            return true;
        }
        return super.onPlayerRightClickWrench(player, side, hit);
    }
}
