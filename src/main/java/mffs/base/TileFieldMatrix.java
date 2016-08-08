package mffs.base;

import com.builtbroken.mc.api.process.IProcessListener;
import com.builtbroken.mc.api.process.IThreadProcess;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.ModularForceFieldSystem;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IPermissionProvider;
import mffs.api.modules.ICardModule;
import mffs.api.modules.IProjectorMode;
import mffs.field.mobilize.event.DelayedEvent;
import mffs.field.mobilize.event.IDelayedEventHandler;
import mffs.item.card.ItemCard;
import mffs.util.TCache;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class TileFieldMatrix extends TileModuleAcceptor implements IFieldMatrix, IDelayedEventHandler, IPermissionProvider, IProcessListener
{
    protected final Queue<DelayedEvent> delayedEvents = new LinkedList();

    public static final int[] _getModuleSlots = new int[]{14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
    protected int modeSlotID = 1;

    protected List<Pos> calculatedField = null;
    protected boolean isCalculating = false;

    public TileFieldMatrix(String name)
    {
        super(name);
    }

    @Override
    public int getSizeInventory()
    {
        return 0;
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

        return itemStack.getItem() instanceof ICardModule;
    }

    public int getSidedModuleCount(ICardModule module, ForgeDirection... directions)
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
        int zScalePos = 0;
        int xScalePos = 0;
        int yScalePos = 0;

        zScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.SOUTH));
        xScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.EAST));
        yScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.UP));


        int omnidirectionalScale = getModuleCount(ModularForceFieldSystem.moduleScale, getModuleSlots());

        zScalePos += omnidirectionalScale;
        xScalePos += omnidirectionalScale;
        yScalePos += omnidirectionalScale;

        return new Pos(xScalePos, yScalePos, zScalePos);
    }

    @Override
    public Pos getNegativeScale()
    {
        int zScaleNeg = 0;
        int xScaleNeg = 0;
        int yScaleNeg = 0;

        zScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.NORTH));
        xScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.WEST));
        yScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.DOWN));

        int omnidirectionalScale = this.getModuleCount(ModularForceFieldSystem.moduleScale, getModuleSlots());
        zScaleNeg += omnidirectionalScale;
        xScaleNeg += omnidirectionalScale;
        yScaleNeg += omnidirectionalScale;

        return new Pos(xScaleNeg, yScaleNeg, zScaleNeg);
    }


    public int[] getModuleSlots()
    {
        return _getModuleSlots;
    }


    public int[] getDirectionSlots(ForgeDirection direction)
    {
        //TODO: These arrays are STATIC, should just create final variable
        //TODO: Use ordinal of direction to return array index
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

    //TODO move to helper, or base tile class.
    private int[] newIntArray(int start, int end)
    {
        int[] array = new int[end - start];
        for (int i = start; i < end; i++)
        {
            array[end - start - 1] = i;
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
    public void setCalculatedField(List<Pos> field)
    {
        this.calculatedField = field;
    }

    @Override
    public void queueEvent(DelayedEvent evt)
    {
        delayedEvents.add(evt);
    }

    /**
     * Calculates the force field
     */
    protected void calculateField()
    {
        if (isServer() && !isCalculating)
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
                FieldCalculationTask task = new FieldCalculationTask(this, this);
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

        for (ItemStack stack : getModuleStacks(getModuleSlots()))
        {
            if (stack != null && stack.getItem() instanceof ICardModule)
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

        for (ItemStack stack : getModuleStacks(getModuleSlots()))
        {
            if (stack != null && stack.getItem() instanceof ICardModule)
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
        int zTranslationNeg = 0;
        int zTranslationPos = 0;

        int xTranslationNeg = 0;
        int xTranslationPos = 0;

        int yTranslationPos = 0;
        int yTranslationNeg = 0;

        zTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.NORTH));
        zTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.SOUTH));
        xTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.WEST));
        xTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.EAST));
        yTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.UP));
        yTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN));

        return new Pos(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg);
    }

    @Override
    public int getRotationYaw()
    {
        return 0; //TODO
    }

    @Override
    public int getRotationPitch()
    {
        return 0; //TODO
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
}