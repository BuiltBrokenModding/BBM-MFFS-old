package mffs.field;

import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.machine.IForceField;
import mffs.api.machine.IProjector;
import mffs.api.modules.IModule;
import mffs.security.MFFSPermissions;
import mffs.security.TileBiometricIdentifier;
import mffs.util.MFFSUtility;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;

public class TileForceField extends Tile implements IPacketIDReceiver, IForceField
{
    private ItemStack camoStack = null;
    private Pos projector = null;

    public TileForceField()
    {
        super("ForceField", Material.glass); //TODO get tile name
        hardness = -1;
        resistance = Float.MAX_VALUE;
        creativeTab = null;
        isOpaque = false;
        this.renderNormalBlock = false;
    }

    @Override
    public Tile newTile()
    {
        return new TileForceField();
    }

    @Override
    public boolean canSilkHarvest(EntityPlayer player, int metadata)
    {
        return false;
    }

    @Override
    public int quantityDropped(int meta, int fortune)
    {
        return 0;
    }

    @Override
    public int getRenderBlockPass()
    {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public boolean renderStatic(RenderBlocks renderer, Pos pos, int pass)
    {
        int renderType = 0;
        Block camoBlock = null;
        TileEntity tileEntity = getAccess().getTileEntity(xi(), yi(), zi());

        if (camoStack != null && camoStack.getItem() instanceof ItemBlock)
        {
            camoBlock = ((ItemBlock) camoStack.getItem()).field_150939_a;

            if (camoBlock != null)
            {
                renderType = camoBlock.getRenderType();
            }
        }

        if (renderType >= 0)
        {
            try
            {
                if (camoBlock != null)
                {
                    renderer.setRenderBoundsFromBlock(camoBlock);
                }

                switch (renderType)
                {
                    case 4:
                        renderer.renderBlockLiquid(camoBlock, xi(), yi(), zi());
                        break;
                    case 31:
                        renderer.renderBlockLog(camoBlock, xi(), yi(), zi());
                        break;
                    case 1:
                        renderer.renderCrossedSquares(camoBlock, xi(), yi(), zi());
                        break;
                    case 20:
                        renderer.renderBlockVine(camoBlock, xi(), yi(), zi());
                        break;
                    case 39:
                        renderer.renderBlockQuartz(camoBlock, xi(), yi(), zi());
                        break;
                    case 5:
                        renderer.renderBlockRedstoneWire(camoBlock, xi(), yi(), zi());
                        break;
                    case 13:
                        renderer.renderBlockCactus(camoBlock, xi(), yi(), zi());
                        break;
                    case 23:
                        renderer.renderBlockLilyPad(camoBlock, xi(), yi(), zi());
                        break;
                    case 6:
                        renderer.renderBlockCrops(camoBlock, xi(), yi(), zi());
                        break;
                    case 7:
                        renderer.renderBlockDoor(camoBlock, xi(), yi(), zi());
                        break;
                    case 12:
                        renderer.renderBlockLever(camoBlock, xi(), yi(), zi());
                        break;
                    case 29:
                        renderer.renderBlockTripWireSource(camoBlock, xi(), yi(), zi());
                        break;
                    case 30:
                        renderer.renderBlockTripWire(camoBlock, xi(), yi(), zi());
                        break;
                    case 14:
                        renderer.renderBlockBed(camoBlock, xi(), yi(), zi());
                        break;
                    case 16:
                        renderer.renderPistonBase(camoBlock, xi(), yi(), zi(), false);
                        break;
                    case 17:
                        renderer.renderPistonExtension(camoBlock, xi(), yi(), zi(), true);
                        break;
                    default:
                        super.renderStatic(renderer, pos, pass);
                }
            }
            catch (Exception e)
            {
                if (camoStack != null && camoBlock != null)
                {
                    renderer.renderBlockAsItem(camoBlock, camoStack.getItemDamage(), 1);
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Block Logic
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(int side)
    {
        if (camoStack != null)
        {
            try
            {
                Block block = ((ItemBlock) camoStack.getItem()).field_150939_a;
                return block.shouldSideBeRendered(getAccess(), xi(), yi(), zi(), side);
            }
            catch (Exception e)
            {
                e.printStackTrace();

            }
            return true;
        }

        return getAccess().getBlock(xi(), yi(), zi()) == getBlockType() ? false : super.shouldSideBeRendered(side);
    }

    @Override
    public boolean onPlayerLeftClick(EntityPlayer player)
    {
        IProjector projector = getProjector();

        if (projector != null)
        {
            return projector.getModuleStacks(projector.getModuleSlots()).stream().allMatch(stack -> ((IModule)stack.getItem()).onCollideWithForceField(world(), xi(), yi(), zi(), player, stack));
        }
        return true;
    }

    public Iterable<Cube> getCollisionBoxes(Cube intersect, Entity entity)
    {
        //TODO: Check if the entity filter actually works...
        TileElectromagneticProjector projector = getProjector();

        if (projector != null && entity instanceof EntityPlayer)
        {
            TileBiometricIdentifier biometricIdentifier = projector.getBiometricIdentifier();
            EntityPlayer entityPlayer = (EntityPlayer) entity;

            if (entityPlayer.isSneaking())
            {
                if (entityPlayer.capabilities.isCreativeMode)
                {
                    return null;
                }
                else if (biometricIdentifier != null)
                {
                    if (biometricIdentifier.hasPermission(entityPlayer.getGameProfile(), MFFSPermissions.forceFieldWarp))
                    {
                        return null;
                    }
                }
            }
        }

        return super.getCollisionBoxes(intersect, entity);
    }

    public void collide(Entity entity)
    {
        TileElectromagneticProjector projector = getProjector();

        if (projector != null)
        {
            if (!projector.getModuleStacks(projector.getModuleSlots()).stream().allMatch(stack -> ((IModule)stack.getItem()).onCollideWithForceField(world(), xi(), yi(), zi(), entity, stack)))
            {
                return;
            }

            TileBiometricIdentifier biometricIdentifier = projector.getBiometricIdentifier();

            if (toPos().distance(new Pos(entity)) < 0.5)
            {
                if (!world().isRemote && entity instanceof EntityLiving)
                {
                    EntityLiving entityLiving = (EntityLiving) entity;

                    //TODO move effect to shock damage
                    //TODO add config for effects to be turned off
                    entityLiving.addPotionEffect(new PotionEffect(Potion.confusion.id, 4 * 20, 3));
                    entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 1));

                    if (entity instanceof EntityPlayer)
                    {
                        EntityPlayer player = (EntityPlayer) entity;

                        if (player.isSneaking())
                        {
                            if (player.capabilities.isCreativeMode)
                            {
                                return;
                            }
                            else if (biometricIdentifier != null)
                            {
                                if (biometricIdentifier.hasPermission(player.getGameProfile(), MFFSPermissions.forceFieldWarp))
                                {
                                    return;
                                }
                            }
                        }
                    }

                    entity.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 100);
                }
            }
        }

    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side)
    {
        if (camoStack != null)
        {
            try
            {
                Block block = ((ItemBlock) camoStack.getItem()).field_150939_a;
                IIcon icon = block.getIcon(side, camoStack.getItemDamage());

                if (icon != null)
                {
                    return icon;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return super.getIcon(side);
    }

    @Override
    public int getColorMultiplier()
    {
        if (camoStack != null)
        {
            try
            {
                return ((ItemBlock)camoStack.getItem()).field_150939_a.colorMultiplier(getAccess(), xi(), yi(), zi());
            }
            catch(Exception e)
                {
                    e.printStackTrace();
                }

        }
        return super.getColorMultiplier();
    }

    @Override
    public int getLightValue()
    {
        try
        {
            TileElectromagneticProjector projector = getProjectorSafe();
            if (projector != null)
            {
                return (int)((float)(Math.min(projector.getModuleCount(ModularForceFieldSystem.moduleGlow), 64) / 64) * 15f);
            }
        }
        catch(Exception e)
            {
                e.printStackTrace();
            }


        return 0;
    }

    @Override
    public float getExplosionResistance(Entity entity)
    {
        return Float.MAX_VALUE;
    }

    @Override
    public void weakenForceField(int energy)
    {
        IProjector projector = getProjector();

        if (projector != null)
        {
            projector.provideFortron(energy, true);
        }

        if (!world().isRemote)
        {
            world().setBlockToAir(xi(), yi(), zi());
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target)
    {
        return null;
    }

    /**
     * Tile Logic
     */
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public PacketTile getDescPacket()
    {
        if (getProjector() != null)
        {
            if (camoStack != null)
            {
                NBTTagCompound nbt = new NBTTagCompound();
                camoStack.writeToNBT(nbt);
                return new PacketTile(this, projector.xi(), projector.yi(), projector.zi(), true, nbt);
            }

            return new PacketTile(this, projector.xi(), projector.yi(), projector.zi(), false);
        }
        return null;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
    {

        setProjector(new Pos(buf.readInt(), buf.readInt(), buf.readInt()));
        markRender();
        camoStack = null;

        if (buf.readBoolean())
        {
            camoStack = ItemStack.loadItemStackFromNBT(ByteBufUtils.readTag(buf));
        }
        return true;
    }

    public void setProjector(Pos position)
    {
        projector = position;

        if (!world().isRemote)
        {
            refreshCamoBlock();
        }
    }

    /**
     * Server Side Only
     */
    public void refreshCamoBlock()
    {
        if (getProjectorSafe() != null)
        {
            camoStack = MFFSUtility.getCamoBlock(getProjector(), toPos());
        }
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        projector = new Pos(nbt.getCompoundTag("projector"));
    }

    /**
     * Writes a tile entity to NBT.
     */
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (getProjector() != null)
        {
            nbt.setTag("projector", projector.toNBT());
        }
    }

    /**
     * @return Gets the projector block controlling this force field. Removes the force field if no
     * projector can be found.
     */
    public TileElectromagneticProjector getProjector()
    {
        if (this.getProjectorSafe() != null)
        {
            return getProjectorSafe();
        }

        if (!this.worldObj.isRemote)
        {
            world().setBlock(xCoord, yCoord, zCoord, Blocks.air);
        }

        return null;
    }

    public TileElectromagneticProjector getProjectorSafe()
    {
        if (projector != null)
        {
            TileEntity projTile = projector.getTileEntity(world());

            if (projTile instanceof TileElectromagneticProjector)
            {
                IProjector projector = (IProjector) projTile;
                if (world().isRemote || (projector.getCalculatedField() != null && projector.getCalculatedField().contains(toPos())))
                {
                    return (TileElectromagneticProjector) projTile;
                }
            }
        }
        return null;
    }
}