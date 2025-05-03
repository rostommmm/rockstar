package fun.rockstarity.client.modules.render;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.Rockstar;
import fun.rockstarity.api.connection.globals.ClientAPI;
import fun.rockstarity.api.connection.globals.GlobalsColors;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventPickupItem;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.entity.EventRenderTag;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.friends.Friend;
import fun.rockstarity.api.helpers.game.ItemUtility;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.ColorUtility;
import fun.rockstarity.api.helpers.render.NameTagsRender;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Clickable;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.other.NameProtect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

// TODO почистить это все дело
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="NameTags", desc="Теги, отображающие информацию о сущностях", type=Category.RENDER)
public class NameTags extends Module {
	
	@Getter @Setter @NonFinal boolean visible;
	@NonFinal String name;
	@Getter
	Map<LivingEntity, Vector2f> playerTags = new HashMap<>();
	Map<Entity, Vector2f> itemTags = new HashMap<>();
	Map<LivingEntity, Vector2f> villagerTag = new HashMap<>();
	
	Set<UUID> valuablePlayers = new HashSet<>();
	Map<UUID, List<ItemStack>> collectedItemsMap = new HashMap<>();
	
	Select targets = new Select(this, "Отображать").min(1).hide(() -> visible);
	Element shulkers = new Select.Element(targets, "Шалкера");
	Element villagers = new Select.Element(targets, "Жителей");
	Element players = new Select.Element(targets, "Игроков").set(true);
	Element noSelf = new Element(targets, "Себя").hide(() -> !players.get());
	Element noBots = new Element(targets, "Ботов").hide(() -> !players.get());
	Element noNaked = new Element(targets, "Голых").hide(() -> !players.get());
	Element items = new Select.Element(targets, "Предметы");
	Element trash = new Element(targets, "Бесценные").hide(() -> !items.get()).set(true);
	
	CheckBox optimized = new CheckBox(this, "Оптимизированный").desc("Будет использоваться оптимизированный вариант").hide(() -> visible);
	CheckBox health = new CheckBox(this, "Здоровье").desc("Отображает единицы здоровья игроков").hide(() -> !players.get() || visible);
	CheckBox fon = new CheckBox(this, "Фон").hide(() -> !items.get() || visible);
	
	Clickable addFriend = new Clickable(this, "Добавить в друзья")
		    .set(() -> {
				if (name == null){
					return;
				}
		        if (rock.getFriendsHandler().isFriend(name)) {
		            rock.getFriendsHandler().remove(name);
		        } else {
		            rock.getFriendsHandler().add(name);
		        }
		    })
		    .hide(() -> !visible);
	Clickable addTarget = new Clickable(this, "Добавить в цели")
		    .set(() -> {
				if (name == null){
					return;
				}
		        if (rock.getTargetHandler().isTarget(name)) {
		            rock.getTargetHandler().remove(name);
		        } else {
		            rock.getTargetHandler().add(name);
		        }
		    })
		    .hide(() -> !visible);
	Clickable copy = new Clickable(this, "Скопировать").set(() -> TextUtility.copyText(name)).hide(() -> !visible);
	
	@NonFinal long lastClickTime = 0;
	
