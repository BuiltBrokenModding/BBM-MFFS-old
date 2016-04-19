package mffs.base

import com.builtbroken.mc.api.tile.{IRemovable, IPlayerUsing}
import com.builtbroken.mc.core.Engine
import com.builtbroken.mc.core.network.{IPacketIDReceiver, IPacketReceiver}
import com.builtbroken.mc.core.network.netty.PacketManager
import com.builtbroken.mc.core.network.packet.PacketType
import com.builtbroken.mc.lib.transform.vector.Pos
import com.builtbroken.mc.prefab.tile.Tile
import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.item.card.ItemCardLink
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import resonant.api.mffs.machine.IActivatable
import resonant.api.tile.ICamouflageMaterial

import scala.collection.convert.wrapAll._

/**
  * A base tile class for all MFFS blocks to inherit.
  *
  * @author Calclavia
  */
abstract class TileMFFS(name: String) extends Tile(name, Material.iron) with ICamouflageMaterial with IPacketIDReceiver with IActivatable with IPlayerUsing with IRemovable.ISneakPickup {
  /**
    * Used for client side animations.
    */
  var animation = 0f
  /**
    * Is this machine switched on internally via GUI?
    */
  var isRedstoneActive = false
  /**
    * Is the machine active and working?
    */
  private var active = false

  /**
    * Constructor
    */
  hardness = Float.MaxValue
  resistance = 100f
  stepSound = Block.soundTypeMetal
  textureName = "machine"
  isOpaque = false;
  renderNormalBlock = false;

  override def onNeighborChanged(block: Block) {
    if (!world.isRemote) {
      if (world.isBlockIndirectlyGettingPowered(xi, yi, zi)) {
        powerOn()
      }
      else {
        powerOff()
      }
    }
  }

  def powerOn() {
    this.setActive(true)
  }

  def powerOff() {
    if (!this.isRedstoneActive && !this.worldObj.isRemote) {
      this.setActive(false)
    }
  }

  override def getExplosionResistance(entity: Entity): Float = 100

  override def update() {
    super.update()

    if (!world.isRemote && ticks % 3 == 0 && playersUsing.size > 0) {
      playersUsing foreach (player => Engine.instance.packetHandler.sendToPlayer(getDescPacket, player.asInstanceOf[EntityPlayerMP]))
    }
  }

  //override def getDescPacket: PacketType = PacketManager.request(this, TilePacketType.description.id)

  override def read(buf: ByteBuf, id: Int, entityplayer: EntityPlayer, packetType: PacketType): Boolean = {
    if (id == TilePacketType.description.id) {
      val prevActive = active
      active = buf.readBoolean()
      isRedstoneActive = buf.readBoolean()

      if (prevActive != this.active) {
        markRender()
      }
      return true
    }
    else if (id == TilePacketType.toggleActivation.id) {
      isRedstoneActive = !isRedstoneActive

      if (isRedstoneActive) {
        setActive(true)
      }
      else {
        setActive(false)
      }
      return true
    }
    return false
  }

  def setActive(flag: Boolean) {
    active = flag
    worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
  }

  def write(buf: ByteBuf, id: Int) {

    if (id == TilePacketType.description.id) {
      buf.writeBoolean(active)
      buf.writeBoolean(isRedstoneActive)
      //TODO replace with state system to save on network(eg two boolean == two bytes, could be one byte with 8 states instead)
    }
  }

  def isPoweredByRedstone: Boolean = world.isBlockIndirectlyGettingPowered(xi, yi, zi)

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    this.active = nbt.getBoolean("isActive")
    this.isRedstoneActive = nbt.getBoolean("isRedstoneActive")
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    nbt.setBoolean("isActive", this.active)
    nbt.setBoolean("isRedstoneActive", this.isRedstoneActive)
  }

  def isActive: Boolean = active

  override protected def onPlayerActivated(player: EntityPlayer, side: Int, hit: Pos): Boolean = {

    if (!world.isRemote) {
      if (player.getCurrentEquippedItem != null) {
        if (player.getCurrentEquippedItem().getItem().isInstanceOf[ItemCardLink]) {
          return false
        }
      }

      player.openGui(ModularForceFieldSystem, 0, world, xi, yi, zi)
    }
    return super.onPlayerActivated(player, side, hit)
  }
}
