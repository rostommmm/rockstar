package fun.rockstarity.api.render.ui.draggables;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.windows.SettingsWindow;
import fun.rockstarity.api.render.ui.draggables.grid.Grid;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 19 мар. 2024 г.
 */

public class DraggableHandler implements IAccess {

	@Getter
	private final ArrayList<Draggable> draggables = new ArrayList<>();
	private ArrayList<SettingsWindow> windows = new ArrayList<>();
	private final Animation leftClick = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);
	private final Grid grid = new Grid(); // Сетка
	
	// Для мышки
	private boolean clicked, moveBack;
	private final InfinityAnimation animX = new InfinityAnimation();
	private final InfinityAnimation animY = new InfinityAnimation();
	private final Animation mouseMove = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);
	private final TimerUtility clickTimer = new TimerUtility();
	private final Animation mouseClick = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);
	private final TimerUtility waitTimer = new TimerUtility();
	private Draggable lastDrag;
	private int lastMouseX, lastMouseY;
	
	// Выделение
	private final Animation selAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private boolean selection;
	private float startX, startY;

	public void init() {
		this.grid.updateLineList();
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.grid.render(matrixStack, mouseX, mouseY, partialTicks);
		
		SettingsWindow toRemove = null;
	    this.lastMouseX = mouseX;
	    this.lastMouseY = mouseY;
		for (SettingsWindow window : this.windows) {
			window.render(matrixStack, mouseX, mouseY, partialTicks);
			
			if (window.getOpening().finished(false)) toRemove = window;
		}
		
		if (toRemove != null) {
			this.windows.remove(toRemove);
		}
		
		boolean cursorRendered = false;
		Draggable draggable = null;

		selAnim.setSpeed(200);
		float startX = this.startX;
		float startY = this.startY;		
		float x = mouseX;
		float y = mouseY;
		
		if (x < startX) {
			x = startX;
			startX = mouseX;
		}
		
		if (y < startY) {
			y = startY;
			startY = mouseY;
		}
		
		Rect sel = new Rect(startX, startY, x-startX, y-startY);
		
		if (selection) {
			selAnim.setForward(Math.abs(x - startX) > 30 || Math.abs(y - startY) > 30);
		} else {
			selAnim.setForward(false);
		}
		
		for (Draggable drag : this.draggables) {
			if (drag.isDragging()) {
				boolean canDrag = true;
				
				if (canDrag) {
					drag.setX(mouseX + drag.getDragX());
					drag.setY(mouseY + drag.getDragY());
				}
			}
			if (drag.getX() < 0) drag.setX(0);
			if (drag.getY() < 0) drag.setY(0);
			if (drag.getX() + drag.getWidth() > sr.getScaledWidth()) drag.setX(sr.getScaledWidth() - drag.getWidth());
			if (drag.getY() + drag.getHeight() > sr.getScaledHeight()) drag.setY(sr.getScaledHeight() - drag.getHeight());
			
			if (drag.isDragging()) {
			//	drag.setLastWidth(drag.getWidth());
			//	drag.setLastHeight(drag.getHeight());
			}
			
			if (Hover.isHovered(drag, mouseX, mouseY)) {
				draggable = drag;
			}
			
			if (selection && drag.contains(sel)) {
				drag.setSelected(true);
			}
		}
		
        Draggable drag = draggable;
		if (drag != null || this.lastDrag != null) {
			if (drag == null) drag = this.lastDrag;
			
			boolean dragging = drag.isDragging() && this.waitTimer.passed(500);
			
			if (!drag.isDragging()) {
				this.waitTimer.reset();
			}
			
			this.mouseMove.setForward(Hover.isHovered(drag, mouseX, mouseY) && !cursorRendered && !this.moveBack && this.windows.isEmpty() && !dragging /* && !clicked*/);
			this.mouseClick.setForward(this.mouseMove.finished() && !this.clicked && this.leftClick.finished(false));
			if (!this.mouseMove.finished(false) && !rock.isPanic()) {
	            this.animX.animate(drag.getX() + drag.getWidth() / 2 - mouseX - 10, 150);
				this.animY.animate(drag.getY() + drag.getHeight() / 2 - mouseY, 150);
				
				float size = 20;
				
				GL11.glPushMatrix();
				GL11.glTranslated(drag.getX() - 60 + drag.getWidth() / 1.5f - this.animX.get(), drag.getY() + 40 + drag.getHeight() / 2 - this.animY.get(), 0);
				GL11.glRotated( - 10 - 30 * this.mouseMove.get(), 0, 0, 1);
				Render.image("icons/hud/mouse/center.png", 50, 0, size, size, rock.getThemes().getTextFirstColor().alpha(this.mouseMove.get()));
				Render.image("icons/hud/mouse/tail.png", 50, -size/2+6, size, size, rock.getThemes().getTextFirstColor().alpha(this.mouseMove.get()));
				
				Render.scale(50 + size/2, size/2, 1 - 0.15f * this.leftClick.get());
				Render.image("icons/hud/mouse/left.png", 50, 0, size, size, rock.getThemes().getTextFirstColor().alpha(this.mouseMove.get() - 0.4f * this.leftClick.get()));
				Render.end();
				
				Render.scale(50 + size/2, size/2, 1 - 0.15f * this.mouseClick.get());
				Render.image("icons/hud/mouse/right.png", 50, 0, size, size, rock.getThemes().getTextFirstColor().alpha(this.mouseMove.get() - 0.4f * this.mouseClick.get()));
				Render.end();
				GL11.glPopMatrix();
				
				bold.get(16).draw(matrixStack, "", -10, -10, FixColor.WHITE); // :_(
				
				if (this.mouseClick.finished() && !this.clicked) {
					this.clicked = true;
					this.clickTimer.reset();
				}
				
				if (this.mouseClick.finished(false) && this.clicked && this.clickTimer.passed(500)) {
				//	drag.setMoveBack(true);
					this.clicked = false;
				}
				cursorRendered = true;
			} else {
				this.moveBack = false;
				this.clicked = false;
			}
			
			this.lastDrag = drag;
		}
		
		this.grid.handleDragPositions();
		
		if (!selAnim.finished(false)) {
			Outline.draw(matrixStack, sel, 4, 0.5f, FixColor.WHITE.alpha(0.5f * selAnim.get()));
			Round.draw(matrixStack, sel, 4, FixColor.WHITE.alpha(0.5f * selAnim.get()));
		}
	}
	
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (lastDrag != null && Hover.isHovered(this.lastDrag, lastMouseX, lastMouseY)) {
			switch (keyCode) {
				case GLFW.GLFW_KEY_UP -> this.lastDrag.setY(this.lastDrag.getY() - 1);
	            case GLFW.GLFW_KEY_DOWN -> this.lastDrag.setY(this.lastDrag.getY() + 1);
	            case GLFW.GLFW_KEY_LEFT -> this.lastDrag.setX(this.lastDrag.getX() - 1);
	            case GLFW.GLFW_KEY_RIGHT -> this.lastDrag.setX(this.lastDrag.getX() + 1);
			}
		}
		
		for (SettingsWindow window : this.windows) {
			window.pressed(keyCode, scanCode, modifiers);
		}
	}
	
	public void mouseClicked(double mouseX, double mouseY, int button) {
		boolean noDragAny = true;
		boolean dragging = false;

		for (SettingsWindow window : this.windows) {
			window.clicked(mouseX, mouseY, button);
			if (!window.canClick(mouseX, mouseY)) return;
		}
		
		for (Draggable drag : this.draggables) {
			if (Hover.isHovered(drag.getX(), drag.getY(), drag.getWidth(), drag.getHeight(), mouseX, mouseY) && this.windows.isEmpty() && noDragAny) {
				noDragAny = false;

				if (button == 0) {
					for (Draggable d : this.draggables) {
						d.setDragging(false);
					}
					drag.setDragX((float) (drag.getX() - mouseX));
					drag.setDragY((float) (drag.getY() - mouseY));
					drag.setDragging(true);
					leftClick.setForward(true);
					dragging = true;
				} else if (button == 1) {
					// Супер мега код для получения уи элемента
					UIElement elmt = null;
					
					for (Element element : rock.getModules().get(Interface.class).getElements().getElements()) {
						UIElement ui = (UIElement) element;
						if (ui.getName().equals(drag.getName())) elmt = ui;
					}
					
					float x = (float) mouseX;
					float y = (float) mouseY;
					
					while (y < drag.getY() + drag.getHeight())
						y += 10;
					
					if (elmt != null && !elmt.getSettings().isEmpty())
						this.windows.add(new SettingsWindow(elmt, x - 2, y - 2));
					clicked = true;
				}
			}
		}
		
		if (dragging) {
			for (Draggable drag : this.draggables) {
				if (drag.isSelected()) {
					drag.setDragX((float) (drag.getX() - mouseX));
					drag.setDragY((float) (drag.getY() - mouseY));
					drag.setDragging(true);
				}
			}
		}
		
		if (noDragAny) {
			for (Draggable drag : this.draggables) {
				drag.setSelected(false);
			}
			
			startX = (float) mouseX;
			startY = (float) mouseY;
			selection = true;
		}
	}
	
	public void mouseReleased(double mouseX, double mouseY, int button) {
		selection = false;

		for (Draggable drag : this.draggables) {
			if (drag.isDragging()) {
				drag.setDragging(false);
			}
		}
		
		if (button == 0) {
			leftClick.setForward(false);
		}
		
		for (SettingsWindow window : this.windows) {
			window.released(mouseX, mouseY, button);
		}
	}
	
	public void charTyped(char codePoint, int modifiers) {
		for (SettingsWindow window : this.windows) {
			window.charTyped(codePoint, modifiers);
		}
	}
	
	public void tick() {
		for (SettingsWindow window : this.windows) {
			window.tick();
		}
	}
	
	public void closeChat() {
		this.windows.clear();
		
		for (Draggable drag : rock.getDraggableHandler().getDraggables()) {
			drag.setDragging(false);
			drag.setSelected(false);
		}
		
		selection = false;
	}
	
}
