package mffs.api.modules;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface IModuleProvider
{
    /**
     * Gets the ItemStack of a specific module type. This ItemStack is constructed and NOT a reference to the actual stacks within the block.
     */
    ItemStack getModule(IModule module);

    int getModuleCount(IModule module, int... slots);

    List<ItemStack> getModuleStacks(int... slots);

    List<IModule> getModules(int... slots);

    int getFortronCost();
}
