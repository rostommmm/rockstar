package fun.rockstarity.client.modules.render;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.connection.globals.ClientAPI;
import fun.rockstarity.api.connection.globals.GlobalsColors;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import javafx.animation.Interpolator;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 7 дек. 2023 г.
 */

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@Info(name = "Arrows", desc = "Отображает стрелочки до игроков", type = Category.RENDER)
public class Arrows extends Module {

	InfinityAnimation radius = new InfinityAnimation();
	InfinityAnimation angle = new InfinityAnimation();

	Mode mode = new Mode(this, "Мод");

	Mode.Element arrows = new Mode.Element(mode, "Стрелки");
	Mode.Element tracer = new Mode.Element(mode, "Линии");

	Slider size = new Slider(this, "Размер").min(0.6f).max(1f).inc(0.05f).set(0.7f)
			.hide(() -> this.mode.is(this.tracer));
	Slider dist = new Slider(this, "Растояние").min(5f).max(150).inc(1f).set(10)
			.hide(() -> this.mode.is(this.tracer));

	Select targets = new Select(this, "Сущности").min(1);

	Select.Element friend = new Select.Element(targets, "Друзей").set(true);
	Select.Element players = new Select.Element(targets, "Игроков").set(true);
	Select.Element nakedPlayers = new Select.Element(targets, "Голых игроков").hide(() -> !players.get());
	Select.Element mobs = new Select.Element(targets, "Мобов");
	Select.Element bot = new Select.Element(targets, "Ботов");
	Select.Element items = new Select.Element(targets, "Предметы");
	Select.Element backward = new Select.Element(targets, "За спиной").set(true);
	
	CheckBox animations = new CheckBox(this, "Анимации").set(true);
	CheckBox thirdperson = new CheckBox(this, "Игнорировать 3-е лицо");

	Slider width = new Slider(this, "Ширина линий").min(0.50f).max(5).inc(0.5f).set(0.50f)
			.hide(() -> !this.mode.is(this.tracer));

	Set<Entity> entities = new HashSet<>();

