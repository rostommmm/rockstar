
package fun.rockstarity.api.render.ui.clickgui;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Clickable;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.ItemSelect;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.windows.BindListWindow;
import fun.rockstarity.api.render.ui.clickgui.windows.ItemSelectWindow;
import fun.rockstarity.api.render.ui.clickgui.windows.PickerWindow;
import fun.rockstarity.api.render.ui.clickgui.windows.SettingsWindow;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.NumberInputWidget;
import fun.rockstarity.api.secure.Debugger;
import fun.rockstarity.client.modules.player.InvUtils;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 5 РґРµРє. 2023 Рі.
 */


public class SettingRect extends Rect implements IAccess {

	@Getter
	private final Setting parent;
	FixColor bgColor, settingsBg, separatorColor, moduleColor, black, white, text;
	@Getter
	private final Animation hover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200),
			slowHover = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500),
			hide = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(200);
	private NumberInputWidget sliderInput;
	private Slider dragSlider;
	private Position dragPosition;
	
	public SettingRect(Setting parent) {
		this.parent = parent;
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim) {
		this.render(matrixStack, mouseX, mouseY, partialTicks, anim, false, true);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim, boolean bind) {
		this.render(matrixStack, mouseX, mouseY, partialTicks, anim, false, false);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim, boolean bind, boolean bg) {
		bgColor = rock.getThemes().getFirstColor().alpha(anim);
		settingsBg = rock.getThemes().getSecondColor().alpha(anim);
		separatorColor = rock.getThemes().getThirdColor().alpha(anim);
		moduleColor = rock.getThemes().getTextSecondColor().alpha(anim);
		text = rock.getThemes().getTextFirstColor().alpha(anim);
		black = FixColor.BLACK.alpha(anim);
		white = FixColor.WHITE.alpha(anim);
		
		if (!bind) {
			if (bg) {
				Round.draw(matrixStack, this, 7, separatorColor.move(bgColor, hover.get() * 0.3f));
				Round.draw(matrixStack, this.size(1), 6.5f, bgColor);
			}
			
			StringBuilder displayName = new StringBuilder();
			float xOffset = 0;
			for (char c : parent.getName().toCharArray()) {
				displayName.append(c);
				xOffset += bold.get(16).getWidth(""+c);
				if (xOffset > width - height / 2 - 40 && parent instanceof CheckBox) {
					displayName.append("..");
					break;
				}
			}
			
			bold.get(16).draw(matrixStack, displayName.toString(), x + 8.5f, y + 8.5f, moduleColor);
		}
		
		hover.setForward(Hover.isHovered(this, mouseX, mouseY));
		slowHover.setForward(Hover.isHovered(this, mouseX, mouseY));
		
		if (Hover.isHovered(this, mouseX, mouseY)) {
			rock.getClickGui().getWindow().setCanDrag(false);
		}
		
		if (parent instanceof Input input) {
			Round.draw(matrixStack, new Rect(x + 8.5f, y + 23, width - 17, 12), 2, separatorColor.alpha(anim));
			
			input.getInput().x = (int) (x + 8);
			input.getInput().y = (int) (y + 25);
			input.getInput().setWidth((int) (width - 17));
			input.getInput().renderButton(matrixStack, mouseX, mouseY, partialTicks, anim);
			
			bold.get(12).draw(matrixStack, "Р’РІРµРґРёС‚Рµ С‚РµРєСЃС‚..", x + 13, y + 25, text.alpha(input.getInput().getText().isEmpty() ? 1-hover.get()*0.5f : 0));
			input.set(input.getInput().getText());
			
			input.setHeight(44, bind);
			rock.getModules().get(InvUtils.class).setStop(input.getInput().isFocused());
			
			if (!Hover.isHovered(x, y, width, height, mouseX, mouseY)) {
				input.getInput().setFocused2(false);
			}
		}
		
		if (parent instanceof CheckBox checkbox) {
			checkbox.getEnableAnim(bind).setForward(bind ? checkbox.bind() : checkbox.get());
			checkbox.getSettingsAnim().setForward(checkbox.hasSettings() && (checkbox.get() || !checkbox.ifEnabled()));

			Round.draw(matrixStack, new Rect(x + width - height / 2 - 5, y + height / 2 - 5, 10, 10), 2, separatorColor.move(Style.getMain().alpha(anim), checkbox.getEnableAnim(bind).get()));
			
			float checkAnim = checkbox.getEnableAnim(bind).get();
			Render.image("icons/checkmark.png", x + width - height / 2 - 3.5f, y + height / 2 - 3.5f + 3 - checkAnim * 3, 7, 7, white.alpha(checkAnim));

			Render.image("icons/close.png", x + width - height / 2 - 3, y + height / 2 - 3 - checkAnim * 3, 6, 6, rock.getThemes().getTextSecondColor().alpha(anim - checkAnim));
			
			Round.draw(matrixStack, new Rect(x + width - height / 2 - 5, y + height / 2 - 5, 10, 10), 2, separatorColor.alpha(0));
			
			if (checkbox.canSettings() && !bind)
				checkbox.setSettingRect(Render.image("icons/menu/setting.png", x + width - height / 2 - 17, y + height / 2 - 4.5f, 9, 9, rock.getThemes().getTextSecondColor().alpha(anim * checkbox.getSettingsAnim().get())));
		}
		
		if (parent instanceof Clickable clickable) {
			String display = clickable.getButtonText();
			parent.setHeight(47, bind);
			
			Rect rect = new Rect(x + 8, y + 23, width - 16, 32 - 16);
			
			clickable.getHoverButtonAnim().setForward(Hover.isHovered(rect, mouseX, mouseY));
			
			Round.draw(matrixStack, rect, 2, separatorColor.darker(clickable.getHoverButtonAnim().get() * 0.03f), separatorColor.darker(0.03f + clickable.getHoverButtonAnim().get() * 0.03f), separatorColor.darker(clickable.getHoverButtonAnim().get() * 0.03f), separatorColor.darker(0.03f + clickable.getHoverButtonAnim().get() * 0.03f));
			bold.get(14).draw(matrixStack, display, x + width / 2 - bold.get(14).getWidth(display) / 2, y + 26, moduleColor);
		}
		
		if (parent instanceof Binding binding) {
			String display = (binding.getBinds().isEmpty() ? "..." : Binds.getName(binding.getBinds().get(0).getKey(), binding.getBinds().get(0).getScancode()));
			float size = bold.get(12).getWidth(display) + 6;
			
			Round.draw(matrixStack, new Rect(x + width - 28 / 2 + 5 - size, y + 28 / 2 - 5, size, 10), 2, separatorColor);
			
			bold.get(12).draw(matrixStack, display, x + width - 9.5f - bold.get(12).getWidth(display) - 2.5f, y + 10, moduleColor);
		}
		
		if (parent instanceof ColorPicker colorPicker) {
			float off = 1;
			Round.draw(matrixStack, new Rect(x + width - height / 2 - 5, y + height / 2 - 5, 10, 10), 5, separatorColor);
			Round.draw(matrixStack, new Rect(x + width - height / 2 - 5 + off, y + height / 2 - 5 + off, 10 - off * 2, 10 - off * 2), 5 - off, colorPicker.get().alpha(anim));
		}
		
		if (parent instanceof Slider slider) {
			parent.setHeight(39, bind);
			
			slider.getAnim(bind).animate(bind ? slider.bind() : slider.get(), 100);
		    float displayValue = slider.getAnim(bind).get() * (slider.isPercentMode() ? 100 : 1);
		    String display = !(this.sliderInput == null || this.sliderInput.isFocused()) && slider.getTextValues().containsKey(Math.round(displayValue * 10F) / 10F) ? slider.getTextValues().get(Math.round(displayValue * 10F) / 10F) : TextUtility.formatNumber(displayValue);
			float size = bold.get(12).getWidth(display) + 5;
			
			if (dragSlider == slider) {
				if (bind)
					slider.bind(Hover.getSliderValue(slider.min(), slider.max(), x + 8.5f, (width - 17), mouseX));
				else
					slider.set(Hover.getSliderValue(slider.min(), slider.max(), x + 8.5f, (width - 17), mouseX));
			}
			Round.draw(matrixStack, new Rect(x + width - 28 / 2 + 5 - size, y + 28 / 2 - 5, size, 10), 2, separatorColor);
			
			if (this.sliderInput == null) {
				this.sliderInput = new NumberInputWidget(bold.get(12), (int) (x + width - 28 / 2 + 5 - size), (int) (y + 28 / 2 - 5), (int) size*2, 10, new TranslationTextComponent(""), false);
			}
			
			this.sliderInput.x = (int) (x + width - 28 / 2 + 2 - size);
			this.sliderInput.y = (int) (y + 28 / 2 - 3);
			
			if (this.sliderInput.isFocused()) {
				sliderInput.renderButton(matrixStack, mouseX, mouseY, partialTicks);
				if (!this.sliderInput.getText().isEmpty()) {
					try {
						if (bind)
							slider.bind(Float.parseFloat(this.sliderInput.getText()));
						else
							slider.set(Float.parseFloat(this.sliderInput.getText()));
					} catch (Exception e) {
						this.sliderInput.setText("0");
					}
				}
					
			} else {
		        bold.get(12).draw(matrixStack, display + (slider.isPercentMode() ? "%" : ""), x + width - 9.5f - bold.get(12).getWidth(display) - 2.5f, y + 10, moduleColor);
				this.sliderInput.setText(TextUtility.formatNumber(slider.getAnim(bind).get()));
			}
			slider.getMaxAnim().animate(slider.max(), 100);
			
			Round.draw(matrixStack, new Rect(x + 8.5f, y + 25, width - 17, 4), 2, separatorColor);
			Round.draw(matrixStack, new Rect(x + 8.5f, y + 25, (width - 17) * Hover.getPercent(slider.getAnim(bind).get(), slider.min(), slider.getMaxAnim().get()), 4), 2, Style.getMain().alpha(anim), Style.getSecond().alpha(anim), Style.getMain().alpha(anim), Style.getSecond().alpha(anim));
			float circle = 5 + hover.get();
			Round.draw(matrixStack, new Rect(x + 8.5f + (width - 17) * Hover.getPercent(slider.getAnim(bind).get(), slider.min(), slider.getMaxAnim().get()) - circle / 2, y + 27 - circle / 2, circle, circle), circle / 2, white.darker(0.3f));
		}
		
		if (parent instanceof Mode mode) {
			float offX = 0, offY = 0;
			
			for (Mode.Element elmt : mode.getElements()) {
				Rect rect = new Rect(x + offX + 8.5f, y + 25 + offY, (float) bold.get(14).getWidth(elmt.getName()) + 5 + (elmt.canSettings() ? 8 * elmt.getSettingsAnim().get() : 0), 10);
				
				elmt.getAnim(bind).setForward(bind ? mode.isBind(elmt) : mode.is(elmt));
				elmt.getHover(bind).setForward(Hover.isHovered(rect, mouseX, mouseY));
				elmt.getSettingsAnim().setForward(elmt.hasSettings() && (elmt.get() || !elmt.ifEnabled()));

				Round.draw(matrixStack, rect, 2, separatorColor.move(Style.getPoint((int) offX).alpha(anim), elmt.getAnim(bind).get()));
				
				bold.get(14).draw(matrixStack, elmt.getName(), x + offX + 10.5f, y + 25 + offY, moduleColor.darker(0.1f).move(white, elmt.getAnim(bind).get()));
				
				if (elmt.canSettings() && !bind)
					elmt.setSettingRect(Render.image("icons/menu/setting.png", x + offX + 7.5f + bold.get(14).getWidth(elmt.getName()) + 5 * elmt.getSettingsAnim().get(), y + 26 + offY, 8, 8, rock.getThemes().getTextFirstColor().alpha(anim * elmt.getSettingsAnim().get())));
			
				
				offX += (rect.getWidth() + 3);
				if (mode.getElements().indexOf(elmt) + 1 != mode.getElements().size() &&
						offX+bold.get(14).getWidth(mode.getElements().get(mode.getElements().indexOf(elmt) + 1).getName()) + 8 > width-8) {
					offX = 0;
					offY += 13;
				}
			}
			
			mode.setHeight(44+offY, bind);
		}
		
		if (parent instanceof Select select) {
			float offX = 0, offY = 0;
			int count = 0;
			ArrayList<Select.Element> updated = new ArrayList<>(select.getElements());
			
			for (Select.Element elmt : select.getElements()) {
				elmt.getHideAnim().setForward(elmt.isHide());
				elmt.getSettingsAnim().setForward(elmt.hasSettings() && (elmt.get() || !elmt.ifEnabled()));

				float prevX = offX, prevY = offY;
				
				if (elmt.isDragging()) {
					offX = MathHelper.clamp(mouseX+elmt.getDragX()+x, -8.5f, this.width-8.5f);
					offY = MathHelper.clamp(mouseY+elmt.getDragY()+y, -25, this.height-25);
				}
				
				Rect rect = new Rect(x + offX + 8.5f, y + 25 + offY, bold.get(14).getWidth(elmt.getName()) + 5 + (elmt.canSettings() ? 8 * elmt.getSettingsAnim().get() : 0), 10);
				elmt.getAnim().setForward(elmt.get());
				elmt.getHover().setForward(Hover.isHovered(rect, mouseX, mouseY));
				Round.draw(matrixStack, rect, 2, separatorColor.alpha(anim * (1-elmt.getHideAnim().get())));
				
				Render.scale(rect.getX() + rect.getWidth()/2F, rect.getY() + rect.getHeight()/2F, 0.5f + elmt.getAnim().get() * 0.5f);
				Round.draw(matrixStack, rect, 2, Style.getPoint((int) offX).alpha(anim * elmt.getAnim().get() * (1-elmt.getHideAnim().get())));
				Render.end();
				
				bold.get(14).draw(matrixStack, elmt.getName(), x + offX + 10.5f - ((bold.get(14).getWidth(elmt.getName()) + 8)/2 * (elmt.getHideAnim().get())), y + 25 + offY, moduleColor.darker(0.1f).move(white, elmt.getAnim().get()).alpha(1-elmt.getHideAnim().get()));
				elmt.setRect(rect);
				
				if (elmt.canSettings() && !bind)
					elmt.setSettingRect(Render.image("icons/menu/setting.png", x + offX + 7.5f + bold.get(14).getWidth(elmt.getName()) + 5 * elmt.getSettingsAnim().get(), y + 26 + offY, 8, 8, rock.getThemes().getTextFirstColor().alpha(anim * elmt.getSettingsAnim().get())));
				
				offX = prevX;
				offY = prevY;
				
				offX += (rect.getWidth() + 3) * (1-elmt.getHideAnim().get());
				if (select.getElements().indexOf(elmt) + 1 != select.getElements().size() &&
						offX+bold.get(14).getWidth(select.getElements().get(select.getElements().indexOf(elmt) + 1).getName()) + 8 > width-8) {
					offX = 0;
					offY += 13;
				}
				if (elmt.get()) count++;
			}
			String displayCount = count + " РёР· " + Math.min(select.getElements().stream().filter(elmt -> !elmt.isHide()).toList().size(), select.getMax());
			bold.get(14).draw(matrixStack, displayCount, x + this.width - bold.get(14).getWidth(displayCount) - 8.5f, y + 8.5f, moduleColor);
			
			select.getElements().clear();
			select.getElements().addAll(updated);
			
			select.setHeight(44+offY, bind);
		}
		
		if (parent instanceof Position position) {
			Round.draw(matrixStack, new Rect(x + 8.5f, y + 25, width - 17, width - 17), 4, separatorColor);
			
			position.setHeight(width - 17 + 34, bind);
			
			position.getXAnim(bind).animate(bind ? position.getBindX() : position.getX(), 70);
			position.getYAnim(bind).animate(bind ? position.getBindY() : position.getY(), 70);
			
			if (dragPosition == position) {
				if (bind) {
					position.bindX(Hover.getSliderValue(position.getMinX(), position.getMaxX(), x + 8.5f, (width - 17), mouseX));
					position.bindY(Hover.getSliderValue(position.getMinY(), position.getMaxY(), y + 25, (width - 17), mouseY));
				} else {
					position.x(Hover.getSliderValue(position.getMinX(), position.getMaxX(), x + 8.5f, (width - 17), mouseX));
					position.y(Hover.getSliderValue(position.getMinY(), position.getMaxY(), y + 25, (width - 17), mouseY));
				}
			}
			
			for (float x = 0.1f; x <= 1; x+=0.1f) {
				Round.draw(matrixStack, new Rect(this.x + 10.5f + (width - 21)*x, y + 27, 1, width - 21), 0, separatorColor.darker(0.1f));
			}
			
			for (float y = 0.1f; y <= 1; y+=0.1f) {
				Round.draw(matrixStack, new Rect(x + 10.5f, this.y + 27 + (width - 21) * y, width - 21, 1), 0, separatorColor.darker(0.1f));
			}
			
			Round.draw(matrixStack, new Rect(x + 10.5f + (width - 21) * Hover.getPercent(position.getXAnim(bind).get(), position.getMinX(), position.getMaxX()), y + 27, 1, width - 21), 0, moduleColor);
			Round.draw(matrixStack, new Rect(x + 10.5f, y + 27 + (width - 21) * Hover.getPercent(position.getYAnim(bind).get(), position.getMinY(), position.getMaxY()), width - 21, 1), 0, moduleColor);
			
			String display = String.format("%.1f", position.getXAnim(bind).get()).replace(",", ".") + " : " + String.format("%.1f", position.getYAnim(bind).get()).replace(",", ".");
			float size = bold.get(12).getWidth(display) + 5;
			
			Round.draw(matrixStack, new Rect(x + width - 28 / 2 + 5 - size, y + 28 / 2 - 5, size, 10), 2, separatorColor);
			
			bold.get(12).draw(matrixStack, display, x + width - 9.5f - bold.get(12).getWidth(display) - 2.5f, y + 10, moduleColor);
		}
	}
	
	public boolean clicked(double mouseX, double mouseY, int button, boolean release) {
		return this.clicked(mouseX, mouseY, button, release, false);
	}
	
	public boolean clicked(double mouseX, double mouseY, int button, boolean release, boolean bind) {
		try {
			if (parent.isHide()) return false;
			if (release) {
				rock.getClickGui().getWindow().setCanDrag(true);
				dragPosition = null;
				dragSlider = null;
			}
			else {
				if (sliderInput != null) sliderInput.mouseClicked(mouseX, mouseY, button);
				// Р¦РёРєР» СЃ mouseClicked РґР»СЏ РєР°Р¶РґРѕРіРѕ inputWidget
                if (this.getParent() instanceof Input input) {
                    input.getInput().mouseClicked(mouseX, mouseY, button);
                }
				if ((button == 2 || button == 1 || parent instanceof Binding) && Hover.isHovered(this, mouseX, mouseY) && !(parent instanceof Select) && !(parent instanceof ColorPicker)) rock.getClickGui().getWindow().getWindows().add(new BindListWindow(this.getParent(), (float)mouseX, (float)mouseY, 60, 100));
				if (parent instanceof ItemSelect select) {
					if (Hover.isHovered(x, y, width, height, (float) mouseX, (float) mouseY)) {
						float width = 150;
						float x = (float) (mouseX + width > sr.getScaledWidth() ? mouseX - width : mouseX);
						
						rock.getClickGui().getWindow().getWindows().add(new ItemSelectWindow(select, x, (float) mouseY, width, 150));
					}
				}
				if (Hover.isHovered(this, mouseX, mouseY) && button == 0 && parent instanceof ColorPicker picker) rock.getClickGui().getWindow().getWindows().add(new PickerWindow(picker, (float)mouseX, (float)mouseY, 135, 100));
			}
			if (button == 0) {
				if (Hover.isHovered(this, mouseX, mouseY)) {
					if (release) {
						if (parent instanceof Select select) {
							ArrayList<Select.Element> updated = new ArrayList<>(select.getElements());				
							
							for (Select.Element elmt : select.getElements()) {
								if (elmt.isDragging()) {
									elmt.setDragging(false);
						            for (Select.Element other : select.getElements()) {
						            	if (elmt.isHide()) continue;
						                if (other == elmt) continue;

						                double elmtX = elmt.getRect().getX();
						                double elmtY = elmt.getRect().getY();
						                double elmtHeight = elmt.getRect().getHeight();
						                double otherX = other.getRect().getX();
						                double otherY = other.getRect().getY();
						                double otherHeight = other.getRect().getHeight();
						                
						                if (Hover.isHovered(other.getRect(), mouseX, mouseY)) {
						                    int currentIndex = select.getElements().indexOf(elmt),
						                    diff = currentIndex + select.getElements().indexOf(other) - currentIndex;
						                    
						                    if (diff < select.getElements().size() && diff >= 0) {
						                        updated.remove(elmt);
						                        updated.add(diff, elmt);
						                    }
						                }
						            }
						        }
							}
							
							float offX = 0, offY = 0;
							
							for (Select.Element elmt : select.getElements()) {
								if (elmt.isHide()) continue;
								if (elmt.hasSettings() && elmt.canSettings() && Hover.isHovered(elmt.getSettingRect(), mouseX, mouseY)) {
									rock.getClickGui().getWindow().getWindows().add(new SettingsWindow(elmt, (float) mouseX, (float) mouseY));
									return true;
								}
								
								Rect rect = new Rect(x + offX + 8.5f, y + 25 + offY, bold.get(14).getWidth(elmt.getName()) + 5 + (elmt.canSettings() ? 8 * elmt.getSettingsAnim().get() : 0), 10);
								
								if (Hover.isHovered(rect, mouseX, mouseY) && select.getElements().equals(updated)) {
									elmt.set(!elmt.get());
								}

								offX += (rect.getWidth() + 3) * (1-elmt.getHideAnim().get());
								if (select.getElements().indexOf(elmt) + 1 != select.getElements().size() &&
										offX+bold.get(14).getWidth(select.getElements().get(select.getElements().indexOf(elmt) + 1).getName()) + 8 > width-8) {
									offX = 0;
									offY += 13;
								}
							}
							
							select.getElements().clear();
							select.getElements().addAll(updated);
						}
						return true;
					} else {
						rock.getClickGui().getWindow().setCanDrag(false);
						
						if (parent.hasSettings() && parent.canSettings() && Hover.isHovered(parent.getSettingRect(), mouseX, mouseY)) {
							rock.getClickGui().getWindow().getWindows().add(new SettingsWindow(parent, (float) mouseX, (float) mouseY));
							return true;
						}
						
						if (parent instanceof CheckBox checkbox) {
							if (bind)
								checkbox.bind(!checkbox.bind());
							else
								checkbox.set(!checkbox.get());
						}
						
						if (parent instanceof Slider slider) {
							if (Hover.isHovered(this.y(this.y+this.height/2).height(this.height/2), mouseX, mouseY)) {
								if (bind)
									slider.bind(Hover.getSliderValue(slider.min(), slider.max(), x + 8.5f, (width - 17), mouseX));
								else
									slider.set(Hover.getSliderValue(slider.min(), slider.max(), x + 8.5f, (width - 17), mouseX));
								dragSlider = slider;
							}
						}
						
						if (parent instanceof Position position) {
							if (bind) {
								position.bindX(Hover.getSliderValue(position.getMinX(), position.getMaxX(), x + 8.5f, (width - 17), mouseX));
								position.bindY(Hover.getSliderValue(position.getMinY(), position.getMaxY(), y + 25, (width - 17), mouseY));
							} else {
								position.x(Hover.getSliderValue(position.getMinX(), position.getMaxX(), x + 8.5f, (width - 17), mouseX));
								position.y(Hover.getSliderValue(position.getMinY(), position.getMaxY(), y + 25, (width - 17), mouseY));
							}
							dragPosition = position;
						}
						
						if (parent instanceof Mode mode) {
							float offX = 0, offY = 0;
							
							for (Mode.Element elmt : mode.getElements()) {
								if (elmt.hasSettings() && elmt.canSettings() && Hover.isHovered(elmt.getSettingRect(), mouseX, mouseY)) {
									rock.getClickGui().getWindow().getWindows().add(new SettingsWindow(elmt, (float) mouseX, (float) mouseY));
									return true;
								}
								
								Rect rect = new Rect(x + offX + 8.5f, y + 25 + offY, bold.get(14).getWidth(elmt.getName()) + 5, 10);
								
								if (Hover.isHovered(rect, mouseX, mouseY)) {
									if (bind)
										mode.bind(elmt);
									else
										mode.set(elmt);
								}
								
								offX += bold.get(14).getWidth(elmt.getName()) + 8;
								if (mode.getElements().indexOf(elmt) + 1 != mode.getElements().size() &&
										offX+bold.get(14).getWidth(mode.getElements().get(mode.getElements().indexOf(elmt) + 1).getName()) + 8 > width-8) {
									offX = 0;
									offY += 13;
								}
							}
						}
						
						if (parent instanceof Select select) {
							float offX = 0, offY = 0;
							
							for (Select.Element elmt : select.getElements()) {
								if (elmt.isHide()) continue;
								
								Rect rect = new Rect(x + offX + 8.5f, y + 25 + offY, bold.get(14).getWidth(elmt.getName()) + 5 + (elmt.canSettings() ? 8 * elmt.getSettingsAnim().get() : 0), 10);
								
								if (Hover.isHovered(rect, mouseX, mouseY) && select.isDraggable()) {
									elmt.setDragging(true);
									elmt.setDragX((int) (offX - mouseX - x));
									elmt.setDragY((int) (offY - mouseY - y));
								}
								
								offX += (rect.getWidth() + 3) * (1-elmt.getHideAnim().get());
								if (select.getElements().indexOf(elmt) + 1 != select.getElements().size() &&
										offX+bold.get(14).getWidth(select.getElements().get(select.getElements().indexOf(elmt) + 1).getName()) + 8 > width-8) {
									offX = 0;
									offY += 13;
								}
							}
						}
						
						if (parent instanceof Clickable clickable) {
							if (Hover.isHovered(x + 8, y + 23, width - 16, 32 - 16, mouseX, mouseY)) {
								clickable.get().run();
							}
						}
					}
					
					return true;
				}
			} else {
				if (parent instanceof Select select) {
					float offX = 0, offY = 0;
					
					for (Select.Element elmt : select.getElements()) {
						if (elmt.isHide()) continue;
						Rect rect = new Rect(x + offX + 8.5f, y + 25 + offY, bold.get(14).getWidth(elmt.getName()) + 5, 10);
						
						if (Hover.isHovered(rect, mouseX, mouseY)) {
							rock.getClickGui().getWindow().getWindows().add(new BindListWindow(elmt, (float)mouseX, (float)mouseY, 60, 100));
						}
						
						offX += bold.get(14).getWidth(elmt.getName()) + 8;
						if (select.getElements().indexOf(elmt) + 1 != select.getElements().size() &&
								offX+bold.get(14).getWidth(select.getElements().get(select.getElements().indexOf(elmt) + 1).getName()) + 8 > width-8) {
							offX = 0;
							offY += 13;
						}
					}
				}
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
		
		return false;
	}
	
	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return this.dragged(mouseX, mouseY, button, dragX, dragY, false);
	}
	
	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY, boolean bind) {
		try {
			if (Hover.isHovered(this, mouseX, mouseY) && button == 0) {
				return true;
			}
		} catch (Exception e) {
			Debugger.print(e);
		}
		return false;
	}
	
	public boolean pressed(int keyCode, int scanCode, int modifiers) {
		if (sliderInput != null) sliderInput.keyPressed(keyCode, scanCode, modifiers);
		if (this.getParent() instanceof Input input) {
            input.getInput().keyPressed(keyCode, scanCode, modifiers);
        }
		return false;
	}
	
	public boolean charTyped(char codePoint, int modifiers) {
		if (sliderInput != null) sliderInput.charTyped(codePoint, modifiers);
		if (this.getParent() instanceof Input input) {
            input.getInput().charTyped(codePoint, modifiers);
        }
		return false;
	}
	
	public void tick() {
		if (sliderInput != null) sliderInput.tick();
		if (this.getParent() instanceof Input input) {
            input.getInput().tick();
        }
	}
	
	public float getHeight() {
		return parent.getHeight();
	}
	
}
