package fun.rockstarity.api.helpers.math.aura.ai;

import ai.catboost.CatBoostModel;
import ai.catboost.CatBoostPredictions;
import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.IdealHitUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.math.aura.modes.NeuroRotation;
import fun.rockstarity.api.helpers.player.FallingPlayer;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Converter;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.render.animation.infinity.RotationAnimation;
import fun.rockstarity.api.render.ui.alerts.Alert;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

@UtilityClass
public class AIPredictor implements IAccess {
    boolean loading;
    public String LAST_MODEL;
    private CatBoostModel yawModel;
    private CatBoostModel pitchModel;
    private final TimerUtility timer = new TimerUtility();
    private LivingEntity target;

    public RotationAnimation interp = new RotationAnimation();
    private final TimerUtility collide = new TimerUtility();

    public void updateModel(String model) {
        if ((LAST_MODEL == null || LAST_MODEL != model) && !loading) {
            ThreadManager.run(() -> {
                loading = true;
                Alert alert = rock.getAlertHandler().alert("Загружаю модель", AlertType.WAIT);
                try {
                    String link = "http://rockstar.moscow/api/v1/files/premium/ai/rotations/" + model + "_";
                    System.out.println(link + "yaw.cbm");
                    yawModel = CatBoostModel.loadModel(Converter.getInputStream(link + "yaw.cbm"));
                    pitchModel = CatBoostModel.loadModel(Converter.getInputStream(link + "pitch.cbm"));
                    LAST_MODEL = model;
                    rock.getAlertHandler().alert("Модель загружена", AlertType.SUCCESS);
                } catch (Exception e) {
                    e.printStackTrace();
                    rock.getAlertHandler().alert("Ошибка при загрузке модели", AlertType.ERROR);
                }
                alert.hide();
                loading = false;
            });
        }
    }

    public void onEvent(Event event) {
        if (event instanceof EventAttack e) {
            //target = e.getTarget();
            timer.reset();
        }
    	
    	/*
    	
    	if (event instanceof EventTick && target != null && !timer.passed(2000)) {
    		try {
    			Vector2f predicted = predict(new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch), new Vector2f(mc.player.prevRotationYaw, mc.player.prevRotationPitch), new Vector2f(50, 50));
                
                mc.player.rotationYaw = predicted.x;
                mc.player.rotationPitch = predicted.y;
    		} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	*/
    }

