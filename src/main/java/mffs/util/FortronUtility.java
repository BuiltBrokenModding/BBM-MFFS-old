package mffs.util;

import java.util.List;

import com.builtbroken.mc.lib.transform.vector.Pos;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * A class with useful functions related to Fortron.
 *
 */
public class FortronUtility {
	private Fluid fluidFortron = new Fluid("fortron");
	private FluidStack fluidstackFortron = new FluidStack(fluidFortron, 0);

	public FluidStack getFortron(int amount) {
		return new FluidStack(fluidFortron, amount);
	}

	public int getAmount(FluidTank fortronTank) {
		return fortronTank != null ? getAmount(fortronTank.getFluid()) : 0;
	}

	public int getAmount(FluidStack liquidStack) {
		return liquidStack != null ? liquidStack.amount : 0;
	}

	public void transferFortron(IFortronFrequency source, List<IFortronFrequency> frequencyTiles, TransferMode transferMode, int limit) {
    if (frequencyTiles.size > 1 && Settings.allowFortronTeleport) {
      int totalFortron = 0, totalCapacity = 0;

      for (IFortronFrequency machine : frequencyTiles) {
        if (machine != null) {
          totalFortron += machine.getFortronEnergy();
          totalCapacity += machine.getFortronCapacity();
        }
      }
      
      if (totalFortron > 0 && totalCapacity > 0) {
        switch(transferMode) {
        
          case equalize:
          
            for (IFortronFrequency machine : frequencyTiles) {
              if (machine != null) {
                double capacityPercentage = machine.getFortronCapacity() / (double) totalCapacity;
                int amountToSet = (int) (totalFortron * capacityPercentage);
                doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit);
              }
            }
            break;
          
          case distribute:
            int amountToSet = totalFortron / frequencyTiles.size;
            for (IFortronFrequency machine : frequencyTiles) {
              if (machine != null)
                doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit);
              
            }
            break;
          
          case drain:
          
            frequencyTiles.remove(source);

            for(IFortronFrequency machine : frequencyTiles) {
              if (machine != null) {
                double capacityPercentage = machine.getFortronCapacity() / (double) totalCapacity;
                int amountToSet = totalFortron * capacityPercentage;

                if (amountToSet - machine.getFortronEnergy > 0)
                  doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit);
              }
            }
            break;
          
          case fill:
            if (source.getFortronEnergy < source.getFortronCapacity) {
            	
              frequencyTiles.remove(source);
              int requiredFortron = source.getFortronCapacity - source.getFortronEnergy;

              for (IFortronFrequency machine : frequencyTiles) {
                if (machine != null) {
                  int amountToConsume = Math.min(requiredFortron, machine.getFortronEnergy);
                  int amountToSet = -machine.getFortronEnergy - amountToConsume;
                  if (amountToConsume > 0)
                	  doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit);
                  
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
	 * @param receiver
	 *            : The machine to be transfered to.
	 * @param joules
	 *            : The amount of energy to be transfered.
	 */
	public void doTransferFortron(IFortronFrequency transferer, IFortronFrequency receiver, int joules, int limit) {
		if (transferer != null && receiver != null) {
			TileEntity tileEntity = transferer;
			World world = tileEntity.getWorldObj();
			boolean isCamo = false;

			if (transferer instanceof IModuleProvider)
				isCamo = ((IModuleProvider) transferer).getModuleCount(ModularForceFieldSystem.moduleCamouflage) > 0;

			if (joules > 0) {
				int transferEnergy = Math.min(joules, limit);
				int toBeInjected = receiver.provideFortron(transferer.requestFortron(transferEnergy, false), false);
				toBeInjected = transferer.requestFortron(receiver.provideFortron(toBeInjected, true), true);
				if (world.isRemote && toBeInjected > 0 && !isCamo)
					ModularForceFieldSystem.proxy.renderBeam(world, new Pos(tileEntity).add(0.5),
							new Pos(receiver.asInstanceOf[TileEntity]).add(0.5), FieldColor.blue, 20);
			} else {
				int transferEnergy = Math.min(Math.abs(joules), limit);
				int toBeEjected = transferer.provideFortron(receiver.requestFortron(transferEnergy, false), false);
				toBeEjected = receiver.requestFortron(transferer.provideFortron(toBeEjected, true), true);
				if (world.isRemote && toBeEjected > 0 && !isCamo)
					ModularForceFieldSystem.proxy.renderBeam(world, new Pos(receiver.asInstanceOf[TileEntity]).add(0.5),
							new Pos(tileEntity).add(0.5), FieldColor.blue, 20);

			}
		}
	}
}
