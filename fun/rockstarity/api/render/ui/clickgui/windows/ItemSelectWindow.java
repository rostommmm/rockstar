package fun.rockstarity.api.render.ui.clickgui.windows;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.autobuy.logic.items.MinecraftItem;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.settings.list.ItemSelect;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import fun.rockstarity.api.render.ui.widgets.Window;
import net.minecraft.item.AirItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemSelectWindow extends Window {
	
	private float x, y, width, height = 50, scroll, dragX, dragY;
	private boolean dragWin, dragSize;
	private final ItemSelect parent;
	private InputWidget input;

	public ItemSelectWindow(ItemSelect parent, float x, float y, float width, float height) {
		super(x, y, width, height);
		this.x = x;
		this.y = y;
		this.width = width;
		this.parent = parent;
		this.height = 150;
		input = new InputWidget(bold.get(15), 0, 0, 56, 15, new TranslationTextComponent(""), false);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		FixColor mainColor = rock.getThemes().getFirstColor().alpha(opening.get());
		FixColor textColor = rock.getThemes().getTextFirstColor().alpha(opening.get());
		float y = (float) (this.y - (height + 25) + (height + 25) * opening.get());
		float offset = y + 18;
		
		if (dragWin) {
			this.x = mouseX + dragX;
			this.y = mouseY + dragY;
		}
		
		if (dragSize) {
			this.width = MathHelper.clamp(mouseX - x, 127, 530);
			this.height = MathHelper.clamp(mouseY - y, 107, 430);
		}
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Render.scissor(x-2, this.y, width+4, height+1);
		Round.draw(matrixStack, new Rect(x, y, width, height), 5, mainColor);
		bold.get(15).draw(matrixStack, "Выбор предмета", x + 4, y + 3, textColor);
		String hover = null;
		Stencil.init();
		Round.draw(matrixStack, new Rect(x, y + 18, width, height - 18), 5, mainColor);
		Stencil.read(1);
		float scrollOff=0;
		float xOff = 7, yOff = 22 + scroll;
		
		for (MinecraftItem set : Items.getMinecraftItems()) {
			Item item = set.getItem().asItem();
			if (!(item instanceof BlockItem) && parent.isOnlyBlocks()) continue;
			if (item instanceof AirItem) continue;
			
			if (xOff > width || yOff > height) break;
			if (!set.getName().toLowerCase().contains(input.getText().toLowerCase())) continue;
			Round.draw(matrixStack, new Rect(x + xOff, y + yOff, 15, 15), 2, textColor.alpha(0.1f).move(Style.getMain(), parent.getItems().contains(set) ? 1 : 0));
			GL11.glPushMatrix();
	        GlStateManager.translated(x + xOff, y + yOff, 0);
	        GlStateManager.scaled(1.55, 1.55, 1.55);
	        mc.getItemRenderer().zLevel = -900;
			Render.drawStack(new ItemStack(item), 0, 0);
			GL11.glPopMatrix();
			
			if (Hover.isHovered(x + xOff, y + yOff, 20, 20, mouseX, mouseY)) {
				hover = set.getName();
			}
			
			xOff += 20;
			if (xOff + 20 > width) {
				yOff += 20;
				scrollOff += 20;
				xOff = 7;
			}
		}

		if (-scrollOff > scroll) {
			scroll = -scrollOff;
		}

		Stencil.finish();
		Round.draw(matrixStack, new Rect(x+width-52.5f, y+2.5f, 50, 10), 2, textColor.alpha(0.1));
        input.x = (int) (x + width - 56);
        input.y = (int) y + 2;
        input.renderButton(matrixStack, mouseX, mouseY, partialTicks, 1);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
        
        if (hover != null) {
        	Round.draw(matrixStack, new Rect(mouseX + 10, mouseY, bold.get(15).getWidth(hover) + 5, 11), 2, mainColor.alpha(0.5));
        	bold.get(15).draw(matrixStack, hover, mouseX + 12, mouseY + 1.5f, textColor);
        }
	}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		float y = (float) (this.y - 25 + 25 * opening.get());
		float addOff = 12.5f;
		float offset = y + 18;
		float scrollOff=0;
		float xOff = 7, yOff = 22 + scroll;
		
		for (MinecraftItem set : Items.getMinecraftItems()) {
			
			if (xOff > width || yOff > height) break;
			if (!set.getName().toLowerCase().contains(input.getText().toLowerCase())) continue;
			if (set.getItem() instanceof AirItem) continue;

			if (Hover.isHovered(x + xOff, y + yOff, 15, 15, mouseX, mouseY)) {
				if (this.parent.getItems().contains(set)) {
					this.parent.getItems().remove(set);
				} else {
					this.parent.getItems().add(set);
				}
				return true;
			}

			xOff += 20;
			if (xOff + 20 > width) {
				yOff += 20;
				scrollOff += 20;
				xOff = 7;
			}
		}
		
		if (Hover.isHovered(x + width - 7, this.y + height - 7, 7, 7, mouseX, mouseY)) {
			dragSize = true;
		} else if (Hover.isHovered(x, this.y, width, height, mouseX, mouseY)) {
			dragX = (float) (x - mouseX);
			dragY = (float) (this.y - mouseY);
			dragWin = true;
		}
		
		input.mouseClicked(mouseX, mouseY, button);
		if (!Hover.isHovered(x, this.y, width, height, mouseX, mouseY)) {
			if (this.opening.finished())
			opening.setForward(false);
			return false;
		} else {
			return true;
		}
	}
	
	public boolean released(double mouseX, double mouseY, int button) {
		dragWin = false;
		dragSize = false;
		return false;
	}
	
	@Override
	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (Hover.isHovered(this, mouseX, mouseY)) {
			//rock.getClickGui().getWindow().setCanDrag(false);
			
			//x += dragX;
			//y += dragY;
			
			return true;
		}
		return super.dragged(mouseX, mouseY, button, dragX, dragY);
	}
	
	public void scrolled(double mouseX, double mouseY, double delta) {
		scroll += Hover.isHovered(new Rect(x, y, width, height), mouseX, mouseY) ? (float) (delta * 15) : 0;
		if (scroll > 0) scroll = 0;
	}
	
	public boolean pressed(int keyCode, int scanCode, int modifiers) {
		input.keyPressed(keyCode, scanCode, modifiers);
		return false;
	}
	
	public void charTyped(char typedChar, int keyCode) {
		input.charTyped(typedChar, keyCode);
	}
	
}
