package fun.rockstarity.api.render.ui.draggables.grid.line;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author ConeTin
 * @since 10 авг. 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GridLine implements IAccess {

	final Animation hoveredAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	final Animation activeAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	final GridRotationType rotationType;
	final float coord;
	
	public GridLine(float coord, GridRotationType rotationType) {
        this.rotationType = rotationType;
        this.coord = coord;
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.hoveredAnim.finished(false) || rock.isPanic()) return;
		
		Round.draw(matrixStack, this.getLineRect(), 0, FixColor.WHITE.alpha(0.4f * this.hoveredAnim.get() + 0.3f * this.activeAnim.get()));
	}
	
	public Rect getLineRect() {
		return this.rotationType == GridRotationType.HORIZONTAL ? new Rect(0, coord, sr.getScaledWidth(), 1) : new Rect(coord, 0, 1, sr.getScaledHeight());
	}
	
}
