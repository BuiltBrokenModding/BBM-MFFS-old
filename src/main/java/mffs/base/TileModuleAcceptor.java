package mffs.base;

import com.builtbroken.mc.api.modules.IModule;
import com.builtbroken.mc.core.network.packet.PacketType;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.modules.IModuleProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.Set;

public abstract class TileModuleAcceptor extends TileFortron implements IModuleProvider, TCache
{
	int startModuleIndex = 1;
	int endModuleIndex = getSizeInventory() - 1;
	/**
	 * Used for client-side only.
	 */
	int clientFortronCost = 0;
	protected int capacityBase = 500;
	protected int capacityBoost = 5;

	public void write(ByteBuf buf , int id)
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
	public int getFortronCost()
	{
		if (this.worldObj.isRemote)
		{
			return this.clientFortronCost
		}

		val cacheID = "getFortronCost"

		if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

		val result = doGetFortronCost

		cache(cacheID, result)

		return result
	}

	protected def doGetFortronCost: Int = Math.round((getModuleStacks() foldLeft 0f)((a: Float, b: ItemStack) => a + b.stackSize * b.getItem.asInstanceOf[IModule].getFortronCost(getAmplifier)))

	protected def getAmplifier: Float = 1f

	override def read(buf: ByteBuf, id: Int, packetType: PacketType)
	{
		super.read(buf, id, packetType)

		if (id == TilePacketType.description.id)
		{
			clientFortronCost = buf.readInt()
		}
	}

	override def start()
	{
		super.start()
		fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.moduleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME)
	}

	def consumeCost()
	{
		if (getFortronCost() > 0)
		{
			requestFortron(getFortronCost(), true)
		}
	}

	override def getModule(module: IModule): ItemStack =
	{
		val cacheID = "getModule_" + module.hashCode

		if (hasCache(classOf[ItemStack], cacheID))
			return getCache(classOf[ItemStack], cacheID)

		val returnStack = new ItemStack(module.asInstanceOf[Item], getModuleStacks().count(_.getItem == module))

		cache(cacheID, returnStack.copy)
		return returnStack
	}

	@SuppressWarnings(Array("unchecked"))
	def getModules(slots: Int*): JSet[IModule] =
	{
		var cacheID: String = "getModules_"
		if (slots != null)
		{
			cacheID += slots.hashCode()
		}

		if (hasCache(classOf[Set[IModule]], cacheID)) return getCache(classOf[Set[IModule]], cacheID)

		var modules: Set[IModule] = null

		if (slots == null || slots.length <= 0)
			modules = ((startModuleIndex until endModuleIndex) map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule]) map (_.getItem.asInstanceOf[IModule])).toSet
		else
			modules = (slots map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule]) map (_.getItem.asInstanceOf[IModule])).toSet

		cache(cacheID, modules)
		return modules
	}

	override def markDirty()
	{
		super.markDirty()
		this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.moduleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME)
		clearCache()
	}

	@unchecked
	override def getModuleCount(module: IModule, slots: Int*): Int =
	{
		var cacheID = "getModuleCount_" + module.hashCode

		if (slots != null)
		{
			cacheID += "_" + slots.hashCode()
		}

		if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

		var count = 0

		if (slots != null && slots.length > 0)
			count = (slots.view map (getStackInSlot(_)) filter (_ != null) filter (_.getItem == module) foldLeft 0)(_ + _.stackSize)
		else
			count = (getModuleStacks() filter (_.getItem == module) foldLeft 0)(_ + _.stackSize)

		cache(cacheID, count)

		return count
	}

	def getModuleStacks(slots: Int*): JSet[ItemStack] =
	{
		var cacheID: String = "getModuleStacks_"

		if (slots != null)
		{
			cacheID += slots.hashCode()
		}

		if (hasCache(classOf[Set[ItemStack]], cacheID)) return getCache(classOf[Set[ItemStack]], cacheID)

		var modules: Set[ItemStack] = null

		if (slots == null || slots.length <= 0)
			modules = ((startModuleIndex until endModuleIndex) map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule])).toSet
		else
			modules = (slots map (getStackInSlot(_)) filter (_ != null) filter (_.getItem.isInstanceOf[IModule])).toSet

		cache(cacheID, modules)

		return modules
	}

	override def readFromNBT(nbt: NBTTagCompound)
	{
		clearCache()
		super.readFromNBT(nbt)
		this.clientFortronCost = nbt.getInteger("fortronCost")
	}

	override def writeToNBT(nbt: NBTTagCompound)
	{
		super.writeToNBT(nbt)
		nbt.setInteger("fortronCost", this.clientFortronCost)
	}

}