	@Getter
	NameTagsRender window = new NameTagsRender(this);
	
	
	@Override
	@EventType({EventRender3D.class, EventRender2D.class, EventRenderTag.class, EventPickupItem.class})
	public void onEvent(Event event) {
		if (event instanceof EventRender3D) {
			this.updatePlayers();
			this.updateItems();
			this.updateVillagers();
		}
		
		if (event instanceof EventWorldChange) {
			valuablePlayers.clear();
			collectedItemsMap.clear();
			villagerTag.clear();
			playerTags.clear();
			itemTags.clear();
		}
		
		if (event instanceof EventBlur e && rock.getModules().get(Interface.class).getBlur().get() && !optimized.get()) {
			for (Entry<LivingEntity, Vector2f> tag : this.playerTags.entrySet()) 
				this.renderPlayerTag(e, tag.getKey(), tag.getValue());
			
			for (Entry<Entity, Vector2f> tag : itemTags.entrySet()) {
				renderItemTag(e, tag.getKey(), tag.getValue());
			}

			for (Entry<LivingEntity, Vector2f> tag : villagerTag.entrySet()) {
				renderPlayerTag(e, tag.getKey(), tag.getValue());
			}
		}
		
		if (event instanceof EventRender2D e) {
			window.getOpening().setForward(mc.currentScreen instanceof ChatScreen && visible);
			
	    	if (!window.getOpening().finished(false) && !window.getOpening().isForward()) {
	    		window.render(e.getMatrixStack(), 0, 0, 0);
	    	}
			
			for (Entry<LivingEntity, Vector2f> tag : this.playerTags.entrySet()) 
				this.renderPlayerTag(e, tag.getKey(), tag.getValue());
		
			for (Entry<Entity, Vector2f> tag : this.itemTags.entrySet()) 
				this.renderItemTag(e, tag.getKey(), tag.getValue());

			for (Entry<LivingEntity, Vector2f> tag : villagerTag.entrySet()) {
				renderPlayerTag(e, tag.getKey(), tag.getValue());
			}
		}
		
		if (event instanceof EventRenderTag e) {
			Entity ent = e.getEntity();
			
			if (ent instanceof PlayerEntity player) {
				if (players.get()) {
					if (!noNaked.get() && isNaked(player)) {
						return;
					}
					e.cancel();
				}
			}
			
		}
		
		if (event instanceof EventUpdate && visible && name == null) {
			visible = false;
			window.getOpening().setForward(false).finish();
		}
	}
	
    private void renderPlayerTag(Event event, LivingEntity entity, Vector2f tag) {
		if (mc.world == null) return;

		if (event instanceof EventRender2D || event instanceof EventBlur) {
			// Получаем анимацию скрытия тегов
			Animation anim = entity.getTagsHide();
			anim.setForward(mc.world.getAllEntities().contains(entity));

			float alpha = anim.get();

			// Определяем, будет ли отображаться текст
			boolean blur = event instanceof EventBlur;
			MatrixStack ms = blur ? ((EventBlur) event).getMatrixStack() : ((EventRender2D) event).getMatrixStack();

			FontSize font = optimized.get() ? semibold.get(13) : bold.get(14);
			NameProtect nameProtect = rock.getModules().get(NameProtect.class);
			ITextComponent name = entity.getDisplayName();
			
			// Френды
			Friend friend = Rockstar.getInstance().getFriendsHandler().get(entity);
			boolean isFriend = friend != null && nameProtect.get() && nameProtect.getFriend().get();
			
			if (isFriend)
				name = new StringTextComponent(friend.getHiddenName(nameProtect.getNickFriend().get()));
			
			// Селф
			boolean isSelf = entity == mc.player && nameProtect.get() && nameProtect.getSelf().get();
			
			if (isSelf)
				name = new StringTextComponent(nameProtect.getNick().get());
			
			// Отхер
			boolean isOther = nameProtect.get() && nameProtect.getAll().get();
			
			if (isOther)
				name = new StringTextComponent(nameProtect.getNickAll().get());
			
			// Бля я этот неймпротект ебаный пишу под кис-кис в 3:38 пизда
			
			// Другая хуйня
			String client = ClientAPI.getClient(entity.getName().getString());

			String healthStr = health.get() ? " " + (int) getHealth(entity) : "";

			float width = font.getWidth((name)) + 6 + (client != null ? 11 : 0) + font.getWidth(healthStr);

			// Рендеринг прямоугольника для коллекции элементов
			renderCollectedItemsRect(ms, entity, tag, alpha);

			// Определяем стиль отрисовки (оптимизированный или не оптимизированный)
			if (optimized.get()) {
				Render.drawRect(ms, tag.x + 2 - width / 2, tag.y, width - 2, 11, this.getColor(entity).alpha(alpha));
			} else {
				Round.draw(ms, new Rect(tag.x + 1 - width / 2, tag.y, width, 11).size(-1), 2,
						(!blur && rock.getModules().get(Interface.class).getBlur().get() ? this.getColor(entity).alpha(0.2f) : this.getColor(entity)).alpha(alpha));
			}

			// Отображаем текстовое имя
			font.draw(ms, name, tag.x - width / 2 + 3 + (client != null ? 11 : 0), tag.y + 0.5f, (rock.getModules().get(Interface.class).getBlur().get() ? FixColor.WHITE : rock.getThemes().getTextFirstColor()).alpha(alpha));

			if (health.get()) {
				font.draw(ms, healthStr, tag.x - width / 2 + 3 + font.getWidth(name) + (client != null ? 11 : 0), tag.y + 0.5f, getHealthColor(entity).alpha(alpha));
			}

			// Отображаем иконку клиента, если она есть
			if (client != null) {
				String clientImageUrl = "https://rockstar.moscow/api/globals/" + client + ".png";
				if (optimized.get()) {
					Render.image(clientImageUrl, tag.x - width / 2 + 2, tag.y, 11, 11, FixColor.WHITE.alpha(alpha));
				} else {
					Round.drawTextured(ms, clientImageUrl, new Rect(tag.x - width / 2F - 1, tag.y - 1, 13, 13), 2, alpha);
				}
			}

			// Отображаем элементы в шелкере (если есть)
			this.drawShulker(ms, entity, tag, ItemUtility.getItemsInShulker(entity.getHeldItemMainhand()), alpha);
			this.drawShulker(ms, entity, tag, ItemUtility.getItemsInShulker(entity.getHeldItemOffhand()), alpha);
		}
	}
	
