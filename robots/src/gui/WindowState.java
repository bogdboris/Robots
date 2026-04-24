package gui;

import java.io.Serializable;
import java.awt.Rectangle;

public class WindowState implements Serializable {
    private String windowId;
    private Rectangle bounds;
    private boolean isIcon;
    private boolean isMaximum;

    public WindowState(String windowId, Rectangle bounds, boolean isIcon, boolean isMaximum) {
        this.windowId = windowId;
        this.bounds = bounds;
        this.isIcon = isIcon;
        this.isMaximum = isMaximum;
    }

    public String getWindowId() {
        return windowId;
    }
    public Rectangle getBounds() {
        return bounds;
    }
    public boolean isIcon() {
        return isIcon;
    }
    public boolean isMaximum() {
        return isMaximum;
    }
}
