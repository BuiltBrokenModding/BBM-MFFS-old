package mffs.base;

import com.builtbroken.mc.core.network.packet.PacketType;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.modules.IModule;
import mffs.api.modules.IModuleProvider;
import mffs.util.TCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TileModuleAcceptor extends TileFortron implements IModuleProvider, TCache
{
    protected int capacityBase = 500;
    protected int capacityBoost = 5;
    protected int startModuleIndex = 1;
    protected int endModuleIndex = getSizeInventory() - 1;
    /**
     * Used for client-side only.
     */
    int clientFortronCost = 0;
    /* Cache mapping */
    public Map<String, Object> cache = new HashMap<String, Object>();

    public TileModuleAcceptor(String name)
    {
        super(name);
    }

    @Override
    public Map<String, Object> cache()
    {
        return cache;
    }
    @Override
    public void write(ByteBuf buf, int id)
    {
        //super.write(buf, id);

        if (id == TilePacketType.description.ordinal())
        {
            buf.writeInt(getFortronCost());
        }
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

        String cacheID = "getFortronCost";

        if (cacheExists(cacheID))
        {
            return (Integer) getCache(cacheID);
        }

        int result = doGetFortronCost();
        putCache(cacheID, result);
        return result;
    }

    protected int doGetFortronCost()
    {
        float cost = 0.0F;
        for (ItemStack stack : getModuleStacks())
        {
            if (stack != null)
            {
                cost += stack.stackSize * ((IModule) stack.getItem()).getFortronCost(getAmplifier());
            }
        }
        return Math.round(cost);
    }

    protected float getAmplifier()
    {
        return 1f;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
    {
        if (!super.read(buf, id, player, packetType))
        {

            if (id == TilePacketType.description.ordinal())
            {
                clientFortronCost = buf.readInt();
                return true;
            }
        }
        return false; //this allows any childClass to continue reading.
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
    public ItemStack getModule(IModule module)
    {
        String cacheID = "getModule_" + module.hashCode();

        if (cacheExists(cacheID))
        {
            return (ItemStack) getCache(cacheID);
        }

        ItemStack returnStack = new ItemStack((Item) module, getModuleCount(module));

        putCache(cacheID, returnStack.copy());
        return returnStack;
    }

    @Override
    public List<IModule> getModules(int... slots)
    {
        String cacheID = "getModules_";
        if (slots != null)
        {
            cacheID += slots.hashCode();
        }

        if (cacheExists(cacheID))
        {
            return (List<IModule>) getCache(cacheID);
        }

        List<IModule> stacks = new ArrayList<IModule>();
        if (slots != null && slots.length > 0)
        {
            for (int slot : slots)
            {
                ItemStack stack = getStackInSlot(slot);
                if (stack != null && stack.getItem() instanceof IModule)
                {
                    stacks.add((IModule) stack.getItem());
                }
            }
        }
        else
        {
            for (int i = startModuleIndex; i < endModuleIndex; i++)
            {
                ItemStack stack = getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof IModule)
                {
                    stacks.add((IModule) stack.getItem());
                }
            }
        }

        putCache(cacheID, stacks);
        return stacks;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.moduleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME);
        clearCache();
    }


    @Override
    public int getModuleCount(IModule module, int... slots)
    {
        String cacheID = "getModuleCount_" + module.hashCode();

        if (slots != null)
        {
            cacheID += "_" + slots.hashCode();
        }

        if (cacheExists(cacheID))
        {
            return (Integer) getCache(cacheID);
        }

        int count = 0;
        if (slots != null && slots.length > 0)
        {
            for (int slot : slots)
            {
                ItemStack stack = getStackInSlot(slot);
                if (stack != null && stack.getItem() instanceof IModule && stack.getItem() == module)
                {//would be easier to check if assignable.
                    count += stack.stackSize;
                }
            }
        }
        else
        {
            for (ItemStack stack : getModuleStacks())
            {
                if (stack != null && stack.getItem() instanceof IModule && stack.getItem() == module)
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
        String cacheID = "getModuleStacks_";

        if (slots != null)
        {
            cacheID += slots.hashCode();
        }

        if (cacheExists(cacheID))
        {
            return (List<ItemStack>) getCache(cacheID);
        }

        List<ItemStack> stacks = new ArrayList<ItemStack>();
        if (slots != null && slots.length > 0)
        {
            for (int slot : slots)
            {
                ItemStack stack = getStackInSlot(slot);
                if (stack != null && stack.getItem() instanceof IModule)
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
                if (stack != null && stack.getItem() instanceof IModule)
                {
                    stacks.add(stack);
                }
            }
        }
        putCache(cacheID, stacks);
        return stacks;
    }

    @Override
    public Object getCache(String paramString)
    {
        return cache.get(paramString);
    }

    @Override
    public void putCache(String param, Object object)
    {
        cache.put(name, object);
    }

    @Override
    public boolean cacheExists(String param)
    {
        return cache.containsKey(param);
    }

    @Override
    public void clearCache(String paramString)
    {
        cache.remove(paramString);
    }

    @Override
    public void clearCache()
    {
        cache.clear();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        clearCache();
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