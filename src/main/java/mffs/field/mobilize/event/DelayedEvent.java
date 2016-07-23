package mffs.field.mobilize.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DelayedEvent
{
    public int ticks = 0;
    Method method;
    IDelayedEventHandler handler;

    public DelayedEvent(IDelayedEventHandler handler, int ticks, Method method)
    {
        this.ticks = ticks;
        this.method = method;
        this.handler = handler;
    }

    protected void onEvent()
    {
        try
        {
            method.invoke(handler);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
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
}