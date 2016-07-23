package mffs.api.machine;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.modules.IModule;
import mffs.api.modules.IModuleProvider;
import mffs.api.modules.IProjectorMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

/**
 * Applied to the field matrix tile
 */
public interface IFieldMatrix extends IModuleProvider, IActivatable, IPermissionProvider
{
	/**
	 * Gets the mode of the projector, mainly the shape and size of it.
	 */
	IProjectorMode getMode();

	ItemStack getModeStack();

	/**
	 * Gets the slot IDs based on the direction given.
	 */
	int[] getDirectionSlots(ForgeDirection direction);

	/**
	 * Gets the unspecified, direction-unspecific module slots on the left side of the GUI.
	 */
	int[] getModuleSlots();

	/**
	 * @param module    - The module instance.
	 * @param direction - The direction facing.
	 * @return Gets the amount of modules based on the side.
	 */
	int getSidedModuleCount(IModule module, ForgeDirection... direction);

	/**
	 * Transformation information functions. Returns CACHED information unless the cache is cleared.
	 * Note that these are all RELATIVE to the projector's position.
	 */
	Pos getTranslation();

	Pos getPositiveScale();

	Pos getNegativeScale();

	int getRotationYaw();

	int getRotationPitch();

	/**
	 * @return Gets all the absolute block coordinates that are occupying the force field. Note that this is a copy of the actual field set.
	 */
	List<Pos> getCalculatedField();

	/**
	 * Gets the absolute interior points of the projector. This might cause lag so call sparingly.
	 *
	 * @return
	 */
	List<Pos> getInteriorPoints();

	/**
	 * @return Gets the facing direction. Always returns the front side of the block.
	 */
	ForgeDirection getDirection();

}
