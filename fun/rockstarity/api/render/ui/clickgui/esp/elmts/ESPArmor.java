package fun.rockstarity.api.render.ui.clickgui.esp.elmts;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPSettings;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * @author ConeTin
 * @since 19 недрочаб. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ESPArmor extends ESPElement {
	
	CheckBox enchants = new CheckBox(this, "Зачарования");
	
	public ESPArmor() {
		super("Броня");
	}

	@Override
	public void drawPreview(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, float anim) {
		super.drawPreview(matrixStack, mouseX, mouseY, partialTicks, anim);
		
		if (activeAnim.finished(false)) return;
		
		anim *= activeAnim.get();
		
		ESPSettings settings = rock.getClickGui().getWindow().getEspSettings();
		Rect previewRect = settings.getPreviewRect();
		
		float x = previewRect.getX() + previewRect.getWidth() / 2F - 25F;
		float y = previewRect.getY() + previewRect.getHeight() / 2F - 60F;
		
		if (!dragging) {
			switch (direction) {
			case 0:
				targetX = x;
				targetY = y - (11.2f + 2);
				break;
			case 1:
				targetX = x;
				targetY = y + 122;
				break;
			case 2:
				targetX = x - (11.2f + 2);
				targetY = y;
				break;
			case 3:
				targetX = x + 52;
				targetY = y;
				break;
			}
		}
		
		if (!dragging) {
			targetY += yOffset;
			targetX += xOffset;
		}
		
		if (direction < 2) {
			rect.setWidth(11.2f * 4);
			rect.setHeight(11.2f);
		} else {
			rect.setWidth(11.2f);
			rect.setHeight(11.2f * 4);
		}
		
		Stencil.init();
		Round.draw(matrixStack, settings.getPreviewRect(), 7, FixColor.WHITE);
		Stencil.read(1);
		
		//Round.draw(matrixStack, rect, 1, FixColor.WHITE);
		
		ArrayList<ItemStack> stacks = new ArrayList<>();
		stacks.add(new ItemStack(Items.NETHERITE_BOOTS));
		stacks.add(new ItemStack(Items.NETHERITE_LEGGINGS));
		stacks.add(new ItemStack(Items.NETHERITE_CHESTPLATE));
		stacks.add(new ItemStack(Items.NETHERITE_HELMET));
		
		float off = 0;
		for (ItemStack stack : stacks) {
			drawIcon(matrixStack, stack, targetX, targetY, direction, off, stacks.size(), 50);
			
			off += 11.2f;
		}
	
		
		Stencil.finish();
	}
	
	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		anim *= activeAnim.get();
		
		/*
		FixColor up = color.alpha(anim);
		FixColor down = up.darker(0.5f);
		
		*/
		
		float posX = 0;
		float posY = 0;
		
		y += yOffset;
		x += xOffset;
		
		switch (direction) {
		case 0:
			posX = x;
			posY = y - (11.2f + 2);
			break;
		case 1:
			posX = x;
			posY = y + height + 2;
			break;
		case 2:
			posX = x - (11.2f + 2);
			posY = y;
			break;
		case 3:
			posX = x + width + 2;
			posY = y;
			break;
		}
		
		ArrayList<ItemStack> stacks = new ArrayList<>();
		entity.getArmorInventoryList().forEach((stack) -> {
			if (!stack.isEmpty()) {
				stacks.add(stack);
			}
		});
		
		float off = 0;
		for (ItemStack stack : stacks) {
			drawIcon(matrixStack, stack, posX, posY, direction, off, stacks.size(), width);
			
			off += 11.2f;
		}
	}
	
	private void drawIcon(MatrixStack matrixStack, ItemStack stack, float x, float y, int direction, float off, int size, float width) {
		size -= 1;
		y += (direction == 2 || direction == 3 ? size * 11.2f - off : 0);
		x += (direction == 0 || direction == 1 ? width/2F - (size+1) * 11.2f / 2F + off : 0);

		if (Server.isHW()) {
			stack.setCount(1);
			stack.setDamage(0);
		}
		
		Render.drawStackOld(stack, x, y, 0.7f);
		
		float xOff = 0;
		float yOff = 0;
		//if (EnchantmentHelper.getEnchantments(stack).keySet().size() < 5)
		if (!Server.isFT() && enchants.get())
		for (Enchantment ench : EnchantmentHelper.getEnchantments(stack).keySet()) {
			if (ench != Enchantments.PROTECTION
					&& ench != Enchantments.PROTECTION
					&& ench != Enchantments.SHARPNESS
					&& ench != Enchantments.MENDING
					&& ench != Enchantments.FIRE_ASPECT
					&& ench != Enchantments.THORNS
					&& ench != Enchantments.KNOCKBACK
					&& ench != Enchantments.SILK_TOUCH) continue;
			
			String display = ench.getDisplayName(EnchantmentHelper.getEnchantments(stack).get(ench)).getString().toLowerCase();
			String name = (display.contains("защита") ? "з" : display.contains("§") ? display.trim().substring(0, 4) : display.trim().substring(0, 2));
			//System.out.println(display);
			String[] splitName = ench.getDisplayName(EnchantmentHelper.getEnchantments(stack).get(ench)).getString().split(" ");
			name += "" + EnchantmentHelper.getEnchantments(stack).get(ench);
			
			//Fonts.bold10.drawString(matrices, name, x, y - 10 - offY, ColorSystem.WHITE);
			Rect rect = bold.get(10).draw(matrixStack, name, x + (direction == 3 ? 11.2f : direction == 2 ? -bold.get(10).getWidth(name) : 0) + xOff, y + (direction == 0 ? - 6 : direction == 1 ? 11.2f : 0) + yOff, FixColor.WHITE);
			
			if (direction == 0) {
				yOff -= 6;
			} else if (direction == 1) {
				yOff += 6;
			} else if (direction == 2) {
				xOff -= rect.getWidth() + 2;
			} else if (direction == 3) {
				xOff += rect.getWidth() + 2;
			}
			
		}
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		if (direction < 2) {
			rect.setWidth(11.2f * 4);
			rect.setHeight(11.2f);
		} else {
			rect.setWidth(11.2f);
			rect.setHeight(11.2f * 4);
		}
		return super.clicked(mouseX, mouseY, button);
	}
	
}
