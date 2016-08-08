package mffs.api.modules;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface IModuleProvider
{
    int getModuleCount(ICardModule module, int... slots);

    List<ItemStack> getModuleStacks(int... slots);

    int getFortronCost();
}
