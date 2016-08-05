package mffs.api.fortron;

import java.util.List;

/**
 * Applied to the Fortron Capacitor TileEntity. Extends IFortronFrequency
 *
 * @author Calclavia
 */
public interface IFortronCapacitor
{
    public List<IFortronFrequency> getFrequencyDevices();

    public int getTransmissionRange();

    public int getTransmissionRate();
}
