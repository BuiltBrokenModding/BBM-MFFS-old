package mffs.field;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.Reference;
import mffs.Settings;
import mffs.api.machine.IProjector;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.base.TileFieldMatrix;
import mffs.base.TilePacketType;
import mffs.field.mode.ItemModeCustom;
import mffs.item.card.ItemCard;
import mffs.render.FieldColor;
import mffs.security.MFFSPermissions;
import mffs.util.TCache;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TileElectromagneticProjector extends TileFieldMatrix implements IProjector
{
    /** A set containing all positions of all force field blocks generated. */
    List<Pos> forceFields = new ArrayList();

    /** Marks the field for an update call */
    boolean markFieldUpdate = true;

    /**
     * True if the field is done constructing and the projector is simply
     * maintaining the field
     */
    private boolean isCompleteConstructing = false;

    /** True to make the field constantly tick */
    private boolean fieldRequireTicks = false;

    /** Are the filters in the projector inverted? */
    private boolean isInverted = false;

    public TileElectromagneticProjector()
    {
        bounds = new Cube(0, 0, 0, 1, 0.8, 1);
        capacityBase = 30;
        startModuleIndex = 1;
    }

    @Override
    public int getSizeInventory()
    {
        return 1 + 25 + 6;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {

        if (slotID == 0)
        {
            return itemStack.getItem() instanceof ItemCard;
        }

        if (slotID == modeSlotID)
        {
            return itemStack.getItem() instanceof IProjectorMode;
        }

        if (slotID < 26)
        {
            return itemStack.getItem() instanceof IModule;
        }

        return true;

    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        calculateField();
        postCalculation();
    }

    public void postCalculation()
    {
        if (clientSideSimulationRequired())
        {
            sendFieldToClient();
        }
    }

    public void sendFieldToClient()
    {
        //TODO find a better way to handle this as this is a big packet
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList nbtList = new NBTTagList();

        calculatedField.forEach(vec -> nbtList.appendTag(vec.toNBT()));
        nbt.setTag("blockList", nbtList);
        Engine.instance.packetHandler.sendToAll(new PacketTile(this, TilePacketType.field.ordinal(), nbt));
    }

    private boolean clientSideSimulationRequired()
    {
        return getModuleCount(ModularForceFieldSystem.moduleRepulsion) > 0;
    }

    /**
     * Initiate a field calculation
     */
    protected void calculateField()
    {
        if (!worldObj.isRemote && !isCalculating)
        {
            if (getMode() != null)
            {
                forceFields.clear();
            }

            super.calculateField();
            isCompleteConstructing = false;
            fieldRequireTicks = getModuleStacks().stream().allMatch(module -> ((IModule) module.getItem()).requireTicks(module));
        }
    }

    @Override
    public int getLightValue()
    {
        return getMode() != null ? 10 : 0;
    }

    @Override
    public void write(ByteBuf buf, int id)
    {
        super.write(buf, id);

        if (id == TilePacketType.description.ordinal())
        {
            ;
        }
        {
            buf.writeBoolean(isInverted);
        }
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packet)
    {
        if (!super.read(buf, id, player, packet))
        {
            if (isClient())
            {
                if (id == TilePacketType.description.ordinal())
                {
                    isInverted = buf.readBoolean();
                }
                else if (id == TilePacketType.effect.ordinal())
                {
                    int packetType = buf.readInt();
                    Pos vector = new Pos(buf.readInt(), buf.readInt(), buf.readInt()).add(0.5);
                    Pos root = toPos().add(0.5);

                    if (packetType == 1)
                    {
                        ModularForceFieldSystem.proxy.renderBeam(this.worldObj, root, vector, FieldColor.RED, 40);
                        ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.RED, 50);
                    }
                    else if (packetType == 2)
                    {
                        ModularForceFieldSystem.proxy.renderBeam(this.worldObj, vector, root, FieldColor.RED, 40);
                        ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.RED, 50);
                    }
                }
                else if (id == TilePacketType.field.ordinal())
                {
                    NBTTagCompound nbt = ByteBufUtils.readTag(buf);
                    NBTTagList nbtList = nbt.getTagList("blockList", 10);
                    calculatedField = new ArrayList();
                    for (int i = 0; i < nbtList.tagCount(); i++)
                    {
                        calculatedField.add(new Pos(nbtList.getCompoundTagAt(i)));
                    }
                }
            }
            else
            {
                if (id == TilePacketType.toggleMode2.ordinal())
                {
                    isInverted = !isInverted;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void update()
    {
        super.update();

        if (isActive() && getMode() != null && requestFortron(getFortronCost(), false) >= this.getFortronCost())
        {
            consumeCost();

            if (ticks % 10 == 0 || markFieldUpdate || fieldRequireTicks)
            {
                if (calculatedField == null)
                {
                    calculateField();
                }
                else
                {
                    projectField();
                }
            }

            if (isActive() && worldObj.isRemote)
            {
                animation += getFortronCost() / 100f;
            }
            if (ticks % (2 * 20) == 0 && getModuleCount(ModularForceFieldSystem.moduleSilence) <= 0)
            {
                worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "field", 0.6f, (1 - this.worldObj.rand.nextFloat() * 0.1f));
            }
        }
        else if (!worldObj.isRemote)
        {
            destroyField();
        }
    }

    /**
     * Projects a force field based on the calculations made.
     */
    public void projectField()
    {
        //TODO: We cannot construct a field if it intersects another field with different frequency. Override not allowed.

        if (!isCalculating)
        {
            List<Pos> potentialField = calculatedField;

            List<IModule> relevantModules = getModules(getModuleSlots());

            if (!relevantModules.stream().anyMatch(m -> m.onProject(this, potentialField)))
            {
                if (!isCompleteConstructing || markFieldUpdate || fieldRequireTicks)
                {
                    markFieldUpdate = false;

                    if (forceFields.size() <= 0)
                    {
                        if (getModeStack().getItem() instanceof TCache)
                        {
                            ((TCache) getModeStack().getItem()).clearCache();
                        }
                    }

                    int constructionSpeed = Math.min(getProjectionSpeed(), Settings.maxForceFieldsPerTick);

                    final Pos tilePos = toPos();
                    //Creates a collection of positions that will be evaluated
                    List<Pos> evaluateField = potentialField.stream()
                            .filter(p -> !p.equals(tilePos))
                            .filter(v -> canReplaceBlock(v, v.getBlock(world())))
                            .filter(v -> v.getBlock(world()) != ModularForceFieldSystem.forceField)
                            .filter(v -> world().getChunkFromBlockCoords(v.xi(), v.zi()).isChunkLoaded).limit(constructionSpeed).collect(Collectors.toList());

                    //The collection containing the coordinates to actually place the field blocks.
                    List<Pos> constructField = new ArrayList();

                    boolean result = true;
                    evaluateField.stream().allMatch(
                            vector ->
                            {
                                int flag = 0;

                                for (IModule module : relevantModules)
                                {
                                    if (flag == 0)
                                    {
                                        flag = module.onProject(this, vector);
                                    }
                                }

                                if (flag != 1 && flag != 2)
                                {
                                    constructField.add(vector);
                                }

                                return flag != 2;
                            });

                    if (result)
                    {
                        constructField.forEach(
                                vector ->
                                {
                                    /**
                                     * Default force field block placement action.
                                     */
                                    if (!world().isRemote)
                                    {
                                        vector.setBlock(world(), ModularForceFieldSystem.forceField);
                                    }

                                    forceFields.add(vector);

                                    TileEntity tileEntity = vector.getTileEntity(world());

                                    if (tileEntity instanceof TileForceField)
                                    {
                                        ((TileForceField)tileEntity).setProjector(toPos());
                                    }
                                });
                    }

                    isCompleteConstructing = evaluateField.size() == 0;
                }
            }
        }
    }

    private boolean canReplaceBlock(Pos vector, Block block)
    {
        return block == null ||
                (getModuleCount(ModularForceFieldSystem.moduleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.xi(), vector.yi(), vector.zi()) != -1) ||
                (block.getMaterial().isLiquid() || block == Blocks.snow || block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.deadbush || block.isReplaceable(world(), vector.xi(), vector.yi(), vector.zi()));
    }

    public int getProjectionSpeed()
    {
        return 28 + 28 * getModuleCount(ModularForceFieldSystem.moduleSpeed, getModuleSlots());
    }

    public void destroyField()
    {
        if (!world().isRemote && calculatedField != null && !isCalculating)
        {
            getModules(getModuleSlots()).forEach(m -> m.onDestroy(this, calculatedField));
            //TODO: Parallelism?
            calculatedField.stream().filter(p -> p.getBlock(world()) == ModularForceFieldSystem.forceField).forEach(p -> p.setBlock(world(), Blocks.air));

            forceFields.clear();
            calculatedField = null;
            isCompleteConstructing = false;
            fieldRequireTicks = false;
        }
    }

    public void markDirty()
    {
        super.markDirty();

        if (world() != null)
        {
            destroyField();
        }
    }

    @Override
    public void invalidate()
    {
        destroyField();
        super.invalidate();
    }

    public List<Pos> getForceFields()
    {
        return forceFields;
    }

    public long getTicks()
    {
        return ticks;
    }

    public boolean isInField(Pos position)
    {
        return getMode() != null ? getMode().isInField(this, position) : false;
    }

    public boolean isAccessGranted(World checkWorld, Pos checkPos, EntityPlayer player, PlayerInteractEvent.Action action)
    {
        boolean hasPerm = true;

        if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && checkPos.getTileEntity(checkWorld) != null)
        {
            if (getModuleCount(ModularForceFieldSystem.moduleBlockAccess) > 0)
            {
                hasPerm = hasPermission(player.getGameProfile(), MFFSPermissions.blockAccess);
            }
        }

        if (hasPerm)
        {
            if (getModuleCount(ModularForceFieldSystem.moduleBlockAlter) > 0 && (player.getCurrentEquippedItem() != null || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
            {
                hasPerm = hasPermission(player.getGameProfile(), MFFSPermissions.blockAlter);
            }
        }

        return hasPerm;
    }

    public List<Item> getFilterItems()
    {
        List<Item> stacks = new ArrayList();
        for (int i = 26; i < 32; i++)
        {
            if (getStackInSlot(i) != null)
            {
                stacks.add(getStackInSlot(i).getItem());
            }
        }
        return stacks;
    }

    public List<ItemStack> getFilterStacks()
    {
        List<ItemStack> stacks = new ArrayList();
        for (int i = 26; i < 32; i++)
        {
            if (getStackInSlot(i) != null)
            {
                stacks.add(getStackInSlot(i));
            }
        }
        return stacks;
    }

    public boolean isInvertedFilter()
    {
        return isInverted;
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

    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        RenderElectromagneticProjector.render(this, pos.x(), pos.y(), pos.z(), frame, isActive(), false);
    }

    @SideOnly(Side.CLIENT)
    public void renderInventory(ItemStack itemStack)
    {
        RenderElectromagneticProjector.render(this, -0.5, -0.5, -0.5, 0, true, true);
    }

    /**
     * Returns Fortron cost in ticks.
     */
    protected int doGetFortronCost()
    {
        if (this.getMode() != null)
        {
            return Math.round(super.doGetFortronCost() + this.getMode().getFortronCost(this.getAmplifier()));
        }
        return 0;
    }

    protected float getAmplifier()
    {
        if (this.getMode() instanceof ItemModeCustom)
        {
            return Math.max(((ItemModeCustom) this.getMode()).getFieldBlocks(this, this.getModeStack()).size() / 100, 1);
        }
        return Math.max(Math.min((calculatedField != null ? calculatedField.size() : 0) / 1000, 10), 1);
    }
}