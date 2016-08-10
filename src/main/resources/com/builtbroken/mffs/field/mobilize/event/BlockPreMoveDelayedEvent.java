package com.builtbroken.mffs.field.mobilize.event;

import com.builtbroken.mc.lib.helper.BlockUtility;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mffs.api.event.EventForceMobilize;
import com.builtbroken.mffs.field.mobilize.TileForceMobilizer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
public class BlockPreMoveDelayedEvent extends DelayedEvent
{
    Location startPosition;
    Location newPosition;

    public BlockPreMoveDelayedEvent(IDelayedEventHandler handler, int ticks, Location startPosition, Location newPosition)
    {
        super(handler, ticks, null);
        this.startPosition = startPosition;
        this.newPosition = newPosition;
    }

    @Override
    protected void onEvent()
    {
        if (!startPosition.world.isRemote)
        {
            if (((TileForceMobilizer) handler).canMove(startPosition, newPosition))
            {
                TileEntity tileEntity = startPosition.getTileEntity();
                EventForceMobilize.EventPreForceManipulate evt = new EventForceMobilize.EventPreForceManipulate(startPosition.world, startPosition.xi(), startPosition.yi(), startPosition.zi(), newPosition.xi(), newPosition.yi(), newPosition.zi());
                MinecraftForge.EVENT_BUS.post(evt);

                if (!evt.isCanceled())
                {
                    Block blockID = startPosition.getBlock();
                    int blockMetadata = startPosition.getBlockMetadata();
                    BlockUtility.setBlockSneaky(startPosition.world, this.startPosition.toPos(), Blocks.air, 0, null);
                    NBTTagCompound tileData = new NBTTagCompound();
                    if (tileEntity != null)
                    {
                        tileEntity.writeToNBT(tileData);
                    }

                    handler.queueEvent(new BlockPostMoveDelayedEvent(handler, 0, this.startPosition, this.newPosition, blockID, blockMetadata, tileEntity, tileData));
                }
            }
            else
            {
                ((TileForceMobilizer) handler).failedPositions.add(startPosition.toPos());
                ((TileForceMobilizer) handler).markFailMove();
                ((TileForceMobilizer) handler).clearQueue();
            }
        }
    }
}