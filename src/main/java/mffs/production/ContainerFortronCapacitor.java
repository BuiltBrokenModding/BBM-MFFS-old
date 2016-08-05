package mffs.production;

import com.builtbroken.mc.prefab.gui.ContainerBase;
import com.builtbroken.mc.prefab.gui.slot.SlotSpecific;
import mffs.item.card.ItemCardFrequency;
import mffs.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerFortronCapacitor extends ContainerBase
{

    public ContainerFortronCapacitor(EntityPlayer player, TileFortronCapacitor tileEntity)
    {
        super(tileEntity);
        //Frequency
        addSlotToContainer(new SlotSpecific(tileEntity, 0, 8, 114, ItemCardFrequency.class));

        //Upgrade slots
        for (int y = 0; y <= 2; y++)
        {
            addSlotToContainer(new SlotBase(tileEntity, y + 1, 154, 47 + y * 18));
        }

        //Input slots
        for (int x = 0; x <= 1; x++)
        {
            for (int y = 0; y <= 1; y++)
            {
                addSlotToContainer(new SlotBase(this.inventory, x + y * 2 + 4, 9 + x * 18, 74 + y * 18));
            }
        }

        //Output slots
        for (int x = 0; x <= 1; x++)
        {
            for (int y = 0; y <= 1; y++)
            {
                addSlotToContainer(new SlotBase(this.inventory, x + y * 2 + 8, 91 + x * 18, 74 + y * 18));
            }
        }

        addPlayerInventory(player);
    }
}