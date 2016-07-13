package mffs.field.mobilize.event;

import java.lang.reflect.Method;

class DelayedEvent
{
    int ticks = 0;

    public DelayedEvent(IDelayedEventHandler handler, int ticks, Method method)
    {

    }

    protected void onEvent()
    {
        evtMethod.apply();
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
}