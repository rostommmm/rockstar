package fun.rockstarity.api.render.ui.clickgui.esp.elmts;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.game.EventUsePearl;
import fun.rockstarity.api.events.list.player.EventFinishEat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPEventable;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPSettings;
import fun.rockstarity.api.render.ui.clickgui.esp.elmts.cooldowns.ItemData;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * @author ConeTin
 * @since 19 недрочаб. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ESPCooldowns extends ESPEventable {
	
	public ESPCooldowns() {
		super("Задержки");
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
		
		String text = "13.1 сек";
		float width = semibold.get(14).getWidth(text) + 19;
		
		if (!dragging) {
			switch (direction) {
			case 0:
				targetX = x;
				targetY = y - 15;
				break;
			case 1:
				targetX = x;
				targetY = y + 122;
				break;
			case 2:
				targetX = x - width;
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

		rect.setWidth(width);
		rect.setHeight(13);
		
		Round.draw(matrixStack, new Rect(targetX, targetY, width, 13), 3, rock.getThemes().getDarkTheme().getThirdColor().alpha(anim));
		Render.drawStackOld(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), targetX + 2, targetY, 0.7f * anim);
		semibold.get(14).draw(matrixStack, text, targetX + 14, targetY + 1, FixColor.WHITE.alpha(anim));
	}
	
	@Override
	public void drawOnEntity(MatrixStack matrixStack, LivingEntity entity, float x, float y, float width, float height, float anim) {
		anim *= activeAnim.get();
		
		float posX = 0;
		float posY = 0;
		
		y += yOffset;
		x += xOffset;
		
		switch (direction) {
		case 0:
			posX = x + width / 2F;
			posY = y - 15;
			break;
		case 1:
			posX = x + width / 2F;
			posY = y + height;
			break;
		case 2:
			posX = x;
			posY = y;
			break;
		case 3:
			posX = x + width;
			posY = y;
			break;
		}
		
		entity.getDatas().removeIf(data -> data.getCreation().passed(data.getCooldown()));
		
		float off = 0;
		for (ItemData stack : entity.getDatas()) {
			drawData(matrixStack, stack, posX, posY, direction, off, anim);
			
			off += 14f;
		}
	}
	
	private void drawData(MatrixStack matrixStack, ItemData data, float x, float y, int direction, float off, float anim) {
		String text = TextUtility.formatNumberOld(((data.getCooldown() - data.getCreation().getElapsed()) / 1000F)) + " сек";
		float width = semibold.get(14).getWidth(text) + 19;
		
		x += (direction == 2 ? -width : 0);
		x -= (direction == 0 || direction == 1 ? width / 2F  : 0);
		y += direction == 0 ? -off : off;
		
		Round.draw(matrixStack, new Rect(x, y, width, 13), 3, rock.getThemes().getDarkTheme().getThirdColor().alpha(anim));
		Render.drawStackOld(new ItemStack(data.getItem()), x + 2, y, 0.7f * anim);
		semibold.get(14).draw(matrixStack, text, x + 14, y + 1, FixColor.WHITE.alpha(anim));
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		String text = "13.1 сек";
		float width = semibold.get(14).getWidth(text) + 19;
		
		rect.setWidth(width);
		rect.setHeight(13);
		
		return super.clicked(mouseX, mouseY, button);
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventTotemBreak e) {
			e.getEntity().getDatas().removeIf(item -> item.getItem() == Items.TOTEM_OF_UNDYING);
			e.getEntity().getDatas().add(new ItemData(Server.is("spooky") ? 15_000 : 120_000, Items.TOTEM_OF_UNDYING));
		}
		
		if (event instanceof EventFinishEat e) {
			if (e.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
				e.getEntity().getDatas().removeIf(item -> item.getItem() == Items.ENCHANTED_GOLDEN_APPLE);
				e.getEntity().getDatas().add(new ItemData(150_000, Items.ENCHANTED_GOLDEN_APPLE));
			}
			
			if (e.getItem() == Items.GOLDEN_APPLE) {
				e.getEntity().getDatas().removeIf(item -> item.getItem() == Items.GOLDEN_APPLE);
				e.getEntity().getDatas().add(new ItemData(30_000, Items.GOLDEN_APPLE));
			}
			
			if (e.getItem() == Items.CHORUS_FRUIT) {
				e.getEntity().getDatas().removeIf(item -> item.getItem() == Items.CHORUS_FRUIT);
				e.getEntity().getDatas().add(new ItemData(25_000, Items.CHORUS_FRUIT));
			}
		}
		
		if (event instanceof EventUsePearl e) {
			e.getThrower().getDatas().removeIf(item -> item.getItem() == Items.ENDER_PEARL);
			e.getThrower().getDatas().add(new ItemData(60_000, Items.ENDER_PEARL));
		}
	}
	
}
