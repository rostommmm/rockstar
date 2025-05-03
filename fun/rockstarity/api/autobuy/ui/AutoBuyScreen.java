package fun.rockstarity.api.autobuy.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.autobuy.logic.items.AutoBuyItem;
import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.NumberInputWidget;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;


@Getter
public class AutoBuyScreen extends Screen implements IAccess {

	private Rect window;
	private AutoBuyRenderer renderer;
	
	public AutoBuyScreen() {
		super(new TranslationTextComponent("Auto Buy"));
		
		float width = 500;
		float height = 300;
		
		this.window = new Rect(sr.getScaledWidth() / 2 - width / 2, sr.getScaledHeight() / 2 - height / 2, width, height);
		this.renderer = new AutoBuyRenderer(window);
	}

	@Override
	public void init() {
		
	}

	public void tick() {
		for (Widget input : renderer.getInputs()) {
			input.tick();
		}
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderer.render(matrixStack, mouseX, mouseY, partialTicks);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		boolean up = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
		boolean down = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
		float offset = .1f / Math.max((float) Minecraft.debugFPS, 5) * 500;
		
		if (up || down)
			this.mouseScrolled(mouseX, mouseY, up ? offset : down ? -offset : 0);
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		for (Widget input : renderer.getInputs()) {
			input.charTyped(typedChar, keyCode);
		}
		return false;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		for (Widget input : renderer.getInputs()) {
			input.keyPressed(keyCode, scanCode, modifiers);
		}

		if (keyCode == GLFW.GLFW_KEY_A && GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS && !renderer.getSearchInput().isFocused()) {
			this.renderer.getSelected().clear();
			
			for (MinecraftItem item : Items.getMinecraftItems()) {
				if (!item.getName().toLowerCase().contains(this.renderer.getSearchInput().getText())) continue;
				this.renderer.getSelected().add(item);
			}
			
			this.renderer.getCurrentItem().updateItems(this.renderer.getSelected());
		}
		
		if (keyCode == 256) {
			this.closeScreen();
			this.renderer.getOpening().setForward(false);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		float x = window.getX();
		float y = window.getY();
		float width = window.getWidth();
		float height = window.getHeight();
		float rightPanelWidth = 151 * renderer.getSeparatorAnim().get();
		Rect rightPanelRect = new Rect(x + width - rightPanelWidth - 12, y + 29, rightPanelWidth, height-41);
		Rect leftPanelRect = new Rect(x + 12, y + 29, 315 * renderer.getSeparatorAnim().get(), height-41);

		if (Hover.isHovered(leftPanelRect, mouseX, mouseY)) {
			renderer.setScroll((float) (renderer.getScroll()+delta * 15));
		}
		
		if (Hover.isHovered(rightPanelRect, mouseX, mouseY)) {
			if (renderer.isSelectEnchant()) {
				renderer.setEnchantScroll((float) (renderer.getEnchantScroll()+delta * 15));
			} else {
				renderer.setItemScroll((float) (renderer.getItemScroll()+delta * 15));
			}
		}
		
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0 && Hover.isHovered(window, mouseX, mouseY)) {
			this.window.setX((float) (this.window.getX() + dragX));
			this.window.setY((float) (this.window.getY() + dragY));
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (Widget input : renderer.getInputs()) {
			input.mouseClicked(mouseX, mouseY, button);
		}
		
		float x = window.getX();
		float y = window.getY();
		float width = window.getWidth();
		float height = window.getHeight();
		float rightPanelWidth = 151 * renderer.getSeparatorAnim().get();
		Rect rightPanelRect = new Rect(x + width - rightPanelWidth - 12, y + 29, rightPanelWidth, height-41);
		String add = "Добавить";
		float btnWidth = bold.get(14).getWidth(add) + 10;
		float btnHeight = 15;
		
		Rect leftPanelRect = new Rect(x + 12, y + 29, 315 * renderer.getSeparatorAnim().get(), height-41);

		if (Hover.isHovered(leftPanelRect, mouseX, mouseY) && renderer.isSelecting()) {
			float selectX = leftPanelRect.getWidth() * (1-renderer.getSeparatorAnim().get());
			
			float xOff = 0;
			float yOff = 25 + this.renderer.getScrollAnim().get();
			float scrollOff = 0;
			for (MinecraftItem item : Items.getMinecraftItems()) {
				if (!item.getName().toLowerCase().contains(this.renderer.getSearchInput().getText())) continue;
				
				if (Hover.isHovered(leftPanelRect, leftPanelRect.getX() + 10 + xOff + selectX, leftPanelRect.getY() + 10 + yOff)) {
					if (Hover.isHovered(leftPanelRect.getX() + 10 + xOff + selectX, leftPanelRect.getY() + 10 + yOff, 30, 30, mouseX, mouseY)) {
						this.renderer.handleSelect(item);
					}
				}

				xOff += 33;
				if (xOff + 33 > leftPanelRect.getWidth()) {
					xOff = 0;
					yOff += 33;
					scrollOff += 33;
				}
			}
		}
		
		if (Hover.isHovered(leftPanelRect, mouseX, mouseY) && !renderer.isSelecting()) {
			float listX = leftPanelRect.getWidth() * (1-renderer.getSeparatorAnim().get()) - leftPanelRect.getWidth();
			
			float xOff = 0;
			float yOff = 25 + this.renderer.getScrollAnim().get();
			float scrollOff = 0;
			for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
				for (MinecraftItem item1 : item.getItems()) {
					if (!item1.getName().toLowerCase().contains(this.renderer.getSearchInput().getText())) continue;
				}

				if (Hover.isHovered(leftPanelRect, mouseX, mouseY)) {
					if (Hover.isHovered(leftPanelRect.getX() + 10 + xOff, leftPanelRect.getY() + 10 + yOff, 30, 30, mouseX, mouseY)) {
						if (renderer.getCurrentItem() == item) {
							reset();
						} else {
							renderer.setCurrentItem(item);
							renderer.getSelected().clear();
							renderer.getSelected().addAll(item.getItems());
							renderer.getSelectedEnchants().clear();
							for (Entry<String, Integer> set : item.getEnchants().entrySet()) {
			    	    	    int enchantmentId = Integer.parseInt(set.getKey().replace(set.getValue() +  "", ""));
			    	    	    Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentId);
			    	    	    if (enchantment != null) {
			    	    	    	renderer.getSelectedEnchants().add(enchantment);
					    	    }
					    	}
							
							renderer.getPriceInput().setText(item.getMaxPrice()+"");
							renderer.getCountInput().setText(item.getMinCount()+"");
							renderer.getDurabilityInput().setText(item.getMinDurability()+"");
							renderer.getSellInput().setText(item.getSellPrice()+"");

							renderer.swapText("Изменение предмета");
						}
					}
				}

				xOff += 33;
				if (xOff + 33 > leftPanelRect.getWidth()) {
					xOff = 0;
					yOff += 33;
					scrollOff += 33;
				}
			}
		}
		
