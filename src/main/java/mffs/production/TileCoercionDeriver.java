package mffs.production;

import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.card.IItemFrequency;
import mffs.api.modules.ICardModule;
import mffs.base.TileModuleAcceptor;
import mffs.util.FortronUtility;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileCoercionDeriver extends TileModuleAcceptor implements IGuiTile
{
    /**
     * The amount of power (watts) this machine uses.
     */
    public static final int POWER = 5000000;

    /* Static slots */
    public static final byte SLOT_FREQ = 0, SLOT_BATTERY = 1, SLOT_FUEL = 2;

    /**
     * Ration from UE to Fortron. Multiply J by this value to convert to Fortron.
     */
    public static final float ueToFortronRatio = 0.005f;
    public static final byte productionMultiplier = 4;

    /* Amount of time for 1 fuel cycle */
    public static int fuelProcessTime = 200;

    /* The percentage at which energy is converted from fortron */
    public static float energyConversionPercentage = 1F;

    int processTime = 0;
    //Client
    float animationTween = 0f;

    int energy = 0;

    public TileCoercionDeriver()
    {
        super("coercionDeriver");
        capacityBase = 30;
        startModuleIndex = 3;
    }

    @Override
    public Tile newTile()
    {
        return new TileCoercionDeriver();
    }

    @Override
    public int getSizeInventory()
    {
        return 6;
    }

    @Override
    public void update()
    {
        super.update();

        if (!worldObj.isRemote)
        {
            if (isActive())
            {
                if (getFortronEnergy() < getFortronCapacity())
                {
                    //Create power from items if electricity is not enabled
                    ItemStack stack = getStackInSlot(TileCoercionDeriver.SLOT_FUEL);
                    if( /* TODO !Settings.enableElectricity && */ isItemValidForSlot(TileCoercionDeriver.SLOT_FUEL, stack))
                    {
                        energy += getPower(); //TODO balance
                        if (processTime == 0 && isItemValidForSlot(TileCoercionDeriver.SLOT_FUEL, stack))
                        {
                            decrStackSize(TileCoercionDeriver.SLOT_FUEL, 1);
                            processTime = TileCoercionDeriver.fuelProcessTime * Math.max(this.getModuleCount(ModularForceFieldSystem.moduleScale) / 20, 1);
                        }

                        if (processTime > 0)
                        {
                            processTime -= 1;

                            if (processTime < 1)
                            {
                                processTime = 0;
                            }
                        }
                        else
                        {
                            processTime = 0;
                        }
                    }

                    //Create fortron fluid
                    if (energy >= getPower())
                    {
                        fortronTank.fill(FortronUtility.getFortron(getProductionRate()), true);
                        energy -= getPower();
                    }
                }
            }
        }
        else
        {
            /**
             * Handle animation
             */
            if (isActive())
            {
                animation += 1;

                if (animationTween < 1)
                {
                    animationTween += 0.01f;
                }
            }
            else
            {
                if (animationTween > 0)
                {
                    animationTween -= 0.01f;
                }
            }
        }
    }

    public int getProductionRate()
    {
        if (this.isActive())
        {
            float production = (float) (getPower() / 20f * Settings.fortronProductionMultiplier * ueToFortronRatio);
            if (processTime > 0)
            {
                production *= productionMultiplier;
            }
            return (int) Math.floor(production);
        }
        return 0;
    }

    public double getPower()
    {
        return POWER + (POWER * getModuleCount(ModularForceFieldSystem.moduleSpeed) / 8d);
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        if (itemStack != null)
        {
            if (slotID >= startModuleIndex)
            {
                return itemStack.getItem() instanceof ICardModule;
            }
            switch (slotID)
            {
                case TileCoercionDeriver.SLOT_FREQ:
                    return itemStack.getItem() instanceof IItemFrequency;
                case TileCoercionDeriver.SLOT_BATTERY:
                    return false; //TODO add battery handler
                // return Compatibility.isHandler(itemStack.getItem(), null);
                case TileCoercionDeriver.SLOT_FUEL:
                    return InventoryUtility.stacksMatch(itemStack, new ItemStack(Items.dye, 1, 4)) || InventoryUtility.stacksMatch(itemStack, new ItemStack(Items.quartz));
            }
        }
        return false;
    }

    public boolean canConsume()
    {
        if (this.isActive())
        {
            return FortronUtility.getAmount(this.fortronTank) < this.fortronTank.getCapacity();
        }
        return false;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(processTime);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        processTime = buf.readInt();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        processTime = nbt.getInteger("processTime");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("processT;ime", processTime);
    }

    /**
     * Rendering
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean renderStatic(RenderBlocks renderer, Pos pos, int pass)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        RenderCoercionDeriver.render(this, pos.x(), pos.y(), pos.z(), frame, isActive(), false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(ItemStack itemStack)
    {
        RenderCoercionDeriver.render(this, -0.5, -0.5, -0.5, 0, true, true);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerCoercionDeriver(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiCoercionDeriver(player, this);
    }
}