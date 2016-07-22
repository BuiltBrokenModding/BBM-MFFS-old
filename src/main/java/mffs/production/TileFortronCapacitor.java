package mffs.production

import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.card.ICoordLink;
import mffs.api.fortron.IFortronCapacitor;
import mffs.api.fortron.IFortronFrequency;
import mffs.api.fortron.IFortronStorage;
import mffs.base.TileModuleAcceptor;
import mffs.base.TilePacketType;
import mffs.item.card.ItemCardFrequency;
import mffs.util.FortronUtility;
import mffs.util.TransferMode;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.ArrayList;
import java.util.List;

public class TileFortronCapacitor extends TileModuleAcceptor implements IFortronStorage, IFortronCapacitor
{
    private int tickRate = 10;
    private TransferMode transferMode = TransferMode.equalize;

    public TileFortronCapacitor()
    {
        super("FortonCapacitor"); //TODO get actual tile name
        capacityBase = 700;
        capacityBoost = 10;
        startModuleIndex = 1;
    }

    @Override
    public int getSizeInventory()
    {
        return 3 + 4 * 2 + 1;
    }

    @Override
    public void update()
    {
        super.update();
        this.consumeCost();

        if (isActive())
        {
            /**
             * Draw from input slots and eject from output slots
             */
            getInputStacks().stream().filter (stack -> stack.getItem() instanceof IFluidContainerItem);
            foreach(stack = > fortronTank.fill(stack.getItem.asInstanceOf[IFluidContainerItem].drain(stack, Math.min(getFortronEmpty, getTransmissionRate), true), true))

            if (fortronTank.getFluidAmount > 0)
            {
                val transferFluid = fortronTank.getFluid.copy()
                transferFluid.amount = Math.min(transferFluid.amount, getTransmissionRate)
                getOutputStacks filter (_.getItem.isInstanceOf[IFluidContainerItem])
                foreach(stack = > fortronTank.drain(stack.getItem.asInstanceOf[IFluidContainerItem].fill(stack, transferFluid, true), true))
            }

            if (ticks % tickRate == 0)
            {
                /**
                 * Transfer based on input/output slots
                 */
                FortronUtility.transferFortron(this, getInputDevices, TransferMode.fill, getTransmissionRate * tickRate)
                FortronUtility.transferFortron(this, getOutputDevices, TransferMode.drain, getTransmissionRate * tickRate)

                /**
                 * Transfer based on frequency
                 */
                FortronUtility.transferFortron(this, getFrequencyDevices, transferMode, getTransmissionRate * tickRate)
            }
        }
    }

    public int getTransmissionRate()
    {
        return 300 + 60 * getModuleCount(ModularForceFieldSystem.moduleSpeed);
    }

    @Override
    public float getAmplifier()
    {
        return 0f;
    }

    /**
     * Packet Methods
     */

    @Override
    public void write(buf:ByteBuf, id:Int)
    {
        super.write(buf, id)

        if (id == TilePacketType.description.id)
        {
            buf << < transferMode.id
        }
    }

    @Override
    public void read(buf:ByteBuf, id:Int, packetType:PacketType)
    {
        super.read(buf, id, packetType)

        if (id == TilePacketType.description.id)
        {
            transferMode = TransferMode(buf.readInt)
        }
        else if (id == TilePacketType.toggleMoe.id)
        {
            transferMode = transferMode.toggle
        }
    }

    @Override
    public void readFromNBT(nbt:NBTTagCompound)
    {
        super.readFromNBT(nbt)
        this.transferMode = TransferMode(nbt.getInteger("transferMode"))
    }

    @Override
    public void writeToNBT(nbttagcompound:NBTTagCompound)
    {
        super.writeToNBT(nbttagcompound)
        nbttagcompound.setInteger("transferMode", this.transferMode.id)
    }

    public void getDeviceCount = getFrequencyDevices.size + getInputDevices.size + getOutputDevices.size

    @Override
    public JSet[IFortronFrequency]getFrequencyDevices
    :=FrequencyGridRegistry.instance.getNodes(classOf[IFortronFrequency],world,toVector3,getTransmissionRange,getFrequency)

    public int getTransmissionRange()
    {
        return 15 + getModuleCount(ModularForceFieldSystem.moduleScale);
    }


    public List<IFortronFrequency> getInputDevices()
    {
        return getDevicesFromStacks(getInputStacks());
    }

    public List<ItemStack> getInputStacks()
    {
        List<ItemStack> list = new ArrayList();
        for (int i = 4; i <= 7; i++)
        {
            if (getStackInSlot(i) != null)
            {
                list.add(getStackInSlot(i));
            }
        }
        return list;
    }

    public List<IFortronFrequency> getDevicesFromStacks(List<ItemStack> stacks)
    {
        List<IFortronFrequency> devices = new ArrayList();
        //TODO optimize as this seems like a waste of CPU time
        stacks.stream()
                .filter(stack -> stack.getItem() instanceof ICoordLink)
                .map(stack -> ((ICoordLink) stack.getItem()).getLink(stack))
                .filter(link -> link != null && link.getTileEntity(world) instanceof IFortronFrequency)
                .foreach(link -> devices.add((IFortronFrequency) link.getTileEntity(world)));

        return devices;
    }

    public List<IFortronFrequency> getOutputDevices()
    {

        return getDevicesFromStacks(getOutputStacks());
    }

    public List<ItemStack> getOutputStacks()
    {
        List<ItemStack> list = new ArrayList();
        for (int i = 0; i <= 11; i++)
        {
            if (getStackInSlot(i) != null)
            {
                list.add(getStackInSlot(i));
            }
        }
        return list;
    }


    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        if (slotID == 0)
        {
            return itemStack.getItem() instanceof ItemCardFrequency;
        }
        else if (slotID < 4)
        {
            return itemStack.getItem() instanceof IModule;
        }
        return true;
    }

    public TransferMode getTransferMode()
    {
        return transferMode;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean renderStatic(RenderBlocks renderer, Pos pos, int pass)
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        RenderFortronCapacitor.render(this, pos.x(), pos.y(), pos.z(), frame, isActive(), false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(ItemStack itemStack)
    {
        RenderFortronCapacitor.render(this, -0.5, -0.5, -0.5, 0, true, true);
    }
}