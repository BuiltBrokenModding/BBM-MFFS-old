package com.builtbroken.mffs.api.fortron;

import java.util.List;

/**
 * Applied to the Fortron Capacitor TileEntity. Extends IFortronFrequency
 *
 * @author Calclavia
 */
public interface IFortronCapacitor
{
    List<IFortronFrequency> getFrequencyDevices();

    int getTransmissionRange();

    int getTransmissionRate();
}
