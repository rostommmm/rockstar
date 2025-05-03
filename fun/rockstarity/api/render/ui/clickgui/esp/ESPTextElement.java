package fun.rockstarity.api.render.ui.clickgui.esp;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 6 окт. 2024 г.
 */

@Setter @Getter
public class ESPTextElement extends ESPElement {
	
	private String text;

	public ESPTextElement(String name) {
		super(name);
	}
	
	@Override
	public void drawPreview(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim) {
		super.drawPreview(matrixStack, mouseX, mouseY, partialTicks, anim);
		
		if (activeAnim.finished(false)) return;
		
		anim *= activeAnim.get();
		
		ESPSettings settings = rock.getClickGui().getWindow().getEspSettings();
		Rect previewRect = settings.getPreviewRect();
		
		FontSize font = bold.get(14);
		float textWidth = font.getWidth(name);
		float x = previewRect.getX() + previewRect.getWidth() / 2F - 25F;
		float y = previewRect.getY() + previewRect.getHeight() / 2F - 60F;
		
		if (!dragging) {
			switch (direction) {
			case 0:
				targetX = x + 25 - textWidth / 2F;
				targetY = y - 11;
				break;
			case 1:
				targetX = x + 25 - textWidth / 2F;
				targetY = y + 120;
				break;
			case 2:
				targetX = x - 3 - textWidth;
				targetY = y;
				break;
			case 3:
				targetX = x + 53;
				targetY = y;
				break;
			}
		}
		
		if (!dragging) {
			targetY += yOffset;
			targetX += xOffset;
		}
		
		rect.setWidth(font.getWidth(name));
		
		Stencil.init();
		Round.draw(matrixStack, settings.getPreviewRect(), 7, FixColor.WHITE);
		Stencil.read(1);
		
		font.draw(matrixStack, name, rect.getX() + 0.5f, rect.getY() + 0.5f, FixColor.BLACK.alpha(0.5f * anim));
		font.draw(matrixStack, name, rect.getX(), rect.getY(), rock.getThemes().getDarkTheme().getTextFirstColor().alpha(anim));
		
		Stencil.finish();
	}

	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		anim *= activeAnim.get();
		
		FontSize font = bold.get(14);
		float textWidth = font.getWidth(text);
		
		y += yOffset;
		x += xOffset;
		
		switch (direction) {
		case 0:
			font.draw(matrixStack, text, x + width/2f - textWidth / 2 + 0.5f, y - 10.5f, FixColor.BLACK.alpha(0.5f * anim));
			font.draw(matrixStack, text, x + width/2f - textWidth / 2, y - 11, rock.getThemes().getDarkTheme().getTextFirstColor().alpha(anim));
			break;
		case 1:
			font.draw(matrixStack, text, x + width/2f - textWidth / 2 +0.5f, y + height + .5f, FixColor.BLACK.alpha(0.5f * anim));
			font.draw(matrixStack, text, x + width/2f - textWidth / 2, y + height, rock.getThemes().getDarkTheme().getTextFirstColor().alpha(anim));
			break;
		case 2:
			font.draw(matrixStack, text, x - 4 - textWidth, y+0.5f, FixColor.BLACK.alpha(0.5f * anim));
			font.draw(matrixStack, text, x - 4 - textWidth, y, rock.getThemes().getDarkTheme().getTextFirstColor().alpha(anim));
			break;
		case 3:
			font.draw(matrixStack, text, x + 3 + width + 0.5f, y+0.5f, FixColor.BLACK.alpha(0.5f * anim));
			font.draw(matrixStack, text, x + 3 + width, y, rock.getThemes().getDarkTheme().getTextFirstColor().alpha(anim));
			break;
		}
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		rect.setWidth(bold.get(14).getWidth(name));
		rect.setHeight(10);
		return super.clicked(mouseX, mouseY, button);
	}
	
}
