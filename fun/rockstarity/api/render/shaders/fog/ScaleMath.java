package fun.rockstarity.api.render.shaders.fog;

import fun.rockstarity.api.IAccess;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScaleMath implements IAccess {
    private final int SCALE = 2;

    public void scalePre() {
        mc.gameRenderer.setupOverlayRendering(SCALE);
    }

    public void scalePost() {
        mc.gameRenderer.setupOverlayRendering();
    }

    public Vector2d getMouse(double mouseX, double mouseY) {
        return new Vector2d(mouseX * mc.getMainWindow().getGuiScaleFactor() / SCALE, mouseY * mc.getMainWindow().getGuiScaleFactor() / SCALE);
    }

}