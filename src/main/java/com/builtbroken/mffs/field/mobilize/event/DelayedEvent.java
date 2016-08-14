package com.builtbroken.mffs.field.mobilize.event;

public class DelayedEvent
{
    public int ticks = 0;
    DelayedAction action;
    IDelayedEventHandler handler;

    public DelayedEvent(IDelayedEventHandler handler, int ticks, DelayedAction method)
    {
        this.ticks = ticks;
        this.action = method;
        this.handler = handler;
    }

    protected void onEvent()
    {
        if (action != null)
        {
            action.doAction(handler);
        }
    }

    public void update()
    {
        if (ticks == 0)
        {
            onEvent();
        }

        ticks -= 1;
    }

    /**
     * The higher the number, the higher the priority.
     *
     * @return
     */
    public int priority()
    {
        return 0;
    }

    public static class DelayedAction
    {
        public void doAction(IDelayedEventHandler handler)
        {

        }
    }
}