package mffs.api.machine;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.fortron.IBlockFrequency;
import net.minecraft.inventory.IInventory;

import java.util.List;

/**
 * Also extends IDisableable, IFortronFrequency
 *
 * @author Calclavia
 */
public interface IProjector extends IInventory, IFieldMatrix, IBlockFrequency
{
	/**
	 * Projects the force field.
	 */
	void projectField();

	/**
	 * Destroys the force field.
	 */
	void destroyField();

	/**
	 * @return The speed in which a force field is constructed.
	 */
	int getProjectionSpeed();

	/**
	 * @return The amount of ticks this projector has existed in the world.
	 */
	long getTicks();

	/**
	 * DO NOT modify this list. Read-only.
	 *
	 * @return The actual force field block coordinates in the world.
	 */
	List<Pos> getForceFields();

	int provideFortron(int energy, boolean b);
}