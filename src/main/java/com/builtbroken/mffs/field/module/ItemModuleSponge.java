package com.builtbroken.mffs.field.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mffs.api.machine.IProjector;
import com.builtbroken.mffs.base.ItemModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.List;

public class ItemModuleSponge extends ItemModule
{
    public ItemModuleSponge()
    {
        setMaxStackSize(1);
    }

    @Override
    public boolean onProject(ItemStack stack, IProjector projector, List<Pos> fields)
    {
        if (projector.getTicks() % 60 == 0)
        {
            World world = ((TileEntity) projector).getWorldObj();

            if (!world.isRemote)
            {
                for (Pos point : projector.getInteriorPoints())
                {
                    Block block = point.getBlock(world);

                    if (block instanceof BlockLiquid || block instanceof BlockFluidBase)
                    {
                        point.setBlock(world, Blocks.air);
                    }
                }
            }
        }

        return super.onProject(stack, projector, fields);
    }

    @Override
    public boolean requireTicks(ItemStack moduleStack)
    {
        return true;
    }
}