	private boolean drawShulker(MatrixStack ms, Entity entity, Vector2f tag, List<ItemStack> items, float alpha) {
		if (!items.isEmpty()) {
			float shulkerWidth = 96;
			int x = 0;
			int y = 0;
			
			if (items.size() == ItemUtility.getItemsInShulker(((LivingEntity) entity).getHeldItemOffhand()).size()) {
				y = -4;
			}

			Round.draw(ms, new Rect(tag.x - shulkerWidth / 2, tag.y - 38 + y * 10, shulkerWidth, 36), 2, this.getColor((LivingEntity) entity).alpha(alpha));
			
			for (ItemStack item : items) {
				Render.drawStack(item, tag.x - shulkerWidth / 2 + 3 + x * 10, tag.y + y * 10 - 35);
				x++;
				if (x > 8) {
					x = 0;
					y++;
				}
			}
			return true;
		}
		return false;
	}
	
	private void renderItemTag(Event event, Entity entity, Vector2f tag) {
		if (event instanceof EventRender2D || event instanceof EventBlur) {
			Animation anim = entity.getTagsHide();
	    	anim.setForward(mc.world.getAllEntities().contains(entity));
	    	
	    	float alpha = anim.get();
	    	boolean blur = event instanceof EventBlur;
	    	MatrixStack ms = blur ? ((EventBlur) event).getMatrixStack() : ((EventRender2D) event).getMatrixStack();
			FontSize font = this.bold.get(14);
			String name = entity.getDisplayName().getString().trim();
			float width = font.getWidth(entity.getDisplayName()) + 4;
			ItemEntity ent = (ItemEntity) entity;
			
			if (fon.get()) {
				if (optimized.get()) {
					Render.drawRect(ms, tag.x - font.getWidth(ent.getItem().getDisplayName().getString() + " x" + ent.getItem().getCount())
							/ 2 - 2.5f, tag.y, font.getWidth(ent.getItem().getDisplayName().getString() + " x" +
							ent.getItem().getCount()) + 5, 11, rock.getThemes().getSecondColor().alpha(alpha));
				} else {
					Round.draw(ms, new Rect(tag.x - font.getWidth(ent.getItem().getDisplayName().getString() + " x" + ent.getItem().getCount())
							/ 2 - 2.5f, tag.y, font.getWidth(ent.getItem().getDisplayName().getString() +
							" x" + ent.getItem().getCount()) + 5, 11).size(-1), 2F,
							!blur && rock.getModules().get(Interface.class).getBlur().get() ?
									rock.getThemes().getSecondColor().alpha(0.2f) : rock.getThemes().getSecondColor().alpha(alpha));
				}
			}

			FixColor color = ItemUtility.isRare(ent.getItem()) && trash.get() ? Style.getMain() : FixColor.WHITE;
			if (ent.getName().getString().equals("Player Head") ||
					ent.getName().getString().equals("Голова Игрока")) color = FixColor.CYAN;
			if (ent.getName().getString().equals("Totem of Undying") ||
					ent.getName().getString().equals("Тотем бессмертия")) color = FixColor.YELLOW;
			if (ent.getName().getString().equals("Elytra") ||
					ent.getName().getString().equals("Элитры")) color = FixColor.GRAY;
			if (ent.getName().getString().equals("Enchanted Golden Apple") ||
					ent.getName().getString().equals("Зачарованное золотое яблоко") ||
					ent.getName().getString().equals("Golden Apple") ||
					ent.getName().getString().equals("Золотое яблоко")) color = FixColor.ORANGE;
			
			font.draw(ms, ent.getItem().getDisplayName().getString(), 
					tag.x - font.getWidth(ent.getItem().getDisplayName().getString() + " x" + ent.getItem().getCount()) / 2, 
					tag.y + 0.5f, 
					color.alpha(alpha)
			);
			
			font.draw(ms, " x" + ent.getItem().getCount(), 
					tag.x - font.getWidth(ent.getItem().getDisplayName().getString() + " x" + ent.getItem().getCount()) / 2 + font.getWidth(ent.getItem().getDisplayName().getString()), 
					tag.y + 0.5f,
					color.alpha(alpha)
			);
			
			if (ent.getItem().getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock) {
				List<ItemStack> items = ItemUtility.getItemsInShulker(ent.getItem());
				if (!items.isEmpty()) {
					float boxWidth = 96;
		            float boxX = tag.x - boxWidth / 2;
		            float boxY = tag.y - 38;
		            Round.draw(ms,new Rect(boxX, boxY, boxWidth, 36),2,rock.getThemes().getSecondColor().alpha(0.8f));
		            int x = 0, y = 0;
		            for (ItemStack item : items) {
		                Render.drawStack(item, boxX + 3 + x * 10, boxY + 3 + y * 10);
		                x++;
		                if (x > 8) {
		                    x = 0;
		                    y++;
		                }
		            }
				}
			}
		}
	}

