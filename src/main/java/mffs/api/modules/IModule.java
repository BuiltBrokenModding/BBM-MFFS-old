package mffs.api.modules;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * A module for any matrix based machines.
 */
public interface IModule extends IFortronCost
{
	/**
	 * Called before the projector projects a field.
	 *
	 * @param projector
	 * @return True to stop projecting.
	 */
	default boolean onProject(IProjector projector, List<Pos> field)
    {
        return false;
    }

	default boolean onDestroy(IProjector projector, List<Pos> field)
    {
        return false;
    }

	/**
	 * Called right before the projector creates a force field block.
	 *
	 * @return 0 - Do nothing; 1 - Skip this block and continue; 2 - Cancel rest of projection;
	 */
	default int onProject(IProjector projector, Pos position)
	{
		return 0;
	}

	/**
	 * Called when an entity collides with a force field block.
	 *
	 * @return False to stop the default process of entity collision.
	 */
	default boolean onCollideWithForceField(World world, int x, int y, int z, Entity entity, ItemStack moduleStack)
	{
		return true;
	}

	/**
	 * Called in this module when it is being calculated by the projector. Called BEFORE
	 * transformation is applied to the field.
	 *
	 * @return False if to prevent this position from being added to the projection add.
	 */
	default void onPreCalculate(IFieldMatrix projector, List<Pos> calculatedField){}
	/**
	 * Called in this module when after being calculated by the projector.
	 *
	 * @return False if to prevent this position from being added to the projection add.
	 */
	default void onPostCalculate(IFieldMatrix projector, List<Pos> fieldDefinition){}

	/**
	 * @param moduleStack
	 * @return Does this module require ticking from the force field projector?
	 */
	default boolean requireTicks(ItemStack moduleStack)
    {
        return false;
    }

	/**
	 * DEFUALT:
	 * TileEntity tile = (TileEntity) projector;
	 Cube volume = new Cube(projector.getNegativeScale().multiply(-1), projector.getPositiveScale().add(1)).add(new Pos(tile).add(projector.getTranslation()));
	 return tile.getWorldObj().getEntitiesWithinAABB(Entity.class, volume.toAABB());
	 * @param projector
	 * @return
     */
	default List<Entity> getEntitiesInField(IProjector projector)
    {
        return new ArrayList();
    }
}
