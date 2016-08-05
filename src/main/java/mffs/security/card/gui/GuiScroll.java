package mffs.security.card.gui;

import org.lwjgl.input.Mouse;

/**
 * @author Calclavia
 */
class GuiScroll
{
    private final int height;
    private float _currentScroll = 0f;

    public GuiScroll(int height)
    {
        this.height = height;
    }

    /**
     * Percentage of scroll
     *
     * @return value between 0 - 1
     */
    public float getCurrent()
    {
        return _currentScroll;
    }

    public void setCurrent(float scroll)
    {
        _currentScroll = Math.min(Math.max(scroll, 0), 1);
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput()
    {
        int i = Mouse.getEventDWheel();

        if (i != 0)
        {
            i = Math.min(Math.max(i, -1), 1);
            setCurrent(getCurrent() - (float) i / (float) height);
        }
    }
}
