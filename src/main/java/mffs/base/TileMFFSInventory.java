package mffs.base;

import com.builtbroken.mc.api.ISave;
import com.builtbroken.mc.api.tile.IInventoryProvider;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public abstract class TileMFFSInventory extends TileMFFS implements IInventory, IPacketIDReceiver, IInventoryProvider
{
    public TileMFFSInventory(String name)
    {
        super(name);
    }

    public abstract IInventory getInventory();

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (getInventory() != null && getInventory() instanceof ISave && nbt.hasKey("inventory"))
        {
            ((ISave) getInventory()).load(nbt.getCompoundTag("inventory"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (getInventory() != null && getInventory() instanceof ISave)
        {
            nbt.setTag("inventory", ((ISave) getInventory()).save(new NBTTagCompound()));
        }
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
    {
        if (!super.read(buf, id, player, packetType))
        {
            //TODO implement inventory reading
            return false;
        }
        return true;
    }

    /**
     * Inventory Methods
     */
    public List<ItemStack> getCards()
    {
        List<ItemStack> set = new ArrayList();
        if (getStackInSlot(0) != null)
        {
            set.add(getStackInSlot(0));
        }
        return set;
    }
}
