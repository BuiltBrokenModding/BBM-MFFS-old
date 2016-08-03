package mffs.field.mobilize

import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import mffs.Content;
import mffs.ModularForceFieldSystem;
import mffs.Reference;
import mffs.Settings;
import mffs.api.card.ICoordLink;
import mffs.api.modules.IModule;
import mffs.api.modules.IProjectorMode;
import mffs.base.TileFieldMatrix;
import mffs.base.TilePacketType;
import mffs.field.mobilize.event.BlockPreMoveDelayedEvent;
import mffs.field.mobilize.event.DelayedEvent;
import mffs.item.card.ItemCard;
import mffs.render.FieldColor;
import mffs.render.fx.IEffectController;
import mffs.security.MFFSPermissions;
import mffs.util.MFFSUtility;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class TileForceMobilizer extends TileFieldMatrix implements IEffectController
{
  int packetRange = 60;
  int animationTime = 20;

  List<Pos> failedPositions = new ArrayList();
  public Location anchor = new Location(this.worldObj, 0, 0, 0);

  /**
   * The display mode. 0 = none, 1 = minimal, 2 = maximal.
   */
  public byte previewMode = 1;
  public boolean doAnchor = true;
  public int clientMoveTime = 0;
  boolean performingMove = false;
  /**
   * Marking failures
   */
  private boolean failedMove = false;
  /**
   * Used ONLY for teleporting.
   */
  private int moveTime = 0;
  private boolean canRenderMove = true;

    public TileForceMobilizer()
    {
        rotationMask = 63;
    }

  public void markFailMove()
  {
    failedMove = true;
  }

    @Override
  public int getSizeInventory()
  {
      return 26;
  }

  public void update()
  {
    super.update()

    if (getMode != null && Settings.enableForceManipulator)
    {
      if (delayedEvents.size == 0)
        performingMove = false

      checkActivation()
      whileMoving()

      executePreviews()
      executeFailures()
    }
    else if (!worldObj.isRemote && isActive)
    {
      setActive(false)
    }
  }

  public void checkActivation()
  {
    if (!world.isRemote)
    {
      if (isActive && !performingMove)
      {
        if (calculatedField != null)
        {
          performingMove = true
          executeMovement()
          calculatedField = null

          if (!worldObj.isRemote)
          {
            setActive(false)
          }
        }
        else
        {
          calculateField()
        }
      }
    }
  }

  /**
   * @return True if we started moving.
   */
  public boolean executeMovement()
  {
    /**
     * Check if there is a valid field that has been calculated. If so, we will move this field.
     */
    val movedBlocks = calculatedField filter moveBlock

    if (movedBlocks.size > 0)
    {
      /**
       * Queue an entity move event.
       */
      queueEvent(new DelayedEvent(this, getMoveTime, () =>
      {
        moveEntities
        ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(TileForceMobilizer.this, TilePacketType.field.id: Integer))

        if (!isTeleport && doAnchor)
        {
          anchor += getDirection
        }
      }))

      val renderBlocks = movedBlocks filter isVisibleToPlayer take Settings.maxForceFieldsPerTick

      if (renderBlocks.size > 0)
      {
        /**
         * If we have more than one block that is visible that was moved, we will tell the client to render it.
         *
         * Packet Params: id, Type1, Type2, Size, the coordinate
         */
        val coordPacketData = renderBlocks.toSeq flatMap (_.toIntList)

        val packet = new PacketTile(this)
        packet <<< TilePacketType.effect.id

        if (!isTeleport)
        {
          packet <<< 1 <<< 2 <<< coordPacketData.size <<< coordPacketData

          if (getModuleCount(Content.moduleSilence) <= 0)
          {
            worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.prefix + "fieldmove", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
          }

          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, toPos, packetRange)
        }
        else
        {
          packet <<< 2 <<< getMoveTime <<< (getAbsoluteAnchor + 0.5) <<< (getTargetPosition + 0.5) <<< false <<< coordPacketData.size <<< coordPacketData
          moveTime = getMoveTime
          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, toPos, packetRange)
        }
      }

      return true
    }
    else
    {
      markFailMove()
    }

    return false
  }

  public void whileMoving()
  {
    if (!worldObj.isRemote && performingMove)
    {
      if (requestFortron(getFortronCost, false) >= getFortronCost)
      {
        requestFortron(getFortronCost, true)

        if (moveTime > 0)
        {
          if (isTeleport)
          {
            if (getModuleCount(Content.moduleSilence) <= 0 && ticks % 10 == 0)
            {
              val moveTime = getMoveTime
              worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "fieldmove", 1.5f, 0.5f + 0.8f * (moveTime - this.moveTime) / moveTime)
            }

            moveTime -= 1

            if (moveTime <= 0)
            {
              worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "teleport", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
            }
          }
        }

        return
      }

      markFailMove()
    }
  }

  public void executePreviews()
  {
    if (!world.isRemote)
    {
      if (previewMode > 0 && Settings.highGraphics && !performingMove)
      {
        if (calculatedField == null)
          calculateField()

        /**
         * Send preview field packet
         */
        if (ticks % 120 == 0 && calculatedField != null)
        {
          val renderBlocks = getInteriorPoints.view filter isVisibleToPlayer filter (pos => previewMode == 2 || !world.isAirBlock(pos.xi, pos.yi, pos.zi)) take Settings.maxForceFieldsPerTick
          val coordPacketData = renderBlocks.toSeq flatMap (_.toIntList)

          val packet = new PacketTile(this)
          packet <<< TilePacketType.effect.id

          if (isTeleport)
          {
            var targetPosition: Pos = null

            if (getTargetPosition.world == null)
            {
              targetPosition = new Pos(getTargetPosition)
            }
            else
            {
              targetPosition = getTargetPosition
            }

            packet <<< 2 <<< 60 <<< (getAbsoluteAnchor + 0.5) <<< (targetPosition + 0.5) <<< true

          }
          else
          {
            packet <<< 1 <<< 1
          }

          packet <<< coordPacketData.size <<< coordPacketData

          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, toPos, packetRange)
          markDirty()
        }
      }
    }
  }

 public void executeFailures()
  {
    /**
     * Check if the move failed. If so, we tell the client which positions were the cause of failure.
     */
    if (failedMove)
    {
      /**
       * Stop teleportation field
       */
      moveTime = 0
      performingMove = false

      delayedEvents.clear()
      worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "powerdown", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
      val playPoint = toPos + anchor + 0.5
      worldObj.playSoundEffect(playPoint.x, playPoint.y, playPoint.z, Reference.prefix + "powerdown", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
      ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this) <<< TilePacketType.render.id, world, toPos, packetRange)


      if (failedPositions.size > 0)
      {
        /**
         * Send failure coordinates to client
         */
        val coords = failedPositions.toSeq flatMap (_.toIntList)
        val packetTile = new PacketTile(this) <<< TilePacketType.effect.id <<< 3 <<< coords.size <<< coords
        ModularForceFieldSystem.packetHandler.sendToAllAround(packetTile, world, toPos, packetRange)
      }

      failedMove = false
      failedPositions.clear()
    }
  }

  override def generateCalculatedField: mutable.Set[Pos] =
  {
    var moveField: mutable.Set[Pos] = null

    if (canMove)
      moveField = getInteriorPoints
    /*else
        markFailMove()*/

    return moveField
  }

  /**
   * Scan target field area to see if we can move this block. Called on a separate thread.
   */
  public boolean canMove()
  {
    val mobilizationPoints = getInteriorPoints
    val targetCenterPosition = getTargetPosition

    for (position <- mobilizationPoints)
    {
      if (world.isAirBlock(position.xi, position.yi, position.zi))
      {
        val relativePosition = position - getAbsoluteAnchor
        val targetPosition = (targetCenterPosition + relativePosition)

        if (!canMove(new VectorWorld(this.worldObj, position), targetPosition))
        {
          failedPositions.add(position)
          return false
        }
      }
    }
    return true
  }

  /**
   * Checks if a specific block can be moved from its position to a target
   * @param position - The position of the block to be moved.
   * @param target - The target position
   * @return True if the block can be moved.
   */
  public boolean canMove(position: VectorWorld, target: VectorWorld)
  {
    if (Blacklist.mobilizerBlacklist.contains(position.getBlock))
    {
      return false
    }
    val evt = new EventForceMobilize.EventCheckForceManipulate(position.world, position.xi, position.yi, position.zi, target.xi, target.yi, target.zi)
    MinecraftForge.EVENT_BUS.post(evt)

    if (evt.isCanceled)
    {
      return false
    }

    if (!MFFSUtility.hasPermission(worldObj, position, MFFSPermissions.blockAlter, ModularForceFieldSystem.fakeProfile) && !MFFSUtility.hasPermission(target.world, target, MFFSPermissions.blockAlter, ModularForceFieldSystem.fakeProfile))
    {
      return false
    }

    if (target.getTileEntity == this)
    {
      return false
    }
    for (checkPos <- this.getInteriorPoints)
    {
      if (checkPos == target)
      {
        return true
      }
    }

    val targetBlock = target.getBlock
    return target.world.isAirBlock(target.xi, target.yi, target.zi) || (targetBlock.isReplaceable(target.world, target.xi, target.yi, target.zi))
  }

  /**
   * Gets the position in which the manipulator will try to translate the field into.
   *
   * @return A vector of the target position.
   */
  public Location getTargetPosition()
  {
    if (isTeleport)
    {
      val cardStack = getLinkCard

      if (cardStack != null)
        return cardStack.getItem.asInstanceOf[ICoordLink].getLink(cardStack)
    }

    return new VectorWorld(worldObj, getAbsoluteAnchor + getDirection)
  }

  private boolean isTeleport()
  {
    if (Settings.allowForceManipulatorTeleport)
    {
      val cardStack = getLinkCard

      if (cardStack != null)
        return cardStack.getItem.asInstanceOf[ICoordLink].getLink(cardStack) != null
    }
    return false
  }

  public ItemStack getLinkCard()
  {
    getInventory().getContainedItems filter (_ != null) find (_.getItem.isInstanceOf[ICoordLink]) match
    {
      case Some(itemStack) => return itemStack
      case _ => return null
    }
  }

  public Pos getAbsoluteAnchor()
  {
      return toPos().add(this.anchor);
  }

  public boolean isVisibleToPlayer(position: Pos)
  {
    return (ForgeDirection.VALID_DIRECTIONS count ((dir: ForgeDirection) => (position + dir).getBlock(world).isOpaqueCube)) < 6
  }

  public void write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == TilePacketType.description.id)
    {
      buf <<< anchor
      buf <<< previewMode
      buf <<< doAnchor
      buf <<< (if (moveTime > 0) moveTime else getMoveTime)
    }
  }

    @Override
  public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType packetType)
  {
    if(!super.read(buf, id, player, packetType))
    {

        if (world().isRemote)
        {
            if (id == TilePacketType.effect.ordinal())
            {
                switch (buf.readInt())
                {
                    case 1 =>
                    {
                        /**
                         * If we have more than one block that is visible that was moved, we will tell the client to render it.
                         *
                         * Params: id, Type1, Type2, Size, the coordinate
                         */
                        val isTeleportPacket = buf.readInt();
                        val vecSize = buf.readInt();

                        val hologramRenderPoints = ((0 until vecSize)map(i = > buf.readInt().toDouble + 0.5)).
                        toList grouped 3 map(new Pos(_))

                        /**
                         * Movement Rendering
                         */
                        val direction = getDirection

                        isTeleportPacket match
                        {
                            case 1 =>hologramRenderPoints foreach (vector = > ModularForceFieldSystem.proxy.renderHologram(world, vector, FieldColor.blue, 30, vector + direction))
                            case 2 =>hologramRenderPoints foreach (vector = > ModularForceFieldSystem.proxy.renderHologram(world, vector, FieldColor.green, 30, vector + direction))
                        }
                    }
                    case 2 =>
                    {
                        /**
                         * Teleportation Rendering
                         */
                        val animationTime = buf.readInt()
                        val anchorPosition = new Pos(buf)
                        val targetPosition = new VectorWorld(buf)
                        val isPreview = buf.readBoolean()
                        val vecSize = buf.readInt()
                        val hologramRenderPoints = ((0 until vecSize)map(i = > buf.readInt().toDouble + 0.5)).
                        toList grouped 3 map(new Pos(_))
                        val color = if (isPreview) FieldColor.blue
                    else FieldColor.green

                        hologramRenderPoints foreach (vector = >
                            {
                                    //Render teleport start
                                    ModularForceFieldSystem.proxy.renderHologramOrbit(this, world, anchorPosition, vector, color, animationTime, 30f)

                        if (targetPosition.world != null && targetPosition.world.getChunkProvider.chunkExists(targetPosition.xi, targetPosition.zi))
                        {
                            //Render teleport end
                            val destination = vector - anchorPosition + targetPosition
                            ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, color, animationTime, 30f)
                        }
                        })

                        canRenderMove = true
                    }
                    case 3 =>
                    {
                        /**
                         * Fail hologram rendering
                         */
                        val vecSize = buf.readInt()
                        val hologramRenderPoints = ((0 until vecSize)map(i = > buf.readInt().toDouble + 0.5)).
                        toList grouped 3 map(new Pos(_))

                        hologramRenderPoints foreach
                        (ModularForceFieldSystem.proxy.renderHologram(world, _, FieldColor.red, 30, null))
                    }
                }
            }
            else if (id == TilePacketType.render.ordinal())
            {
                canRenderMove = false
            }
            else if (id == TilePacketType.field.ordinal())
            {
                this.moveEntities
            }
            else if (id == TilePacketType.description.ordinal())
            {
                anchor = new Pos(buf)
                previewMode = buf.readInt()
                doAnchor = buf.readBoolean()
                clientMoveTime = buf.readInt
            }
        }
        else
        {
            if (id == TilePacketType.toggleMoe.ordinal())
            {
                anchor = new Pos();
                markDirty();
            }
            else if (id == TilePacketType.toggleMode2.ordinal())
            {
                previewMode = (previewMode + 1) % 3
            }
            else if (id == TilePacketType.toggleMode3.ordinal())
            {
                doAnchor = !doAnchor;
            }
        }
        return false;
    }
      return true;
  }

    @Override
  public void markDirty()
  {
    super.markDirty();

    if (world() != null)
    {
      clearCache();
      calculateField();
    }
  }

  protected void moveEntities()
  {
    Location targetLocation = getTargetPosition();
    AxisAlignedBB bounds = getSearchBounds();

    if (bounds != null)
    {
      List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, bounds);
      entities.stream().forEach(entity -> moveEntity(entity, targetLocation.add(0.5).add(new Pos(entity)).sub(getAbsoluteAnchor().add(0.5))));
    }
  }

  public AxisAlignedBB getSearchBounds()
  {
    Pos positiveScale = toPos().add(getTranslation()).add(getPositiveScale()).add(1);
    Pos negativeScale = toPos().add(getTranslation()).sub(getNegativeScale());
    Pos minScale = positiveScale.min(negativeScale); //TODO check if needed as cube should auto min max
    Pos maxScale = positiveScale.max(negativeScale);
    return new Cube(minScale, maxScale).toAABB();
  }

  public Pos getTranslation()
  {
      return super.getTranslation().add(anchor);
  }

  protected void moveEntity(Entity entity, Location location)
  {
    if (entity != null && location != null)
    {
      if (entity.worldObj.provider.dimensionId != location.world.provider.dimensionId)
      {
        entity.travelToDimension(location.world.provider.dimensionId);
      }
      entity.motionX = 0;
      entity.motionY = 0;
      entity.motionZ = 0;

      if (entity instanceof EntityPlayerMP)
      {
          ((EntityPlayerMP)entity).playerNetServerHandler.setPlayerLocation(location.x(), location.y(), location.z(), entity.rotationYaw, entity.rotationPitch);
      }
      else
      {
        entity.setPositionAndRotation(location.x(), location.y(), location.z(), entity.rotationYaw, entity.rotationPitch);
      }
    }
  }

    @Override
  public int doGetFortronCost()
{
    return Math.round(super.doGetFortronCost() + (int)(this.anchor != null ? this.anchor.magnitude() * 1000 : 0));
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

    return itemStack.getItem() instanceof IModule || itemStack.getItem() instanceof ICoordLink;
  }

  /**
   * Gets the movement time required in TICKS.
   *
   * @return The time it takes to teleport (using a link card) to another coordinate OR
   *         ANIMATION_TIME for default move.
   */
  public int getMoveTime()
  {
    if (isTeleport())
    {
      int time = (20 * (int)this.getTargetPosition().distance(this.getAbsoluteAnchor()));
      if (this.getTargetPosition().world != this.worldObj);
      {
        time += 20 * 60;
      }
      return time;
    }
    return animationTime;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt)
  {
    super.readFromNBT(nbt);
    this.anchor = new Pos(nbt.getCompoundTag("anchor"));
    this.previewMode = nbt.getInteger("displayMode");
    this.doAnchor = nbt.getBoolean("doAnchor");
  }

    @Override
  public void writeToNBT(NBTTagCompound nbt)
  {
    super.writeToNBT(nbt);

    if (anchor != null)
    {
      nbt.setTag("anchor", anchor.toNBT());
    }

    nbt.setInteger("displayMode", previewMode);
    nbt.setBoolean("doAnchor", doAnchor);
  }

  @Override
  public boolean canContinueEffect() {
    return canRenderMove;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean renderStatic(RenderBlocks renderer, Pos pos, int pass)
  {
    return false;
  }

  /* TODO figure out what this commented out section is
   def getMethodNames: Array[String] =
  {
    return Array[String]("isActivate", "setActivate", "resetAnchor", "canMove")
  }

  def callMethod(computer: Pos, context: Pos, method: Int, arguments: Array[AnyRef]): Array[AnyRef] =
  {
    method match
    {
      case 2 =>
      {
        this.anchor = new Pos
        return null
      }
      case 3 =>
      {
        val result: Array[AnyRef] = Array(false)
        if (this.isActive || this.isCalculatingManipulation)
        {
          return result
        }
        else
        {
          result(0) = this.canMove
          this.failedPositions.clear
          return result
        }
      }
    }
    return super.callMethod(computer, context, method, arguments)
  }
*/

  @SideOnly(Side.CLIENT)
  public void renderDynamic(Pos pos, float frame, int pass)
  {
    RenderForceMobilizer.render(this, pos.x(), pos.y(), pos.z(), frame, isActive(), false);
  }

  @SideOnly(Side.CLIENT)
  public void renderInventory(ItemStack itemStack)
  {
    RenderForceMobilizer.render(this, -0.5, -0.5, -0.5, 0, true, true);
  }

  /**
   * Called to queue a block move from its position to a target.
   * @param position - The position of the block to be moved.
   * @return True if move is successful.
   */
  protected boolean moveBlock(Pos position)
  {
    if (!world().isRemote)
    {
      Pos relativePosition = position.sub(getAbsoluteAnchor());
      Location newPosition = getTargetPosition().add(relativePosition);
      TileEntity tileEntity = position.getTileEntity(world());

      if (!world().isAirBlock(position.xi(), position.yi(), position.zi()) && tileEntity != this)
      {
        queueEvent(new BlockPreMoveDelayedEvent(this, getMoveTime(), new Location(world(), position), newPosition));
        return true;
      }
    }

    return false;
  }
}