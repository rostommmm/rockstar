package fun.rockstarity.client.modules.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventPostMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.InvUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.render.color.themes.Style;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author ConeTin
 * @since 20.04.2025 10:43
 */

@Info(name="AutoDodge", desc="Автоматически уворачивается от плохих зелий", type=Category.PLAYER)
public class AutoDodge extends Module {

	Mode mode = new Mode(this, "Режим");
	Mode.Element auto = new Mode.Element(mode, "Авто");
	Mode.Element dodge = new Mode.Element(mode, "Уворот");
	Mode.Element plast = new Mode.Element(mode, "Пласт");
	
	private final Map<Entity, Vector3d> potions = new HashMap<>();
	private final TimerUtility checkTimer = new TimerUtility();

    @Override
    public void onEvent(Event event) {
    	if (Player.isLookEvent(event) || event instanceof EventPostMotion) {
    		for (Entry<Entity, Vector3d> potion : potions.entrySet()) {
    			if (auto.get()) { // Авто режим
    				if (mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP) || !checkTimer.passed(2000) || mc.player.getDistance(potion.getKey().getPositionVec()) > 7) {
    					if (mc.player.getDistance(potion.getValue()) > 6) continue;
            			
            			if (event instanceof EventInput e) {
            				e.setForward(1);
            				e.setJump(!mc.player.isPotionActive(Effects.SPEED) || mc.player.collidedHorizontally);
            				
            				if (e.isJump() && mc.player.isPotionActive(Effects.SLOWNESS) && mc.player.getActivePotionEffect(Effects.SLOWNESS).getAmplifier() > mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier()) {
            					e.setJump(true);
            				}
            			}
            			
            			Player.look(event, Rotation.get(potion.getValue()).x - 180, mc.player.rotationPitch, true);
    				} else {
    					if (mc.player.getDistance(potion.getValue()) > 6) continue;
            			
            			Player.look(event, Rotation.get(potion.getKey().getPositionVec()).x, Rotation.get(potion.getKey().getPositionVec()).y, true);
            			
            			if (event instanceof EventPostMotion && !mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP) && mc.player.getDistance(potion.getKey().getPositionVec()) > 3 && potion.getKey().ticksExisted > 1) {
            				InvUtility.use(Items.DRIED_KELP);
            			}
    				}
    			} else {
    				if (dodge.get()) {
        				if (mc.player.getDistance(potion.getValue()) > 6) continue;
            			
            			if (event instanceof EventInput e) {
            				e.setForward(1);
            				e.setJump(!mc.player.isPotionActive(Effects.SPEED) || mc.player.collidedHorizontally);
            			}
            			
            			Player.look(event, Rotation.get(potion.getValue()).x - 180, mc.player.rotationPitch, true);
        			}
        			
        			if (plast.get()) {
        				if (mc.player.getDistance(potion.getValue()) > 6) continue;
            			
            			Player.look(event, Rotation.get(potion.getKey().getPositionVec()).x, Rotation.get(potion.getKey().getPositionVec()).y, true);
            			
            			if (event instanceof EventPostMotion && !mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP) && mc.player.getDistance(potion.getKey().getPositionVec()) > 3 && potion.getKey().ticksExisted > 1) {
            				InvUtility.use(Items.DRIED_KELP);
            			}
        			}
    			}
    			
    			break;
    		}
    	}
    	
    	 if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
 	        String message = packet.getChatComponent().getString();
 	        
 	        if (message.contains("Здесь уже стоит трапка/пласт")) {
 	        	checkTimer.reset();
 	        }
    	 }
    	
    	if (event instanceof EventUpdate) {
    		potions.clear();
    		
    		for (Entity ent : mc.world.getAllEntities()) { // Перебираем всех ентити
            	if (ent instanceof PotionEntity pot) { // Проверяем что ентити можно кинуть либо что это стрела
            		BUILDER.begin(1, DefaultVertexFormats.POSITION); // Начинаем отрисовку
                	Vector3d motion = ent.getMotion(); // Скорость полета
                    Vector3d pos = ent.getPositionVec(); // Позиция
                    Vector3d prevPos; // Предыдущая позиция
                    Vector3d startPos = null;
                    double distance = 0;
                    double ticks = 0; 
                    
                    for (int i = 0; i < 150; i++) { // Цикл из 150 итераций (Если больше то мейби лагать будет если вдруг у стрелы очень большой путь)
                        prevPos = pos; // Записываем в переменную позицию
                        pos = pos.add(motion); // Прибавляем скорость к позиции
                        
                        // Вычисляем дистанцию от одной точки до другой и прибавляем к дистанции
                        // Так мы вычисляем дистанцию между всеми соединительными точками и вычисляем тотальную дистанцию
                        distance += prevPos.distanceTo(pos);
                        
                        // Тотальную скорость и записываем в переменную в Entity
                        Vector3d motion1 = ent.getMotion();
            	        
            	        double speed = Math.hypot(motion1.x, motion1.z);
            	        ent.getSpeeds().add(speed);
                        
                        motion = calculateNext(ent, motion); // Расчитываем следующую скорость
                        
                        if (startPos == null) startPos = prevPos;
                        
                        Render.color(Style.getPoint(i * 15).getRGB()); // Устанавливаем цвет

                        BUILDER.pos(prevPos.x, prevPos.y, prevPos.z).endVertex(); // Первая "точка" (по точкам будет соединяться линия)

                        // Выполняем трассировку лучей
                        RayTraceContext rayTraceContext = new RayTraceContext(
                                prevPos,
                                pos,
                                RayTraceContext.BlockMode.COLLIDER,
                                RayTraceContext.FluidMode.NONE,
                                ent
                        );

                        // Получаем блок, с которым столкнётся стрела
                        BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
                        
                        // Если это рил блок, то записываем в переменную что это последняя точка
                        boolean isLast = blockHitResult.getType() == RayTraceResult.Type.BLOCK;
                        
                        if (isLast) {
                        	for (EffectInstance eff : PotionUtils.getEffectsFromStack(pot.getItem())) {
                                Effect effect = eff.getPotion();
                                EffectInstance inst = new EffectInstance(effect, i, eff.getAmplifier(), eff.isAmbient(), eff.doesShowParticles());
            					
            					if (effect.getEffectType() == EffectType.HARMFUL || effect == Effects.JUMP_BOOST) {
                                	potions.put(ent, blockHitResult.getHitVec());
                                	break;
            					}
                		    }
                        	potions.put(ent, blockHitResult.getHitVec());
                        	
                            pos = blockHitResult.getHitVec(); // Записываем в позицию если это последняя "точка"
                        }
                        
                        if (isLast || pos.y < 0) break; // Прерываем цикл если точка последняя или если она ниже бедрока
                        ticks++; //Высчитываем тики
                        BUILDER.pos(pos.x, pos.y, pos.z).endVertex(); // Устанавливаем вторую "точку"
                    }
                    
                    TESSELLATOR.draw(); // Заканчиваем отрисовку
            	}
            }
            
        }
    }
    
    private Vector3d calculateNext(Entity ent, Vector3d motion) {
		// Уменьшаем скорость сущности
		motion = motion.scale(0.99);

        if (!ent.hasNoGravity()) { // Проверяем что сущность имеет гравитацию
        	// Расчитываем следующую скорость сущности по y
            motion.y -= ent instanceof ThrowableEntity th ? th.getGravityVelocity() : 0.05F; // Если это не "кидаемая" сущность, то устанавливаем гравитацию 0.05
        }
        
        return motion; // Возвращаем изменённую скорость
    }
    
    @Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