	@Override
	@EventType({ EventRender2D.class, EventRender3D.class, EventUpdate.class})
	public void onEvent(Event event) {

		if (event instanceof EventUpdate) {
	        if (mode.is(arrows) || mode.is(tracer)) {
	            entities.addAll(mc.world.getAllEntities().stream().filter(this::isValid).collect(Collectors.toSet()));
	        }
		}
		
		if (this.mode.is(this.arrows)) {
			if (event instanceof EventRender2D e) { // Проверяем, является ли Event экземпляром EventRender2D

				float size = 0, xOffset = mc.getMainWindow().getScaledWidth() / 2F - 24.5F,
						yOffset = mc.getMainWindow().getScaledHeight() / 2F - 25.2F;

				float sizeValue = 0; // Переменная размера стрелок
				if (Move.isMoving())
					sizeValue += Move.getSpeed() * 100;
				if (mc.player.isSneaking())
					sizeValue -= 20;
				if ((mc.currentScreen instanceof ContainerScreen || mc.currentScreen instanceof IngameMenuScreen)
						&& this.dist.get() < 86) // Увеличиваем размер стрелок, если контейнер открыт
					sizeValue += 110;

				// Анимируем угол вращения игрока
				float shortestYawPath = (float) (((((mc.player.rotationYaw - angle.get()) % 360) + 540) % 360) - 180);
				angle.animate(mc.player.rotationYaw + shortestYawPath, 100);

				sizeValue += Math.abs(mc.player.rotationYaw - angle.get()) / 10;

				// Анимируем радиус
				radius.animate(animations.get() ? sizeValue : 0, 100 );

				size += radius.get() + this.dist.get();

				GL11.glPushMatrix();

				float value = Math.max(0.1f, mc.player.rotationPitch / 90);

				Entity toRemove = null;

				for (Entity ent : this.entities) {
					ent.getArrowHide().setForward(mc.world.getAllEntities().contains(ent) && (!thirdperson.get() || mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON) && this.isValid(ent));
					ent.getArrowHide().setSpeed(animations.get() ? 300 : 1);
					
					if (ent.getArrowHide().finished(false) && !ent.getArrowHide().isForward())
						toRemove = ent;
					
					if (ent.ticksExisted < 10 && !ent.getArrowHide().isForward())
						continue;

					// Chat.debug(ent.getDisplayName().getString());
					GlStateManager.pushMatrix();
					GlStateManager.disableBlend();

					// Рассчитываем координаты игроков
					double x = ent.lastTickPosX + (ent.getPosX() - ent.lastTickPosX) * mc.getRenderPartialTicks()
							- mc.getRenderManager().info.getProjectedView().getX();
					double z = ent.lastTickPosZ + (ent.getPosZ() - ent.lastTickPosZ) * mc.getRenderPartialTicks()
							- mc.getRenderManager().info.getProjectedView().getZ();

					int i = 2;
					double cos = Math.cos(mc.player.rotationYaw * (Math.PI * i / 360));
					double sizes = 0.8;
					double sin = Math.sin(mc.player.rotationYaw * (Math.PI * i / 360));
					double rotY = -(z * cos - x * sin);
					double rotX = -(x * cos + z * sin);
					float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI)
							+ (mc.player.rotationYaw - this.angle.get());
					double valX = (sizes + size) * Math.cos(Math.toRadians(angle));
					double valY = (sizes + size) * Math.sin(Math.toRadians(angle));
					double circleX = mc.currentScreen instanceof ContainerScreen con
									? MathHelper.clamp(valX, -con.xSize/2F+20, con.xSize/2F-20)
									: valX;
					double circleY = mc.currentScreen instanceof ContainerScreen con
									? MathHelper.clamp(valY, -con.ySize/2F+20, con.ySize/2F-20)
									: valY;
					double xPos = xOffset + 50 / i + circleX;
					double y = yOffset + 50 / i + circleY;
					GlStateManager.translated(xPos, y, 0);

					// Поворачиваем игрока по углу
					GlStateManager.rotatef(angle, 0, 0, 1);
					GlStateManager.disableBlend();
					GlStateManager.translatef(51, 0, 0);
					// Изменяем скейл стрелочки
					GlStateManager.scaled(2.f, 2.f, 2.f);
					GlStateManager.rotatef(90, 0, 0, 1);
					GlStateManager.translatef(-5, 0, 0);
					Render.drawImage(e.getMatrixStack(), "masks/arrow.png", 0, 0, 0, this.size.get() * 15, this.size.get() * 15, getColor(ent, angle).alpha(ent.getArrowHide().get()));
					GlStateManager.popMatrix();
				}

				if (toRemove != null)
					this.entities.remove(toRemove);

				GL11.glPopMatrix();
			}
		} else {
			if (event instanceof EventRender3D e) {

				GL11.glRotated(e.getRenderInfo().getPitch(), 1.0, 0.0, 0.0);
				GL11.glRotated(e.getRenderInfo().getYaw() + 180, 0.0, 1.0, 0.0);
				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glLineWidth(this.width.get());
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glDepthMask(false);
				GL11.glEnable(GL11.GL_LINE_SMOOTH);
				GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
				Vector3d vec = new Vector3d(0, 0, 150).rotatePitch((float) -(Math.toRadians(mc.player.rotationPitch)))
						.rotateYaw((float) -Math.toRadians(mc.player.rotationYaw));
				double partialTicks = mc.getRenderPartialTicks();
				Vector3d projectedView = mc.getRenderManager().info.getProjectedView();
				Entity toRemove = null;
				for (Entity entity : this.entities) {
					entity.getArrowHide().setForward(mc.world.getAllEntities().contains(entity) && (!thirdperson.get() || mc.getGameSettings().getPointOfView() == PointOfView.FIRST_PERSON) && this.isValid(entity));
						entity.getArrowHide()
								.setForward(mc.world.getAllEntities().contains(entity) ? this.isValid(entity) : false);
						if (entity.ticksExisted < 10 && !entity.getArrowHide().isForward())
							continue;
						FixColor color = rock.getFriendsHandler().isFriend(entity.getName().getString())
								? FixColor.GREEN
								: (rock.getTargetHandler().isTarget(entity.getName().getString())
								? FixColor.RED
								: FixColor.WHITE);
						double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * partialTicks - projectedView.getX();
					    double y = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * partialTicks - projectedView.getY();
					    double z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * partialTicks - projectedView.getZ();
						Render.setColor(color.alpha(entity.getArrowHide().get()).getRGB());
						BUILDER.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
						BUILDER.pos(vec.x, vec.y, vec.z).endVertex();
						BUILDER.pos(x, y, z).endVertex();
						TESSELLATOR.draw();
						if (entity.getArrowHide().finished(false) && !entity.getArrowHide().isForward())
							toRemove = entity;

				}
				GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
				GL11.glDisable(GL11.GL_LINE_SMOOTH);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glDepthMask(true);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_CULL_FACE);
				if (toRemove != null)
					this.entities.remove(toRemove);
				GL11.glPopMatrix();
			}

			if (event instanceof EventRender3D e) {
				if (this.mode.is(this.tracer)) {
					GL11.glRotated(e.getRenderInfo().getYaw() + 180, 0.0, -1.0, 0.0);
					GL11.glRotated(e.getRenderInfo().getPitch(), -1.0, 0.0, 0.0);
				}
			}
		}
	}

	private FixColor getColor(Entity entity, float angle) {
		if (rock.getFriendsHandler().isFriend(entity))
			return FixColor.GREEN;
		if (rock.getTargetHandler().isTarget(entity))
			return FixColor.RED;
		String client = ClientAPI.getClient(entity.getName().getString());

		if (client != null)
			return GlobalsColors.getColor(client);

		return Style.getPoint((int) angle);
	}

	private boolean isValid(Entity entity) {
		if (entity == mc.player || entity instanceof ClientPlayerEntity)
			return false;

		if (this.backward.get() || PositionTracker.isInView(entity)) {
			if (entity instanceof ItemEntity && this.items.get())
				return true;
			if ((entity instanceof MobEntity || entity instanceof AnimalEntity) && this.mobs.get())
				return true;
			if (entity instanceof PlayerEntity player && this.players.get() && (player.getTotalArmorValue() != 0 || nakedPlayers.get()))
				return true;
			if (entity instanceof PlayerEntity player && rock.getFriendsHandler().isFriend(player) && friend.get()) return true;
			
			if (entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(entity.getName().getString())) && bot.get()) return true;
		}

		return false;
	}
	
	@Override
	public void onDisable() {
		entities.clear();
	}

	@Override
	public void onEnable() {
		
	}
}
