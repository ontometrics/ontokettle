package be.ibridge.kettle.core;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Class to keep track of which font is a system font (managed by the OS) and which is not.
 * 
 * @author Matt
 * @since 2006-06-15
 *
 */
public class ManagedFont
{
    private Font font;
    private boolean systemFont;
    
    /**
     * @param font
     * @param sytemFont
     */
    public ManagedFont(Font font, boolean systemFont)
    {
        this.font = font;
        this.systemFont = systemFont;
    }

    /**
     * Create a new managed font by using fontdata
     * @param display
     * @param rgb
     */
    public ManagedFont(Display display, FontData fontData)
    {
        this.font = new Font(display, fontData);
        this.systemFont = false;
    }

    /**
     * Free the managed resource if it hasn't already been done and if this is not a system font
     *
     */
    public void dispose()
    {
        // System color and already disposed off colors don't need to be disposed!
        if (!systemFont && !font.isDisposed())
        {
            font.dispose();
        }
    }

    /**
     * @return Returns the font.
     */
    public Font getFont()
    {
        return font;
    }

    /**
     * @return true if this is a system font.
     */
    public boolean isSystemFont()
    {
        return systemFont;
    }
}