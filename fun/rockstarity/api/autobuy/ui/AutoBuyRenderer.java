package fun.rockstarity.api.autobuy.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.autobuy.logic.items.AutoBuyItem;
import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import fun.rockstarity.api.render.ui.widgets.NumberInputWidget;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 24 апр. 2024 г.
 */


@Getter 
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoBuyRenderer implements IAccess {
	FixColor bgColor, settingsBg, separatorColor, moduleColor, text, black, white;
	
	Animation swapText = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			opening = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setForward(false).finish(),
			separatorAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			selectEnchantAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300),
			selectingAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	
	String swapping = "Добавление предмета";
	String displayText = "Добавление предмета";
	@Setter boolean selecting, selectEnchant;
	float yaw, pitch;
	Rect window;
	@Setter AutoBuyItem currentItem;
	InputWidget searchInput;
	NumberInputWidget priceInput, countInput, durabilityInput, sellInput;
	final InfinityAnimation scrollAnim = new InfinityAnimation(), enchantScrollAnim = new InfinityAnimation(), itemScrollAnim = new InfinityAnimation();
	@Setter float scroll, enchantScroll, itemScroll;
	final List<MinecraftItem> selected = new ArrayList<>();
	final List<Enchantment> selectedEnchants = new ArrayList<>();
	final List<Widget> inputs = new ArrayList<>();
	
	private Rect addEnchantButton, confirmEnchantButton, cancelEnchantButton;

	public AutoBuyRenderer(Rect window) {
		this.window = window;
		
		this.currentItem = new AutoBuyItem(Items.DIAMOND, 5000, 0, false);
		this.priceInput = new NumberInputWidget(bold.get(12), 0, 0, 0, 0, new TranslationTextComponent("Price"), false); 
		this.priceInput.setText("10000"); 
		this.priceInput.setEndText(" $");
		this.priceInput.setMin(10);
		inputs.add(priceInput);
		
		this.countInput = new NumberInputWidget(bold.get(12), 0, 0, 0, 0, new TranslationTextComponent("Count"), false); 
		this.countInput.setText("1"); 
		this.countInput.setEndText(" шт");
		this.countInput.setMin(1);
		this.countInput.setMax(64);
		inputs.add(countInput);
		
		this.durabilityInput = new NumberInputWidget(bold.get(12), 0, 0, 0, 0, new TranslationTextComponent("Durability"), false); 
		this.durabilityInput.setText("100"); 
		this.durabilityInput.setEndText(" %");
		this.durabilityInput.setMin(0);
		this.durabilityInput.setMax(100);
		inputs.add(durabilityInput);
		
		this.sellInput = new NumberInputWidget(bold.get(12), 0, 0, 0, 0, new TranslationTextComponent("Sell"), false); 
		this.sellInput.setText("15000"); 
		this.sellInput.setEndText(" $");
		this.sellInput.setMin(10);
		inputs.add(sellInput);
		
		this.searchInput = new InputWidget(bold.get(12), 0, 0, 0, 0, new TranslationTextComponent("Search"));
		inputs.add(searchInput);
	}
			
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		bgColor = rock.getThemes().getFirstColor().alpha(opening.get());
		settingsBg = rock.getThemes().getSecondColor().alpha(opening.get() * separatorAnim.get());
		separatorColor = rock.getThemes().getThirdColor().alpha(opening.get() * separatorAnim.get());
		moduleColor = rock.getThemes().getTextSecondColor().alpha(opening.get() * separatorAnim.get());
		text = rock.getThemes().getTextFirstColor().alpha(opening.get() * separatorAnim.get());
		black = FixColor.BLACK.alpha(opening.get());
		white = FixColor.WHITE.alpha(opening.get());
		
		this.separatorAnim.setForward(this.opening.finished());
		this.selectingAnim.setForward(this.selecting);
		this.scrollAnim.animate(scroll, 150);
		this.enchantScrollAnim.animate(enchantScroll, 150);
		this.itemScrollAnim.animate(itemScroll, 150);
		
		// Перенос гуишки при закрытии
		float modif = 4;
		
		if (opening.isForward() && mc.player != null) {
			yaw = mc.player.rotationYaw * modif;
			pitch = mc.player.rotationPitch * modif;
		}
		
		GL11.glPushMatrix();
        GL11.glTranslatef(yaw - mc.player.rotationYaw * modif, pitch - mc.player.rotationPitch * modif, 0);
		
        // Анимация на размер гуишки (открытие/закрытие)
		Render.scale(rock.getClickGui().getWindow().getX() + window.getWidth() / 2, window.getY() + window.getHeight() / 2, 0.5f + (float) opening.get() * 0.5f);
		
		this.renderWindow(matrixStack, mouseX, mouseY, partialTicks);
		
		Render.end();
		
		GL11.glPopMatrix();
	}
	
	private void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		float x = window.getX();
		float y = window.getY();
		float width = window.getWidth();
		float height = window.getHeight();
		
		Round.draw(matrixStack, window, 12, bgColor);
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Render.scissor(window.getX(), window.getY(), window.getWidth(), window.getHeight());
		
		Rect leftPanelRect = new Rect(x + 12, y + 29, 315 * separatorAnim.get(), height-41);
		Round.draw(matrixStack, leftPanelRect, 10, settingsBg);
		
		float rightPanelWidth = 151 * separatorAnim.get();
		Rect rightPanelRect = new Rect(x + width - rightPanelWidth - 12, y + 29, rightPanelWidth, height-41);
		Round.draw(matrixStack, rightPanelRect, 10, settingsBg);
		
		bold.get(20).draw(matrixStack, "Rockstar AutoBuy", x + 12, y + 8, text);
		
		if (!swapping.equals(displayText) && swapText.finished(false)) {
			displayText = swapping;
			swapText.setForward(true);
		}
		
		if (swapping.equals(displayText)) {
			swapText.setForward(true);
		}
		
		bold.get(20).draw(matrixStack, displayText, x + width - bold.get(20).getWidth(displayText) - 12, y - 12 + 20 * swapText.get(), text);
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		{ // Правая хуета
			
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			Render.scissor(rightPanelRect.getX(), rightPanelRect.getY(), rightPanelRect.getWidth(), rightPanelRect.getHeight());
			
			this.selectEnchantAnim.setForward(this.selectEnchant);
			
			float selectX = rightPanelRect.getWidth() * (1-this.selectEnchantAnim.get());
			float listX = rightPanelRect.getWidth() * (1-this.selectEnchantAnim.get()) - rightPanelRect.getWidth();
			
			if (currentItem.getItems().isEmpty()) {
				currentItem.getItems().add(new MinecraftItem(Items.DIAMOND));
			}
			 
			if (!this.selectEnchantAnim.finished() && !currentItem.getItems().isEmpty()) {
				if (!this.selectingAnim.finished(false))
					Round.draw(matrixStack, new Rect(rightPanelRect.getX() + 10 + listX, rightPanelRect.getY() + 10 + itemScrollAnim.get(), 30, 30), 7, separatorColor.alpha(separatorAnim.get()));
				Round.draw(matrixStack, new Rect(rightPanelRect.getX() + 10 + listX, rightPanelRect.getY() + 10 + itemScrollAnim.get(), 30, 30).size(2 * this.selectingAnim.get()), 7 - 2 * this.selectingAnim.get(), bgColor.alpha(separatorAnim.get()));
				Render.drawStack(new ItemStack(currentItem.getItems().stream().findFirst().get().getItem()), rightPanelRect.getX() + 14 + listX, rightPanelRect.getY() + 13 + itemScrollAnim.get(), 1.4f);
				
				bold.get(14).draw(matrixStack, "Имя предмета", rightPanelRect.getX() + 50 + listX, rightPanelRect.getY() + 15 + itemScrollAnim.get(), moduleColor);
				bold.get(20).draw(matrixStack, currentItem.getItems().get(0).getItem().getName().getString(), rightPanelRect.getX() + 50 + listX, rightPanelRect.getY() + 21 + itemScrollAnim.get(), text);

				Round.draw(matrixStack, new Rect(rightPanelRect.getX() + rightPanelWidth - 50, rightPanelRect.getY() + 21 + itemScrollAnim.get(), 40, 20), 0.1f, settingsBg.alpha(0), settingsBg, settingsBg.alpha(0), settingsBg);
				Round.draw(matrixStack, new Rect(rightPanelRect.getX() + rightPanelWidth - 10, rightPanelRect.getY() + 21 + itemScrollAnim.get(), 10, 20), 0.1f, settingsBg);

				float yOff = rightPanelRect.getY() + 45 + itemScrollAnim.get();
				float startX = rightPanelRect.getX() + 10 + listX;
				float xOff = startX;
				float inputWidth = 83;
				
				bold.get(14).draw(matrixStack, "Минимальная цена за штуку", xOff, yOff - 1, moduleColor);
				yOff += 13;
				Round.draw(matrixStack, new Rect(xOff, yOff, inputWidth, 15), 3, bgColor.alpha(separatorAnim.get()));
				this.priceInput.set(xOff, yOff, inputWidth, 19);
				this.priceInput.renderButton(matrixStack, mouseX, mouseY, partialTicks);
				yOff += 20;
				
				bold.get(14).draw(matrixStack, "Минимальное кол-во предмета", xOff, yOff - 1, moduleColor);
				yOff += 13;
				Round.draw(matrixStack, new Rect(xOff, yOff, inputWidth, 15), 3, bgColor.alpha(separatorAnim.get()));
				this.countInput.set(xOff, yOff, inputWidth, 19);
				this.countInput.renderButton(matrixStack, mouseX, mouseY, partialTicks);
				yOff += 20;
				
				boolean damage = false;
				for (MinecraftItem item : currentItem.getItems()) {
					if (item.getItem().isDamageable()) {
						damage = true;
						break;
					}
				}
				
				if (damage) {
					bold.get(14).draw(matrixStack, "Минимальная прочность", xOff, yOff - 1, moduleColor);
					yOff += 13;
					Round.draw(matrixStack, new Rect(xOff, yOff, inputWidth, 15), 3, bgColor.alpha(separatorAnim.get()));
					this.durabilityInput.set(xOff, yOff, inputWidth, 19);
					this.durabilityInput.renderButton(matrixStack, mouseX, mouseY, partialTicks);
					yOff += 20;
				}
				
				bold.get(14).draw(matrixStack, "Цена продажи одной штуки", xOff, yOff - 1, moduleColor);
				yOff += 13;
				Round.draw(matrixStack, new Rect(xOff, yOff, inputWidth, 15), 3, bgColor.alpha(separatorAnim.get()));
				this.sellInput.set(xOff, yOff, inputWidth, 19);
				this.sellInput.renderButton(matrixStack, mouseX, mouseY, partialTicks);
				yOff += 20;
				
				
				this.currentItem.setMaxPrice((int) this.priceInput.getFloat());
				this.currentItem.setMinCount((int) this.countInput.getFloat());
				this.currentItem.setMinDurability((int) this.durabilityInput.getFloat());
				this.currentItem.setSellPrice((int) this.sellInput.getFloat());
				
				bold.get(14).draw(matrixStack, "Зачарования", xOff, yOff - 1, moduleColor);
				yOff += 13;
				
				for (Entry<String, Integer> enchant : this.currentItem.getEnchants().entrySet()) {
					String name = Registry.ENCHANTMENT.getByValue(Integer.parseInt(enchant.getKey())).getDisplayName(enchant.getValue()).getString();
					
					if (xOff + bold.get(12).getWidth(name) + 10 > rightPanelRect.getX() + rightPanelRect.getWidth()) {
						xOff = startX;
						yOff += 14;
					}
					
					Round.draw(matrixStack, new Rect(xOff, yOff, bold.get(12).getWidth(name) + 8, 12), 3, bgColor.alpha(separatorAnim.get() * this.selectEnchantAnim.reversed()));
					bold.get(12).draw(matrixStack, name, xOff + 4, yOff + 1.5f, text.alpha(this.selectEnchantAnim.reversed()));
					
					xOff += bold.get(12).getWidth(name) + 10;
					if (xOff + bold.get(12).getWidth(name) + 10 > rightPanelRect.getX() + rightPanelRect.getWidth()) {
						xOff = startX;
						yOff += 14;
					}
				}
				
				String addString = "Изменить";
				Round.draw(matrixStack, addEnchantButton = new Rect(xOff, yOff, bold.get(12).getWidth(addString) + 9, 12), 3, bgColor.alpha(separatorAnim.get()*this.selectEnchantAnim.reversed()));
				bold.get(12).draw(matrixStack, addString, xOff + 4, yOff + 1.5f, moduleColor.alpha(this.selectEnchantAnim.reversed()));
				
				float btnHeight = 15;

				if (!swapText.finished(false) && !displayText.equals("Изменение предмета")) {
					String add = "Добавить";
					float btnWidth = bold.get(14).getWidth(add) + 10;
					Round.draw(matrixStack, new Rect(rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - btnWidth + listX, rightPanelRect.getY() + 50 - 50 * swapText.get() + rightPanelRect.getHeight() - 10 - btnHeight, btnWidth, btnHeight), 3, bgColor.alpha(separatorAnim.get()));
					bold.get(14).draw(matrixStack, add, rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - btnWidth + 4.5f + listX, rightPanelRect.getY() + 50 - 50 * swapText.get() + rightPanelRect.getHeight() - 7.5f - btnHeight, text);
				}
				
				if (!swapText.finished(false) && displayText.equals("Изменение предмета")) {
					String cancel = "Отмена";
					float cancelbtnWidth = bold.get(14).getWidth(cancel) + 10;
					Round.draw(matrixStack, new Rect(rightPanelRect.getX() + 10 + listX, rightPanelRect.getY() + 50 - 50 * swapText.get() + rightPanelRect.getHeight() - 10 - btnHeight, cancelbtnWidth, btnHeight), 3, bgColor.alpha(separatorAnim.get()));
					bold.get(14).draw(matrixStack, cancel, rightPanelRect.getX() + 10 + 4.5f + listX, rightPanelRect.getY() + 50 - 50 * swapText.get() + rightPanelRect.getHeight() - 7.5f - btnHeight, text);
				
				
					String delete = "Удалить";
					float deletebtnWidth = bold.get(14).getWidth(delete) + 10;
					Round.draw(matrixStack, new Rect(rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - deletebtnWidth + listX, rightPanelRect.getY() + 50 - 50 * swapText.get() + rightPanelRect.getHeight() - 10 - btnHeight, deletebtnWidth, btnHeight), 3, bgColor.alpha(separatorAnim.get()));
					bold.get(14).draw(matrixStack, delete, rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - deletebtnWidth + 4.5f + listX, rightPanelRect.getY() + 50 - 50 * swapText.get() + rightPanelRect.getHeight() - 7.5f - btnHeight, text);
				}
				
				if (itemScrollAnim.get() > 0) 
					itemScroll = 0;
				
				if (itemScrollAnim.get() < -(yOff - (rightPanelRect.getY() + 45 + itemScrollAnim.get())))
					itemScroll = -(yOff - (rightPanelRect.getY() + 45 + itemScrollAnim.get()));
			}
			
			if (!this.selectEnchantAnim.finished(false)) {
				
				float xOff = rightPanelRect.getX() + 10 + selectX;
				float yOff = rightPanelRect.getY() + 10 + this.enchantScrollAnim.get();
				float scrollOff = -rightPanelRect.getHeight()+40;
				
				Rect stencilRect = rightPanelRect.size(10).height(rightPanelRect.getHeight()-40);
				
				Stencil.init();
				Round.draw(matrixStack, stencilRect, 3, bgColor.alpha(separatorAnim.get()));
				Stencil.read(1);
				
				for (Entry<String, Enchantment> enchant : Enchantments.AUTOBUY.entrySet()) {
					if (Hover.isHovered(stencilRect.height(rightPanelRect.getHeight()-15), xOff, yOff+25)) {
						enchant.getValue().getSelectAnim().setForward(this.selectedEnchants.contains(enchant.getValue()));
						
						Round.draw(matrixStack, new Rect(xOff, yOff, rightPanelWidth - 20, 20), 3, bgColor.alpha(separatorAnim.get()));
						bold.get(14).draw(matrixStack, enchant.getValue().getDisplayName(0).getString().replace("enchantment.level.0", "")/* + " " + Registry.ENCHANTMENT.getId(enchant.getValue())*/, xOff + 6 + 11 * enchant.getValue().getSelectAnim().get(), yOff + 5, text);
						Render.image("icons/yes.png", xOff + 7 * enchant.getValue().getSelectAnim().get(), yOff + 7.5f, 6, 6, text.alpha(enchant.getValue().getSelectAnim().get()));
						
						String lvl = enchant.getValue().getAbLvl()+"";
						float lvlWidth = bold.get(14).getWidth(lvl);
						
						bold.get(14).draw(matrixStack, "-", xOff + rightPanelWidth - 35 - bold.get(14).getWidth("-+") - lvlWidth, yOff + 5, text);
						
						bold.get(14).draw(matrixStack, lvl, xOff + rightPanelWidth - 32 - bold.get(14).getWidth("+") - lvlWidth, yOff + 5, text);
						
						bold.get(14).draw(matrixStack, "+", xOff + rightPanelWidth - 28 - bold.get(14).getWidth("+"), yOff + 5, text);
					}
					
					scrollOff += 25;
					yOff += 25;
				}
				
				if (-scrollOff > enchantScroll) {
					enchantScroll = -scrollOff;
				}
				
				if (enchantScroll > 0) enchantScroll = 0;
				
				Stencil.finish();
				
				String add = "Добавить";
				float btnWidth = bold.get(14).getWidth(add) + 10;
				float btnHeight = 15;
				Round.draw(matrixStack, confirmEnchantButton = new Rect(rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - btnWidth + selectX, rightPanelRect.getY() + rightPanelRect.getHeight() - 10 - btnHeight, btnWidth, btnHeight), 3, bgColor.alpha(separatorAnim.get()));
				bold.get(14).draw(matrixStack, add, rightPanelRect.getX() + rightPanelRect.getWidth() - 10 - btnWidth + 4.5f + selectX, rightPanelRect.getY() + rightPanelRect.getHeight() - 7.5f - btnHeight, text);
				
				String cancel = "Отмена";
				float cancelbtnWidth = bold.get(14).getWidth(cancel) + 10;
				Round.draw(matrixStack, cancelEnchantButton = new Rect(rightPanelRect.getX() + 10 + selectX, rightPanelRect.getY() + rightPanelRect.getHeight() - 10 - btnHeight, cancelbtnWidth, btnHeight), 3, bgColor.alpha(separatorAnim.get()));
				bold.get(14).draw(matrixStack, cancel, rightPanelRect.getX() + 10 + 4.5f + selectX, rightPanelRect.getY() + rightPanelRect.getHeight() - 7.5f - btnHeight, text);
				
			}
			
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
		
		{ // Левая хуета
			Stencil.init();
			Round.draw(matrixStack, leftPanelRect, 10, settingsBg);
			Stencil.read(1);
			
			float selectX = leftPanelRect.getWidth() * (1-this.selectingAnim.get());
			float listX = leftPanelRect.getWidth() * (1-this.selectingAnim.get()) - leftPanelRect.getWidth();
			
			if (!this.selectingAnim.finished(false)) {
				float btnWidth = 80;
				float btnHeight = 15;
				Round.draw(matrixStack, new Rect(leftPanelRect.getX() + leftPanelRect.getWidth() + selectX - 10 - btnWidth, leftPanelRect.getY() + 10, btnWidth, btnHeight), 3, bgColor.alpha(separatorAnim.get()));
				Render.image("category/search.png", leftPanelRect.getX() + leftPanelRect.getWidth() + selectX - 6 - btnWidth + .5f, leftPanelRect.getY() + 14.5f, 6, 6, text);
				
				bold.get(20).draw(matrixStack, "Выбор предмета", leftPanelRect.getX() + 10 + selectX, leftPanelRect.getY() + 11, text);
				
				if (!this.searchInput.isFocused() && this.searchInput.getText().isEmpty())
					bold.get(12).draw(matrixStack, "Найти предмет", leftPanelRect.getX() + leftPanelRect.getWidth() + selectX + 3 - btnWidth, leftPanelRect.getY() + 13, text);
				
				btnWidth -= 10;
				this.searchInput.set(leftPanelRect.getX() + leftPanelRect.getWidth() + selectX - 10 - btnWidth, leftPanelRect.getY() + 12, btnWidth, btnHeight);
				this.searchInput.renderButton(matrixStack, mouseX, mouseY, partialTicks);
				
				
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Render.scissor(leftPanelRect.getX(), leftPanelRect.getY() + 30, leftPanelRect.getWidth(), leftPanelRect.getHeight() - 30);
				
				float xOff = 0;
				float yOff = 25 + this.scrollAnim.get();
				float scrollOff = 0;
				for (MinecraftItem item : Items.getMinecraftItems()) {
					if (!item.getName().toLowerCase().contains(this.searchInput.getText())) continue;
					
					if (Hover.isHovered(leftPanelRect, leftPanelRect.getX() + 10 + xOff + selectX, leftPanelRect.getY() + 10 + yOff)) {
						item.getSelectAnim().setForward(this.selected.contains(item));
						
						if (!item.getSelectAnim().finished(false))
							Round.draw(matrixStack, new Rect(leftPanelRect.getX() + 10 + xOff + selectX, leftPanelRect.getY() + 10 + yOff, 30, 30), 7, Style.getMain().alpha(separatorAnim.get()));
						Round.draw(matrixStack, new Rect(leftPanelRect.getX() + 10 + xOff + selectX, leftPanelRect.getY() + 10 + yOff, 30, 30).size(item.getSelectAnim().get()), 7 - item.getSelectAnim().get(), bgColor.alpha(separatorAnim.get()));
						Render.drawStack(new ItemStack(item.getItem()), leftPanelRect.getX() + 14 + xOff + selectX, leftPanelRect.getY() + 13 + yOff, 1.4f);
					}

					xOff += 33;
					if (xOff + 33 > leftPanelRect.getWidth()) {
						xOff = 0;
						yOff += 33;
						scrollOff += 33;
					}
				}
				
				if (-scrollOff > scroll) {
					scroll = -scrollOff;
				}
				
				if (scroll > 0) scroll = 0;
				
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
				
			}
			
			if (!this.selectingAnim.finished()) {
				bold.get(20).draw(matrixStack, "Список предметов", leftPanelRect.getX() + 10 + listX, leftPanelRect.getY() + 11, text);
				
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Render.scissor(leftPanelRect.getX(), leftPanelRect.getY() + 30, leftPanelRect.getWidth(), leftPanelRect.getHeight() - 30);
				
				float xOff = 0;
				float yOff = 25 + this.scrollAnim.get();
				float scrollOff = 0;
				for (AutoBuyItem item : rock.getAutoBuy().getItems()) {
					//if (!item.getName().toLowerCase().contains(this.searchInput.getText())) continue;
					
					if (Hover.isHovered(leftPanelRect, leftPanelRect.getX() + 10 + xOff + listX, leftPanelRect.getY() + 10 + yOff) && !selectingAnim.finished(true)) {
						item.getItems().get(0).getSelectAnim().setForward(currentItem == item);
						
						if (!item.getItems().get(0).getSelectAnim().finished(false))
							Round.draw(matrixStack, new Rect(leftPanelRect.getX() + 10 + xOff + listX, leftPanelRect.getY() + 10 + yOff, 30, 30), 7, Style.getMain().alpha(separatorAnim.get()));
						
						Round.draw(matrixStack, new Rect(leftPanelRect.getX() + 10 + xOff + listX, leftPanelRect.getY() + 10 + yOff, 30, 30).size(item.getItems().get(0).getSelectAnim().get()), 7 - item.getItems().get(0).getSelectAnim().get(), bgColor.alpha(separatorAnim.get()));
						Render.drawStack(new ItemStack(item.getItems().stream().findFirst().get().getItem()), leftPanelRect.getX() + 14 + xOff + listX, leftPanelRect.getY() + 13 + yOff, 1.4f);
					}

					xOff += 33;
					if (xOff + 33 > leftPanelRect.getWidth()) {
						xOff = 0;
						yOff += 33;
						scrollOff += 33;
					}
				}
				
				if (-scrollOff > scroll) {
					scroll = -scrollOff;
				}
				
				if (scroll > 0) scroll = 0;
				
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			}
			
			Stencil.finish();
		}
	}
	
	public void swapText(String text) {
		swapText.setSpeed(150);
		swapping = text;
		
		if (!swapping.equals(displayText)) {
			swapText.setForward(false);
		}
	}
	
	public void handleSelect(MinecraftItem item) {
		if (!selecting) return;
		
		if (GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) != GLFW.GLFW_PRESS)
			this.selected.clear();
		
		if (this.selected.contains(item)) {
			if (selected.size() == 1 && selected.get(0) == item)
				return;
			
			this.selected.remove(item);
		} else
			this.selected.add(item);
		
		this.currentItem.updateItems(this.selected);
	}
	
	public void handleItemsSelectButtonClick() {
		this.selecting = !this.selecting;
		
		if (selecting) {
			for (MinecraftItem item : Items.getMinecraftItems()) {
				item.getSelectAnim().setForward(false);
			}
		}
	}
	
	public void handleAddButtonClick() {
		rock.getAutoBuy().getItems().add(currentItem);
		this.currentItem = new AutoBuyItem(Items.DIAMOND, 5000, 0, false);
		this.selectedEnchants.clear();
		
		for (Entry<String, Enchantment> enchant : Enchantments.AUTOBUY.entrySet()) {
			enchant.getValue().setAbLvl(1);
		}
	}
	
}
