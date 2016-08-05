package mffs.base;

import com.builtbroken.mc.api.ISave;
import com.builtbroken.mc.api.tile.IInventoryProvider;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.prefab.inventory.BasicInventory;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public abstract class TileMFFSInventory extends TileMFFS implements IInventory, IPacketIDReceiver, IInventoryProvider
{
    protected IInventory inventory;

    public TileMFFSInventory(String name)
    {
        super(name);
    }

    @Override
    public IInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new BasicInventory(getSizeInventory());
        }
        return inventory;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (getInventory() instanceof ISave && nbt.hasKey("inventory"))
        {
            ((ISave) getInventory()).load(nbt.getCompoundTag("inventory"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (getInventory() instanceof ISave)
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

    @Override
    public abstract int getSizeInventory();

    @Override
    public ItemStack getStackInSlot(int p_70301_1_)
    {
        return getInventory().getStackInSlot(p_70301_1_);
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
    {
        return getInventory().decrStackSize(p_70298_1_, p_70298_2_);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int p_70304_1_)
    {
        return getInventory().getStackInSlotOnClosing(p_70304_1_);
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
    {
        getInventory().setInventorySlotContents(p_70299_1_, p_70299_2_);
    }

    @Override
    public String getInventoryName()
    {
        return getInventory().getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return getInventory().hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return getInventory().getInventoryStackLimit();
    }

    @Override
    public void markDirty()
    {
        getInventory().markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
    {
        return getInventory().isUseableByPlayer(p_70300_1_);
    }

    @Override
    public void openInventory()
    {
        getInventory().openInventory();
    }

    @Override
    public void closeInventory()
    {
        getInventory().closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
    {
        return getInventory().isItemValidForSlot(p_94041_1_, p_94041_2_);
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        return false;
    }

    @Override
    public boolean canRemove(ItemStack stack, int slot, ForgeDirection side)
    {
        return false;
    }

    public void mergeIntoInventory(ItemStack itemStack)
    {
        //TODO implement
    }
}