    public Vector2f predict(LivingEntity target, Vector2f current, Vector2f prev, Vector2f speed) {
        try {
            ClientPlayerEntity player = mc.player;
            Aura aura = rock.getModules().get(Aura.class);
            NeuroRotation neuro = aura.getNeuro();

            float offsetX = aura.getAdditional().get() ? aura.getOffset().getX() : 0;
            float offsetY = aura.getAdditional().get() ? aura.getOffset().getY() : 0;

            if (neuro.getAll().get()) {
                offsetX += 5;
            } else if (neuro.getArtem().get()) {
                offsetX += 15;
            } else if (neuro.getStas().get() || neuro.getPowen().get()) {
                offsetX += 5;
            } else if (neuro.getZahar().get()) {
                offsetX += mc.player.isOnGround() ? 0 : -5;
            } else if (neuro.getAlex().get()) {
                offsetX += -10;
            } else if (neuro.getEgor().get()) {
                offsetX += 5;
            } else if (neuro.getNikita().get()) {
                offsetX += 5;
            } else if (neuro.getAkhmedx().get()) {
                offsetX -= 7;
            }

            float yawDiff = MathHelper.wrapDegrees(Rotation.get(target.getPositionVec()).x + offsetX);
            float pitchDiff = MathHelper.clamp(Rotation.get(target.getPositionVec()).y + offsetY, -90, 90);

            // Дата, нужная для пердикции (входные потоки или как они там у ии)
            RotationData data = new RotationData(
                    (float) Math.abs(current.x - prev.x), (float) Math.abs(current.y - prev.y),
                    0, pitchDiff,
                    2000,
                    (float) player.getDistance(target),
                    player.isOnGround() ? 1 : 0,
                    player.isElytraFlying() || player.isSwimming() ? 1 : 0,
                    MathHelper.wrapDegrees(current.x) - yawDiff, current.y
            );

            float[] features = getCurrentFeatures(data);
            String[] categoricals = {
                    String.valueOf(data.onGround),
                    String.valueOf(data.miniHitbox)
            };

            // Пердиктим значения при помощи ии основываясь на RotationData
            CatBoostPredictions predictedYaw = yawModel.predict(features, categoricals);
            CatBoostPredictions predictedPitch = pitchModel.predict(features, categoricals);

            // Супер $$$ фикс интерполяции
            float shortestYawPath = (float) (((((predictedYaw.get(0, 0) + yawDiff - interp.getYaw()) % 360) + 540) % 360) - 180);
            float targetYaw = interp.getYaw() + shortestYawPath;
            float targetPitch = (float) predictedPitch.get(0, 0);

            if (mc.player.getPosY() > target.getPosY() + target.getHeight()) {
                targetPitch = Rotation.get(target.getPositionVec().add(0, target.getHeight(), 0)).y;
            }

            if (mc.player.getPosY() + 1 < target.getPosY()) {
                targetPitch = Rotation.get(target.getPositionVec().add(0, 0.5f, 0)).y;
            }


            if (Server.is("spooky")) {
                if (Player.getBlock(0, 2, 0) != Blocks.AIR || Player.getBlock(0, 3, 0) != Blocks.AIR || mc.world.getBlock(target.getPosition().add(0, 2, 0)) != Blocks.AIR || mc.world.getBlock(target.getPosition().add(0, 3, 0)) != Blocks.AIR) {
                    //	speed = new Vector2f(MathUtility.random(60, 100), MathUtility.random(60, 100));
                }

                targetPitch = Rotation.get(target.getPositionVec()).y;

                if (Player.collideWith(target, 3)) {
                    targetPitch = (float) ((target.isOnGround() ? 34 : 24) + (2.3f - mc.player.getDistance(target)) * 20 - (mc.player.fallDistance) * (40) - 5 + 10 * aura.getRandomFactor());
                    if (mc.player.getPosY() + 1 < target.getPosY()) {
                        targetPitch -= 30;
                    }
                    speed.y = MathUtility.random(50, 100) - 10 + 20 * aura.getRandomFactor();
                }

                speed.x = yawSpeed(Math.abs(interp.getYaw() - targetYaw)) / 100 * speed.x - 10 + 20 * aura.getRandomFactor();

                if (!aura.getAttackTimer().passed(200)) {
                    //	speed.x /= 1.5F;
                }

                if (mc.player.fallDistance > 0) {
                    speed.x *= 2F;
                    speed.y *= 2F;
                } else {
                    speed.y /= 2F;
                }

                if (Player.collideWith(target)) {
                    //	targetPitch += 30;
                }

                if (Player.collideWith(target) && (stalin(target) || Player.getBlock(0, 2, 0) != Blocks.AIR && Player.getBlock(0, -1, 0) != Blocks.AIR && Player.getBlock(0, 2, 0) != Blocks.WATER && Player.getBlock(0, -1, 0) != Blocks.WATER)) {
                    //if (collide.passed(100)) {
                    targetPitch = (float) (64 + (mc.player.getPosY() - target.getPositionVec().y + 1) * (5 + Math.sin((int) (mc.player.ticksExisted % 100 / 5) * 1924.12f) * 35) - 5 + 10 * aura.getRandomFactor());
                    speed.x *= MathUtility.random(30, 50);
                    //}
                } else {
                    collide.reset();
                }
                
                /*
                if (!MathUtility.canSeen(target.getPositionVec())) {
                	if (!aura.canCritical() || !aura.getAttackTimer().passed(300)) {
                    	targetPitch = mc.player.rotationPitch;
                    	targetYaw = mc.player.rotationYaw;
                    	
                 		speed.x = MathUtility.random(1, 1);
                 		speed.y = MathUtility.random(1, 1);
                	} else {
                		Vector3d pos = mc.player.getEyePosition(0);

            			Vector3d fastPoint = new Vector3d(
            	                MathHelper.clamp(pos.x, 
            	                		target.getBoundingBox().minX, 
            	                		target.getBoundingBox().maxX),
            	                
            	                target.getPositionVec().y,
            	                
            	                MathHelper.clamp(pos.z, 
            	                		target.getBoundingBox().minZ, 
            	                		target.getBoundingBox().maxZ)
            	        );
            			
                    	targetPitch = Rotation.get(fastPoint).y;
                 		speed.x = MathUtility.random(1, 1);
                 		speed.y = MathUtility.random(1, 1);
                	}
                }
                */
            }


            if (mc.player.isSwimming()) {
                targetPitch = Rotation.get(target.getPositionVec().add(0, target.getHeight() / 2F, 0)).y;
                speed = new Vector2f(MathUtility.random(350, 200), MathUtility.random(350, 200));
            }


            if (timer.getElapsed() < 300 && Server.is("spooky")) {
                //if (Move.getSpeed(target) > 0.2f && mc.player.getDistance(target.getPositionVec()) < mc.player.getDistance(target.getPositionVec().add(target.getMotion()))) {
                targetPitch += MathUtility.random(5, 10);
                targetYaw += MathUtility.random(5, 10);

                speed = new Vector2f(speed.x * 1.7f, speed.y * 1.7f);
            }

            if (timer.getElapsed() < 300 && Server.isFT()) {
                //if (Move.getSpeed(target) > 0.2f && mc.player.getDistance(target.getPositionVec()) < mc.player.getDistance(target.getPositionVec().add(target.getMotion()))) {
                targetPitch += MathUtility.random(5, 30);
                targetYaw += MathUtility.random(5, 10);

                speed = new Vector2f(speed.x * 1.7f, speed.y * 1.7f);
            }

            targetPitch = MathHelper.clamp(targetPitch, -90, 90);


            // Интерполируем чтобы сильно не трясло
            interp.animate(new Vector2f(targetYaw, targetPitch), (int) speed.x, (int) speed.y);

            Vector2f rot = Rotation.correctRotation(interp.getYaw(), interp.getPitch());
            rot.y = MathHelper.clamp(rot.y, -90, 90);


            if (!MathUtility.canSeen(target.getPositionVec()) && Server.is("spooky")) { // Wall Bypass by deluxe client
                target.setPose(Pose.STANDING);
                target.getPosition().add(MathUtility.random(-0.15F, 0.15F), target.getBoundingBox().getYSize(), MathUtility.random(-0.15F, 0.15F));

                Vector3d vec = target.getPositionVec().add(0, MathHelper.clamp(mc.player.getPosYEye() - target.getPosY(), 0, target.getHeight() * (mc.player.getEyePosition(0).distanceTo(target.getPositionVec()) / 3)), 0).subtract(mc.player.getEyePosition(1.0F));

                float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
                float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));

                float yawDelta = (MathHelper.wrapDegrees(yawToTarget - rot.x));
                float pitchDelta = (MathHelper.wrapDegrees(pitchToTarget - rot.y));
                float yaw, pitch;

                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1f), 90);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1f), 90);
                yaw = rot.x + (yawDelta > 0 ? clampedYaw : -clampedYaw) + MathUtility.random(-3f, 3f);
                pitch = MathHelper.clamp(rot.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -70.0F, 70.0F) + MathUtility.random(-3f, 3f);

                if (!aura.isSnapTick() && (!(aura.canCritical()) || !aura.getAttackTimer().passed(300))) {
                    yaw = rot.x + (mc.player.rotationYaw - rot.x) / (float) 2 + MathUtility.random(-3, 3f);
                    pitch = MathHelper.clamp(rot.y + (mc.player.rotationPitch - rot.y) / (float) 2, -70, 70) + MathUtility.random(-3f, 3f);
                }

                float gcd = Rotation.getGCDValue();
                yaw -= (yaw - rot.x) % gcd;
                pitch -= (pitch - rot.y) % gcd;
                pitch += 8;

                rot = new Vector2f(yaw, pitch);
            }


            return rot;
        } catch (Exception e) {
        }
        return current;
    }

    private float yawSpeed(float diff) {
        return MathHelper.clamp(
                diff,                                // Разница между интерполированной ротацией и обычной
                MathUtility.randomInt(40, 50),    // Максимальная возможная скорость
                MathUtility.randomInt(450, 300)        // Минимальная возможная скорость - втф??? ты по-моему перепутал - не перепутал, у нас скорость в миллисекундах, чем больше - тем медленнее
        );
    }

    private boolean stalin(LivingEntity target) {
        Vector3d pos = target.getPositionVec();
        AxisAlignedBB hitbox = target.getBoundingBox();

        float off = 0.05f;

        return !isAir(hitbox.minX - off, pos.y, hitbox.minZ - off)
               || !isAir(hitbox.maxX + off, pos.y, hitbox.minZ - off)
               || !isAir(hitbox.minX - off, pos.y, hitbox.maxZ + off)
               || !isAir(hitbox.maxX + off, pos.y, hitbox.maxZ + off);
    }

    private boolean isAir(double x, double y, double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR;
    }

    private float[] getCurrentFeatures(RotationData data) {
        return new float[]{
                data.yawDelta,
                data.pitchDelta,
                data.targetYaw,
                data.targetPitch,
                data.sinceAttack,
                data.distance,
                data.onGround,
                data.miniHitbox
        };
    }
}