package fun.rockstarity.api.render.ui.clickgui.windows;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.Window;
import fun.rockstarity.api.secure.Debugger;
import net.minecraft.client.Minecraft;

/**
 * @author ConeTin
 * @since 12 дек. 2023 г.
 */

public class PickerWindow extends Window {
	
	private final ColorPicker parent;
	protected final Animation clientColorAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private boolean dragPicker, dragHue, pick;
	private int current;
	private float hue, brightness, saturation;
	protected final Animation pickAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	public PickerWindow(ColorPicker parent, float x, float y, float width, float height) {
		super(x, y, width, height);
		this.parent = parent;
		
		if (parent.getColors().isEmpty()) 
			parent.add(FixColor.RED);
		
		this.updateColor();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		this.clientColorAnim.setForward(this.parent.isClient());
		
		Round.draw(matrixStack, this.height(height*opening.get()), 7, bgColor);
		
		Stencil.init();
		Round.draw(matrixStack, this.width(width*opening.get()), 7, bgColor);
		Stencil.read(1);
		
		float animX = width * this.clientColorAnim.get() - width;
		
		Round.draw(matrixStack, new Rect(x + 5, y + 5, 9, 9), 2, actionsColor.move(Style.getMain(), this.clientColorAnim.get()).alpha(this.opening.get()));
		
		Render.image("icons/checkmark.png", x + 6, y + 6, 7, 7, FixColor.WHITE.alpha(opening.get()*this.clientColorAnim.get()));
		
		bold.get(12).draw(matrixStack, "Брать цвет из стиля", x + 17, y + 5, rock.getThemes().getTextFirstColor().alpha(this.opening.get()));
		
		semibold.get(12).draw(matrixStack, "На данный момент выбран цвет", x + 5 - animX, y + 17, rock.getThemes().getTextFirstColor().alpha(this.opening.get()));
		semibold.get(12).draw(matrixStack, "стиля клиента. Стиль клиента", x + 5 - animX, y + 25, rock.getThemes().getTextFirstColor().alpha(this.opening.get()));
		semibold.get(12).draw(matrixStack, "можно поменять на ", x + 5 - animX, y + 33, rock.getThemes().getTextFirstColor().alpha(this.opening.get()));

		bold.get(12).draw(matrixStack, "этой странице", x + 6f - animX + semibold.get(12).getWidth("можно поменять на "), y + 33.25f, new FixColor(0,0,0,100).alpha(this.opening.get()));
		bold.get(12).draw(matrixStack, "этой странице", x + 5.5f - animX + semibold.get(12).getWidth("можно поменять на "), y + 32.75f, Style.getMain().alpha(this.opening.get()));
		
		if (!this.parent.isClient() || this.opening.finished()) {
			FixColor clor = this.parent.getColors().get(current);
			float[] hsb = Color.RGBtoHSB(clor.getRed(), clor.getGreen(), clor.getBlue(), null);
			float ht = 108;
			
			Round.draw(matrixStack, new Rect(x + 5 - animX - width, y + 33, 108, 108), 5, 
					FixColor.WHITE.alpha(opening.get()*(1-this.clientColorAnim.get())),
					new FixColor(FixColor.getHSBColor(hue, 1, 1)).alpha(opening.get()*(1-this.clientColorAnim.get())),
					FixColor.BLACK.alpha(opening.get()*(1-this.clientColorAnim.get())),
					FixColor.BLACK.alpha(opening.get()*(1-this.clientColorAnim.get())));
			
			float xOff = 0, i = 0;
			for (FixColor color : this.parent.getColors()) {
				color.getSelectAnim().setForward(current == i++);
				
				float off = 1 + color.getSelectAnim().get();
				Round.draw(matrixStack, new Rect(x + 5 - animX - width + xOff, y + 18, 10, 10), 5, actionsColor);
				Round.draw(matrixStack, new Rect(x + 5 - animX - width + xOff + off, y + 18 + off, 10 - off * 2, 10 - off * 2), 5 - off, color);
				
				xOff += 12;
			}
			
			if (this.parent.getColors().size() < 9) {
				Round.draw(matrixStack, new Rect(x + 5 - animX - width + xOff, y + 18, 10, 10), 5, actionsColor);
				Round.draw(matrixStack, new Rect(x + 5 - animX - width + xOff + 4.5f, y +20, 1, 6), 0, rock.getThemes().getTextFirstColor().alpha(opening.get()*(1-this.clientColorAnim.get())));
				Round.draw(matrixStack, new Rect(x + 5 - animX - width + xOff + 2, y + 18 + 4.5f, 6, 1), 0, rock.getThemes().getTextFirstColor().alpha(opening.get()*(1-this.clientColorAnim.get())));
			}
			
			Render.image("icons/pick.png", x + width - 5 - 8, y + 5, 8, 8, rock.getThemes().getTextFirstColor().alpha(opening.get()*(1-this.clientColorAnim.get())));
			
			float size = 10;
			for (int i1 = 0; i1 < size; i1++) {
				FixColor color = new FixColor(FixColor.getHSBColor(1/size * ((float)i1), 1, 1));
				FixColor next = new FixColor(FixColor.getHSBColor(1/size * ((float)i1+1), 1, 1));
				
				Round.draw(matrixStack, new Rect(x - animX - 18, y + 33 + i1 * ht/size, 13, ht/size + (i1 != size - 1 ? 0.3f : 0)), 
						i1 == 0 ? 5 : 0.1f, i1 == 0 ? 5 : 0.1f, i1 == size - 1 ? 5 : 0.1f, i1 == size - 1 ? 5 : 0.1f,
						color.alpha(opening.get()*(1-this.clientColorAnim.get())),
						color.alpha(opening.get()*(1-this.clientColorAnim.get())),
						next.alpha(opening.get()*(1-this.clientColorAnim.get())),
						next.alpha(opening.get()*(1-this.clientColorAnim.get())));
			}
			
			{
				Round.draw(matrixStack, new Rect(x - animX - 14.6f, y + 35 + ht * hue, 6, 6), 3, FixColor.WHITE.alpha(opening.get()*(1-this.clientColorAnim.get())));
				
				Outline.draw(matrixStack, new Rect(x + 5 - animX - width + 108 * saturation + .5f, y + 33 + 108 * brightness + .5f, 6, 6), 3, 0.5f, FixColor.WHITE.alpha(opening.get()*(1-this.clientColorAnim.get())));
				
				if (this.dragPicker) {
					saturation = Hover.getSliderValue(0, 0.95f, x + 5 - animX - width, ht*0.95f, mouseX);
					brightness = Hover.getSliderValue(0, 0.95f, y + 33, ht*0.95f, mouseY);
					this.parent.getColors().set(current, new FixColor(FixColor.getHSBColor(hue, saturation, 1-brightness)));
				}
				
				if (this.dragHue) {
					hue = Hover.getSliderValue(0, 0.92f, y + 36, ht*0.92f, mouseY);
					this.parent.getColors().set(current, new FixColor(FixColor.getHSBColor(hue, hsb[1], hsb[2])));
				}
			}
		}
		
		this.setHeight(46 + 100 * (1-this.clientColorAnim.get()));
		
		Stencil.finish();
		
		if (pick || (!pickAnim.finished(false) && opening.finished())) {
        	FixColor color = FixColor.WHITE;
    		try {
    			double mouseX1 = 0.0, mouseY1 = 0.0;
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    DoubleBuffer xBuffer = stack.mallocDouble(1);
                    DoubleBuffer yBuffer = stack.mallocDouble(1);
                    GLFW.glfwGetCursorPos(Minecraft.getInstance().getMainWindow().getHandle(), xBuffer, yBuffer);
                    mouseX1 = xBuffer.get(0);
                    mouseY1 = Minecraft.getInstance().getMainWindow().getHeight() - yBuffer.get(0);
                }

                // Чтение цвета пикселя
                ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4); // RGBA
                GL11.glReadPixels((int) mouseX1, (int) mouseY1, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
                
                int red = pixelBuffer.get() & 0xFF;
                int green = pixelBuffer.get() & 0xFF;
                int blue = pixelBuffer.get() & 0xFF;
                int alpha = pixelBuffer.get() & 0xFF;
                color = new FixColor(red, green, blue);
                Round.draw(matrixStack, new Rect(mouseX-9, mouseY-27, 20,20), 10f, FixColor.WHITE.alpha(pickAnim.get()));
                Round.draw(matrixStack, new Rect(mouseX-7, mouseY-25, 16,16), 8f, color.alpha(pickAnim.get()));
    		} catch (Exception e) {
    			Debugger.print(e);
    		}
        }
        pickAnim.setForward(pick);
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		float animX = width * this.clientColorAnim.get() - width;
		
