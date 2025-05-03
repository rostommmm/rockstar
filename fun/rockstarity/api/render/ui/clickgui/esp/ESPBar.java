package fun.rockstarity.api.render.ui.clickgui.esp;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 28 РѕРєС‚. 2024вЂЇРі.
 */

public class ESPBar extends ESPElement {
	
	protected float percent;
	protected FixColor color;
	
	public ESPBar(String name) {
		super(name);
	}

	@Override
	public void drawPreview(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim) {
		super.drawPreview(matrixStack, mouseX, mouseY, partialTicks, anim);
		
		if (activeAnim.finished(false)) return;
		
		anim *= activeAnim.get();
		
		ESPSettings settings = rock.getClickGui().getWindow().getEspSettings();
		Rect previewRect = settings.getPreviewRect();
		
		float x = previewRect.getX() + previewRect.getWidth() / 2F - 25F;
		float y = previewRect.getY() + previewRect.getHeight() / 2F - 60F;
		
		if (!dragging) {
			switch (direction) {
			case 0:
				targetX = x;
				targetY = y - 6;
				break;
			case 1:
				targetX = x;
				targetY = y + 122;
				break;
			case 2:
				targetX = x - 6;
				targetY = y;
				break;
			case 3:
				targetX = x + 52;
				targetY = y;
				break;
			}
		}
		
		if (direction < 2) {
			rect.setWidth(50);
			rect.setHeight(6);
		} else {
			rect.setWidth(6);
			rect.setHeight(120);
		}
		
		Stencil.init();
		Round.draw(matrixStack, settings.getPreviewRect(), 7, FixColor.WHITE);
		Stencil.read(1);
		
		FixColor first = FixColor.GREEN.alpha(anim);
		FixColor second = first.darker(0.5f);
		
		Round.draw(matrixStack, new Rect(
				rect.getX() + (direction == 2 ? 2 : 0), 
				rect.getY() + (direction == 0 ? 2 : 0),
				rect.getWidth() - (direction < 2 ? 0 : 4), 
				rect.getHeight() - (direction < 2 ? 4 : 0)),
				1, 
				first, 
				direction < 2 ? second : first, 
				direction < 2 ? first : second, 
				second);
		
		Stencil.finish();
	}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		anim *= activeAnim.get();
		
		FixColor up = color.alpha(anim);
		FixColor down = up.darker(0.5f);
		
		switch (direction) {
		case 0:
			Round.draw(matrixStack, new Rect(x, y - 4, width, 2), 1, rock.getThemes().getFoursColor().alpha(anim));
			Round.draw(matrixStack, new Rect(x + width/2F - width/2F * percent, y - 4, width * percent, 2), 1, up, down, up, down);
			break;
		case 1:
			Round.draw(matrixStack, new Rect(x, y + height + 2, width, 2), 1, rock.getThemes().getFoursColor().alpha(anim));
			Round.draw(matrixStack, new Rect(x + width/2F - width/2F * percent, y + height + 2, width * percent, 2), 1, up, down, up, down);
			break;
		case 2:
			Round.draw(matrixStack, new Rect(x - 4, y, 2, height), 1, rock.getThemes().getFoursColor().alpha(anim));
			Round.draw(matrixStack, new Rect(x - 4, y + height - height * percent, 2, height * percent), 1, up, up, down, down);
			break;
		case 3:
			Round.draw(matrixStack, new Rect(x + width + 2, y, 2, height), 1, rock.getThemes().getFoursColor().alpha(anim));
			Round.draw(matrixStack, new Rect(x + width + 2, y + height - height * percent, 2, height * percent), 1, up, up, down, down);
			break;
		}
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		if (direction < 2) {
			rect.setWidth(50);
			rect.setHeight(4);
		} else {
			rect.setWidth(4);
			rect.setHeight(120);
		}
		return super.clicked(mouseX, mouseY, button);
	}
	
}
