package fun.rockstarity.api.constuctor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.constuctor.blocks.Block;
import fun.rockstarity.api.constuctor.window.AddWindow;
import fun.rockstarity.api.constuctor.window.Window;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.ClickGui;
import fun.rockstarity.client.modules.render.Constructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 */

public class ConstructorScreen extends Screen implements IAccess {
	
	@Getter
	private final ArrayList<Window> windows = new ArrayList<>();
	private boolean drag;
	private int mouseX, mouseY;
	public static Animation opening = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);
	
	public ConstructorScreen() {
		super(new TranslationTextComponent(""));
		opening.getTimer().reset();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Round.draw(matrixStack, new Rect(0,0,sr.getScaledWidth(),sr.getScaledHeight()), 0, FixColor.BLACK.alpha(0.4 * opening.get()));
		
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		
		for (Window window : windows) {
			if (window.getOpening().finished(false)) {
				windows.remove(window);
				break;
			}
			window.render(matrixStack, mouseX, mouseY, partialTicks);
		}
		
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			if (block.getOpening().finished(false)) {
				rock.getScriptConstructor().getBlocks().remove(block);
				break;
			}
			block.render(matrixStack, mouseX, mouseY, partialTicks);
		}
		
		if (drag) {
			for (Block block : rock.getScriptConstructor().getBlocks()) {
				block.setX((float) (mouseX + block.getDragX()));
				block.setY((float) (mouseY + block.getDragY()));
			}
		}
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean canDrag = true;
		
		for (Window window : windows) {
			if (window.clicked(mouseX, mouseY, button)) break;
		}
		
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			if (Hover.isHovered(block, mouseX, mouseY)) {
				canDrag = false;
			}
			if (block.clicked(mouseX, mouseY, button)) break;
		}
		
		if (canDrag) {
			for (Block block : rock.getScriptConstructor().getBlocks()) {
				block.setDragX((float) (block.getX() - mouseX));
				block.setDragY((float) (block.getY() - mouseY));
			}
			drag = true;
		}
		
		if (button == 1) windows.add(new AddWindow((float) mouseX, (float) mouseY, 75, 100));
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			if (block.released(mouseX, mouseY, button)) break;
		}
		drag = false;
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		boolean canDrag = true;
		
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			if (block.dragged(mouseX, mouseY, button, dragX, dragY)) {
				break;
			}
			if (Hover.isHovered(block, mouseX, mouseY)) {
				canDrag = false;
			}
		}
		
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE && opening.finished()) {
			mc.displayGuiScreen(null);
			opening.setForward(false);
			rock.getModules().get(Constructor.class).toggle();
		}
		
		if (keyCode == GLFW.GLFW_KEY_DELETE) {
			for (Block block : rock.getScriptConstructor().getBlocks()) {
				if (Hover.isHovered(block, mouseX, mouseY)) {
					block.getOpening().setForward(false);
					return true;
				}
			}
		}
		
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			block.pressed(keyCode, scanCode, modifiers);
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void tick() {
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			block.tick();
		}
		super.tick();
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			block.charTyped(codePoint, modifiers);
		}
		return super.charTyped(codePoint, modifiers);
	}
	
}
