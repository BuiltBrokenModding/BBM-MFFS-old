package com.builtbroken.mffs.field.mobilize.event;

public interface IDelayedEventHandler
{
    void queueEvent(DelayedEvent evt);
}
