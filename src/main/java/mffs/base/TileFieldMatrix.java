package mffs.base;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.helper.RotationUtility;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IPermissionProvider;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.field.mobilize.event.DelayedEvent;
import mffs.field.mobilize.event.IDelayedEventHandler;
import mffs.field.module.ItemModuleArray;
import mffs.item.card.ItemCard;
import mffs.util.TCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class TileFieldMatrix extends TileModuleAcceptor implements IFieldMatrix, IDelayedEventHandler, IRotatable, IPermissionProvider
{
    protected final Queue<DelayedEvent> delayedEvents = new LinkedList();

    public static final int[] _getModuleSlots = new int[]{14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
    protected int modeSlotID = 1;
    /**
     * Are the directions on the GUI absolute values?
     */
    public boolean absoluteDirection = false;
    protected List<Pos> calculatedField = null;
    protected boolean isCalculating = false;

    public TileFieldMatrix(String name)
    {
        super(name);
    }

    @Override
    public void update()
    {
        super.update();

        /**
         * Evaluated queued objects
         */
        delayedEvents.forEach(d -> d.update());
        delayedEvents.removeIf(d -> d.ticks < 0);
    }

    public void clearQueue()
    {
        delayedEvents.clear();
    }

    @Override
    public void write(ByteBuf buf, int id)
    {
        super.write(buf, id);

        if (id == TilePacketType.description.ordinal())
        {
            buf.writeBoolean(absoluteDirection);
        }
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
    {
        if (!super.read(buf, id, player, packetType))
        {

            if (world().isRemote)
            {
                if (id == TilePacketType.description.ordinal())
                {
                    absoluteDirection = buf.readBoolean();
                }
            }
            else
            {
                if (id == TilePacketType.toggleMode4.ordinal())
                {
                    absoluteDirection = !absoluteDirection;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        if (slotID == 0)
        {
            return itemStack.getItem() instanceof ItemCard;
        }
        else if (slotID == modeSlotID)
        {
            return itemStack.getItem() instanceof IProjectorMode;
        }

        return itemStack.getItem() instanceof IModule;
    }

    @Override
    public int getSidedModuleCount(IModule module, ForgeDirection... directions)
    {
        ForgeDirection[] actualDirs = directions;

        if (directions == null || directions.length > 0)
        {
            actualDirs = ForgeDirection.VALID_DIRECTIONS;
        }
        int count = 0;
        for (ForgeDirection dir : actualDirs)
        {
            count += getModuleCount(module, getDirectionSlots(dir));
        }

        return count;
    }

    @Override
    public Pos getPositiveScale()
    {
        String cacheID = "getPositiveScale";

        if (cache.containsKey(cacheID))
        {
            return (Pos) cache.get(cacheID);
        }

        int zScalePos = 0;
        int xScalePos = 0;
        int yScalePos = 0;

        if (absoluteDirection)
        {
            zScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.SOUTH));
            xScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.EAST));
            yScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.UP));
        }
        else
        {
            ForgeDirection direction = getDirection();

            zScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)));
            xScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)));
            yScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.UP));
        }

        int omnidirectionalScale = getModuleCount(ModularForceFieldSystem.moduleScale, getModuleSlots());

        zScalePos += omnidirectionalScale;
        xScalePos += omnidirectionalScale;
        yScalePos += omnidirectionalScale;

        Pos positiveScale = new Pos(xScalePos, yScalePos, zScalePos);

        cache.put(cacheID, positiveScale);

        return positiveScale;
    }

    @Override
    public Pos getNegativeScale()
    {
        final String cacheID = "getNegativeScale";

        if (cache.containsKey(cacheID))
        {
            return (Pos) cache.get(cacheID);
        }

        int zScaleNeg = 0;
        int xScaleNeg = 0;
        int yScaleNeg = 0;

        ForgeDirection direction = getDirection();

        if (absoluteDirection)
        {
            zScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.NORTH));
            xScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.WEST));
            yScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.DOWN));
        }
        else
        {
            zScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)));
            xScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)));
            yScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.DOWN));
        }

        int omnidirectionalScale = this.getModuleCount(ModularForceFieldSystem.moduleScale, getModuleSlots());
        zScaleNeg += omnidirectionalScale;
        xScaleNeg += omnidirectionalScale;
        yScaleNeg += omnidirectionalScale;

        Pos negativeScale = new Pos(xScaleNeg, yScaleNeg, zScaleNeg);

        cache.put(cacheID, negativeScale);

        return negativeScale;
    }

    @Override
    public int[] getModuleSlots()
    {
        return _getModuleSlots;
    }

    @Override
    public int[] getDirectionSlots(ForgeDirection direction)
    {
        switch (direction)
        {
            case UP:
                return newIntArray(10, 11);
            case DOWN:
                return newIntArray(12, 13);
            case SOUTH:
                return newIntArray(2, 3);
            case NORTH:
                return newIntArray(4, 5);
            case WEST:
                return newIntArray(6, 7);
            case EAST:
                return newIntArray(8, 9);
            default:
                return new int[]{};
        }
    }

    //TODO move to helper, or base tile class
    private int[] newIntArray(int start, int end)
    {
        int[] array = new int[end - start];
        for (int i = start; i < end; i++)
        {
            array[end - start] = i;
        }
        return array;
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
    public void queueEvent(DelayedEvent evt)
    {
        delayedEvents.add(evt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        absoluteDirection = nbt.getBoolean("isAbsolute");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setBoolean("isAbsolute", absoluteDirection);
    }

    /**
     * Calculates the force field
     */
    protected void calculateField()
    {
        if (!worldObj.isRemote && !isCalculating)
        {
            if (getMode() != null)
            {
                //Clear mode cache
                if (getModeStack().getItem() instanceof TCache)
                {
                    ((TCache) getModeStack().getItem()).clearCache();
                }
                isCalculating = true;
                //TODO implement thread priority so MFFS has one of the worker threads to itself
                //TODO only claim thread if more than a few force fields are running
                FieldCalculationTask task = new FieldCalculationTask(this);
                task.queProcess();
            }
        }
    }

    public List<Pos> generateCalculatedField()
    {
        return getExteriorPoints();
    }

    @Override //This is threaded, so ensure thread safe actions
    public List<Pos> getInteriorPoints()
    {
        final String cacheID = "getInteriorPoints";

        //TODO replace generic cache with object variable holding the field data
        if (cache.containsKey(cacheID))
        {
            return (List<Pos>) cache.get(cacheID);
        }

        if (getModeStack() != null && getModeStack().getItem() instanceof TCache)
        {
            ((TCache) getModeStack().getItem()).clearCache();
        }

        List<Pos> newField = getMode().getInteriorPoints(this);
        if (getModuleCount(ModularForceFieldSystem.moduleArray) > 0)
        {
            ((ItemModuleArray) ModularForceFieldSystem.moduleArray).onPreCalculateInterior(this, getMode().getExteriorPoints(this), newField);
        }

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

        cache.put(cacheID, field);
        return field;
    }

    protected List<Pos> getExteriorPoints()
    {
        List<Pos> newField;

        if (getModuleCount(ModularForceFieldSystem.moduleInvert) > 0)
        {
            newField = getMode().getInteriorPoints(this);
        }
        else
        {
            newField = getMode().getExteriorPoints(this);
        }

        getModules().forEach(m -> m.onPreCalculate(this, newField));

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
                newField.add(pos2);
            }
        }
        newField.clear(); //faster memory cleanup, at least in theory?

        getModules().forEach(m -> m.onPostCalculate(this, field));

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

    @Override
    public ItemStack getModeStack()
    {
        if (this.getStackInSlot(modeSlotID) != null)
        {
            if (this.getStackInSlot(modeSlotID).getItem() instanceof IProjectorMode)
            {
                return this.getStackInSlot(modeSlotID);
            }
        }
        return null;
    }

    @Override
    public Pos getTranslation()
    {
        final String cacheID = "getTranslation";

        if (cache.containsKey(cacheID))
        {
            return (Pos) cache.get(cacheID);
        }

        ForgeDirection direction = getDirection();

        int zTranslationNeg = 0;
        int zTranslationPos = 0;
        int xTranslationNeg = 0;
        int xTranslationPos = 0;
        int yTranslationPos = 0;
        int yTranslationNeg = 0;

        if (absoluteDirection)
        {
            zTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.NORTH));
            zTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.SOUTH));
            xTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.WEST));
            xTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.EAST));
            yTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.UP));
            yTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN));
        }
        else
        {
            zTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)));
            zTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)));
            xTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)));
            xTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)));
            yTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.UP));
            yTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN));
        }

        Pos translation = new Pos(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg);

        cache.put(cacheID, translation);

        return translation;
    }

    @Override
    public int getRotationYaw()
    {
        final String cacheID = "getRotationYaw";
        if (cache.containsKey(cacheID))
        {
            return (int) cache.get(cacheID);
        }

        int horizontalRotation = 0;
        ForgeDirection direction = getDirection();

        if (this.absoluteDirection)
        {
            horizontalRotation = getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.EAST))
                    - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.WEST))
                    + getModuleCount(ModularForceFieldSystem.moduleRotate, this.getDirectionSlots(ForgeDirection.SOUTH))
                    - this.getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.NORTH));
        }
        else
        {
            horizontalRotation = getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)))
                    - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)))
                    + this.getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)))
                    - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)));
        }

        horizontalRotation *= 2;

        cache.put(cacheID, horizontalRotation);

        return horizontalRotation;
    }

    @Override
    public int getRotationPitch()
    {
        final String cacheID = "getRotationPitch";

        if (cache.containsKey(cacheID))
        {
            return (int) cache.get(cacheID);
        }

        int verticalRotation = getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.UP)) - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.DOWN));
        verticalRotation *= 2;

        cache.put(cacheID, verticalRotation);

        return verticalRotation;
    }

}