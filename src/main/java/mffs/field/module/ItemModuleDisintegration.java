package mffs.field.module;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.ModularForceFieldSystem;
import mffs.api.Blacklist;
import mffs.api.machine.IProjector;
import mffs.base.ItemModule;
import mffs.base.TileMFFSInventory;
import mffs.base.TilePacketType;
import mffs.field.TileElectromagneticProjector;
import mffs.field.mobilize.event.BlockDropDelayedEvent;
import mffs.field.mobilize.event.BlockInventoryDropDelayedEvent;
import mffs.field.mobilize.event.IDelayedEventHandler;
import mffs.util.MFFSUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.List;

public class ItemModuleDisintegration extends ItemModule
{
    private int blockCount = 0; //TODO remove as this is stored for all items and could cause issues with threading

    public ItemModuleDisintegration()
    {
        setMaxStackSize(1);
        setCost(20);
    }

    @Override
    public boolean onProject(ItemStack stack, IProjector projector, List<Pos> fields)
    {
        this.blockCount = 0;
        return false;
    }

    @Override
    public int onProject(ItemStack stack, IProjector projector, Pos position)
    {
        TileElectromagneticProjector proj = (TileElectromagneticProjector) projector;
        World world = proj.world();

        Block block = position.getBlock(world);

        if (block != null)
        {
            int blockMetadata = position.getBlockMetadata(world);

            boolean filterMatch = !proj.getFilterStacks().stream().anyMatch(
                    itemStack -> MFFSUtility.getFilterBlock(itemStack) != null && (itemStack.isItemEqual(new ItemStack(block, 1, blockMetadata)) || ((ItemBlock) itemStack.getItem()).field_150939_a == block && projector.getModuleCount(ModularForceFieldSystem.moduleApproximation) > 0));

            if (proj.isInvertedFilter() != filterMatch)
            {
                return 1;
            }

            if (Blacklist.disintegrationBlacklist.contains(block) || block instanceof BlockLiquid || block instanceof IFluidBlock)
            {
                return 1;
            }

            Engine.instance.packetHandler.sendToAllInDimension(new PacketTile(proj, TilePacketType.effect.ordinal(), 2, position.xi(), position.yi(), position.zi()), world);

            if (projector.getModuleCount(ModularForceFieldSystem.moduleCollection) > 0)
            {
                proj.queueEvent(new BlockInventoryDropDelayedEvent(((IDelayedEventHandler) projector), 39, block, world, position, (TileMFFSInventory) projector));
            }
            else
            {
                proj.queueEvent(new BlockDropDelayedEvent(((IDelayedEventHandler) projector), 39, block, world, position));
            }

            blockCount += 1;

            if (blockCount >= projector.getModuleCount(ModularForceFieldSystem.moduleSpeed) / 3)
            {
                return 2;
            }
            else
            {
                return 1;
            }
        }

        return 1;
    }

    @Override
    public float getFortronCost(ItemStack stack, float amplifier)
    {
        return super.getFortronCost(stack, amplifier) + (super.getFortronCost(stack, amplifier) * amplifier);
    }

}