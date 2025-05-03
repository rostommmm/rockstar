package fun.rockstarity.client.modules.move;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.aura.AuraUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.math.aura.RotationMode;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.render.Interface;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

/**
 * @author Malecharik
 * @since 22 Mar 2024 22:24:37
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Info(name = "PeekAssist", desc = "Помогает при пике", type = Category.MOVE)
public class PeekAssist extends Module {
	
	final Animation alpha = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	final Animation target = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	Vector3d peekPos = Vector3d.ZERO;
	Vector3d targetPos = Vector3d.ZERO;
	boolean peek;
	
	public PeekAssist() {
		super(11);
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventRender3D e) {
			MatrixStack ms = e.getMatrixStack();
			
			float size = 3;
			
			ms.push();
			GlStateManager.depthMask(false);
           	ms.translate(peekPos.sub(Render.cameraPos().add(0, -0.49f, 0)));
           	ms.rotate(Vector3f.XP.rotationDegrees(90));
           	
           	FixColor[] circle = Interface.getCircle(alpha.get());
           	
   			Render.drawImage(ms, "masks/glow.png", (float) -size / 2, -size / 2, (float) -size / 2, size, size, circle[0], circle[1], circle[2], circle[3]);
   			GlStateManager.depthMask(true);
            ms.pop();
            
            // Таргет
            target.setForward(target() != null);
            ms.push();
            GlStateManager.depthMask(false); // Отключаем запись в буфер глубины
            GlStateManager.disableDepthTest(); // Игнорируем тест глубины
            ms.translate(targetPos.sub(Render.cameraPos().add(0, -0.49f, 0)));
            ms.rotate(Vector3f.XP.rotationDegrees(90));
            Render.drawImage(ms, "masks/glow.png", (float) -size / 2, -size / 2, (float) -size / 2, size, size, FixColor.RED.alpha(target.get()));
            GlStateManager.enableDepthTest(); // Восстанавливаем тест глубины
            GlStateManager.depthMask(true); // Включаем запись в буфер глубины
            ms.pop();
		}
		
		if (target() != null) {
			peek = mc.player.fallDistance > 0 && target().fallDistance <= 0;
			
			if (event instanceof EventMove e) {
				RotationMode rot = rock.getModules().get(Aura.class).rotation();
				e.setYaw(rot.getYaw());
				e.setPitch(rot.getPitch());
			}
			
			if (event instanceof EventInput e) {
				e.setStrafe(0);
				
				if (AuraUtility.getBestPoint(target().getEyePosition(0), mc.player, true).distanceTo(target().getEyePosition(0)) < 3) {
					e.setForward(-1);
				}
				
				if (peek) {
					e.setForward(1);
				}
			}
			
			if (AuraUtility.getBestPoint(target().getEyePosition(0), mc.player, true).distanceTo(target().getEyePosition(0)) > 3 && !peek) {
				if (event instanceof EventUpdate) {
					update(mc.player.getPositionVec());
				}
				
				if (event instanceof EventMotionMove e) {
					//mc.player.getMotion().x = 0;
					//mc.player.getMotion().z = 0;
				}
			}
		}
	}
	
	@Override
	public void onDisable() {
		alpha.setForward(false);
	}

	@Override
	public void onEnable() {
		update(mc.player.getPositionVec());
		alpha.setForward(true);
	}
	
	private void update(Vector3d pos) {
		for (int i = 0; i < 15; i++) {
			 if (mc.world.getBlock(new BlockPos(pos).add(0,-i,0)) != Blocks.AIR/*
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof TallGrassBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof FlowerBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof DoublePlantBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof DeadBushBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() != Blocks.SNOW)*/) {

				 peekPos = new Vector3d(pos.x, new BlockPos(pos).add(0,-i,0).y - 1 + 0.05f, pos.z);
				 break;
			 }
		}
		
		if (target() == null) {
			return;
		}
		
		pos = target().getPositionVec();
		
		for (int i = 0; i < 15; i++) {
			 if (mc.world.getBlock(new BlockPos(pos).add(0,-i,0)) != Blocks.AIR/*
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof TallGrassBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof FlowerBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof DoublePlantBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() instanceof DeadBushBlock)
					 && !(mc.world.getBlockState(target.getPosition().add(0,-i,0)).getBlock() != Blocks.SNOW)*/) {

				 targetPos = new Vector3d(pos.x, new BlockPos(pos).add(0,-i,0).y - 1 + 0.05f, pos.z);
				 break;
			 }
		}
	}
	
	private LivingEntity target() {
		return rock.getModules().get(Aura.class).getTarget();
	}
	
}
