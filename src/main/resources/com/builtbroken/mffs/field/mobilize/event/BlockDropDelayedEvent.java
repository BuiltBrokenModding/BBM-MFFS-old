package com.builtbroken.mffs.field.mobilize.event;

import com.builtbroken.mc.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockDropDelayedEvent extends DelayedEvent
{
    protected World world;
    protected Block block;
    protected Pos pos;

    public BlockDropDelayedEvent(IDelayedEventHandler handler, int ticks, Block block, World world, Pos pos)
    {
        super(handler, ticks, null);
        this.world = world;
        this.block = block;
        this.pos = pos;
    }

    @Override
    protected void onEvent()
    {
        if (!this.world.isRemote)
        {
            if (this.pos.getBlock(this.world) == this.block)
            {
                this.block.dropBlockAsItem(this.world, this.pos.xi(), this.pos.yi(), this.pos.zi(), this.pos.getBlockMetadata(world), 0);
                this.pos.setBlock(this.world, Blocks.air);
            }
        }
    }
}