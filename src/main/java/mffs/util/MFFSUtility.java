package mffs.util;

import java.util.HashMap;
import java.util.List;

import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;

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

/**
 * @see <a href=
 *      "https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a>
 *      for what you can and can't do with the code. Created by
 *      Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class MFFSUtility {
	/**
	 * Gets the first itemStack that is an ItemBlock in this TileEntity or in
	 * nearby chests.
	 */
	public ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack) {
		return getFirstItemBlock(tileEntity, itemStack, true);
	}

	public ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack, Boolean recur) {
        if (tileEntity instanceof IProjector) {
        	IProjector projector = (IProjector)tileEntity;

            projector.getModuleSlots().find(getFirstItemBlock(_, projector, itemStack) != null) match
            {
                case Some(entry) =>return getFirstItemBlock(entry, projector, itemStack)
                case _ =>
            }
        }
        else if (tileEntity instanceof IInventory)
        {
        	IInventory inventory = (IInventory)tileEntity;

            (0 until inventory.getSizeInventory()).view map (getFirstItemBlock(_, inventory, itemStack))
            headOption match
            {
                case Some(entry) =>return entry
                case _ =>
            }
        }

        if (recur)
        {
            for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    
                            Pos vector = new Pos(tileEntity).add(direction);
                            TileEntity checkTile = vector.getTileEntity(tileEntity.getWorldObj());

            if (checkTile != null)
            {
                ItemStack checkStack = getFirstItemBlock(checkTile, itemStack, false);

                if (checkStack != null)
                   return checkStack;
                
            }
            }
        }
        return null;
    }

	public ItemStack getFirstItemBlock(int i, IInventory inventory, ItemStack itemStack) {
		ItemStack checkStack = inventory.getStackInSlot(i);
		if (checkStack != null && checkStack.getItem() instanceof ItemBlock
				&& (itemStack == null || checkStack.isItemEqual(itemStack)))
			return checkStack;
		return null;
	}

	public ItemStack getCamoBlock(IProjector proj, Pos position)
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
                        HashMap<Pos, Pair<Block, Integer>> fieldMap = ((ItemModeCustom) projector.getMode()).getFieldBlockMap(projector, projector.getModeStack());

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

                   return projector.getFilterStacks().size() > 0 ? projector.getFilterStacks().get(0);
                }
            }
        }
        return null;
    }

	public Block getFilterBlock(ItemStack itemStack) {
		if (itemStack != null) {
			return getFilterBlock(itemStack.getItem());

		}
		return null;
	}

	public Block getFilterBlock(Item item) {
		if (item instanceof ItemBlock) {
			return ((ItemBlock) item).field_150939_a;
		}
		return null;
	}

	public boolean hasPermission(World world, Pos position, Permission permission, EntityPlayer player) {
		return hasPermission(world, position, permission, player.getGameProfile());
	}

	public boolean hasPermission(World world, Pos position, Permission permission, GameProfile profile) {
		return getRelevantProjectors(world, position).forall(_.hasPermission(profile, permission));
	}

	public boolean hasPermission(World world, Pos position, PlayerInteractEvent.Action action, EntityPlayer player)
    {
        return getRelevantProjectors(world, position) forall(_.isAccessGranted(world, position, player, action))
    }

	/**
	 * Gets the set of projectors that have an effect in this position.
	 */
	public List<TileElectromagneticProjector> getRelevantProjectors(World world, Pos position) {
		return FrequencyGridRegistry.SERVER_INSTANCE.getNodes(TileElectromagneticProjector.class);
	}
}
