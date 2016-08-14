package com.builtbroken.mffs.security;

import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.lib.access.AccessUser;
import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.builtbroken.mffs.ModularForceFieldSystem;
import com.builtbroken.mffs.Settings;
import com.builtbroken.mffs.api.card.IAccessCard;
import com.builtbroken.mffs.base.TileFrequency;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileBiometricIdentifier extends TileFrequency implements IGuiTile
{
    public static final int SLOT_COPY = 12;
    /**
     * Rendering
     */
    public long lastFlicker = 0L;

    public TileBiometricIdentifier()
    {
        super("biometricIdentifier");
    }

    /**
     * 2 slots: Card copying
     * 9 x 4 slots: Access Cards
     * Under access cards we have a permission selector
     */
    @Override
    public int getSizeInventory()
    {
        return 46;
    }

    @Override
    public Tile newTile()
    {
        return new TileBiometricIdentifier();
    }

    @Override
    public void update()
    {
        super.update();
        animation += 0.1f;
    }

    @Override
    public boolean hasPermission(GameProfile profile, Permission permission)
    {
        if (!isActive() || ModularForceFieldSystem.proxy.isOp(profile) && Settings.allowOpOverride)
        {
            return true;
        }
        for (ItemStack stack : getCards())
        {
            AccessUser access = ((IAccessCard) stack.getItem()).getAccess(stack);
            if (access != null && access.hasNode(permission.toString()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getCards()
    {
        List<ItemStack> set = new ArrayList();
        for (int slot = 0; slot < getSizeInventory(); slot++)
        {
            if (getStackInSlot(slot) != null && getStackInSlot(slot).getItem() instanceof IAccessCard)
            {
                set.add(getStackInSlot(slot));
            }
        }
        return set;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        return itemStack.getItem() instanceof IAccessCard;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public List<TileBiometricIdentifier> getBiometricIdentifiers()
    {
        return Collections.singletonList(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        RenderBiometricIdentifier.render(this, pos.xf(), pos.yf(), pos.zf(), frame, isActive(), false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(ItemStack itemStack)
    {
        RenderBiometricIdentifier.render(this, -0.5, -0.5, -0.5, 0, true, true);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerBiometricIdentifier(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiBiometricIdentifier(player, this);
    }
}