	private void updateItems() {
	    Entity toRemove = null;
	    for (Entity entity : itemTags.keySet()) {
	        if (entity.getTagsHide().finished(false) || !PositionTracker.isInView(entity))
	            toRemove = entity;
	    }
	    if (toRemove != null) {
	        itemTags.remove(toRemove);
	    }
	    itemTags.clear();

	    float pTicks = mc.timer.renderPartialTicks;

	    if (items.get() || shulkers.get()) {
	        for (Entity e : mc.world.getAllEntities()) {
	            if (!(e instanceof ItemEntity)) continue;

	            ItemEntity itemEntity = (ItemEntity) e;
	            ItemStack stack = itemEntity.getItem();

	            boolean isShulker = stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
	            boolean shouldRender = (items.get() && (trash.get() || stack.getDisplayName().getString().contains("★")))
	                                   || (shulkers.get() && isShulker);

	            if (!shouldRender) continue;

	            double x = e.lastTickPosX + (e.getPosX() - e.lastTickPosX) * (double) pTicks;
	            double y = e.lastTickPosY + (e.getPosY() - e.lastTickPosY) * (double) pTicks + e.getHeight() + 0.5f;
	            double z = e.lastTickPosZ + (e.getPosZ() - e.lastTickPosZ) * (double) pTicks;

	            double[] pos = Render.worldToScreen(x, y, z);
	            if (pos == null) continue;

	            if (PositionTracker.isInView(e)) {
	                itemTags.put(e, new Vector2f((float) pos[0], (float) pos[1]));
	            }
	        }
	    }
	}

