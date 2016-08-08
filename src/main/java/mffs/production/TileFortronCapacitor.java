package mffs.production;

import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.card.ICoordLink;
import mffs.api.fortron.*;
import mffs.api.modules.ICardModule;
import mffs.base.TileModuleAcceptor;
import mffs.base.TilePacketType;
import mffs.util.FortronUtility;
import mffs.util.TransferMode;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TileFortronCapacitor extends TileModuleAcceptor implements IFortronStorage, IFortronCapacitor, IGuiTile
{
    private int tickRate = 10;
    private TransferMode transferMode = TransferMode.equalize;

    public TileFortronCapacitor()
    {
        super("fortronCapacitor"); //TODO get actual tile name
        capacityBase = 700;
        capacityBoost = 10;
        startModuleIndex = 1;
    }

    @Override
    public Tile newTile()
    {
        return new TileFortronCapacitor();
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
        this.consumeCost(); //TODO figure out why we have a cost for moving power

        if (isActive())
        {
            //Drain fluid containers
            if (getFortronEmpty() > 0)
            {
                getInputStacks().stream()
                        .filter(stack -> stack.getItem() instanceof IFluidContainerItem)
                        .forEach(stack -> fortronTank.fill(((IFluidContainerItem) stack.getItem()).drain(stack, Math.min(getFortronEmpty(), getTransmissionRate()), true), true));
            }

            //Fill fluid containers
            if (fortronTank.getFluidAmount() > 0)
            {
                FluidStack transferFluid = fortronTank.getFluid().copy();
                transferFluid.amount = Math.min(transferFluid.amount, getTransmissionRate());
                getOutputStacks().stream()
                        .filter(stack -> stack.getItem() instanceof IFluidContainerItem)
                        .forEach(stack -> fortronTank.drain(((IFluidContainerItem) stack.getItem()).fill(stack, transferFluid, true), true));
            }

            if (ticks % tickRate == 0)
            {
                //TODO recode into a wireless network
                FortronUtility.transferFortron(this, getFrequencyDevices(), TransferMode.fill, getTransmissionRate() * tickRate);
                FortronUtility.transferFortron(this, getFrequencyDevices(), TransferMode.drain, getTransmissionRate() * tickRate);
                FortronUtility.transferFortron(this, getFrequencyDevices(), transferMode, getTransmissionRate() * tickRate);
            }
        }
    }

    @Override
    public int getTransmissionRate()
    {
        return 300 + 60 * getModuleCount(ModularForceFieldSystem.moduleSpeed);
    }

    @Override
    public float getAmplifier()
    {
        return 0f;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(transferMode.ordinal());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        transferMode = TransferMode.get(buf.readInt());
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
    {
        if (!super.read(buf, id, player, packetType))
        {
            if (id == TilePacketType.toggleMoe.ordinal())
            {
                transferMode = transferMode.toggle();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.transferMode = TransferMode.get(nbt.getInteger("transferMode"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setInteger("transferMode", this.transferMode.ordinal());
    }

    public int getDeviceCount()
    {
        return getFrequencyDevices().size() + getInputDevices().size() + getOutputDevices().size();
    }

    @Override
    public List<IFortronFrequency> getFrequencyDevices()
    {
        return FrequencyGrid.instance().getNodes(world(), toPos(), getTransmissionRange(), getFrequency()).stream().filter(s -> s instanceof IFortronFrequency).map(s -> (IFortronFrequency) s).collect(Collectors.toList());
    }

    @Override
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
                .filter(link -> link != null && link.getTileEntity(world()) instanceof IFortronFrequency)
                .forEach(link -> devices.add((IFortronFrequency) link.getTileEntity(world())));

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
        if (slotID < 4)
        {
            return itemStack.getItem() instanceof ICardModule;
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

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerFortronCapacitor(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiFortronCapacitor(player, this);
    }
}