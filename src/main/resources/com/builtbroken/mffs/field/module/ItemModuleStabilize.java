package com.builtbroken.mffs.field.module;

import com.builtbroken.jlib.type.Pair;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mffs.ModularForceFieldSystem;
import com.builtbroken.mffs.Reference;
import com.builtbroken.mffs.api.Blacklist;
import com.builtbroken.mffs.api.event.EventStabilize;
import com.builtbroken.mffs.api.machine.IProjector;
import com.builtbroken.mffs.base.ItemModule;
import com.builtbroken.mffs.base.TilePacketType;
import com.builtbroken.mffs.field.mode.ItemModeCustom;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.List;
import java.util.Map;

public class ItemModuleStabilize extends ItemModule
{
    private int blockCount = 0;

    public ItemModuleStabilize()
    {
        setMaxStackSize(1);
        setCost(20);
    }

    @Override
    public boolean onProject(ItemStack stack, IProjector projector, List<Pos> fields)
    {
        blockCount = 0;
        return false;
    }

    @Override
    public int onProject(ItemStack stack, IProjector projector, Pos position)
    {
        TileEntity tile = (TileEntity) projector;
        World world = tile.getWorldObj();
        Pair<Block, Integer> blockInfo = null;

        if (projector.getMode() instanceof ItemModeCustom && !(projector.getModuleCount(ModularForceFieldSystem.moduleCamouflage) > 0))
        {
            Map<Pos, Pair<Block, Integer>> fieldBlocks = ((ItemModeCustom) projector.getMode()).getFieldBlockMap(projector, projector.getModeStack());
            Pos fieldCenter = new Pos(tile).add(projector.getTranslation());
            Pos relativePosition = position.clone().subtract(fieldCenter);
            relativePosition.transform(new EulerAngle(-projector.getRotationYaw(), -projector.getRotationPitch(), 0));
            blockInfo = fieldBlocks.get(relativePosition.round());
        }

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
        {
            TileEntity tileEntity = (new Location(tile).add(direction)).getTileEntity();

            if (tileEntity instanceof IInventory)
            {
                IInventory inventory = (IInventory) tileEntity;

                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    ItemStack checkStack = inventory.getStackInSlot(i);

                    if (checkStack != null)
                    {
                        EventStabilize evt = new EventStabilize(world, position.xi(), position.yi(), position.zi(), checkStack);
                        MinecraftForge.EVENT_BUS.post(evt);

                        if (!evt.isCanceled())
                        {
                            if (checkStack.getItem() instanceof ItemBlock)
                            {
                                Block itemBlock = ((ItemBlock) checkStack.getItem()).field_150939_a;

                                if (blockInfo == null || (blockInfo.left() == itemBlock && (blockInfo.right() == checkStack.getItemDamage() || projector.getModuleCount(ModularForceFieldSystem.moduleApproximation) > 0)) || (projector.getModuleCount(ModularForceFieldSystem.moduleApproximation) > 0 && isApproximationEqual(blockInfo.left(), checkStack)))
                                {
                                    try
                                    {
                                        if (world.canPlaceEntityOnSide(itemBlock, position.xi(), position.yi(), position.zi(), false, 0, null, checkStack))
                                        {
                                            int metadata = blockInfo != null ? blockInfo.right() : checkStack.getHasSubtypes() ? checkStack.getItemDamage() : 0;
                                            Block block = blockInfo != null ? blockInfo.left() : null;

                                            if (Blacklist.stabilizationBlacklist.contains(block) || block instanceof BlockLiquid || block instanceof IFluidBlock)
                                            {
                                                return 1;
                                            }

                                            ItemStack copyStack = checkStack.copy();
                                            inventory.decrStackSize(i, 1);
                                            ((ItemBlock) copyStack.getItem()).placeBlockAt(copyStack, null, world, position.xi(), position.yi(), position.zi(), 0, 0, 0, 0, metadata);
                                            Engine.instance.packetHandler.sendToAllInDimension(new PacketTile(tile, TilePacketType.effect.ordinal(), 1, position.xi(), position.yi(), position.zi()), world);

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
                                    }
                                    catch (Exception e)
                                    {
                                        Reference.logger.error("Stabilizer failed to place item '" + checkStack + "'. The item or block may not have correctly implemented the placement methods.");
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }
                        else
                        {
                            return 1;
                        }
                    }
                }
            }
        }
        return 1;
    }

    private boolean isApproximationEqual(Block block, ItemStack checkStack)
    {
        //TODO add config to disable
        return block == Blocks.grass && ((ItemBlock) checkStack.getItem()).field_150939_a == Blocks.dirt;
    }

    @Override
    public float getFortronCost(ItemStack stack, float amplifier)
    {
        return super.getFortronCost(stack, amplifier) + (super.getFortronCost(stack, amplifier) * amplifier);
    }
}