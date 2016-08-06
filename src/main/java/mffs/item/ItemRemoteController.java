package mffs.item;

import com.builtbroken.jlib.data.science.units.UnitDisplay;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import mffs.api.card.ICoordLink;
import mffs.api.event.EventForceMobilize;
import mffs.api.fortron.FrequencyGrid;
import mffs.api.fortron.IBlockFrequency;
import mffs.api.fortron.IFortronFrequency;
import mffs.item.card.ItemCardHz;
import mffs.security.MFFSPermissions;
import mffs.util.MFFSUtility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemRemoteController extends ItemCardHz implements ICoordLink
{
    private final Set<ItemStack> remotesCached = new HashSet();
    private final Set<ItemStack> temporaryRemoteBlacklist = new HashSet();

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
    {
        super.addInformation(itemstack, entityplayer, list, flag);

        if (hasLink(itemstack))
        {
            Location vec = getLink(itemstack);
            Block block = vec.getBlock(entityplayer.worldObj);
            if (block != Blocks.air)
            {
                list.add(LanguageUtility.getLocal("info.item.linkedWith") + " " + block.getLocalizedName());
            }
            list.add(vec.xi() + ", " + vec.yi() + ", " + vec.zi());
            list.add(LanguageUtility.getLocal("info.item.dimension") + " '" + vec.world.provider.getDimensionName() + "'");
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

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        if (!world.isRemote && player.isSneaking())
        {
            Location vector = new Location(world, x, y, z);
            setLink(itemStack, vector);
            Block block = vector.getBlock();

            if (block != null)
            {
                player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.remoteController.linked").replaceAll("#p", x + ", " + y + ", " + z).replaceAll("#q", block.getLocalizedName())));
            }
        }
        return true;
    }

    public void clearLink(ItemStack itemStack)
    {
        if (itemStack.getTagCompound() != null)
        {
            itemStack.getTagCompound().removeTag("link");
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
    {
        if (!entityPlayer.isSneaking())
        {
            Location position = this.getLink(itemStack);
            if (position != null)
            {
                Block block = position.getBlock(world);
                if (block != Blocks.air)
                {
                    Chunk chunk = world.getChunkFromBlockCoords(position.xi(), position.zi());
                    if (chunk != null && chunk.isChunkLoaded && (MFFSUtility.hasPermission(world, position.toPos(), Action.RIGHT_CLICK_BLOCK, entityPlayer) || MFFSUtility.hasPermission(world, position.toPos(), MFFSPermissions.remoteControl, entityPlayer)))
                    {
                        double requiredEnergy = new Pos(entityPlayer).distance(position) * (FluidContainerRegistry.BUCKET_VOLUME / 100);
                        double receivedEnergy = 0;
                        List<IBlockFrequency> fortronTiles = FrequencyGrid.instance().getNodes(world, new Pos(entityPlayer), 50, this.getFrequency(itemStack));

                        for (IBlockFrequency tile : fortronTiles)
                        {
                            if (tile instanceof IFortronFrequency)
                            {
                                IFortronFrequency fortronTile = (IFortronFrequency) tile;
                                double consumedEnergy = fortronTile.requestFortron((int) Math.ceil(requiredEnergy / fortronTiles.size()), true);
                                if (consumedEnergy > 0)
                                {
                                    if (world.isRemote)
                                    {
                                        ModularForceFieldSystem.proxy.renderBeam(world, new Pos(entityPlayer).add(new Pos(0, entityPlayer.getEyeHeight() - 0.2, 0)), new Pos((TileEntity) fortronTile).add(0.5), ModularForceFieldSystem.fieldColor, 20);
                                    }
                                    receivedEnergy += consumedEnergy;
                                }
                                if (receivedEnergy >= requiredEnergy)
                                {
                                    try
                                    {
                                        block.onBlockActivated(world, position.xi(), position.yi(), position.zi(), entityPlayer, 0, 0, 0, 0);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    return itemStack;
                                }
                            }
                        }
                        if (!world.isRemote)
                        {
                            entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.remoteController.fail").replaceAll("#p", new UnitDisplay(UnitDisplay.Unit.JOULES, requiredEnergy).toString())));
                        }
                    }
                }
            }
        }
        else
        {
            super.onItemRightClick(itemStack, world, entityPlayer);
        }

        return itemStack;
    }

    @Override
    public Location getLink(ItemStack itemStack)
    {
        if (itemStack.stackTagCompound == null || !itemStack.getTagCompound().hasKey("link"))
        {
            return null;
        }
        return new Location(itemStack.getTagCompound().getCompoundTag("link"));
    }

    @SubscribeEvent
    public void preMove(EventForceMobilize.EventPreForceManipulate evt)
    {
        this.temporaryRemoteBlacklist.clear();
    }

    /**
     * Moves the coordinates of the link if the Force Manipulator moved a block that is linked by
     * the remote.
     *
     * @param evt
     */
    @SubscribeEvent
    public void onMove(EventForceMobilize.EventPostForceManipulate evt)
    {
        if (!evt.world.isRemote)
        {
            for (ItemStack itemStack : this.remotesCached)
            {
                if (!temporaryRemoteBlacklist.contains(itemStack) && (new Location(evt.world, evt.beforeX, evt.beforeY, evt.beforeZ) == this.getLink(itemStack)))
                {
                    this.setLink(itemStack, new Location(evt.world, evt.afterX, evt.afterY, evt.afterZ));
                    temporaryRemoteBlacklist.add(itemStack);
                }
            }
        }
    }

    @Override
    public void setLink(ItemStack itemStack, Location vec)
    {
        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setTag("link", vec.toNBT());
    }

}