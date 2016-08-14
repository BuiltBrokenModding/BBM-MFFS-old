package com.builtbroken.mffs.util;

import com.builtbroken.jlib.type.Pair;
import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;
import com.builtbroken.mffs.ModularForceFieldSystem;
import com.builtbroken.mffs.api.fortron.FrequencyGrid;
import com.builtbroken.mffs.api.machine.IProjector;
import com.builtbroken.mffs.field.TileElectromagneticProjector;
import com.builtbroken.mffs.field.mode.ItemModeCustom;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

/**
 * @see <a href=
 * "https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a>
 * for what you can and can't do with the code. Created by
 * Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class MFFSUtility
{
    /**
     * Gets the first itemStack that is an ItemBlock in this TileEntity or in
     * nearby chests.
     */
    public static ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack)
    {
        return getFirstItemBlock(tileEntity, itemStack, true);
    }

    public static ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack, boolean recur)
    {
        if (tileEntity instanceof IProjector)
        {
            IProjector projector = (IProjector) tileEntity;

            for (int slot : projector.getModuleSlots())
            {
                ItemStack stack = getFirstItemBlock(slot, projector, itemStack);
                if (stack != null)
                {
                    return stack;
                }

            }
            return null;
        }
        else if (tileEntity instanceof IInventory)
        {
            IInventory inventory = (IInventory) tileEntity;

            for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
            {
                ItemStack stack = getFirstItemBlock(slot, inventory, itemStack);
                if (stack != null)
                {
                    return stack;
                }
            }
            return null;
        }

        if (recur)
        {
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {

                Pos vector = new Pos(tileEntity).add(dir);
                TileEntity checkTile = vector.getTileEntity(tileEntity.getWorldObj());

                if (checkTile != null)
                {
                    ItemStack checkStack = getFirstItemBlock(checkTile, itemStack, false);

                    if (checkStack != null)
                    {
                        return checkStack;
                    }

                }
            }
        }
        return null;
    }

    public static ItemStack getFirstItemBlock(int i, IInventory inventory, ItemStack itemStack)
    {
        ItemStack checkStack = inventory.getStackInSlot(i);
        if (checkStack != null && checkStack.getItem() instanceof ItemBlock
                && (itemStack == null || checkStack.isItemEqual(itemStack)))
        {
            return checkStack;
        }
        return null;
    }

    public static ItemStack getCamoBlock(IProjector proj, Pos position)
    {
        if (proj instanceof TileElectromagneticProjector)
        {
            TileElectromagneticProjector projector = (TileElectromagneticProjector) proj;

            if (!projector.world().isRemote)
            {
                if (projector.getModuleCount(ModularForceFieldSystem.moduleCamouflage) > 0)
                {
                    if (projector.getMode() instanceof ItemModeCustom)
                    {
                        Map<Pos, Pair<Block, Integer>> fieldMap = ((ItemModeCustom) projector.getMode()).getFieldBlockMap(projector, projector.getModeStack());

                        if (fieldMap != null)
                        {
                            Pos fieldCenter = new Pos(projector).add(projector.getTranslation());
                            Pos relativePosition = position.subtract(fieldCenter);
                            relativePosition = new Pos(relativePosition.transform(new EulerAngle(-projector.getRotationYaw(), -projector.getRotationPitch(), 0))).round();

                            Pair<Block, Integer> blockInfo = fieldMap.get(relativePosition);

                            if (blockInfo != null && !blockInfo.left().isAir(projector.getWorldObj(), position.xi(), position.yi(), position.zi()))
                            {
                                return new ItemStack(blockInfo.left(), 1, blockInfo.right());
                            }
                        }
                    }

                    return projector.getFilterStacks().size() > 0 ? projector.getFilterStacks().get(0) : null;
                }
            }
        }
        return null;
    }

    public static Block getFilterBlock(ItemStack itemStack)
    {
        if (itemStack != null)
        {
            return getFilterBlock(itemStack.getItem());

        }
        return null;
    }

    public static Block getFilterBlock(Item item)
    {
        if (item instanceof ItemBlock)
        {
            return ((ItemBlock) item).field_150939_a;
        }
        return null;
    }

    public static boolean hasPermission(World world, Pos position, Permission permission, EntityPlayer player)
    {
        return hasPermission(world, position, permission, player.getGameProfile());
    }

    public static boolean hasPermission(World world, Pos position, Permission permission, GameProfile profile)
    {
        for (TileElectromagneticProjector projector : getRelevantProjectors(world, position))
        {
            if (projector.hasPermission(profile, permission))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermission(World world, Pos position, PlayerInteractEvent.Action action, EntityPlayer player)
    {
        for (TileElectromagneticProjector projector : getRelevantProjectors(world, position))
        {
            if (projector.isAccessGranted(world, position, player, action))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the set of projectors that have an effect in this position.
     */
    public static List<TileElectromagneticProjector> getRelevantProjectors(World world, Pos position)
    {
        return FrequencyGrid.instance().getNodes(TileElectromagneticProjector.class);
    }
}
