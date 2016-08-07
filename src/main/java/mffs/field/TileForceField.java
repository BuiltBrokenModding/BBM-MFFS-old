package mffs.field;

import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.Reference;
import mffs.api.machine.IForceField;
import mffs.api.machine.IProjector;
import mffs.api.modules.IModule;
import mffs.security.MFFSPermissions;
import mffs.security.TileBiometricIdentifier;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;

public class TileForceField extends Tile implements IPacketIDReceiver, IForceField
{
    //private ItemStack camoStack = null;
    private Pos projector = null;

    private static IIcon icon;

    public TileForceField()
    {
        super("forceField", Material.glass); //TODO get tile name
        hardness = -1;
        resistance = Float.MAX_VALUE;
        creativeTab = null;
        isOpaque = false;
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

    @Override
    public boolean onPlayerLeftClick(EntityPlayer player)
    {
        IProjector projector = getProjector();

        if (projector != null)
        {
            return projector.getModuleStacks(projector.getModuleSlots()).stream().allMatch(stack -> ((IModule) stack.getItem()).onCollideWithForceField(world(), xi(), yi(), zi(), player, stack));
        }
        return true;
    }

    @Override
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

    @Override
    public void onCollide(Entity entity)
    {
        TileElectromagneticProjector projector = getProjector();

        if (projector != null)
        {
            if (!projector.getModuleStacks(projector.MODULE_SLOTS).stream().allMatch(stack -> ((IModule) stack.getItem()).onCollideWithForceField(world(), xi(), yi(), zi(), entity, stack)))
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

    @Override
    public int getLightValue()
    {
        try
        {
            TileElectromagneticProjector projector = getProjectorSafe();
            if (projector != null)
            {
                return (int) ((float) (Math.min(projector.getModuleCount(ModularForceFieldSystem.moduleGlow), 64) / 64) * 15f);
            }
        }
        catch (Exception e)
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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg)
    {
        icon = reg.registerIcon(Reference.prefix + "forceField");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon()
    {
        return icon;
    }

    /**
     * Tile Logic
     */
    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public PacketTile getDescPacket()
    {
        if (getProjector() != null)
        {
            return new PacketTile(this, 0, projector.xi(), projector.yi(), projector.zi());
        }
        return null;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
    {
        if (id == 0)
        {
            setProjector(new Pos(buf.readInt(), buf.readInt(), buf.readInt()));
            markRender();
            return true;
        }
        return false;
    }

    public void setProjector(Pos position)
    {
        projector = position;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        projector = new Pos(nbt.getCompoundTag("projector"));
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
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
    @Override
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