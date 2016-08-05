package mffs.field.gui;

import mffs.field.TileElectromagneticProjector;
import mffs.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Container for the projector.
 *
 * @author Calclavia
 */
public class ContainerElectromagneticProjector extends ContainerMatrix
{
    public ContainerElectromagneticProjector(EntityPlayer player, TileElectromagneticProjector tileEntity)
    {
        super(player, tileEntity);
        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                addSlotToContainer(new SlotBase(tileEntity, x + y * 2 + (1 + 25), 21 + 18 * x, 31 + 18 * y));
            }
        }
    }
}
