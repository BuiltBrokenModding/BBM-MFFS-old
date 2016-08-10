package com.builtbroken.mffs.security;

import com.builtbroken.mc.prefab.gui.ContainerBase;
import com.builtbroken.mffs.slot.SlotBase;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerBiometricIdentifier extends ContainerBase
{

    public ContainerBiometricIdentifier(EntityPlayer player, TileBiometricIdentifier tile)
    {
        super(tile);
        //Frequency
        //addSlotToContainer(new SlotSpecific(tile, 0, 8, 114, ItemCardFrequency.class));

        for (int x = 0; x < 9; x++)
        {
            for (int y = 0; y < 4; y++)
            {
                addSlotToContainer(new SlotBase(tile, x + y * 9 + 1, 9 + x * 18, 36 + y * 18));
            }
        }

        addPlayerInventory(player, 8, 135);
    }
}