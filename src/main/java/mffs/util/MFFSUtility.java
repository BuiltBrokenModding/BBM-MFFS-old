package mffs.util;

import com.builtbroken.jlib.type.Pair;
import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;
import mffs.ModularForceFieldSystem;
import mffs.field.TileElectromagneticProjector;
import mffs.field.mode.ItemModeCustom;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import resonant.api.mffs.fortron.FrequencyGridRegistry;
import resonant.api.mffs.machine.IProjector;

import java.util.HashMap;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class MFFSUtility
{
    /**
     * Gets the first itemStack that is an ItemBlock in this TileEntity or in nearby chests.
     */
    def getFirstItemBlock(tileEntity:TileEntity, itemStack:ItemStack)

    :ItemStack=

    {
        return getFirstItemBlock(tileEntity, itemStack, true)
    }

    def getFirstItemBlock(tileEntity:TileEntity, itemStack:ItemStack, recur:Boolean)

    :ItemStack=

    {
        if (tileEntity.isInstanceOf[IProjector])
        {
            val projector = tileEntity.asInstanceOf[IProjector]

            projector.getModuleSlots().find(getFirstItemBlock(_, projector, itemStack) != null) match
            {
                case Some(entry) =>return getFirstItemBlock(entry, projector, itemStack)
                case _ =>
            }
        }
        else if (tileEntity.isInstanceOf[IInventory])
        {
            val inventory = tileEntity.asInstanceOf[IInventory]

            (0 until inventory.getSizeInventory()).view map (getFirstItemBlock(_, inventory, itemStack))
            headOption match
            {
                case Some(entry) =>return entry
                case _ =>
            }
        }

        if (recur)
        {
            ForgeDirection.VALID_DIRECTIONS.foreach(
                    direction = >
                    {
                            val vector = new Pos(tileEntity).add(direction)
                            val checkTile = vector.getTileEntity(tileEntity.getWorldObj())

            if (checkTile != null)
            {
                val checkStack:ItemStack = getFirstItemBlock(checkTile, itemStack, false)

                if (checkStack != null)
                {
                    return checkStack
                }
            }
            })
        }
        return null
    }

    def getFirstItemBlock(i:Int, inventory:IInventory, itemStack:ItemStack)

    :ItemStack=

    {
        val checkStack:ItemStack = inventory.getStackInSlot(i)
        if (checkStack != null && checkStack.getItem.isInstanceOf[ItemBlock])
        {
            if (itemStack == null || checkStack.isItemEqual(itemStack))
            {
                return checkStack
            }
        }
        return null
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

    public Block getFilterBlock(ItemStack itemStack)
    {
        if (itemStack != null)
        {
            return getFilterBlock(itemStack.getItem());

        }
        return null;
    }

    public Block getFilterBlock(Item item)
    {
        if (item instanceof ItemBlock)
        {
            return ((ItemBlock) item).field_150939_a;
        }
        return null;
    }

    public boolean hasPermission(World world, Pos position, Permission permission, EntityPlayer player)
    {
        return hasPermission(world, position, permission, player.getGameProfile());
    }

    public boolean hasPermission(World world, Pos position, Permission permission, GameProfile profile)
    {
        return getRelevantProjectors(world, position).forall(_.hasPermission(profile, permission));
    }

    public boolean hasPermission(World world, Pos position, PlayerInteractEvent.Action action, EntityPlayer player)
    {
        return getRelevantProjectors(world, position) forall(_.isAccessGranted(world, position, player, action))
    }

    /**
     * Gets the set of projectors that have an effect in this position.
     */
    public List<TileElectromagneticProjector> getRelevantProjectors(World world, Pos position)
    {
        return FrequencyGridRegistry.SERVER_INSTANCE.getNodes(TileElectromagneticProjector.class);
    }
}