	private void updatePlayers() {
		Entity toRemove = null;
        for (Entity entity : playerTags.keySet()) {
        	if (entity.getTagsHide().finished(false) || !PositionTracker.isInView(entity) || mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON && entity == mc.player)
        		toRemove = entity;
        }
        if (toRemove != null) {
        	playerTags.remove(toRemove);
        }
        playerTags.clear();

        float pTicks = mc.timer.renderPartialTicks;
        
        if (this.players.get())
	        for (PlayerEntity e : mc.world.getPlayers()) {
	            if ((mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON && e == mc.player)
	            		|| (!noSelf.get() && e == mc.player)
	            		|| (!noBots.get() && !e.getUniqueID().equals(PlayerEntity.getOfflineUUID(e.getName().getString())))) continue;

	            boolean[] naked = { true };
	            
	            e.getArmorInventoryList().forEach(a -> {
	            	if (a.getItem() != Items.AIR)
	            		naked[0] = false;
	            });
	            
	            if (!noNaked.get() && naked[0]) continue;
	            
	            double x = e.lastTickPosX + (e.getPosX() - e.lastTickPosX) * (double) pTicks;
	            double y = e.lastTickPosY + (e.getPosY() - e.lastTickPosY) * (double) pTicks + e.getHeight() + 0.2f;
	            double z = e.lastTickPosZ + (e.getPosZ() - e.lastTickPosZ) * (double) pTicks;
	            
	            double[] pos = Render.worldToScreen(x, y, z);
	            if (pos == null) continue;
	            
	            if (PositionTracker.isInView(e)) {
	            	playerTags.put(e, new Vector2f((float) pos[0], (float) pos[1] - 12));
	            	e.getTagsHide().setSpeed(300);
	            }
	        }
    }

	private void updateVillagers() {
		Entity toRemove = null;
		for (Entity entity : villagerTag.keySet()) {
			if (entity.getTagsHide().finished(false) ||
					!PositionTracker.isInView(entity) ||
					mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON)
				toRemove = entity;
		}

		if (toRemove != null) {
			villagerTag.remove(toRemove);
		}

		villagerTag.clear();

		float pTicks = mc.timer.renderPartialTicks;

		if (this.villagers.get())
			for (Entity e : mc.world.getAllEntities()) {
				if (!(e instanceof VillagerEntity)) continue;

				if ((mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON)
						|| (!noSelf.get())
						|| (!noBots.get() && !e.getUniqueID().equals(PlayerEntity.getOfflineUUID(e.getName().getString())))) continue;

				double x = e.lastTickPosX + (e.getPosX() - e.lastTickPosX) * (double) pTicks;
				double y = e.lastTickPosY + (e.getPosY() - e.lastTickPosY) * (double) pTicks + e.getHeight() + 0.2f;
				double z = e.lastTickPosZ + (e.getPosZ() - e.lastTickPosZ) * (double) pTicks;

				double[] pos = Render.worldToScreen(x, y, z);
				if (pos == null) continue;

				if (PositionTracker.isInView(e)) {
					villagerTag.put((LivingEntity) e, new Vector2f((float) pos[0], (float) pos[1] - 12));
					e.getTagsHide().setSpeed(300);
				}
			}
	}
	
