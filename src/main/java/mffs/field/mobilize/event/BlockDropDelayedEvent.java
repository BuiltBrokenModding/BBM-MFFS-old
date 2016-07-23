package mffs.field.mobilize.event

import com.builtbroken.mc.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockDropDelayedEvent extends DelayedEvent
{
    public BlockDropDelayedEvent(IDelayedEventHandler handler, int ticks, Block block, World world, Pos pos)
    {
        super(handler, ticks);
    }

    @Override
    protected void onEvent()
    {
        if (!this.world.isRemote)
        {
            if (this.position.getBlock(this.world) eq this.block)
            {
                this.block.dropBlockAsItem(this.world, this.position.xi, this.position.yi, this.position.zi, this.position.getBlockMetadata(world), 0)
                this.position.setBlock(this.world, Blocks.air)
            }
        }
    }
}