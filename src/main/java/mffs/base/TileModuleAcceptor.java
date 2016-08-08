package mffs.base;

import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.modules.ICardModule;
import mffs.api.modules.IModuleProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.ArrayList;
import java.util.List;

public abstract class TileModuleAcceptor extends TileFortron implements IModuleProvider
{
    protected int capacityBase = 500;
    protected int capacityBoost = 5;
    protected int startModuleIndex = 1;
    protected int endModuleIndex = getSizeInventory() - 1;
    /**
     * Used for client-side only.
     */
    int clientFortronCost = 0;

    public TileModuleAcceptor(String name)
    {
        super(name);
    }

    /**
     * Returns Fortron cost in ticks.
     */
    @Override
    public int getFortronCost()
    {
        if (this.worldObj.isRemote)
        {
            return this.clientFortronCost;
        }
        return doGetFortronCost();
    }

    protected int doGetFortronCost()
    {
        float cost = 0.0F;
        for (ItemStack stack : getModuleStacks())
        {
            if (stack != null)
            {
                cost += stack.stackSize * ((ICardModule) stack.getItem()).getFortronCost(stack, getAmplifier());
            }
        }
        return Math.round(cost);
    }

    protected float getAmplifier()
    {
        return 1f;
    }


    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(getFortronCost());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        clientFortronCost = buf.readInt();
    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.moduleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME);
    }

    public void consumeCost()
    {
        if (getFortronCost() > 0)
        {
            requestFortron(getFortronCost(), true);
        }
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.moduleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME);
    }


    @Override
    public int getModuleCount(ICardModule module, int... slots)
    {
        int count = 0;
        if (slots != null && slots.length > 0)
        {
            for (int slot : slots)
            {
                ItemStack stack = getStackInSlot(slot);
                if (stack != null && stack.getItem() instanceof ICardModule && stack.getItem() == module)
                {//would be easier to check if assignable.
                    count += stack.stackSize;
                }
            }
        }
        else
        {
            for (ItemStack stack : getModuleStacks())
            {
                if (stack != null && stack.getItem() instanceof ICardModule && stack.getItem() == module)
                {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }

    @Override
    public List<ItemStack> getModuleStacks(int... slots)
    {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        if (slots != null && slots.length > 0)
        {
            for (int slot : slots)
            {
                ItemStack stack = getStackInSlot(slot);
                if (stack != null && stack.getItem() instanceof ICardModule)
                {
                    stacks.add(stack);
                }
            }
        }
        else
        {
            for (int i = startModuleIndex; i < endModuleIndex; i++)
            {
                ItemStack stack = getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ICardModule)
                {
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.clientFortronCost = nbt.getInteger("fortronCost");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("fortronCost", this.clientFortronCost);
    }
}