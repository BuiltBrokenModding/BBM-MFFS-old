package mffs.api.machine;

public interface IForceField
{
	IProjector getProjector();

	/**
	 * Weakens a force field block, destroying it temporarily and draining power from the projector.
	 *
	 * @param joules - Power to drain.
	 */
	void weakenForceField(int joules);
}