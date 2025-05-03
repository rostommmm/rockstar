package fun.rockstarity.api.constuctor.blocks.types.actions;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.constuctor.ConstructorScreen;
import fun.rockstarity.api.constuctor.blocks.types.BlockAction;
import fun.rockstarity.api.constuctor.interfaces.IPlayerAction;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import lombok.Getter;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 8 дек. 2023 г.
 */

public class BlockChatMsg extends BlockAction {
	
	@Getter
	private final InputWidget input;

	public BlockChatMsg(String name, float x, float y, float width, float height, IPlayerAction action) {
		super(name, x, y, width, height, action);
		input = new InputWidget(bold.get(14), 0, 0, 90,20, new TranslationTextComponent(""), false);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		input.x = (int) this.x + 2;
		input.y = (int) this.y + 15;
		Round.draw(matrixStack, new Rect(x + 5, y + 18, 90, 10), 2, bgColor.alpha(ConstructorScreen.opening.get()).darker(0.1f));
		input.setMaxStringLength(Integer.MAX_VALUE);
		input.renderButton(matrixStack, mouseX, mouseY, partialTicks, ConstructorScreen.opening.get());
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		input.mouseClicked(mouseX, mouseY, button);
		return super.clicked(mouseX, mouseY, button);
	}
	
	@Override
	public void pressed(int keyCode, int scanCode, int modifiers) {
		input.keyPressed(keyCode, scanCode, modifiers);
		super.pressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void charTyped(char codePoint, int modifiers) {
		input.charTyped(codePoint, modifiers);
		super.charTyped(codePoint, modifiers);
	}

	@Override
	public void tick() {
		input.tick();
		super.tick();
	}

}
