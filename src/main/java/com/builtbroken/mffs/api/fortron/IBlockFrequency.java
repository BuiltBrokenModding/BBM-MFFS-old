package com.builtbroken.mffs.api.fortron;

import com.builtbroken.mc.api.IWorldPosition;

public interface IBlockFrequency extends IWorldPosition

{
    int getFrequency();
    //TODO get actual interface from where every it went to, this is a place holder to remove errors

    void setFrequency(int f);
}