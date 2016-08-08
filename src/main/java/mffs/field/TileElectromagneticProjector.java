package mffs.field;

import com.builtbroken.mc.api.process.IProcessListener;
import com.builtbroken.mc.api.process.IThreadProcess;
import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.Reference;
import mffs.Settings;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IPermissionProvider;
import mffs.api.machine.IProjector;
import mffs.api.modules.ICardModule;
import mffs.api.modules.IProjectorMode;
import mffs.base.FieldCalculationTask;
import mffs.base.TileModuleAcceptor;
import mffs.base.TilePacketType;
import mffs.field.gui.ContainerElectromagneticProjector;
import mffs.field.gui.GuiElectromagneticProjector;
import mffs.field.mobilize.event.DelayedEvent;
import mffs.field.mobilize.event.IDelayedEventHandler;
import mffs.field.mode.ItemModeCustom;
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
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class TileElectromagneticProjector extends TileModuleAcceptor implements IProjector, IGuiTile, IFieldMatrix, IDelayedEventHandler, IPermissionProvider, IProcessListener
{
    public static final int[] MODULE_SLOTS = new int[]{0, 1, 2, 3, 4, 5};
    public static final int MODE_SLOT = 0;

    /** List of events to que later */ //TODO
    protected final Queue<DelayedEvent> delayedEvents = new LinkedList();


    /** Is the tile currently waiting on a thread to calculate the field */
    protected boolean isCalculating = false; //TODO add tick to ensure this value resets if the field doesn't finish calculating
    /** A set containing all positions of the calculated field data */
    protected List<Pos> calculatedField = null;
    /** A set containing all positions of all force field blocks generated. */
    protected List<Pos> forceFields = new ArrayList();

    /** How far to move from center point */
    public Pos translation = new Pos();
    /** How much to scale in each direction */
    public int[] scale = new int[6];

    /** How much we can move the field in any direction */
    public int translationPoints = 0;
    /** How much we can expand the field in any direction */
    public int scalePoints = 0;

    /** Marks the field for an update call */
    public boolean markFieldUpdate = true;
    /**
     * True if the field is done constructing and the projector is simply
     * maintaining the field
     */
    private boolean isCompleteConstructing = false;

    /** True to make the field constantly tick */
    private boolean fieldRequireTicks = false;

    /** Are the filters in the projector inverted? */
    private boolean isInverted = false;

    private int yaw = 0, pitch = 0;

    public TileElectromagneticProjector()
    {
        super("electromagneticProjector");
        bounds = new Cube(0, 0, 0, 1, 0.8, 1);
        capacityBase = 30;
        startModuleIndex = 1;
    }

    @Override
    public Tile newTile()
    {
        return new TileElectromagneticProjector();
    }

    /** Ensures the field size and position is valid for modules we contain */
    public void validateField()
    {
        if ((Math.abs(translation.xi()) + Math.abs(translation.yi()) + Math.abs(translation.zi())) > translationPoints)
        {
            //TODO invalidate field and recalculate
        }
        int size = scale[0] + scale[1] + scale[2] + scale[3] + scale[4] + scale[5];
        if (size > scalePoints)
        {
            //TODO invalidate field and recalculate
        }
    }

    @Override
    public int getSizeInventory()
    {
        return 7;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        return slotID == 0 ? itemStack.getItem() instanceof IProjectorMode : itemStack.getItem() instanceof ICardModule;
    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        calculateField(); //TODO Delay calculation to improve load time
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

    /** Initiate a field calculation */
    protected void calculateField()
    {
        if (isServer() && !isCalculating)
        {
            if (getMode() != null)
            {
                forceFields.clear();
                //Clear mode cache
                if (getModeStack().getItem() instanceof TCache)
                {
                    ((TCache) getModeStack().getItem()).clearCache();
                }
                isCalculating = true;
                //TODO implement thread priority so MFFS has one of the worker threads to itself
                //TODO only claim thread if more than a few force fields are running
                FieldCalculationTask task = new FieldCalculationTask(this, this);
                task.queProcess();
            }
            isCompleteConstructing = false;
            fieldRequireTicks = getModuleStacks().stream().allMatch(module -> ((ICardModule) module.getItem()).requireTicks(module));
        }
    }

    @Override
    public int getLightValue()
    {
        return getMode() != null ? 10 : 0;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf); //TODO check if is client side only
        buf.writeBoolean(isInverted);
        for (int i = 0; i < scale.length; i++)
        {
            buf.writeInt(scale[i]);
        }
        translation.writeByteBuf(buf);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        isInverted = buf.readBoolean();
        for (int i = 0; i < scale.length; i++)
        {
            scale[i] = buf.readInt();
        }
        translation = new Pos(buf);
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packet)
    {
        if (!super.read(buf, id, player, packet))
        {
            if (isClient())
            {
                if (id == TilePacketType.effect.ordinal())
                {
                    int packetType = buf.readInt();
                    Pos vector = new Pos(buf.readInt(), buf.readInt(), buf.readInt()).add(0.5);
                    Pos root = toPos().add(0.5);

                    if (packetType == 1)
                    {
                        ModularForceFieldSystem.proxy.renderBeam(this.worldObj, root, vector, FieldColor.RED, 40);
                        ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.RED, 50);
                        return true;
                    }
                    else if (packetType == 2)
                    {
                        ModularForceFieldSystem.proxy.renderBeam(this.worldObj, vector, root, FieldColor.RED, 40);
                        ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.RED, 50);
                        return true;
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
                    return true;
                }
            }
            //Prevents client side hacks from messing with the tiles data
            else if (player.openContainer instanceof ContainerElectromagneticProjector) //TODO add permissions check
            {
                if (id == TilePacketType.toggleMode2.ordinal())
                {
                    isInverted = !isInverted;
                    return true;
                }
                else if (id == TilePacketType.increase_scale.ordinal())
                {
                    increaseScale(ForgeDirection.getOrientation(buf.readInt()));
                    return true;
                }
                else if (id == TilePacketType.decrease_scale.ordinal())
                {
                    decreaseScale(ForgeDirection.getOrientation(buf.readInt()));
                    return true;
                }
                else if (id == TilePacketType.translate.ordinal())
                {
                    this.translation = new Pos(buf);
                    validateField();
                    destroyField(); //TODO instead of destroying just update translation
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void increaseScale(ForgeDirection direction)
    {
        scale[direction.ordinal()] = scale[direction.ordinal()] + 1;
        //TODO limit to max
        validateField();
        destroyField();
    }

    public void decreaseScale(ForgeDirection direction)
    {
        scale[direction.ordinal()] = scale[direction.ordinal()] - 1;
        //TODO limit to min
        validateField();
        destroyField();
    }

    @Override
    public void update()
    {
        super.update();
        delayedEvents.forEach(d -> d.update());
        delayedEvents.removeIf(d -> d.ticks < 0);

        setActive(true); //TODO remove
        if (isServer())
        {
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
            else
            {
                destroyField();
            }
        }
    }

    /**
     * Projects a force field based on the calculations made.
     */
    @Override
    public void projectField()
    {
        //TODO: We cannot construct a field if it intersects another field with different frequency. Override not allowed.

        if (!isCalculating)
        {
            List<Pos> potentialField = calculatedField;

            boolean flag1 = true;
            for(ItemStack stack : getModuleStacks(getModuleSlots()))
            {
                if(stack != null && stack.getItem() instanceof ICardModule)
                {
                    flag1 = ((ICardModule) stack.getItem()).onProject(stack, this, potentialField);
                }
            }

            if (!flag1)
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

                                for(ItemStack stack : getModuleStacks(getModuleSlots()))
                                {
                                    if(flag == 0 && stack != null && stack.getItem() instanceof ICardModule)
                                    {
                                        flag = ((ICardModule) stack.getItem()).onProject(stack, this, vector);
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
                                        ((TileForceField) tileEntity).setProjector(toPos());
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
        //TODO move to helper that is easier to add to or remove from
        return block == null ||
                (getModuleCount(ModularForceFieldSystem.moduleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.xi(), vector.yi(), vector.zi()) != -1) ||
                (block.getMaterial().isLiquid() || block == Blocks.snow || block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.deadbush || block.isReplaceable(world(), vector.xi(), vector.yi(), vector.zi()));
    }

    @Override
    public int getProjectionSpeed()
    {
        return 28 + 28 * getModuleCount(ModularForceFieldSystem.moduleSpeed, MODULE_SLOTS);
    }

    @Override
    public void destroyField()
    {
        if (!world().isRemote && calculatedField != null && !isCalculating)
        {
            for(ItemStack stack : getModuleStacks(getModuleSlots()))
            {
                if(stack != null && stack.getItem() instanceof ICardModule)
                {
                    ((ICardModule) stack.getItem()).onDestroy(stack, this, calculatedField);
                }
            }
            //TODO: Parallelism?
            calculatedField.stream().filter(p -> p.getBlock(world()) == ModularForceFieldSystem.forceField).forEach(p -> p.setBlock(world(), Blocks.air));

            forceFields.clear();
            calculatedField = null;
            isCompleteConstructing = false;
            fieldRequireTicks = false;
        }
    }

    @Override
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

    @Override
    public List<Pos> getForceFields()
    {
        return forceFields;
    }

    @Override
    public int[] getModuleSlots()
    {
        return MODULE_SLOTS;
    }

    @Override
    public long getTicks()
    {
        return ticks;
    }

    public boolean isInField(Pos position)
    {
        return getMode() != null ? getMode().isInField(getModeStack(), this, position) : false;
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
        for (int i = 1; i < 7; i++)
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
        for (int i = 1; i < 7; i++)
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

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Pos pos, float frame, int pass)
    {
        RenderElectromagneticProjector.render(this, pos.x(), pos.y(), pos.z(), frame, isActive(), false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(ItemStack itemStack)
    {
        RenderElectromagneticProjector.render(this, -0.5, -0.5, -0.5, 0, true, true);
    }

    /**
     * Returns Fortron cost in ticks.
     */
    @Override
    protected int doGetFortronCost()
    {
        if (this.getMode() != null)
        {
            return Math.round(super.doGetFortronCost() + this.getMode().getFortronCost(getModeStack(), this.getAmplifier()));
        }
        return 0;
    }

    @Override
    protected float getAmplifier()
    {
        if (this.getMode() instanceof ItemModeCustom)
        {
            return Math.max(((ItemModeCustom) this.getMode()).getFieldBlocks(this, this.getModeStack()).size() / 100, 1);
        }
        return Math.max(Math.min((calculatedField != null ? calculatedField.size() : 0) / 1000, 10), 1);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerElectromagneticProjector(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiElectromagneticProjector(player, this);
    }

    public void clearQueue()
    {
        delayedEvents.clear();
    }

    @Override
    public Pos getPositiveScale()
    {
        return new Pos(scale[ForgeDirection.SOUTH.ordinal()], scale[ForgeDirection.EAST.ordinal()], scale[ForgeDirection.UP.ordinal()]);
    }

    @Override
    public Pos getNegativeScale()
    {
        return new Pos(scale[ForgeDirection.NORTH.ordinal()], scale[ForgeDirection.WEST.ordinal()], scale[ForgeDirection.DOWN.ordinal()]);
    }

    @Override
    public List<Pos> getCalculatedField()
    {
        if (calculatedField != null)
        {
            return calculatedField;
        }
        return new ArrayList();
    }

    @Override
    public void setCalculatedField(List<Pos> field)
    {
        this.calculatedField = field;
    }

    @Override
    public void queueEvent(DelayedEvent evt)
    {
        delayedEvents.add(evt);
    }

    @Override
    public List<Pos> generateCalculatedField()
    {
        return getExteriorPoints();
    }

    @Override //This is threaded, so ensure thread safe actions
    public List<Pos> getInteriorPoints()
    {
        if (getModeStack() != null && getModeStack().getItem() instanceof TCache)
        {
            ((TCache) getModeStack().getItem()).clearCache();
        }

        List<Pos> newField = getMode().getInteriorPoints(getModeStack(), this);

        //Data to use to move field
        final Pos translation = getTranslation();
        final int rotationYaw = getRotationYaw();
        final int rotationPitch = getRotationPitch();
        final EulerAngle rotation = new EulerAngle(rotationYaw, rotationPitch, 0);

        //Limiter for field
        final int maxHeight = world().getHeight();

        //TODO optimize as we are generate x2, or more, pos objects each time this is calculated
        List<Pos> field = new ArrayList();
        for (Pos pos : newField)
        {
            Pos pos2 = toPos().add(pos.transform(rotation)).add(translation).round();
            if (pos2.yi() <= maxHeight && pos2.yi() >= 0)
            {
                field.add(pos2);
            }
        }
        newField.clear(); //faster memory cleanup, at least in theory?
        return field;
    }

    protected List<Pos> getExteriorPoints()
    {
        List<Pos> newField;

        if (getModuleCount(ModularForceFieldSystem.moduleInvert) > 0)
        {
            newField = getMode().getInteriorPoints(getModeStack(), this);
        }
        else
        {
            newField = getMode().getExteriorPoints(getModeStack(), this);
        }

        for(ItemStack stack : getModuleStacks(getModuleSlots()))
        {
            if(stack != null && stack.getItem() instanceof ICardModule)
            {
                ((ICardModule) stack.getItem()).onPreCalculate(stack, this, newField);
            }
        }

        Pos translation = getTranslation();
        int rotationYaw = getRotationYaw();
        int rotationPitch = getRotationPitch();

        EulerAngle rotation = new EulerAngle(rotationYaw, rotationPitch);

        int maxHeight = world().getHeight();

        //TODO optimize as we are generate x2, or more, pos objects each time this is calculated
        List<Pos> field = new ArrayList();
        for (Pos pos : newField)
        {
            Pos pos2 = toPos().add(pos.transform(rotation)).add(translation).round();
            if (pos2.yi() <= maxHeight && pos2.yi() >= 0)
            {
                field.add(pos2);
            }
        }
        newField.clear(); //faster memory cleanup, at least in theory?

        for(ItemStack stack : getModuleStacks(getModuleSlots()))
        {
            if(stack != null && stack.getItem() instanceof ICardModule)
            {
                ((ICardModule) stack.getItem()).onPostCalculate(stack, this, newField);
            }
        }

        return field;
    }

    @Override
    public IProjectorMode getMode()
    {
        if (this.getModeStack() != null)
        {
            return (IProjectorMode) this.getModeStack().getItem();
        }
        return null;
    }

    public ItemStack getModeStack()
    {
        if (this.getStackInSlot(MODE_SLOT) != null)
        {
            if (this.getStackInSlot(MODE_SLOT).getItem() instanceof IProjectorMode)
            {
                return this.getStackInSlot(MODE_SLOT);
            }
        }
        return null;
    }

    @Override
    public Pos getTranslation()
    {
        return translation;
    }

    @Override
    public int getRotationYaw()
    {
        return yaw;
    }

    @Override
    public int getRotationPitch()
    {
        return pitch;
    }

    @Override
    public void onProcessStarts(IThreadProcess process)
    {
        this.isCalculating = true;
    }

    @Override
    public void onProcessFinished(IThreadProcess process)
    {
        this.isCalculating = false;
    }

    @Override
    public void onProcessTerminated(IThreadProcess process)
    {
        this.isCalculating = false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("scaleArray"))
        {
            scale = nbt.getIntArray("scaleArray");
        }
        if (nbt.hasKey("translation"))
        {
            translation = new Pos(nbt.getCompoundTag("translation"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setIntArray("scaleArray", scale);
        nbt.setTag("translation", translation.toNBT());
    }
}