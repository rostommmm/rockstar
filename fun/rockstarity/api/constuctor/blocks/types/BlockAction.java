package fun.rockstarity.api.constuctor.blocks.types;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.constuctor.ConstructorScreen;
import fun.rockstarity.api.constuctor.blocks.Block;
import fun.rockstarity.api.constuctor.interfaces.IPlayerAction;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 8 дек. 2023 г.
 */

public class BlockAction extends Block {
	
	@Getter
	private final IPlayerAction action;
	
	public BlockAction(String name, float x, float y, float width, float height, IPlayerAction action) {
		super(name, x, y, width, height);
		this.action = action;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		Round.draw(matrixStack, new Rect(x + 5, y + height - 15, 10, 10), 5, bgColor.alpha(ConstructorScreen.opening.get()).darker(0.1f));
		if (parent != null)
		Round.draw(matrixStack, new Rect(x + 5, y + height - 15, 10, 10), 5, clientColor.alpha(ConstructorScreen.opening.get()));
	}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		super.clicked(mouseX, mouseY, button);
		
		return false;
	}
}
