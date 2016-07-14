package mffs.api.modules;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;

import java.util.List;


public interface IProjectorMode extends IFortronCost
{
    /**
     * Called when the force field projector calculates the shape of the module.
     *
     * @param projector - The Projector Object. Can cast to TileEntity.
     * @return The blocks actually making up the force field. This array of blocks are
     * NOT affected by rotation or translation, and is relative to the center of the projector.
     */
    List<Pos> getExteriorPoints(IFieldMatrix projector);

    /**
     * @return Gets all interior points. Not translated or rotated.
     */
    List<Pos> getInteriorPoints(IFieldMatrix projector);

    /**
     * @return Is this specific position inside of this force field?
     */
    boolean isInField(IFieldMatrix projector, Pos position);

    /**
     * Called to render an object in front of the projection.
     */
    void render(IProjector projector, double x, double y, double z, float f, long ticks);
}