		if (Hover.isHovered(rightPanelRect, mouseX, mouseY)) {

			float selectX = rightPanelRect.getWidth() * (1-renderer.getSelectEnchantAnim().get());
			float listX = rightPanelRect.getWidth() * (1-renderer.getSelectEnchantAnim().get()) - rightPanelRect.getWidth();
			
			Rect stencilRect = rightPanelRect.size(10).height(rightPanelRect.getHeight()-40);
			
			float xOff = rightPanelRect.getX() + 10 + selectX;
			float yOff = rightPanelRect.getY() + 10 + renderer.getEnchantScrollAnim().get();
			float scrollOff = 0;
			
			if (Hover.isHovered(stencilRect, mouseX, mouseY)) {
				for (Entry<String, Enchantment> enchant : Enchantments.AUTOBUY.entrySet()) {
					if (Hover.isHovered(xOff, yOff, rightPanelWidth - 20, 20, mouseX, mouseY)) {
						String lvl = enchant.getValue().getAbLvl()+"";
						float lvlWidth = bold.get(14).getWidth(lvl);
						
						if (Hover.isHovered(xOff + rightPanelWidth - 40 - bold.get(14).getWidth("-+") - lvlWidth, yOff + 3, 15, 15, mouseX, mouseY)) {
							enchant.getValue().setAbLvl(Math.max(1, enchant.getValue().getAbLvl()-1));
						} else if (Hover.isHovered(xOff + rightPanelWidth - 30 - bold.get(14).getWidth("+"), yOff + 3, 15, 15, mouseX, mouseY)) {
							enchant.getValue().setAbLvl(Math.min(10, enchant.getValue().getAbLvl()+1));
						} else {
							if (this.renderer.getSelectedEnchants().contains(enchant.getValue()))
								this.renderer.getSelectedEnchants().remove(enchant.getValue());
							else
								this.renderer.getSelectedEnchants().add(enchant.getValue());
						}
					}
				
					scrollOff += 25;
					yOff += 25;
				}
			}
			
			if (Hover.isHovered(renderer.getCancelEnchantButton(), mouseX, mouseY)) {
				renderer.setSelectEnchant(false);
			}
			
			if (Hover.isHovered(renderer.getConfirmEnchantButton(), mouseX, mouseY) && renderer.isSelectEnchant()) {
				if (this.renderer.getSelectedEnchants().isEmpty())
					rock.getAlertHandler().alert("Зачарование не выбрано", AlertType.ERROR);
				else {
					renderer.setSelectEnchant(false);
					
					Map<String, Integer> enchants = new HashMap<>();
					for (Enchantment enchant : renderer.getSelectedEnchants()) {
						enchants.put(Registry.ENCHANTMENT.getId(enchant)+"", enchant.getAbLvl());
					}
					renderer.getCurrentItem().update(enchants);
				}
			}
			
			if (Hover.isHovered(renderer.getAddEnchantButton(), mouseX, mouseY)) {
				renderer.setSelectEnchant(true);
			}
			
			if (Hover.isHovered(rightPanelRect.getX() + 10, rightPanelRect.getY() + 10 + listX + renderer.getItemScrollAnim().get(), 30, 30, mouseX, mouseY)) {
				this.renderer.handleItemsSelectButtonClick();
			}
			
			if (!renderer.getSwapText().finished(false) && !renderer.getDisplayText().equals("Изменение предмета")) {
				if (Hover.isHovered(rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - btnWidth + listX, rightPanelRect.getY() + rightPanelRect.getHeight() - 10 - btnHeight + renderer.getItemScrollAnim().get(), btnWidth, btnHeight, mouseX, mouseY)) {
					this.renderer.handleAddButtonClick();
					
					renderer.swapText("Изменение предмета");
					reset();
				}
			}
			
			if (!renderer.getSwapText().finished(false) && renderer.getDisplayText().equals("Изменение предмета")) {
				String cancel = "Отмена";
				float cancelbtnWidth = bold.get(14).getWidth(cancel) + 10;
				if (Hover.isHovered(rightPanelRect.getX() + 10 + listX, rightPanelRect.getY() + 50 - 50 * renderer.getSwapText().get() + rightPanelRect.getHeight() - 10 - btnHeight, cancelbtnWidth, btnHeight, mouseX, mouseY)) {
					renderer.swapText("Изменение предмета");
					reset();
				}
				
				String delete = "Удалить";
				float deletebtnWidth = bold.get(14).getWidth(delete) + 10;
				if (Hover.isHovered(rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - deletebtnWidth + listX, rightPanelRect.getY() + 50 - 50 * renderer.getSwapText().get() + rightPanelRect.getHeight() - 10 - btnHeight, deletebtnWidth, btnHeight, mouseX, mouseY)) {
					rock.getAutoBuy().getItems().remove(renderer.getCurrentItem());
					reset();
				}
			}
		}
		
		super.mouseClicked(mouseX, mouseY, button);
		return false;
	}
	
	
	private void reset() {
		renderer.swapText("Изменение предмета");
		
		AutoBuyItem defItem = new AutoBuyItem(Items.DIAMOND, 5000, 0, false);
		renderer.setCurrentItem(defItem);
		renderer.swapText("Добавление предмета");

		renderer.getPriceInput().setText("10000");
		renderer.getCountInput().setText("1");
		renderer.getDurabilityInput().setText("100");
		renderer.getSellInput().setText("15000");
	}
}