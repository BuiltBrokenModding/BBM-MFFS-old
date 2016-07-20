package mffs.production;

import com.builtbroken.mc.prefab.gui.ContainerBase;
import com.builtbroken.mc.prefab.gui.slot.SlotSpecific;
import mffs.item.card.ItemCardFrequency;
import mffs.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/18/2016.
 */
public class ContainerCoercionDeriver extends ContainerBase {
    public ContainerCoercionDeriver(EntityPlayer player, TileCoercionDeriver tileEntity) {
        super(tileEntity);
        addSlotToContainer(new SlotSpecific(tileEntity, 0, 8, 114, ItemCardFrequency.class));

        addSlotToContainer(new SlotBase(tileEntity, 1, 9, 76));
        addSlotToContainer(new SlotBase(tileEntity, 2, 9 + 20, 76));

        //Upgrade slots
        for (int y = 0; y < 2; y++) {
            addSlotToContainer(new SlotBase(tileEntity, y + 3, 154, 47 + y * 18));
        }

        addPlayerInventory(player);
    }
}