		if (pick) {
			if (button == 0) {
				try {
					double mouseX1 = 0.0, mouseY1 = 0.0;
		            try (MemoryStack stack = MemoryStack.stackPush()) {
		                DoubleBuffer xBuffer = stack.mallocDouble(1);
		                DoubleBuffer yBuffer = stack.mallocDouble(1);
		                GLFW.glfwGetCursorPos(Minecraft.getInstance().getMainWindow().getHandle(), xBuffer, yBuffer);
		                mouseX1 = xBuffer.get(0);
		                mouseY1 = Minecraft.getInstance().getMainWindow().getHeight() - yBuffer.get(0);
		            }

		            ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4); // RGBA
		            GL11.glReadPixels((int) mouseX1, (int) mouseY1, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
		            
		            int red = pixelBuffer.get() & 0xFF;
		            int green = pixelBuffer.get() & 0xFF;
		            int blue = pixelBuffer.get() & 0xFF;
		            int alpha = pixelBuffer.get() & 0xFF;
		            
		            FixColor color = new FixColor(red,green,blue);
		            this.parent.getColors().set(current, color);
		            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		            hue = hsb[0];
		            brightness = hsb[1];
		            saturation = hsb[2];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			pick = false;
			return false;
		} else {
			if (!parent.isClient() && Hover.isHovered(x + width - 5 - 8, y + 5, 8, 8, mouseX, mouseY)) {
				pick = true;
			}
			
			if (Hover.isHovered(x + 5, y + 5, 80, 9, mouseX, mouseY)) {
				this.parent.set(!this.parent.isClient());
			}
			
			if (Hover.isHovered(x + 5.5f - animX + semibold.get(12).getWidth("можно поменять на "), y + 32.75f, bold.get(12).getWidth("этой странице"), 12, mouseX, mouseY)) {
				rock.getClickGui().getWindow().changeCategory(Category.THEMES);
				this.opening.setForward(false);
			}
			
			if (Hover.isHovered(this, mouseX, mouseY)) {
				if (Hover.isHovered(x + 5 - animX - width, y + 28, 113, 108, mouseX, mouseY)) {
					this.dragPicker = true;
				}
				
				if (Hover.isHovered(x - animX - 18, y + 34, 13, 108, mouseX, mouseY)) {
					this.dragHue = true;
				}
				
				int i = 0;
				float xOff = 0;
				for (FixColor color : new ArrayList<>(this.parent.getColors())) {
					float off = 1 + color.getSelectAnim().get();
					if (Hover.isHovered(x + 5 - animX - width + xOff, y + 18, 10, 10, mouseX, mouseY)) {
						if (button == 0) {
							this.current = (int) i;
							this.updateColor();
						} else {
							if (this.parent.getColors().size() > 1) {
								this.parent.getColors().remove(color);
								this.current = Math.max(0, (int) i - 1);
							}
						}
					}
					
					i++;
					xOff += 12;
				}
				
				if (Hover.isHovered(x + 5 - animX - width + xOff, y + 18, 10, 10, mouseX, mouseY) && this.parent.getColors().size() < 9) {
					FixColor[] rainbow = {
							FixColor.RED,
							FixColor.ORANGE,
							FixColor.YELLOW,
							FixColor.GREEN,
							new FixColor(66,170,255),
							FixColor.BLUE,
							new FixColor(139,0,255)
					};
					for (FixColor color : rainbow) {
						if (!this.parent.getColors().contains(color)) {
							this.parent.add(color);
							this.current = parent.getColors().size() - 1;
							break;
						}
					}
				}
			}
		}
		
		return super.clicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean released(double mouseX, double mouseY, int button) {
		this.dragPicker = false;
		this.dragHue = false;
		return super.released(mouseX, mouseY, button);
	}
	
	@Override
	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.dragPicker || this.dragHue) return super.dragged(mouseX, mouseY, button, dragX, dragY);
		x += dragX;
		y += dragY;
		return super.dragged(mouseX, mouseY, button, dragX, dragY);
	}
	
	@Override
	public boolean pressed(int keyCode, int scanCode, int modifiers) {
		return super.pressed(keyCode, scanCode, modifiers);
	}
	
	private void updateColor() {
		FixColor clor = this.parent.getColors().get(current);
		float[] hsb = Color.RGBtoHSB(clor.getRed(), clor.getGreen(), clor.getBlue(), null);
		hue = hsb[0];
		saturation = hsb[1];
		brightness = 1-hsb[2];
	}
	
}
