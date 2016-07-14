package mffs.security.card.gui;

import com.builtbroken.jlib.data.Colors;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketPlayerItem;
import com.builtbroken.mc.lib.access.AccessUser;
import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.access.Permissions;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import mffs.ModularForceFieldSystem;
import mffs.item.gui.GuiItem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A gui that contains the permissions
 *
 * @author Calclavia
 */
public abstract class GuiAccessCard extends GuiItem
{
    protected final List<Permission> permissions = new ArrayList();
    protected final GuiScroll scroll;
    protected EntityPlayer player;

    public GuiAccessCard(EntityPlayer player, ItemStack itemStack, Container container)
    {
        super(itemStack, container);
        this.player = player;
        permissions.addAll(Permissions.root.getAllChildren().stream().collect(Collectors.toList()));
        scroll = new GuiScroll(Math.max(permissions.size() - 4, 0));
    }

    @Override
    public void initGui()
    {
        super.initGui();
        for (int i = 0; i < permissions.size(); i++)
        {
            //TODO localize entries
            buttonList.add(new GuiButton(i, 0, 0, 160, 20, permissions.get(i).toString()));
        }
    }

    @Override
    public void handleMouseInput()
    {
        super.handleMouseInput();
        scroll.handleMouseInput();
    }

    @Override
    public void updateScreen()
    {
        int index = (int) (scroll.getCurrent() * buttonList.size());
        int maxIndex = index + 3;
        if(InventoryUtility.stacksMatchExact(stack, player.getCurrentEquippedItem()))
        {
            AccessUser access = ModularForceFieldSystem.cardID.getAccess(stack);
            if (access != null)
            {
                //Loop threw buttons updating status TODO look into replacing as this doesn't need to update each tick
                for (int i = 0; i < permissions.size(); i++)
                {
                    GuiButton button = (GuiButton) buttonList.get(i);
                    if (i >= index && button.id <= maxIndex)
                    {
                        Permission perm = permissions.get(button.id);
                        button.displayString = "";
                        if (access.hasNode(perm))
                        {
                            button.displayString += Colors.BRIGHT_GREEN.code;
                        }
                        else
                        {
                            button.displayString += Colors.RED.code;
                        }
                        button.displayString += LanguageUtility.getLocal(perm.toString());
                        button.xPosition = width / 2 - 80;
                        button.yPosition = height / 2 - 60 + (button.id - index) * 20;
                        button.visible = true;
                    }
                    else
                    {
                        button.visible = false;
                    }
                }
            }
            else
            {
                //TODO show error, or/and kick out of Gui, data on stack is invalid
            }
        }
        else
        {
            //TODO show error, or/and kick out of Gui, invalid stack match was opened
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);

        //Toggle this specific permission
        Engine.instance.packetHandler.sendToServer(new PacketPlayerItem(player, 0, permissions.get(guiButton.id).toString()));
    }
}
