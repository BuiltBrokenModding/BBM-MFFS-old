package mffs.field.gui;

import com.builtbroken.mc.prefab.gui.ContainerBase;
import mffs.field.TileElectromagneticProjector;
import mffs.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Container for the projector.
 *
 * @author Calclavia
 */
public class ContainerElectromagneticProjector extends ContainerBase
{
    public ContainerElectromagneticProjector(EntityPlayer player, TileElectromagneticProjector tileEntity)
    {
        super(player, tileEntity);
        addSlotToContainer(new SlotBase(tileEntity, TileElectromagneticProjector.MODE_SLOT, 50, 40));
        int slotID = 1;
        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                addSlotToContainer(new SlotBase(tileEntity, slotID++, 8 + 18 * x, 31 + 18 * y));
            }
        }
        addPlayerInventory(player, 8, 135);
    }
}
