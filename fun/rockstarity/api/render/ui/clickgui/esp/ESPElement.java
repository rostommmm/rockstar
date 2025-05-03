package fun.rockstarity.api.render.ui.clickgui.esp;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 окт. 2024 г.
 */


@Getter @Setter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class ESPElement extends Bindable implements IAccess {
	
	private ArrayList<Setting> settings = new ArrayList<>();
	// Основное
	boolean active;
	@Setter int direction;
	float xOffset, yOffset;
	final String name;
	@Getter 
	Rect rect = Rect.EMPTY;
	
	// Анимки
	final Animation activeAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	final InfinityAnimation xAnim = new InfinityAnimation();
	final InfinityAnimation yAnim = new InfinityAnimation();
	float targetX, targetY;
	
	// Дополнительное
	boolean dragging;
	float dragX, dragY;
	
	public void drawPreview(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim) {
		activeAnim.setForward(active);
		
		if (this.dragging) {
			targetX = mouseX+dragX;
			targetY = mouseY+dragY;
		}
		
		Round.draw(matrixStack, rect.width((float) bold.get(14).getWidth(name) + 5).height(10), 2, rock.getThemes().getThirdColor().alpha(anim).alpha(1-activeAnim.get()));
		
		bold.get(14).draw(matrixStack, name, rect.getX() + 2, rect.getY() + .5f, rock.getThemes().getTextSecondColor().alpha(anim).darker(0.1f).alpha(1-activeAnim.get()));
	}
	
	public abstract void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim);
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		if (Hover.isHovered(active ? rect : rect.width((float) bold.get(14).getWidth(name) + 5).height(10), mouseX, mouseY)) {
			dragging = true;
			dragX = (int) (rect.getX() - mouseX);
			dragY = (int) (rect.getY() - mouseY);
			return false;
		}
		return false;
	}
	
	public void released(double mouseX, double mouseY, int button) {
		dragging = false;
	}
	
}
