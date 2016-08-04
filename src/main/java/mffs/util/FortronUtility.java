package mffs.util;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.fortron.IFortronFrequency;
import mffs.api.modules.IModuleProvider;
import mffs.render.FieldColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.List;

/**
 * A class with useful functions related to Fortron.
 */
public class FortronUtility
{
    public static Fluid fluidFortron = new Fluid("fortron");
    public static FluidStack fluidstackFortron = new FluidStack(fluidFortron, 0);

    public static FluidStack getFortron(int amount)
    {
        return new FluidStack(fluidFortron, amount);
    }

    public static int getAmount(FluidTank fortronTank)
    {
        return fortronTank != null ? getAmount(fortronTank.getFluid()) : 0;
    }

    public static int getAmount(FluidStack liquidStack)
    {
        return liquidStack != null ? liquidStack.amount : 0;
    }

    public static void transferFortron(IFortronFrequency source, List<IFortronFrequency> frequencyTiles, TransferMode transferMode, int limit)
    {
        if (frequencyTiles.size() > 1 && Settings.allowFortronTeleport)
        {
            int totalFortron = 0;
            int totalCapacity = 0;
            int amountToSet = 0;

            for (IFortronFrequency machine : frequencyTiles)
            {
                if (machine != null)
                {
                    totalFortron += machine.getFortronEnergy();
                    totalCapacity += machine.getFortronCapacity();
                }
            }

            if (totalFortron > 0 && totalCapacity > 0)
            {
                switch (transferMode)
                {

                    case equalize:

                        for (IFortronFrequency machine : frequencyTiles)
                        {
                            if (machine != null)
                            {
                                double capacityPercentage = machine.getFortronCapacity() / (double) totalCapacity;
                                amountToSet = (int) (totalFortron * capacityPercentage);
                                doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy(), limit);
                            }
                        }
                        break;

                    case distribute:
                        amountToSet = totalFortron / frequencyTiles.size();
                        for (IFortronFrequency machine : frequencyTiles)
                        {
                            if (machine != null)
                            {
                                doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy(), limit);
                            }

                        }
                        break;

                    case drain:

                        frequencyTiles.remove(source);

                        for (IFortronFrequency machine : frequencyTiles)
                        {
                            if (machine != null)
                            {
                                double capacityPercentage = machine.getFortronCapacity() / (double) totalCapacity;
                                amountToSet = (int) (totalFortron * capacityPercentage);

                                if (amountToSet - machine.getFortronEnergy() > 0)
                                {
                                    doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy(), limit);
                                }
                            }
                        }
                        break;

                    case fill:
                        if (source.getFortronEnergy() < source.getFortronCapacity())
                        {

                            frequencyTiles.remove(source);
                            int requiredFortron = source.getFortronCapacity() - source.getFortronEnergy();

                            for (IFortronFrequency machine : frequencyTiles)
                            {
                                if (machine != null)
                                {
                                    int amountToConsume = Math.min(requiredFortron, machine.getFortronEnergy());
                                    amountToSet = -machine.getFortronEnergy() - amountToConsume;
                                    if (amountToConsume > 0)
                                    {
                                        doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy(), limit);
                                    }

                                }
                            }

                        }
                        break;
                }
            }
        }
    }

    /**
     * Tries to transfer Fortron to a specific machine from this capacitor.
     * Renders an animation on the client side.
     *
     * @param receiver : The machine to be transfered to.
     * @param joules   : The amount of energy to be transfered.
     */
    public static void doTransferFortron(IFortronFrequency transferer, IFortronFrequency receiver, int joules, int limit)
    {
        if (transferer != null && receiver != null)
        {
            TileEntity tileEntity = (TileEntity) transferer;
            World world = tileEntity.getWorldObj();
            boolean isCamo = false;

            if (transferer instanceof IModuleProvider)
            {
                isCamo = ((IModuleProvider) transferer).getModuleCount(ModularForceFieldSystem.moduleCamouflage) > 0;
            }

            if (joules > 0)
            {
                int transferEnergy = Math.min(joules, limit);
                int toBeInjected = receiver.provideFortron(transferer.requestFortron(transferEnergy, false), false);
                toBeInjected = transferer.requestFortron(receiver.provideFortron(toBeInjected, true), true);
                if (world.isRemote && toBeInjected > 0 && !isCamo)
                {
                    ModularForceFieldSystem.proxy.renderBeam(world, new Pos(tileEntity).add(0.5), new Pos((TileEntity) receiver).add(0.5), FieldColor.BLUE, 20);
                }
            }
            else
            {
                int transferEnergy = Math.min(Math.abs(joules), limit);
                int toBeEjected = transferer.provideFortron(receiver.requestFortron(transferEnergy, false), false);
                toBeEjected = receiver.requestFortron(transferer.provideFortron(toBeEjected, true), true);
                if (world.isRemote && toBeEjected > 0 && !isCamo)
                {
                    ModularForceFieldSystem.proxy.renderBeam(world, new Pos((TileEntity) receiver).add(0.5), new Pos(tileEntity).add(0.5), FieldColor.BLUE, 20);
                }

            }
        }
    }
}
