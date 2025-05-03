package fun.rockstarity.api.render.ui.clickgui.esp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.Reacher;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiRenderer;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiWindow;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPArmor;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPBoxes;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.ESPCooldowns;
import fun.rockstarity.api.render.ui.clickgui.windows.SettingsWindow;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.math.vector.Vector2f;

/**
 * @author ConeTin
 * @since 6 апр. 2024 г.
 */


public class ESPSettings implements IAccess {
	
	FixColor bgColor, settingsBg, separatorColor, moduleColor, black, white, text;
	private final Animation hoverLeft = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);
	private final Animation hoverRight = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);
	
	final InfinityAnimation heightAnim = new InfinityAnimation();
	
	private float rotate;
	final InfinityAnimation rotating = new InfinityAnimation();
	@Getter private boolean dragESP;
	private float dragX;
	
	@Getter private Rect elementsRect, previewRect;
	private float leftHeight = 100, rightHeight = 200;
	
	public void renderPage(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float anim1 = ClickGuiRenderer.opening.get();
		bgColor = rock.getThemes().getFirstColor().alpha(anim1);
		settingsBg = rock.getThemes().getSecondColor().alpha(anim1);
		separatorColor = rock.getThemes().getThirdColor().alpha(anim1);
		moduleColor = rock.getThemes().getTextSecondColor().alpha(anim1);
		text = rock.getThemes().getTextFirstColor().alpha(anim1);
		black = FixColor.BLACK.alpha(anim1);
		white = FixColor.WHITE.alpha(anim1);
		
		int leftY = 0, rightY = 0, column = 1;
		
		ClickGuiWindow window = rock.getClickGui().getWindow();
		
		float anim = window.getWidth() - window.getWidth() * ClickGuiRenderer.openedAnim.get();
		
		this.renderPreview(matrixStack, mouseX, mouseY, partialTicks, previewRect = new Rect(
				window.getRenderer().getLoadedAnimX() + 166 + 160 * column + anim,
				window.getY() + 74 + (column == 0 ? leftY : rightY), 
				152,
				rightHeight
		), anim1);
		
		column = 0;
		
		this.renderElements(matrixStack, mouseX, mouseY, partialTicks, elementsRect = new Rect(
				window.getRenderer().getLoadedAnimX() + 166 + 160 * column + anim,
				window.getY() + 74 + (column == 0 ? leftY : rightY), 
				152,
				leftHeight
		), anim1);
	}
	
	private void renderElements(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, Rect rect, float anim) {
		hoverLeft.setForward(Hover.isHovered(rect, mouseX, mouseY));
		
		Round.draw(matrixStack, rect, 7, separatorColor.darker(hoverLeft.get() * 0.05f));
		Round.draw(matrixStack, rect.x(rect.getX()+1).y(rect.getY()+1).width(rect.getWidth()-2).height(rect.getHeight()-2), 6.5f, bgColor);
		
		float offX = 0, offY = 0;
		
		ArrayList<ESPElement> elements = rock.getEspSettingsHandler().getEspElements();
		
		bold.get(16).draw(matrixStack, "Перетаскиваемые элементы", rect.getX() + 8.5f, rect.getY() + 8.5f, moduleColor);
		
		float[] xOffsets = { 0, 0, 0, 0 };
		float[] yOffsets = { 0, 0, 0, 0 };
		
		for (ESPElement elmt : elements) {
			boolean drag = elmt.isDragging();
			int speed = drag ? 1 : 50;
			
			if (!drag) {
				if (elmt.isActive()) {
					if (elmt instanceof ESPBoxes) {
						elmt.setTargetX(previewRect.getX() + previewRect.getWidth() / 2F - 25F);
						elmt.setTargetY(previewRect.getY() + previewRect.getHeight() / 2F - 60F);
					} else {
						if (elmt instanceof ESPBar) {
							if (elmt.getDirection() < 2) { 
								yOffsets[elmt.getDirection()] += 4;
							} else {
								xOffsets[elmt.getDirection()] += 4;
							}
						} else if (elmt instanceof ESPArmor) {
							switch (elmt.getDirection()) {
							case 0:
								elmt.setYOffset(-yOffsets[elmt.getDirection()]);
								elmt.setXOffset(0);
								break;
							case 1:
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								elmt.setXOffset(0);
								break;
							case 2:
								elmt.setXOffset(-xOffsets[elmt.getDirection()]); 
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								break;
							case 3:
								elmt.setXOffset(xOffsets[elmt.getDirection()]);
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								break;
							}
							
							if (elmt.getDirection() < 2) { 
								yOffsets[elmt.getDirection()] += 14.8f;
							} else {
								xOffsets[elmt.getDirection()] += 14.8f;
							}
						} else if (elmt instanceof ESPCooldowns) {
							switch (elmt.getDirection()) {
							case 0:
								elmt.setYOffset(-yOffsets[elmt.getDirection()]);
								elmt.setXOffset(0);
								break;
							case 1:
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								elmt.setXOffset(0);
								break;
							case 2:
								elmt.setXOffset(-xOffsets[elmt.getDirection()]); 
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								break;
							case 3:
								elmt.setXOffset(xOffsets[elmt.getDirection()]);
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								break;
							}
							
							if (elmt.getDirection() < 2) { 
								yOffsets[elmt.getDirection()] += 14.8f;
							} else {
								xOffsets[elmt.getDirection()] += 14.8f;
							}
						} else {
							switch (elmt.getDirection()) {
							case 0:
								elmt.setYOffset(-yOffsets[elmt.getDirection()]);
								elmt.setXOffset(0);
								break;
							case 1:
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								elmt.setXOffset(0);
								break;
							case 2:
								elmt.setXOffset(-xOffsets[elmt.getDirection()]); 
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								break;
							case 3:
								elmt.setXOffset(xOffsets[elmt.getDirection()]);
								elmt.setYOffset(yOffsets[elmt.getDirection()]);
								break;
							}
							
							yOffsets[elmt.getDirection()] += 8;
						}
					}
				} else {
					elmt.setTargetX(rect.getX() + offX + 8);
					elmt.setTargetY(rect.getY() + offY + 25);
				}
			} else {
				if (elmt.isActive()) {
					List<Vector2f> points = new ArrayList<>();
					points.add(new Vector2f(previewRect.getWidth() / 2, previewRect.getHeight() / 2 - 66));
					points.add(new Vector2f(previewRect.getWidth() / 2, previewRect.getHeight() / 2 + 65));
					//if (elmt instanceof ESPBar) {
					//	points.add(new Vector2f(previewRect.getWidth() / 2 - 25, previewRect.getHeight() / 2));
					//	points.add(new Vector2f(previewRect.getWidth() / 2 + 25, previewRect.getHeight() / 2));
					//} else {
						points.add(new Vector2f(previewRect.getWidth() / 2 - 25, previewRect.getHeight() / 2 - 60));
						points.add(new Vector2f(previewRect.getWidth() / 2 + 25, previewRect.getHeight() / 2 - 60));
					//}
						
					
					if (rock.getEspSettingsHandler().armor.active && !(elmt instanceof ESPBar) && !(elmt instanceof ESPArmor) && !(elmt instanceof ESPCooldowns)) {
						points.get(rock.getEspSettingsHandler().armor.direction).x += 1000;;
					}
					
					List<Vector2f> cache = new ArrayList<>(points);
					
					Vector2f closest = findClosestPoint(cache, elmt.getTargetX() - previewRect.getX(), elmt.getTargetY() - previewRect.getY());
					
					elmt.setDirection(points.indexOf(closest));
					
					if (elmt instanceof ESPArmor) {
						for (ESPElement elmt1 : elements) {
							if (elmt1 instanceof ESPArmor || elmt1 instanceof ESPBar) continue;
							
							if (elmt1.direction == elmt.getDirection()) {
								elmt1.direction++;
								if (elmt1.direction > 3) elmt1.direction=0;
							}
						}
					}
					
					if (elmt instanceof ESPCooldowns) {
						for (ESPElement elmt1 : elements) {
							if (elmt1 instanceof ESPArmor || elmt1 instanceof ESPBar || elmt1 instanceof ESPCooldowns) continue;
							
							if (elmt1.direction == elmt.getDirection()) {
								elmt1.direction++;
								if (elmt1.direction > 3) elmt1.direction=0;
							}
						}
					}
				}
			}
			
			elmt.getXAnim().animate(elmt.getTargetX()-rect.getX(), speed);
			elmt.getYAnim().animate(elmt.getTargetY()-rect.getY(), speed);
			
			elmt.getRect().setX(elmt.getXAnim().get()+rect.getX());
			elmt.getRect().setY(elmt.getYAnim().get()+rect.getY());
			
			if (elmt.isDragging())
				elmt.setActive(Hover.isHovered(previewRect, elmt.getRect().getX(), elmt.getRect().getY()));
			
			elmt.drawPreview(matrixStack, mouseX, mouseY, partialTicks, anim);
			
			if (elmt.isActive()) continue;
			offX += (float) bold.get(14).getWidth(elmt.getName()) + 8;
			if (elements.indexOf(elmt) + 1 != elements.size()
				&& offX+bold.get(14).getWidth(elements.get(elements.indexOf(elmt) + 1).getName()) + 8 > rect.getWidth()-8) {
				offX = 0;
				offY += 13;
			}
		}
		
		Collections.sort(rock.getEspSettingsHandler().getEspElements(), (a, b) -> {
			return Float.compare(a instanceof ESPBar || a instanceof ESPArmor || a instanceof ESPCooldowns ? -50 : a.isDragging() ? mouseY+a.getDragY() : a.getRect().getY(), b instanceof ESPBar || b instanceof ESPArmor || a instanceof ESPCooldowns ? -50 : b.isDragging() ? mouseY+b.getDragY() : b.getRect().getY());
		});

		if (offX == 0) offY -= 13;
		
		leftHeight = heightAnim.animate(44+offY, 25);
		
		if (dragESP) {
			rotate = mouseX + dragX;
		}
	}
	
	private void renderPreview(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, Rect rect, float anim) {
		hoverRight.setForward(Hover.isHovered(rect, mouseX, mouseY));
		this.rotating.animate(this.rotate, 150);
		
		Round.draw(matrixStack, rect, 7, separatorColor.darker(hoverRight.get() * 0.05f));
		Round.draw(matrixStack, rect.x(rect.getX()+1).y(rect.getY()+1).width(rect.getWidth()-2).height(rect.getHeight()-2), 6.5f, bgColor);
		
		bold.get(16).draw(matrixStack, "Предпросмотр", rect.getX() + 8.5f, rect.getY() + 8.5f, moduleColor);

		Reacher.ENTITY_ALPHA = anim;
		Reacher.SILENT = true;
		GL11.glPushMatrix();
		GL11.glTranslated((int) (rect.getX()+rect.getWidth()/2), (int) (rect.getY()+rect.getHeight()/2+60), 100);
		GL11.glRotated(this.rotating.get() - 180 + ClickGuiRenderer.rotationESP.get() * 3 * 180, 0, 1, 0);
		GL11.glTranslated(0, 0, -50);
		
		InventoryScreen.drawEntityOnScreen(0, 0, 60, 0, 0, mc.player);
		
		GL11.glPopMatrix();
		Reacher.ENTITY_ALPHA = 1;
		Reacher.SILENT = false;
	}
	
	public void clicked(double mouseX, double mouseY, int button) {
		boolean cancel = false;
		
		if (cancel) return;
		
		for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
			elmt.getRect().setX(elmt.getXAnim().get()+elementsRect.getX());
			elmt.getRect().setY(elmt.getYAnim().get()+elementsRect.getY());
			if (elmt.clicked(mouseX, mouseY, button)) break;
			
			if (button == 1 && Hover.isHovered(
					!elmt.isActive() ? 
							new Rect(elmt.getTargetX(), elmt.getTargetY(), (float) bold.get(14).getWidth(elmt.getName()) + 5, 10) : 
							new Rect(elmt.getTargetX(), elmt.getTargetY(), elmt.getRect().getWidth(), elmt.getRect().getHeight()), 
							mouseX, mouseY)) {
				if (!elmt.getSettings().isEmpty()) {
					ClickGuiWindow window = rock.getClickGui().getWindow();
					window.getWindows().add(new SettingsWindow(elmt, (float) mouseX, (float) mouseY));
					elmt.setDragging(false);
				}
				break;
			}
			
			if (Hover.isHovered(elmt.rect, mouseX, mouseY)) {
				cancel = true;
			}
		}
		
		if (cancel) return;
		
		if (Hover.isHovered(previewRect, mouseX, mouseY)) {
			dragESP = true;
			dragX = (float) (rotate - mouseX);
		}
	}
	
	public void released(double mouseX, double mouseY, int button) {
		for (ESPElement elmt : rock.getEspSettingsHandler().getEspElements()) {
			elmt.released(mouseX, mouseY, button);
		}
		dragESP = false;
	}
	
	protected Vector2f findClosestPoint(List<Vector2f> points, float x, float y) {
	    points.sort(Comparator.comparing(new Vector2f(x,y)::distance));

	    return points.get(0);
	}
	
}
