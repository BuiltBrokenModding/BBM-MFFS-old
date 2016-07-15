package mffs.base;

import com.builtbroken.mc.api.tile.IPlayerUsing;
import com.builtbroken.mc.api.tile.IRemovable;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.ICamouflageMaterial;
import mffs.api.machine.IActivatable;
import mffs.item.card.ItemCardLink;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A base tile class for all MFFS blocks to inherit.
 *
 * @author Calclavia
 */
public abstract class TileMFFS extends Tile implements ICamouflageMaterial, IPacketIDReceiver, IActivatable, IPlayerUsing, IRemovable.ISneakPickup
{
    /**
     * Used for client side animations.
     */
    protected float animation = 0f;
    /**
     * Is this machine switched on internally via GUI?
     */
    public boolean isRedstoneActive = false;
    /**
     * Is the machine active and working?
     */
    private boolean active = false;

    public TileMFFS(String name)
    {
        super(name, Material.iron);
        hardness = Float.MAX_VALUE;
        resistance = 100f;
        stepSound = Block.soundTypeMetal;
        textureName = "machine";
        isOpaque = false;
        renderNormalBlock = false;
    }

    @Override
    public void onNeighborChanged(Block block)
    {
        if (!world().isRemote)
        {
            if (world().isBlockIndirectlyGettingPowered(xi(), yi(), zi()))
            {
                powerOn();
            }
            else
            {
                powerOff();
            }
        }
    }

    public void powerOn()
    {
        this.setActive(true);
    }

    public void powerOff()
    {
        if (!this.isRedstoneActive && !this.worldObj.isRemote)
        {
            this.setActive(false);
        }
    }

    @Override
    public float getExplosionResistance(Entity entity)
    {
        return 100;
    }

    @Override
    public void doUpdateGuiUsers()
    {
        super.doUpdateGuiUsers();
        if (!world().isRemote && ticks % 3 == 0 && playersUsing.size() > 0)
        {
            sendPacketToGuiUsers(getDescPacket());
        }
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer entityplayer, PacketType packetType)
    {
        if (id == TilePacketType.description.ordinal())
        {
            boolean prevActive = active;
            active = buf.readBoolean();
            isRedstoneActive = buf.readBoolean();

            if (prevActive != this.active)
            {
                markRender();
            }
            return true;
        }
        else if (id == TilePacketType.toggleActivation.ordinal())
        {
            isRedstoneActive = !isRedstoneActive;

            if (isRedstoneActive)
            {
                setActive(true);
            }
            else
            {
                setActive(false);
            }
            return true;
        }
        return false;
    }

    public void setActive(boolean flag)
    {
        active = flag;
        worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void write(ByteBuf buf, int id)
    {

        if (id == TilePacketType.description.ordinal())
        {
            buf.writeBoolean(active);
            buf.writeBoolean(isRedstoneActive);
            //TODO replace with state system to save on network(eg two boolean == two bytes, could be one byte with 8 states instead)
        }
    }

    public boolean isPoweredByRedstone()
    {
        return world().isBlockIndirectlyGettingPowered(xi(), yi(), zi());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.active = nbt.getBoolean("isActive");
        this.isRedstoneActive = nbt.getBoolean("isRedstoneActive");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setBoolean("isActive", this.active);
        nbt.setBoolean("isRedstoneActive", this.isRedstoneActive);
    }

    public boolean isActive()
    {
        return active;
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, Pos hit)
    {
        if (!world().isRemote)
        {
            if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemCardLink)
            {
                return false;
            }
            openGui(player, ModularForceFieldSystem.instance);
        }
        return super.onPlayerActivated(player, side, hit);
    }
}
