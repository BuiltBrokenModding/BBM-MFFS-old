package com.builtbroken.mffs.field.mobilize.event;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mffs.base.TileMFFSInventory;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class BlockInventoryDropDelayedEvent extends BlockDropDelayedEvent
{
    TileMFFSInventory projector;

    public BlockInventoryDropDelayedEvent(IDelayedEventHandler handler, int ticks, Block block, World world, Pos position, TileMFFSInventory projector)
    {
        super(handler, ticks, block, world, position);
        this.projector = projector;
    }

    @Override
    protected void onEvent()
    {
        if (!world.isRemote)
        {
            if (pos.getBlock(this.world) == block)
            {
                List<ItemStack> itemStacks = block.getDrops(this.world, this.pos.xi(), this.pos.yi(), this.pos.zi(), this.pos.getBlockMetadata(world), 0);
                for (ItemStack itemStack : itemStacks)
                {
                    projector.mergeIntoInventory(itemStack);
                }
                pos.setBlock(world, Blocks.air);
            }
        }
    }
}