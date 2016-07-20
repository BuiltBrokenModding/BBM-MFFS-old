package mffs.field.gui;

import com.builtbroken.mc.lib.transform.vector.Point;
import com.builtbroken.mc.prefab.gui.ContainerBase;
import com.builtbroken.mc.prefab.gui.slot.SlotSpecific;
import mffs.item.card.ItemCardFrequency;
import mffs.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class ContainerMatrix extends ContainerBase
{
    Point matrixCenter = new Point(110, 55);

    public ContainerMatrix(EntityPlayer player, IInventory tileEntity)
    {
        super(tileEntity);

        Point slotCenter = matrixCenter.add(1);
        //Frequency
        addSlotToContainer(new SlotSpecific(tileEntity, 0, 8, 114, ItemCardFrequency.class));
        //Mode
        addSlotToContainer(new SlotBase(tileEntity, 1, slotCenter.xi(), slotCenter.yi()));

        //FRONT (SOUTH)
        for (int i = 1; i <= 2; i++)
        {
            addSlotToContainer(new SlotBase(tileEntity, i + 1, slotCenter.xi(), slotCenter.yi() - 18 * i));
            //BACK (NORTH)
            addSlotToContainer(new SlotBase(tileEntity, i + 3, slotCenter.xi(), slotCenter.yi() + 18 * i));
            //RIGHT (WEST)
            addSlotToContainer(new SlotBase(tileEntity, i + 5, slotCenter.xi() + 18 * i, slotCenter.yi()));
            //LEFT (EAST)
            addSlotToContainer(new SlotBase(tileEntity, i + 7, slotCenter.xi() - 18 * i, slotCenter.yi()));
        }

        //UP
        addSlotToContainer(new SlotBase(tileEntity, 10, slotCenter.xi() - 18, slotCenter.yi() - 18));
        addSlotToContainer(new SlotBase(tileEntity, 11, slotCenter.xi() + 18, slotCenter.yi() - 18));
        //DOWN
        addSlotToContainer(new SlotBase(tileEntity, 12, slotCenter.xi() - 18, slotCenter.yi() + 18));
        addSlotToContainer(new SlotBase(tileEntity, 13, slotCenter.xi() + 18, slotCenter.yi() + 18));

        int count = 0;
        //Draw non-directional slots
        for (int x = -2; x <= 2; x++)
        {
            for (int y = -2; y <= 2; y++)
            {
                if (new Point(x, y).magnitude() > 2)
                {
                    addSlotToContainer(new SlotBase(tileEntity, count + 14, slotCenter.xi() + 18 * x, slotCenter.yi() + 18 * y));
                    count += 1;
                }
            }
        }

        addPlayerInventory(player);
    }
}