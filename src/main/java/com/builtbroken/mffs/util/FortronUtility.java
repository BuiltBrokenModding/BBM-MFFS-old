package com.builtbroken.mffs.util;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mffs.ModularForceFieldSystem;
import com.builtbroken.mffs.Settings;
import com.builtbroken.mffs.api.fortron.IFortronFrequency;
import com.builtbroken.mffs.api.modules.IModuleProvider;
import com.builtbroken.mffs.base.TilePacketType;
import com.builtbroken.mffs.render.FieldColor;
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
    public static Fluid fluidFortron;

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
            TileEntity tileTrans = (TileEntity) transferer;
            TileEntity tileRec = (TileEntity) receiver;
            //World world = tileTrans.getWorldObj();
            boolean isCamo = (transferer instanceof IModuleProvider && ((IModuleProvider) transferer).getModuleCount(ModularForceFieldSystem.moduleCamouflage) > 0);

            if (joules < 0) { //we switch the frequencies! Means they have less than the receiver
                IFortronFrequency dummy = transferer;
                transferer = receiver;
                receiver = dummy;
            }

            boolean inverse = joules < 0;
            joules = Math.min(inverse ? Math.abs(joules) : joules, limit);
            int toBeInject = receiver.provideFortron(transferer.requestFortron(joules, false), false);
            toBeInject = transferer.requestFortron(receiver.provideFortron(toBeInject, true), true);

            if(toBeInject > 0 && !isCamo)
                Engine.instance.packetHandler.sendToAllAround(new PacketTile(tileTrans, TilePacketType.effect.ordinal(), inverse, new Pos(tileRec)), tileRec);
        }
    }
}
