package com.builtbroken.mffs.field.mobilize.event;

import com.builtbroken.mc.lib.helper.BlockUtility;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mffs.api.event.EventForceMobilize;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Sets the new position into the original TileEntities' block.
 *
 * @author Calclavia
 */
public class BlockPostMoveDelayedEvent extends DelayedEvent
{
    public Location startPosition;
    public Location newPosition;
    public Block block;
    public int blockMetadata;
    public TileEntity tileEntity;
    public NBTTagCompound tileData;

    public BlockPostMoveDelayedEvent(IDelayedEventHandler handler, int ticks, Location startPosition, Location newPosition, Block block, int blockMetadata, TileEntity tileEntity, NBTTagCompound tileData)
    {
        super(handler, ticks, null);
        this.startPosition = startPosition;
        this.newPosition = newPosition;
        this.block = block;
        this.blockMetadata = blockMetadata;
        this.tileEntity = tileEntity;
        this.tileData = tileData;
    }

    @Override
    protected void onEvent()
    {
        if (!startPosition.world.isRemote)
        {
            if (block != Blocks.air)
            {
                try
                {
                    if (this.tileEntity != null && this.tileData != null)
                    {
                        boolean isMultipart = Objects.equals(this.tileData.getString("id"), "savedMultipart"); //TODO update and move to handler to ensure we can support more mods
                        TileEntity newTile = null;
                        if (isMultipart)
                        {
                            try
                            {
                                Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
                                Method m = multipart.getMethod("createTileFromNBT", World.class, NBTTagCompound.class);
                                newTile = (TileEntity) m.invoke(null, startPosition.world, this.tileData);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            newTile = TileEntity.createAndLoadEntity(this.tileData);
                        }
                        BlockUtility.setBlockSneaky(newPosition.world, newPosition.toPos(), block, this.blockMetadata, newTile);
                        if (newTile != null && isMultipart)
                        {
                            try
                            {
                                Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
                                multipart.getMethod("sendDescPacket", World.class, TileEntity.class).invoke(null, startPosition.world, newTile);
                                Class tileMultipart = Class.forName("codechicken.multipart.TileMultipart");
                                tileMultipart.getMethod("onMoved").invoke(newTile);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        BlockUtility.setBlockSneaky(this.newPosition.world, this.newPosition.toPos(), block, this.blockMetadata, null);
                    }
                    this.handler.queueEvent(new BlockNotifyDelayedEvent(this.handler, 0, startPosition.world, startPosition.toPos()));
                    this.handler.queueEvent(new BlockNotifyDelayedEvent(this.handler, 0, newPosition.world, newPosition.toPos()));
                    MinecraftForge.EVENT_BUS.post(new EventForceMobilize.EventPostForceManipulate(startPosition.world, startPosition.xi(), startPosition.yi(), startPosition.zi(), newPosition.xi(), newPosition.yi(), newPosition.zi()));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}