package fun.rockstarity.client.modules.player;

import java.util.UUID;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.player.EventRotationChange;
import fun.rockstarity.api.events.list.render.world.EventCameraDistance;
import fun.rockstarity.api.events.list.render.world.EventCameraPosition;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Move;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.themes.Style;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.GameType;

/**
 * @author Malecharik
 * Симуляция by @author ConeTin
 * @since 14 Mar 2024 20:49:30
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name="FreeCam", desc="Свободная камера", type=Category.PLAYER)
public class FreeCam extends Module {
	
	final Mode mode = new Mode(this, "Режим");
	final Mode.Element classic = new Mode.Element(mode, "Обычный");
	final Mode.Element simulation = new Mode.Element(mode, "Симуляция");
	
	final Slider speedXZ = new Slider(this, "Скорость по XZ").min(0.1f).max(5).inc(0.5f).set(0.5f);
	final Slider speedY = new Slider(this, "Скорость по Y").min(0.1f).max(5).inc(0.5f).set(0.5f);
	
	final CheckBox display = new CheckBox(this, "Отображать координаты").set(true);
	
	final Slider smooth = new Slider(this, "Плавность камеры").min(1).max(250).inc(25).set(50).hide(() -> !mode.is(simulation));
	
	final Mode control = new Mode(this, "Режим управления").hide(() -> !mode.is(simulation)).desc("Режим управления игроком во время того, как вы находитесь в свободной камере");

	final Mode.Element without = new Mode.Element(control, "Нету");
	final Mode.Element lastButton = new Mode.Element(control, "Последняя клавиша");
	final Mode.Element manual = new Mode.Element(control, "Ручное");
	final Mode.Element dota = new Mode.Element(control, "Dota 2");
	
	boolean manualControl() {
		return !mode.is(simulation) || !control.is(manual);
	}
	
	final CheckBox freeze = new CheckBox(this, "Зависать").hide(() -> !mode.is(simulation) || !control.is(without)).set(true).desc("Замораживает игрока на одной позиции");
	
	final InputBinding forwardBind = new InputBinding(this, "Вперёд").hide(() -> manualControl()).addBind(new Bind(265));
	final InputBinding backBind = new InputBinding(this, "Назад").hide(() -> manualControl()).addBind(new Bind(264));
	final InputBinding leftBind = new InputBinding(this, "Влево").hide(() -> manualControl()).addBind(new Bind(263));
	final InputBinding rightBind = new InputBinding(this, "Вправо").hide(() -> manualControl()).addBind(new Bind(262));
	final InputBinding jumpBind = new InputBinding(this, "Прыжок").hide(() -> manualControl());
	final InputBinding sneakBind = new InputBinding(this, "Присяд").hide(() -> manualControl());
	final CheckBox rawInput = new CheckBox(this, "Ручной ввод").hide(() -> manualControl()).desc("Вы можете забиндить эту настройку по желанию. При её включении вы будете управлять игроком, а не свободной камерой");
	
    float x,y,z;
    GameType prev;
    
    // Для симуляции
    InfinityAnimation xAnim = new InfinityAnimation();
    InfinityAnimation yAnim = new InfinityAnimation();
    InfinityAnimation zAnim = new InfinityAnimation();
    float forward, strafe, yaw, pitch;
    boolean jump, sneak;
    PointOfView previous;
    Vector3d to = Vector3d.ZERO; // Для Dota 2
    
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender2D e && this.display.get()) {
			GL11.glBlendFunc(770, 771);
			BlockPos diff = mc.player.getPosition().subtract(new Vector3i(x,y,z));
			if (mode.is(simulation)) {
				diff = new BlockPos(x,y,z).subtract(mc.player.getPosition());
			}
			String pos = "X: " + diff.getX() + " Y: " + diff.getY() + " Z: " + diff.getZ();
			bold.get(16).draw(e.getMatrixStack(), pos, sr.getScaledWidth() / 2f - bold.get(16).getWidth(pos) / 2F, sr.getScaledHeight() / 2f - 20.0f, rock.getThemes().getTextFirstColor().alpha(0.5f));
		}
		
		if (mode.is(classic)) {
			// Отмена отправки определенных пакетов
			if (event instanceof EventSendPacket e) {
				IPacket<?> packet = e.getPacket();
				
				if (packet instanceof CPlayerPacket || packet instanceof CPlayerAbilitiesPacket) event.cancel();
			}
			
			if (event instanceof EventReceivePacket e) {
				IPacket<?> packet = e.getPacket();
				
				if (packet instanceof SPlayerPositionLookPacket) {
					event.cancel();
				}
			}
			
			if (event instanceof EventMotion) {
				if (!mc.player.isSneaking() && mc.getGameSettings().keyBindJump.isKeyDown()) { // Проверяем, зажат ли прыжок и не зажат шифт у игрока
					mc.player.getMotion().y = this.speedY.get(); // Устанавливаем движение по Y
				} else if (mc.getGameSettings().keyBindSneak.isKeyDown()) { // Проверяем, зажат ли шифт у игрока 
					mc.player.getMotion().y = -this.speedY.get(); // Отрицательное значение Y для движения вниз
				} else {
					mc.player.setMotion(0.0, 0.0, 0.0);
				}
				Move.setSpeed(this.speedXZ.get());
			}
		} else if (mode.is(simulation)) {
			// Изменяем камеру :3
			if (event instanceof EventCameraPosition e) {
				int speed = (int) smooth.get();
				e.setPosition(new Vector3d(
						xAnim.animate(x, speed),
						yAnim.animate(y, speed),
						zAnim.animate(z, speed)
				));
				e.setRotation(new Vector2f(yaw, pitch));
				mc.getGameSettings().setPointOfView(PointOfView.THIRD_PERSON_BACK);
			}
			
			if (event instanceof EventCameraDistance e) {
				e.setDistance(0);
				e.cancel();
			}
			
			// Слушаем изменение камеры
			if (event instanceof EventRotationChange e) {
				if (rawInput.get()) return;
				
				yaw += e.getYaw();
				pitch = (float) MathHelper.clamp(pitch + e.getPitch(), -90, 90);
				
				if (control.is(dota) && !to.equals(Vector3d.ZERO)) {
					Vector2f rotation = Rotation.get(to);
					
					e.setYaw(rotation.x-mc.player.rotationYaw);
					e.setPitch(mc.player.isElytraFlying() || mc.player.isSwimming() ? rotation.y-mc.player.rotationPitch : 0);
				} else {
					e.setYaw(0);
					e.setPitch(0);
				}
			} 
			
			// Слушаем движения игрока
			if (event instanceof EventInput e) {
				if (rawInput.get()) return;
				
				float speed = speedXZ.get();
				
				if (e.getForward() != 0 || e.getStrafe() != 0) {
					double direction = Move.direction(yaw+90, e.getForward(), e.getStrafe());
					float x = (float) Math.cos(direction);
					float z = (float) Math.sin(direction);
					x *= speed;
					z *= speed;
					this.x += x;
					this.z += z;
				}
				
				if (e.isJump()) {
					this.y += speedY.get();
				} else if (e.isSneak()) {
					this.y -= speedY.get();
				}
				
				if (control.is(manual) || control.is(lastButton)) {
					e.setForward(forward);
					e.setStrafe(strafe);
					e.setJump(jump);
					e.setSneak(sneak);
				} else if (control.is(dota)) {
					e.setForward(!to.equals(Vector3d.ZERO) ? 1 : 0);
					e.setStrafe(0);
					e.setJump(!to.equals(Vector3d.ZERO) && mc.player.getDistance(to) > 3 && !mc.player.isPotionActive(Effects.SPEED));
					e.setSneak(false);
				} else {
					e.setForward(0);
					e.setStrafe(0);
					e.setJump(false);
					e.setSneak(false);
				}
			}
			
			if (event instanceof EventKey e && control.is(manual)) {
				if (!empty(forwardBind)) forward = pressed(e, forwardBind) ? 1 : (!empty(backBind) && pressed(e, backBind) ? -1 : 0);
				if (!empty(leftBind)) strafe = pressed(e, leftBind) ? 1 : (!empty(rightBind) && pressed(e, rightBind) ? -1 : 0);
				if (!empty(jumpBind)) jump = pressed(e, jumpBind);
				if (!empty(sneakBind)) sneak = pressed(e, sneakBind);
			}
			
			// Для Dota 2 режима
			if (control.is(dota)) {
				if (event instanceof EventKey e && mc.currentScreen == null) {
					if (e.getKey() == 1 && e.getScancode() == 1) {
						to = MathUtility.rayTrace(150, yaw, pitch, mc.player, new Vector3d(x, y, z)).getHitVec();
					}
				}
				
				if (!to.equals(Vector3d.ZERO)) {
					if (event instanceof EventRender3D e) {
						MatrixStack ms = e.getMatrixStack();

						float size = 4;
		    			
		    			ms.push();
		    			GlStateManager.depthMask(false);
		    			Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();
		    			
		       			ms.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
		               	ms.translate(to.x, to.y-0.49f*size, to.z);
		               	ms.rotate(Vector3f.XP.rotationDegrees(90));
		               	
		       			Render.drawImage(ms, "masks/glow.png", (float) -size / 2, -size / 2, (float) -size / 2, size, size, Style.getPoint(10));
		       			GlStateManager.depthMask(true);
		                ms.pop();
					}
					
					if (event instanceof EventUpdate && mc.player.getDistance(to) < 1) {
						to = Vector3d.ZERO;
					}
				}
			}
			
			if (event instanceof EventMotionMove e && freeze.get()) {
				e.setMotion(Vector3d.ZERO);
			}
		}
		
		if (event instanceof EventWorldChange) {
			onDisable();
			this.set(false); // Выключаем модуль
		}
	}
	
	private boolean pressed(EventKey event, InputBinding binding) {
		return mc.currentScreen == null && binding.getBindByKey(event).isPresent() && (binding.pressed = !binding.pressed);
	}
	
	private boolean empty(InputBinding binding) {
		return binding.getBinds().isEmpty();
	}
	 
	@Override
	public void onEnable() {
        if (mode.is(classic)) {
        	this.prev = mc.playerController.getCurrentGameType();
            mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(GameType.SPECTATOR);
            this.x = (float)mc.player.getPosX();
            this.y = (float)mc.player.getPosY();
            this.z = (float)mc.player.getPosZ();
            RemoteClientPlayerEntity ent = new RemoteClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), mc.getSession().getUsername()));
            ent.inventory = mc.player.inventory;
            ent.setHealth(mc.player.getHealth());
            ent.setPositionAndRotation(this.x, mc.player.getBoundingBox().minY, this.z, mc.player.rotationYaw, mc.player.rotationPitch);
            ent.rotationYawHead = mc.player.rotationYawHead;
            mc.world.addEntity(-1337, ent);
            mc.player.abilities.isFlying = true;
        } else if (mode.is(simulation)) {
        	previous = mc.getGameSettings().getPointOfView();
        	mc.getGameSettings().setPointOfView(PointOfView.THIRD_PERSON_BACK);
        	x = (float)mc.player.getPosX();
            y = (float)mc.player.getPosY() + mc.player.getEyeHeight() + 2;
            z = (float)mc.player.getPosZ();
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            forward = mc.player.movementInput.moveForward;
            strafe = mc.player.movementInput.moveStrafe;
            jump = mc.player.movementInput.jump;
            sneak = mc.player.movementInput.sneaking;
            to = Vector3d.ZERO;
            if (control.is(dota)) {
            //	prev = mc.playerController.getCurrentGameType();
            //    mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(GameType.ADVENTURE);
            }
        }
	}
	
	@Override
	public void onDisable() {
		if (mode.is(classic)) {
			if (prev == null || mc.player.connection.getPlayerInfo(mc.player.getUniqueID()) == null) return;
			mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(this.prev);
	        mc.player.setMotion(0.0, 0.0, 0.0);
	        mc.player.setVelocity(0.0, 0.0, 0.0);
	        mc.player.setPosition(this.x, this.y, this.z);
	        mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 5.941588215E-315, mc.player.getPosZ(), mc.player.isOnGround()));
	        mc.world.removeEntityFromWorld(-1337);
	        mc.player.abilities.isFlying = false;
		} else if (mode.is(simulation)) {
			if (previous != null)
			mc.getGameSettings().setPointOfView(previous);
			int speed = 1;
			xAnim.animate((float)mc.player.getPosX(), speed);
			yAnim.animate((float)mc.player.getPosY(), speed);
			zAnim.animate((float)mc.player.getPosZ(), speed);
			if (control.is(dota)) {
			//	mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(prev);
            }
		}
	}
	
	class InputBinding extends Binding {
		
		private boolean pressed;

		public InputBinding(Module parent, String name) {
			super(parent, name);
		}
		
		public InputBinding addBind(Bind bind) {
			super.addBind(bind);
			return this;
		}
		
		public InputBinding hide(Supplier<Boolean> hide) {
			super.hide(hide);
			return this;
		}
		
	}
	
}