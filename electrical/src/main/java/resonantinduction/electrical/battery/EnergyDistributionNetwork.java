package resonantinduction.electrical.battery;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import calclavia.lib.utility.FluidUtility;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.api.mechanical.fluid.IFluidConnector;
import universalelectricity.core.net.Network;

public class EnergyDistributionNetwork extends Network<EnergyDistributionNetwork, TileEnergyDistribution>
{
	public void redistribute(TileEnergyDistribution... exclusion)
	{
		long totalEnergy = 0;
		long totalCapacity = 0;

		int lowestY = 255, highestY = 0;

		for (TileEnergyDistribution connector : this.getConnectors())
		{
			totalEnergy += connector.energy.getEnergy();
			totalCapacity += connector.energy.getEnergyCapacity();

			lowestY = Math.min(connector.yCoord, lowestY);
			highestY = Math.max(connector.yCoord, highestY);

			connector.renderEnergyAmount = 0;
		}

		/**
		 * Apply render
		 */
		long remainingRenderEnergy = totalEnergy;

		for (int y = lowestY; y <= highestY; y++)
		{
			Set<TileEnergyDistribution> connectorsInlevel = new LinkedHashSet<TileEnergyDistribution>();

			for (TileEnergyDistribution connector : this.getConnectors())
			{
				if (connector.yCoord == y)
				{
					connectorsInlevel.add(connector);
				}
			}

			int levelSize = connectorsInlevel.size();
			long used = 0;

			for (TileEnergyDistribution connector : connectorsInlevel)
			{
				long tryInject = Math.min(remainingRenderEnergy / levelSize, connector.energy.getEnergyCapacity());
				connector.renderEnergyAmount = tryInject;
				used += tryInject;
			}

			remainingRenderEnergy -= used;

			if (remainingRenderEnergy <= 0)
				break;
		}

		/**
		 * Apply energy loss.
		 */
		double percentageLoss = Math.max(0, (1 - (getConnectors().size() * 6 / 100d)));
		long energyLoss = (long) (percentageLoss * 100);
		totalEnergy -= energyLoss;

		int amountOfNodes = this.getConnectors().size() - exclusion.length;

		if (totalEnergy > 0 && amountOfNodes > 0)
		{
			long remainingEnergy = totalEnergy;

			TileEnergyDistribution firstNode = this.getFirstConnector();

			for (TileEnergyDistribution node : this.getConnectors())
			{
				if (node != firstNode && !Arrays.asList(exclusion).contains(node))
				{
					double percentage = ((double) node.energy.getEnergyCapacity() / (double) totalCapacity);
					long energyForBattery = Math.round(totalEnergy * percentage);
					node.energy.setEnergy(energyForBattery);
					remainingEnergy -= energyForBattery;
				}
			}

			firstNode.energy.setEnergy(remainingEnergy);
		}
	}

	@Override
	protected void reconstructConnector(TileEnergyDistribution node)
	{
		node.setNetwork(this);
	}

	@Override
	public EnergyDistributionNetwork newInstance()
	{
		return new EnergyDistributionNetwork();
	}
}