package resonant.api.mffs.modules;

/**
 * @author Calclavia
 */
public interface IFortronCost
{
	/**
	 * The amount of Fortron this module consumes per tick.
	 *
	 * @return
	 */
	float getFortronCost(float amplifier);
}