	private FixColor getHealthColor(LivingEntity target) {
		FixColor color = FixColor.GREEN;
		float health = getHealth(target);
		
		if (health > 10) {
			color = FixColor.ORANGE.move(FixColor.GREEN, (health-10)/10F);
		} else {
			color = FixColor.RED.move(FixColor.ORANGE, health/10F);
		}
		
		return color;
	}
	
	private float getHealth(LivingEntity target) {
		return Server.isServerForHPFix() ? target.getRealHealth() : (target.getHealth()+target.getAbsorptionAmount());
	}
	
	private FixColor getColor(LivingEntity entity) {
		if (rock.getTargetHandler().isTarget(entity)) return rock.getThemes().getFirstColor().move(FixColor.RED, 0.3f).alpha(1);
		
		if (rock.getFriendsHandler().isFriend(entity))
			return rock.getThemes().getFirstColor().move(FixColor.GREEN, Interface.blur() ? 0.5f : 0.2f).alpha(1);
		
		String client = ClientAPI.getClient(entity.getName().getString());
		
		if (client != null)
			return rock.getThemes().getFirstColor().move(GlobalsColors.getColor(client), 0.2f).alpha(1);
		
		return rock.getThemes().getSecondColor().alpha(optimized.get() ? 0.6f : rock.getModules().get(Interface.class).getBlur().get() ? 1 : 0.89);
	}
	
	public void mouseClicked(double mouseX, double mouseY, int button) {
		long currentTime = System.currentTimeMillis();
		for (Entry<LivingEntity, Vector2f> tag : this.playerTags.entrySet()) {
			String client = ClientAPI.getClient(tag.getKey().getName().getString());
			float width = this.bold.get(14).getWidth(tag.getKey().getDisplayName()) + 6 + (client != null ? 11 : 0);
			
			if (tag.getKey() != mc.player && Hover.isHovered(tag.getValue().x - width / 2 + 3 + (client != null ? 11 : 0), tag.getValue().y, width - 5, 11, mouseX, mouseY)) {
				visible = button == 1;
				name = tag.getKey().getName().getString();
				
				if (currentTime - lastClickTime < 300 && button == 0) {
					TextUtility.copyText(name);
					lastClickTime = 0;
					return;
				} else {
					lastClickTime = currentTime;
				}
			}
		}
	}
	
	private boolean isNaked(PlayerEntity player) {
		return player.inventory.armorInventory.stream().allMatch(ItemStack::isEmpty);
	}
	
	private void renderCollectedItemsRect(MatrixStack ms, LivingEntity entity, Vector2f tag, float alpha) {
		List<ItemStack> collectedItems = getCollectedItems(entity);
		
		if (!collectedItems.isEmpty()) {
	        int itemSize = 16;
	        int padding = 2;
	        int rectWidth = itemSize * Math.min(collectedItems.size(), 5) + padding * 2;
	        int rectHeight = itemSize + padding * 2;
	        
	        float rectX = tag.x - rectWidth / 2;
	        float rectY = tag.y - rectHeight - 1;

	        Render.drawRect(ms, rectX, rectY, rectWidth - 6, rectHeight - 5, rock.getThemes().getFirstColor().alpha(0.8f * alpha));

	        int x = 0;
	        for (ItemStack item : collectedItems) {
	            Render.drawStack(item, rectX + padding + x * itemSize, rectY + padding);
	            x++;
	            if (x >= 5) break;
	        }
		}
	}
    
    private List<ItemStack> getCollectedItems(LivingEntity entity) {
    	if (entity instanceof PlayerEntity) {
    		return collectedItemsMap.getOrDefault(entity.getUniqueID(), new ArrayList<>());
    	}
    	
    	return Collections.emptyList();
    }
	
	@AllArgsConstructor
	public static class Tag {
		@Getter
    	private Entity entity;
    	public float x, y;
    }
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
}