package fun.rockstarity.api.constuctor.blocks;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.constuctor.ConstructorScreen;
import fun.rockstarity.api.constuctor.blocks.types.BlockAction;
import fun.rockstarity.api.constuctor.blocks.types.actions.BlockCondition;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ConeTin
 * @since 7 РґРµРє. 2023 Рі.
 */

public class Block extends Rect implements IAccess {
	
	@Getter @Setter
	private float dragX, dragY;
	protected FixColor black, bgColor, actionsColor, clientColor;
	@Getter
	protected final Animation opening = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(1);
	protected final String name;
	protected Rect umbilical;
	protected boolean drag;
	@Setter @Getter
	protected Block child, parent;
	
	public Block(String name, float x, float y, float width, float height) {
		super(x, y, width, height);
		this.name = name;
		opening.setForward(true);
		umbilical = new Rect(x + width - 15, y + height - 15, 10, 10);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		black = FixColor.BLACK.alpha(opening.get());
		bgColor = rock.getThemes().getFirstColor().alpha(opening.get());
		actionsColor = new FixColor(230, 236, 246).alpha(opening.get());
		clientColor = Style.getMain().alpha(opening.get());
		
		Round.draw(matrixStack, this.width(width*opening.get()).height(height*opening.get()), 3, bgColor.alpha(ConstructorScreen.opening.get()));
		
		Stencil.init();
		Round.draw(matrixStack, this.width(width*opening.get()).height(height*opening.get()), 3, bgColor.alpha(ConstructorScreen.opening.get()));
		Stencil.read(1);
		
		Round.draw(matrixStack, this.width(width*opening.get()).height(height*opening.get() - (height-15)), 3, 3, 0, 0, bgColor.darker(0.1f).alpha(ConstructorScreen.opening.get()));
		Render.image("icons/close.png", x + width - 10, y + 5, 6, 6, rock.getThemes().getTextFirstColor().alpha(ConstructorScreen.opening.get()));
		bold.get(14).draw(matrixStack, name, x + 4, y + 3, rock.getThemes().getTextFirstColor().alpha(ConstructorScreen.opening.get()));
		
		Round.draw(matrixStack, new Rect(x + width - 15, y + height - 15, 10, 10), 5, bgColor.darker(0.1f).alpha(ConstructorScreen.opening.get()));

		Stencil.finish();
		
		float tMiddle = 0.5f;

		float startX = x + width - 15;
		float startY = y + height - 15;

		float endX = umbilical.getX();
		float endY = umbilical.getY();

		float control1X = (startX + endX) / 2 + (startY - endY) / 2;
		float control1Y = (startY + endY) / 2 + (endX - startX) / 10;

		float control2X = (startX + endX) / 2 - (startY - endY) / 2;
		float control2Y = (startY + endY) / 2 - (endX - startX) / 10;
		
		drawBezierLine(x + width - 10, y + height - 10, umbilical.getX()+5, umbilical.getY()+5, control1X,control1Y,control2X,control2Y);
		Round.draw(matrixStack, umbilical, 5, clientColor.alpha(ConstructorScreen.opening.get()));
		
		width = Math.max(100, bold.get(14).getWidth(name) + 20);
		
		if (!rock.getScriptConstructor().getBlocks().contains(child)) child = null;
		if (!rock.getScriptConstructor().getBlocks().contains(parent)) parent = null;
		
		if (child != null) {
			umbilical = umbilical.x(child.x + 5).y(child.y + child.getHeight() - 15);
		}
		
		if (drag) {
			umbilical = umbilical.x(mouseX-5).y(mouseY-5);
		}
		
		if (child == null && !drag) {
			umbilical = new Rect(x + width - 15, y + height - 15, 10, 10);
		}
	}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		if (Hover.isHovered(x+width-15, y,15,15, mouseX, mouseY)) {
			opening.setForward(false);
			return true;
		}
		
		if (Hover.isHovered(umbilical, mouseX, mouseY)) {
			drag = true;
		}
		
		return false;
	}
	
	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (Hover.isHovered(this.height(15), mouseX, mouseY)) {
			boolean dragUmbilical = false;
			if (umbilical.getX() == x + width - 15 && umbilical.getY() == y + height - 15) dragUmbilical = true;
			x += dragX;
			y += dragY;
			return true;
		}
		
		return false;
	}
	
	private static void drawBezierLine(float startX, float startY, float endX, float endY, float control1X, float control1Y, float control2X, float control2Y) {
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
	    GL11.glEnable(GL11.GL_BLEND);
	    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Render.color(rock.getThemes().getFirstColor().alpha(ConstructorScreen.opening.get()));

        GL11.glLineWidth(3);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (float t = 0; t <= 1; t += 0.01) {
            float x = (float) (Math.pow(1 - t, 3) * startX + 3 * Math.pow(1 - t, 2) * t * control1X
                    + 3 * (1 - t) * Math.pow(t, 2) * control2X + Math.pow(t, 3) * endX);

            float y = (float) (Math.pow(1 - t, 3) * startY + 3 * Math.pow(1 - t, 2) * t * control1Y
                    + 3 * (1 - t) * Math.pow(t, 2) * control2Y + Math.pow(t, 3) * endY);

            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

	public boolean released(double mouseX, double mouseY, int button) {
		drag = false;
		for (Block block : rock.getScriptConstructor().getBlocks()) {
			if (Hover.isHovered(block, umbilical.getX(), umbilical.getY()) && block != this && (block instanceof BlockAction || block instanceof BlockCondition)) {
				block.setParent(this);
				child = block;
				return false;
			}
		}
		
		if (child != null) {
			child.setParent(null);
			child = null;
		}
		umbilical = new Rect(x + width - 15, y + height - 15, 10, 10);
		
		return false;
	}

	public void tick() {
	}

	public void pressed(int keyCode, int scanCode, int modifiers) {
	}

	public void charTyped(char codePoint, int modifiers) {
	}
	
}
