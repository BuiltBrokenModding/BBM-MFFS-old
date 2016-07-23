package mffs.security.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.machine.IProjector;
import mffs.field.TileElectromagneticProjector;
import mffs.security.MFFSPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class ItemModuleConfiscate extends ItemModuleDefense
{
    @Override
    public boolean onProject(IProjector projector, List<Pos> fields)
    {
        TileElectromagneticProjector proj = (TileElectromagneticProjector) projector;
        List<Entity> entities = getEntitiesInField(projector);

        entities.stream()
                .filter(e -> e instanceof EntityPlayer)
                .map(e -> (EntityPlayer) e)
                .filter(player -> !proj.hasPermission(player.getGameProfile(), MFFSPermissions.bypassConfiscation))
                .forEach(
                        player ->
                        {
                            List<Item> filterItems = proj.getFilterItems();
                            //TODO: Support inventory entities
                            IInventory inventory = player.inventory;

                            //TODO optimize as we are iterating twice
                            List<Integer> relevantSlots = new ArrayList();
                            for (int i = 0; i < inventory.getSizeInventory(); i++)
                            {
                                ItemStack checkStack = inventory.getStackInSlot(i);

                                if (checkStack != null && proj.isInvertedFilter() != filterItems.contains(checkStack.getItem()))
                                {
                                    relevantSlots.add(i);
                                }
                            }


                            for (Integer i : relevantSlots)
                            {
                                proj.mergeIntoInventory(inventory.getStackInSlot(i));
                                inventory.setInventorySlotContents(i, null);
                            }

                            if (relevantSlots.size() > 0)
                            {
                                player.addChatMessage(new ChatComponentTranslation("message.moduleConfiscate.confiscate", relevantSlots.size() + ""));
                                // LanguageUtility.getLocal()
                            }
                        }
                );

        return false;
    }
}