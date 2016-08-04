package mffs.field.mobilize.event;

import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import mffs.field.mobilize.TileForceMobilizer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
public class BlockNotifyDelayedEvent extends DelayedEvent
{
    public World world;
    public Pos position;

    public BlockNotifyDelayedEvent(IDelayedEventHandler handler, int ticks, World world, Pos position)
    {
        super(handler, ticks, null);
        this.world = world;
        this.position = position;
    }

    @Override
    protected void onEvent()
    {
        if (!this.world.isRemote)
        {
            world.notifyBlocksOfNeighborChange(position.xi(), position.yi(), position.zi(), position.getBlock(world));
            TileEntity newTile = position.getTileEntity(world);

            if (newTile != null)
            {
                if (Loader.isModLoaded("BuildCraft|Factory"))
                {
                    try
                    {
                        //TODO move to handler to improve mod compatibility
                        Class clazz = Class.forName("buildcraft.factory.TileQuarry");

                        if (clazz == newTile.getClass())
                        {
                            ReflectionHelper.setPrivateValue(clazz, newTile, true, "isAlive");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            ((TileForceMobilizer) handler).performingMove = false;
        }
    }
}