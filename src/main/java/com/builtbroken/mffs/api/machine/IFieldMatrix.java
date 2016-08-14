package com.builtbroken.mffs.api.machine;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mffs.api.modules.IModuleProvider;
import com.builtbroken.mffs.api.modules.IProjectorMode;
import net.minecraft.item.ItemStack;

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
     * Mainly for internal use, this allows the calculated field to
     * be set from outside of the field matrix.
     *
     * @param field - list of field positions not translated by matrix position
     */
    void setCalculatedField(List<Pos> field);


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
     * Called to generate the field
     *
     * @return list of positions not translated by matrix position or rotation
     */
    List<Pos> generateCalculatedField();

    ItemStack getModeStack();
}
