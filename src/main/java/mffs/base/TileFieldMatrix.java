package mffs.base

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.helper.RotationUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import mffs.ModularForceFieldSystem;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IPermissionProvider;
import mffs.field.mobilize.event.IDelayedEventHandler;
import mffs.item.card.ItemCard;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import scala.util.Failure;
import scala.util.Success;

import java.util.Set;

public abstract class TileFieldMatrix extends TileModuleAcceptor implements IFieldMatrix, IDelayedEventHandler, IRotatable, IPermissionProvider
{
  protected final val delayedEvents = new mutable.SynchronizedQueue[DelayedEvent]()
  val _getModuleSlots = (14 until 25).toArray
  protected val modeSlotID = 1
  /**
   * Are the directions on the GUI absolute values?
   */
  var absoluteDirection = false
  protected var calculatedField: mutable.Set[Pos] = null
  protected var isCalculating = false

  override def update()
  {
    super.update()

    /**
     * Evaluated queued objects
     */
    delayedEvents foreach (_.update())
    delayedEvents.dequeueAll(_.ticks < 0)
  }

  def clearQueue() = delayedEvents.clear()

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == TilePacketType.description.id)
    {
      buf <<< absoluteDirection
    }
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    if (world.isRemote)
    {
      if (id == TilePacketType.description.id)
      {
        absoluteDirection = buf.readBoolean()
      }
    }
    else
    {
      if (id == TilePacketType.toggleMode4.id)
      {
        absoluteDirection = !absoluteDirection
      }
    }
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0)
    {
      return itemStack.getItem.isInstanceOf[ItemCard]
    }
    else if (slotID == modeSlotID)
    {
      return itemStack.getItem.isInstanceOf[IProjectorMode]
    }

    return itemStack.getItem.isInstanceOf[IModule]
  }

  def getSidedModuleCount(module: IModule, directions: ForgeDirection*): Int =
  {
    var actualDirs = directions

    if (directions == null || directions.length > 0)
      actualDirs = ForgeDirection.VALID_DIRECTIONS

    return actualDirs.foldLeft(0)((b, a) => b + getModuleCount(module, getDirectionSlots(a): _*))
  }

  def getPositiveScale: Pos =
  {
    val cacheID = "getPositiveScale"

    if (hasCache(classOf[Pos], cacheID)) return getCache(classOf[Pos], cacheID)

    var zScalePos = 0
    var xScalePos = 0
    var yScalePos = 0

    if (absoluteDirection)
    {
      zScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.SOUTH): _*)
      xScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.EAST): _*)
      yScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.UP): _*)
    }
    else
    {
      val direction = getDirection

      zScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)): _*)
      xScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)): _*)
      yScalePos = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.UP): _*)
    }

    val omnidirectionalScale = getModuleCount(ModularForceFieldSystem.moduleScale, getModuleSlots: _*)

    zScalePos += omnidirectionalScale
    xScalePos += omnidirectionalScale
    yScalePos += omnidirectionalScale

    val positiveScale = new Pos(xScalePos, yScalePos, zScalePos)

    cache(cacheID, positiveScale)

    return positiveScale
  }

  def getNegativeScale: Pos =
  {
    val cacheID = "getNegativeScale"

    if (hasCache(classOf[Pos], cacheID)) return getCache(classOf[Pos], cacheID)

    var zScaleNeg = 0
    var xScaleNeg = 0
    var yScaleNeg = 0

    val direction = getDirection

    if (absoluteDirection)
    {
      zScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.NORTH): _*)
      xScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.WEST): _*)
      yScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.DOWN): _*)
    }
    else
    {
      zScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)): _*)
      xScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)): _*)
      yScaleNeg = getModuleCount(ModularForceFieldSystem.moduleScale, getDirectionSlots(ForgeDirection.DOWN): _*)
    }

    val omnidirectionalScale = this.getModuleCount(ModularForceFieldSystem.moduleScale, getModuleSlots: _*)
    zScaleNeg += omnidirectionalScale
    xScaleNeg += omnidirectionalScale
    yScaleNeg += omnidirectionalScale

    val negativeScale = new Pos(xScaleNeg, yScaleNeg, zScaleNeg)

    cache(cacheID, negativeScale)

    return negativeScale
  }

  def getModuleSlots: Array[Int] = _getModuleSlots

  override def getDirectionSlots(direction: ForgeDirection): Array[Int] =
  {
    direction match
    {
      case ForgeDirection.UP =>
        return Array(10, 11)
      case ForgeDirection.DOWN =>
        return Array(12, 13)
      case ForgeDirection.SOUTH =>
        return Array(2, 3)
      case ForgeDirection.NORTH =>
        return Array(4, 5)
      case ForgeDirection.WEST =>
        return Array(6, 7)
      case ForgeDirection.EAST =>
        return Array(8, 9)
      case _ =>
        return Array[Int]()
    }
  }

  def getInteriorPoints: JSet[Pos] =
  {
    val cacheID = "getInteriorPoints"

    if (hasCache(classOf[Set[Pos]], cacheID)) return getCache(classOf[Set[Pos]], cacheID)

    if (getModeStack != null && getModeStack.getItem.isInstanceOf[TCache])
    {
      (getModeStack.getItem.asInstanceOf[TCache]).clearCache
    }

    val newField = getMode.getInteriorPoints(this)

    if (getModuleCount(ModularForceFieldSystem.moduleArray) > 0)
    {
      ModularForceFieldSystem.moduleArray.asInstanceOf[ItemModuleArray].onPreCalculateInterior(this, getMode.getExteriorPoints(this), newField)
    }

    val translation = getTranslation
    val rotationYaw = getRotationYaw
    val rotationPitch = getRotationPitch
    val rotation = new EulerAngle(rotationYaw, rotationPitch, 0)
    val maxHeight = world.getHeight

    val field = mutable.Set((newField.view.par map (pos => (pos.transform(rotation) + toPos + translation).round) filter (position => position.yi <= maxHeight && position.yi >= 0)).seq.toSeq: _ *)

    cache(cacheID, field)
    return field
  }

  def getCalculatedField: JSet[Pos] =
  {
    return if (calculatedField != null) calculatedField else mutable.Set.empty[Pos]
  }

  def queueEvent(evt: DelayedEvent)
  {
    delayedEvents += evt
  }

  /**
   * NBT Methods
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    absoluteDirection = nbt.getBoolean("isAbsolute")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("isAbsolute", absoluteDirection)
  }

  /**
   * Calculates the force field
    *
    * @param callBack - Optional callback
   */
  protected def calculateField(callBack: () => Unit = null)
  {
    if (!worldObj.isRemote && !isCalculating)
    {
      if (getMode != null)
      {
        //Clear mode cache
        if (getModeStack.getItem.isInstanceOf[TCache])
          getModeStack.getItem.asInstanceOf[TCache].clearCache()

        isCalculating = true

        Future
        {
          generateCalculatedField
        }.onComplete
        {
          case Success(field) =>
          {
            calculatedField = field
            isCalculating = false

            if (callBack != null)
              callBack.apply()
          }
          case Failure(t) =>
          {
            //println(getClass.getName + ": An error has occurred upon field calculation: " + t.getMessage)
            isCalculating = false
          }
        }
      }
    }
  }

  protected def generateCalculatedField = getExteriorPoints

  /**
   * Gets the exterior points of the field based on the matrix.
   */
  protected def getExteriorPoints: mutable.Set[Pos] =
  {
    var field = mutable.Set.empty[Pos]

    if (getModuleCount(ModularForceFieldSystem.moduleInvert) > 0)
      field = getMode.getInteriorPoints(this)
    else
      field = getMode.getExteriorPoints(this)

    getModules() foreach (_.onPreCalculate(this, field))

    val translation = getTranslation
    val rotationYaw = getRotationYaw
    val rotationPitch = getRotationPitch

    val rotation: EulerAngle = new EulerAngle(rotationYaw, rotationPitch)

    val maxHeight = world.getHeight

    field = mutable.Set((field.view.par map (pos => (pos.transform(rotation) + toPos + translation).round) filter (position => position.yi <= maxHeight && position.yi >= 0)).seq.toSeq: _ *)

    getModules() foreach (_.onPostCalculate(this, field))

    return field
  }

  def getMode: IProjectorMode =
  {
    if (this.getModeStack != null)
    {
      return this.getModeStack.getItem.asInstanceOf[IProjectorMode]
    }
    return null
  }

  def getModeStack: ItemStack =
  {
    if (this.getStackInSlot(modeSlotID) != null)
    {
      if (this.getStackInSlot(modeSlotID).getItem.isInstanceOf[IProjectorMode])
      {
        return this.getStackInSlot(modeSlotID)
      }
    }
    return null
  }

  def getTranslation: Pos =
  {
    val cacheID = "getTranslation"

    if (hasCache(classOf[Pos], cacheID)) return getCache(classOf[Pos], cacheID)

    val direction = getDirection

    var zTranslationNeg = 0
    var zTranslationPos = 0
    var xTranslationNeg = 0
    var xTranslationPos = 0
    var yTranslationPos = 0
    var yTranslationNeg = 0

    if (absoluteDirection)
    {
      zTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.NORTH): _*)
      zTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.SOUTH): _*)
      xTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.WEST): _*)
      xTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.EAST): _*)
      yTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN): _*)
    }
    else
    {
      zTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)): _*)
      zTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)): _*)
      xTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)): _*)
      xTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)): _*)
      yTranslationPos = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(ModularForceFieldSystem.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN): _*)
    }

    val translation = new Pos(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg)

    cache(cacheID, translation)

    return translation
  }

  def getRotationYaw: Int =
  {
    val cacheID = "getRotationYaw"
    if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

    var horizontalRotation = 0
    val direction = getDirection

    if (this.absoluteDirection)
    {
      horizontalRotation = getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.EAST): _*) - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.WEST): _*) + getModuleCount(ModularForceFieldSystem.moduleRotate, this.getDirectionSlots(ForgeDirection.SOUTH): _*) - this.getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.NORTH): _*)
    }
    else
    {
      horizontalRotation = getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)): _*) - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)): _*) + this.getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)): _*) - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)): _*)
    }

    horizontalRotation *= 2

    cache(cacheID, horizontalRotation)

    return horizontalRotation
  }

  def getRotationPitch: Int =
  {
    val cacheID = "getRotationPitch"

    if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

    var verticalRotation = getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.UP): _*) - getModuleCount(ModularForceFieldSystem.moduleRotate, getDirectionSlots(ForgeDirection.DOWN): _*)
    verticalRotation *= 2

    cache(cacheID, verticalRotation)

    return verticalRotation
  }

}