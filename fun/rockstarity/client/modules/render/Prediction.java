package fun.rockstarity.client.modules.render;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.PredictUtility;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;

/**
 * @author ConeTin
 * @since 15 мар. 2024 г.
 */


@Info(name="Prediction", desc="Рисует путь полёта перлов, стрел и т.д.", type=Category.RENDER)
public class Prediction extends Module {
	
	private final ArrayList<LandPosition> positions = new ArrayList<>();
	private final ArrayList<PotionInfo> potionInfos = new ArrayList<>();
	private EnderPearlEntity lastThrownEnderPearl;
	
	private final Select targets = new Select(this, "Отображать у..").desc("Цели для предсказывания полета снаряда").min(1);
	
	private final Select.Element pearl = new Select.Element(targets, "Эндер жемчуг").set(true);
	private final Select.Element potions = new Select.Element(targets, "Зелья").set(true);
	private final Select.Element arrow = new Select.Element(targets, "Стрелы");
	private final Select.Element trident = new Select.Element(targets, "Трезубец");
	
	private final CheckBox vinBox = new CheckBox(this, "Виньетка").desc("Отображает виньетку если вы в радиусе плохой зельки");
	
	private final CheckBox render2d = new CheckBox(this, "2D Рендер").desc("Рисует 2д интерфейс в конечной точке").set(true);
	
	private final CheckBox whythrow = new CheckBox(this, "Помощник").desc("Отображает точку, в которую необходимо навестись чтобы кинуть Эндер-Жемчуг по самому быстрому пути").set(true).hide(() -> !render2d.get());
	private final CheckBox assist = new CheckBox(this, "Доводка").desc("Доводит прицел до точки").set(true).hide(() -> !whythrow.get());
	
	private final CheckBox inHand = new CheckBox(this, "В руке").desc("Отображает предикцию полёта, если предмет в руке").hide(() -> !this.pearl.get());
	private final CheckBox others = new CheckBox(this, "У других").desc("Рисует траектории в руках и у других игроков").set(true).hide(() -> !this.pearl.get() || !inHand.get());
	
	private final CheckBox renderCircle = new CheckBox(this, "Круг").desc("Рисует круг в конечной точке у зелий").set(true).hide(() -> !potions.get());
	
	private final Animation rotation = new Animation().setEasing(Easing.BOTH_CIRC).setSpeed(1500);
	
	@Getter private boolean inBadPotionRadius;
	
	@Override
	@EventType({EventRenderWorld.class, EventRender3D.class, EventRender2D.class})
	public void onEvent(Event event) {
		// Все действия будут происходить в событии рендеринга мира
		if (event instanceof EventRenderWorld e) handleRenderWorld(e);
		
		if (event instanceof EventRender2D e && render2d.get()) handleRenderInterface(e);
		
		if (event instanceof EventRender3D e && renderCircle.get()) renderCircle(e);
		if (event instanceof EventMotion) {
		    if (vinBox.get()) {
		        inBadPotionRadius = false;
		        for (Entity entity : mc.world.getAllEntities()) {
		            if (entity instanceof PlayerEntity && entity != mc.player) {
		                PlayerEntity player = (PlayerEntity) entity;
		                ItemStack heldItem = player.getHeldItemMainhand();
		                
		                if (isBadPotion(heldItem)) {
		                    double distance = player.getDistance(mc.player);
		                    double d1 = 1.0D - distance / 4.0D;
		                    
		                    for (EffectInstance eff : PotionUtils.getEffectsFromStack(heldItem)) {
		                        int i = (int)(d1 * (double)eff.getDuration() + 0.5D);
		                        
		                        if (i > 20) {
		                            inBadPotionRadius = true;
		                            break;
		                        }
		                    }
		                    
		                    if (inBadPotionRadius) break;
		                }
		            }
		        }
		        
		        if (inBadPotionRadius) {
		            mc.getGameSettings().graphicFanciness = GraphicsFanciness.FANCY;
		        } else {
		            mc.getGameSettings().graphicFanciness = GraphicsFanciness.FAST;
		        }
		    } else {
		        mc.getGameSettings().graphicFanciness = GraphicsFanciness.FAST;
		    }
		}
		if (assist.get()) {
			try {
				this.positions.forEach(landPos -> {
					if (landPos.getEntity() instanceof EnderPearlEntity && (mc.player.getHeldItemMainhand().getItem() == Items.ENDER_PEARL || mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT)) {
						Vector3d pos3d = landPos.getPos();
					    float pitch = (float) -Math.toDegrees(MathUtility.calcTrajectory(new BlockPos(pos3d)));
				        float yaw = Rotation.get(pos3d).x;
				        
				        if (mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT) {
				        	Vector2f rot = Rotation.get(pos3d);
				        	Vector2f corrected = Rotation.correctRotation(
				        			rot.x, (float) (rot.y - mc.player.getDistance(pos3d) * 0.22f + mc.player.getMotion().y * mc.player.getDistance(pos3d) * (mc.player.getMotion().y > 0 ? 0.5f : 1))
				    		);
				        	yaw = corrected.x;
				        	pitch = corrected.y;
				        }
				        
				        float shortestYawPath = (float) Math.abs(((((yaw - mc.player.rotationYaw) % 360) + 540) % 360) - 180);
				        
				        if (shortestYawPath < 10 && Math.abs(mc.player.rotationPitch - pitch) < 10) {
				        	Vector2f rot = Rotation.correctRotation(yaw, pitch);
					        
					        Player.look(event, rot.x, rot.y, true);
					        return;
				        }
					}
				});
			} catch (Exception e2) {
			}
		}
	}
	
	private void handleRenderInterface(EventRender2D e) {
		this.positions.removeIf(landPos -> landPos.getEntity() instanceof TridentEntity || landPos.getEntity() instanceof ArrowEntity || !mc.world.getAllEntities().contains(landPos.getEntity()));
		
		this.positions.forEach(landPos -> {
		    Vector3d pos3d = landPos.getPos();
		    Entity ent = landPos.getEntity();
		    double[] pos = Render.worldToScreen(pos3d.getX(), pos3d.getY(), pos3d.getZ());
		    if (pos == null) return;

		    if (!PositionTracker.isInView(pos3d)) return;

		    float x = (float) pos[0];
		    float y = (float) pos[1] - 2;
		    float inSize = 14;

		    Vector3d motion = ent.getMotion();
		    double averageSpeed = ent.getSpeeds()
		            .stream()
		            .mapToDouble(Double::doubleValue)
		            .average()
		            .orElse(Math.hypot(motion.x, motion.z));

		    double sec = MathUtility.round(landPos.getTicks() / 20, 1);

		    if (ent.getStartTime() == 0) ent.setStartTime(sec);

		    Vector3d bps = new Vector3d(pos3d.getX(), pos3d.getY(), pos3d.getZ());

		    String text = String.format("%.1f", sec);

		    if (landPos.entity instanceof EnderPearlEntity && pearl.get())
		        Render.drawStackOld(new ItemStack(Items.ENDER_PEARL), x - 5.4f, y - 10.3f, 0.65f);
		    else if (landPos.entity instanceof PotionEntity splash && !splash.isLingering() && potions.get()) 
		        Render.drawStackOld(new ItemStack(Items.SPLASH_POTION), x - 5.4f, y - 10.3f, 0.65f);
		    else if (landPos.entity instanceof TridentEntity && this.trident.get() && sec != 0) 
		        Render.drawStackOld(new ItemStack(Items.TRIDENT), x - 5.4f, y - 10.3f, 0.65f);
		    else if (landPos.entity instanceof ArrowEntity && this.arrow.get() && sec != 0) 
		        Render.drawStackOld(new ItemStack(Items.ARROW), x - 5.4f, y - 10.3f, 0.65f);
		    else if (landPos.entity instanceof PotionEntity splash && splash.isLingering() && potions.get())
		    	Render.drawStackOld(Items.LINGERING_POTION.getDefaultInstance(), x - 5.4f, y - 10.3f, 0.65f);

		    if ((landPos.getEntity() instanceof TridentEntity || landPos.getEntity() instanceof ArrowEntity) && sec == 0) {
		        return;
		    }
		    
		    if (landPos.getEntity() instanceof EnderPearlEntity && (mc.player.getHeldItemMainhand().getItem() == Items.ENDER_PEARL || mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT)) {
		    	float pitch = (float) -Math.toDegrees(MathUtility.calcTrajectory(new BlockPos(pos3d)));
		        float yaw = Rotation.get(pos3d).x;
		        
		        if (mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT) {
		        	Vector2f rot = Rotation.get(pos3d);
		        	Vector2f corrected = Rotation.correctRotation(
		        			rot.x, (float) (rot.y - mc.player.getDistance(pos3d) * 0.22f + mc.player.getMotion().y * mc.player.getDistance(pos3d) * (mc.player.getMotion().y > 0 ? 0.5f : 1))
		    		);
		        	yaw = corrected.x;
		        	pitch = corrected.y;
		        }
		        
		        Vector3d mark3dPos = mc.player.getEyePosition(e.getPartialTicks()).add(mc.player.getVectorForRotation(pitch, yaw).mul(15,15,15));
		        double[] wts = Render.worldToScreen(mark3dPos.getX(), mark3dPos.getY(), mark3dPos.getZ());
		        Vector2f mark2fPos = new Vector2f((float) wts[0], (float) wts[1]);

			    if (wts != null && PositionTracker.isInView(mark3dPos)) {
			    	float size = 20;
			    	
			    	rotation.setSpeed(2000);
			    	
			    	if (rotation.finished())
			    		rotation.setForward(false);
					else if (rotation.finished(false))
						rotation.setForward(true);
			    	
			    	float shortestYawPath = (float) Math.abs(((((yaw - mc.player.rotationYaw) % 360) + 540) % 360) - 180);

			    	Render.initRotate(mark2fPos.x, mark2fPos.y, rotation.get() * 360F);
			    	Render.image("icons/pearl-mark.png", mark2fPos.x - size / 2, mark2fPos.y - size / 2, size, size, (shortestYawPath < 10 && Math.abs(mc.player.rotationPitch - pitch) < 10) ? FixColor.GREEN : FixColor.WHITE);
			    	Render.endRotate();
			    }
		    }
		    
		    Round.draw(e.getMatrixStack(), new Rect(x - inSize / 2 - .5f, y - inSize / 2 - 5f, inSize + 1, inSize + 1), inSize / 2 + 1, rock.getThemes().getFirstColor());

		    Render.drawClientCircle(x - inSize / 2 + 7, y - inSize / 2 + 2.5f, 7f, (int)(360 - (sec / landPos.getEntity().getStartTime()) * 360)-90, 361-90, 2f, 1);

		    Round.draw(e.getMatrixStack(), new Rect(x - inSize / 2 - 1, y - inSize / 2 + 12, inSize + 2f, inSize - 5), 2,  rock.getThemes().getFirstColor());
		    semibold.get(12).draw(e.getMatrixStack(), text, (float) pos[0] - 5, (float) pos[1] + 3.3f, landPos.entity == lastThrownEnderPearl ? Style.getMain() : rock.getThemes().getTextFirstColor());
		});
		
		
		
		
		potionInfos.forEach(potion -> {
		    Vector3d pos3d = potion.getPos();

		    double[] pos = Render.worldToScreen(pos3d.getX(), pos3d.getY(), pos3d.getZ());
		    if (pos == null) return;

		    if (!PositionTracker.isInView(pos3d)) return;

		    float x = (float) pos[0];
		    float y = (float) pos[1] - 10;
		    float width = semibold.get(14).getWidth(potion.item.getDisplayName().getString().replace("[", "").replace("]", "")) + 13;

		    Render.drawStackOld(potion.item, x - width/2F + 2, y - 2.5f, 0.5f);

		    Round.draw(e.getMatrixStack(), new Rect(x - width/2F, y - 5f, width, 13), 3, rock.getThemes().getFirstColor());
		    semibold.get(14).draw(e.getMatrixStack(), potion.item.getDisplayName().getString().replace("[", "").replace("]", ""), x - width/2F + 9, y - 3.5f, rock.getThemes().getTextFirstColor());
		    
		    float offY = 0;
		    for (EffectInstance eff : PotionUtils.getEffectsFromStack(potion.item)) {
                Effect effect = eff.getPotion();
                double d1 = 1.0D - Math.sqrt(potion.getPos().squareDistanceTo(mc.player.getPositionVec())) / 4.0D;

                int i = (int)(d1 * (double)eff.getDuration() + 0.5D);

                if (i > 20)
                {
					EffectInstance inst = new EffectInstance(effect, i, eff.getAmplifier(), eff.isAmbient(), eff.doesShowParticles());
					String str = String.format("%s%s %s %s%s", 
    						effect.getEffectType() == EffectType.HARMFUL ? TextFormatting.RED : TextFormatting.BLUE, I18n.format(eff.getEffectName()), 
    						I18n.format("enchantment.level." + (eff.getAmplifier() + 1)).replace("enchantment.level.", ""), 
    						TextFormatting.GRAY, EffectUtils.getPotionDurationString(inst, 1));
					float potWidth = semibold.get(13).getWidth(str) + 6;
				    Round.draw(e.getMatrixStack(), new Rect(x - potWidth/2F, y + 10 + offY, potWidth, 10), 2, rock.getThemes().getFirstColor());
                	semibold.get(13).draw(e.getMatrixStack(), str, x - potWidth/2F + 3, y + 10.5f + offY, rock.getThemes().getTextFirstColor());
                	
                	offY += 10.5f;
                }
		    }
		});
	}
	
	private void renderCircle(EventRender3D e) {
		this.positions.forEach(landPos -> {
			Vector3d pos3d = landPos.getPos();
			Entity ent = landPos.getEntity();
			double[] pos = Render.worldToScreen(pos3d.getX(), pos3d.getY(), pos3d.getZ());
	        if (pos == null) return;
	        
	        MatrixStack ms = e.getMatrixStack();
	        
	        Vector3d motion = ent.getMotion();
	        double averageSpeed = ent.getSpeeds()
	        		.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(Math.hypot(motion.x, motion.z));

	        //Делим тики на 10 (для того что бы перевести число) и округляем
	        double sec = MathUtility.round(landPos.getTicks() / 10, 1);
	        if (ent.getStartTime() == 0) ent.setStartTime(sec);

           	if (landPos.getEntity() instanceof PotionEntity && potions.get()) {
           		float size = 14;
    			
    			ms.push();
    			GlStateManager.depthMask(false);
    			Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();
    			
       			ms.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
               	ms.translate(pos3d.x, pos3d.y-0.49f*size, pos3d.z);
               	ms.rotate(Vector3f.XP.rotationDegrees(90));
               	
       			Render.drawImage(ms, "masks/glow.png", (float) -size / 2, -size / 2, (float) -size / 2, size, size, Style.getPoint(10).alpha(landPos.getTotalDistance() / 1 / 10 / ent.getStartTime()));
       			GlStateManager.depthMask(true);
                ms.pop();
           	}
			
		});
	}
	
	
	private void handleRenderWorld(EventRenderWorld e) {
		this.positions.clear();
		
		// Пушим матрицу
		glPushMatrix();

		// Устанавливаем режимы отрисовки (чтобы все заебись было тип)
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();

        glTranslated(-renderOffset.x, -renderOffset.y, -renderOffset.z); // Меняем позицию на позицию отрисовки(чтобы они не рендерились в пизде)

        glLineWidth(3); // Ширина линии - 3 пикселя

        for (Entity ent : mc.world.getAllEntities()) { // Перебираем всех ентити
        	if ((ent instanceof ArrowEntity && arrow.get()) || (ent instanceof EnderPearlEntity && pearl.get()) || (ent instanceof PotionEntity && potions.get() || (ent instanceof TridentEntity && this.trident.get()))) { // Проверяем что ентити можно кинуть либо что это стрела
        		BUILDER.begin(1, DefaultVertexFormats.POSITION); // Начинаем отрисовку
        		if (ent instanceof EnderPearlEntity)
        		lastThrownEnderPearl = (EnderPearlEntity) ent;
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
                    	positions.add(new LandPosition(startPos, blockHitResult.getHitVec(), ent, distance, ticks));
                        pos = blockHitResult.getHitVec(); // Записываем в позицию если это последняя "точка"
                    }
                    
                    if (isLast || pos.y < 0) break; // Прерываем цикл если точка последняя или если она ниже бедрока
                    ticks++; //Высчитываем тики
                    BUILDER.pos(pos.x, pos.y, pos.z).endVertex(); // Устанавливаем вторую "точку"
                }
                
                TESSELLATOR.draw(); // Заканчиваем отрисовку
        	}
        }
        
        
        if (others.get()) {
        	potionInfos.clear();
        	for (PlayerEntity player : mc.world.getPlayers()) {
                boolean isSelf = player == mc.player;
                if (isSelf) continue;

                Item itemInHand = player.getHeldItemMainhand().getItem();
                Entity ent = null;
                PotionItem potionItem = null;

                if (itemInHand instanceof SplashPotionItem splash) {
                    potionItem = splash;
                } else if (itemInHand instanceof LingeringPotionItem ling) {
                    potionItem = ling;
                }

                if (itemInHand instanceof EnderPearlItem pearl && this.pearl.get()) {
                    ent = new EnderPearlEntity(mc.world, player);
                    ((EnderPearlEntity) ent).func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
                    ent.setMotion(PredictUtility.Projectile.predictThrowMotion(player, pearl));
                }
                else if (itemInHand instanceof TridentItem trident && this.trident.get()) {
                    ent = new TridentEntity(mc.world, player, trident.getDefaultInstance());
                    ((TridentEntity) ent).func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);
                    ent.setMotion(PredictUtility.Projectile.predictThrowMotion(player, trident));
                }
                else if (potionItem != null && potions.get()) {
                    ent = new PotionEntity(mc.world, player);
                    ((PotionEntity) ent).func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 0.5F, 1.0F);
                    ent.setMotion(PredictUtility.Projectile.predictThrowMotion(player, potionItem));
                }

                if (ent == null) continue;

                BUILDER.begin(1, DefaultVertexFormats.POSITION);
                Vector3d motion = ent.getMotion();
                Vector3d pos = ent.getPositionVec();
                Vector3d prevPos;
                Vector3d startPos = null;
                double distance = 0;
                double ticks = 0;

                for (int i = 0; i < 150; i++) {
                    prevPos = pos;
                    pos = pos.add(motion);
                    distance += prevPos.distanceTo(pos);

                    motion = calculateNext(ent, motion);

                    if (startPos == null) startPos = prevPos;

                    Render.color(Style.getPoint(i * 15).getRGB());

                    RayTraceContext rayTraceContext = new RayTraceContext(
                        prevPos,
                        pos,
                        RayTraceContext.BlockMode.COLLIDER,
                        RayTraceContext.FluidMode.NONE,
                        ent
                    );
                    BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

                    boolean isLast = blockHitResult.getType() == RayTraceResult.Type.BLOCK;

                    if (isLast) {
                    	if (potionItem != null)
                    		potionInfos.add(new PotionInfo(startPos, blockHitResult.getHitVec(), player.getHeldItemMainhand()));
                    	positions.add(new LandPosition(startPos, blockHitResult.getHitVec(), ent, distance, ticks));
                        pos = blockHitResult.getHitVec();
                    }

                    if (isLast || pos.y < 0) break;

                    ticks++;
                    BUILDER.pos(pos.x, pos.y, pos.z).endVertex();
                }

                TESSELLATOR.draw();
            }
        }
        
        // Крутая хуета как в кс чтобы отображать если перка в руке
        if ((mc.player.getHeldItemMainhand().getItem() instanceof EnderPearlItem && pearl.get() && this.inHand.get()) || ((mc.player.getHeldItemMainhand().getItem() instanceof SplashPotionItem || mc.player.getHeldItemMainhand().getItem() instanceof LingeringPotionItem) && potions.get() && this.inHand.get()) || (mc.player.getHeldItemMainhand().getItem() instanceof TridentItem && this.trident.get() && this.inHand.get())) {
        	ClientPlayerEntity player = mc.player;
        	// Если обрабатываем самого игрока, используем настройки от вас
    	    boolean isSelf = player == mc.player;

    	    Item itemInHand = player.getHeldItemMainhand().getItem();
    	    PotionItem potionItem = null;
    	    Entity ent = null;

    	    if (itemInHand instanceof SplashPotionItem splash) {
    	        potionItem = splash;
    	    } else if (itemInHand instanceof LingeringPotionItem ling) {
    	        potionItem = ling;
    	    }
    	    
    	    if (itemInHand instanceof EnderPearlItem pearl && this.pearl.get() && isSelf && this.inHand.get()) {
    	        ent = new EnderPearlEntity(mc.world, player);
    	        ((EnderPearlEntity) ent).func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
    	        ent.setMotion(PredictUtility.Projectile.predictThrowMotion(player, pearl));
    	    } else if (itemInHand instanceof TridentItem trident && this.trident.get() && isSelf && this.inHand.get()) {
    	        ent = new TridentEntity(mc.world, player, trident.getDefaultInstance());
    	        ((TridentEntity) ent).func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);
    	        ent.setMotion(PredictUtility.Projectile.predictThrowMotion(player, trident));
    	    } else if (potionItem != null && potions.get()) {
    	        ent = new PotionEntity(mc.world, player);
    	        ((PotionEntity) ent).func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 0.5F, 1.0F);
    	        ent.setMotion(PredictUtility.Projectile.predictThrowMotion(player, potionItem));
    	    }

    	    if (ent == null) return;

    	    // Начинаем отрисовку траектории
    	    BUILDER.begin(1, DefaultVertexFormats.POSITION);
    	    Vector3d motion = ent.getMotion();
    	    Vector3d pos = ent.getPositionVec();
    	    Vector3d prevPos;
    	    Vector3d startPos = null;
    	    double distance = 0;
    	    double ticks = 0;
    	    double partialTicks = mc.getRenderPartialTicks();

    	    for (int i = 0; i < 150; i++) { // Цикл траектории
    	        prevPos = pos;
    	        pos = pos.add(motion);
    	        distance += prevPos.distanceTo(pos);

    	        // Рассчитываем новую скорость
    	        motion = calculateNext(ent, motion);

    	        if (startPos == null) startPos = prevPos;

    	        Render.color(Style.getPoint(i * 15).getRGB()); // Устанавливаем цвет

    	        // Выполняем трассировку
    	        RayTraceContext rayTraceContext = new RayTraceContext(
    	            prevPos,
    	            pos,
    	            RayTraceContext.BlockMode.COLLIDER,
    	            RayTraceContext.FluidMode.NONE,
    	            ent
    	        );
    	        BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

    	        boolean isLast = blockHitResult.getType() == RayTraceResult.Type.BLOCK;

    	        if (isLast) {
    	            this.positions.add(new LandPosition(startPos, blockHitResult.getHitVec(), ent, distance, ticks));
    	            pos = blockHitResult.getHitVec();
    	        }

    	        if (isLast || pos.y < 0) break; // Прерываем если столкновение или ниже бедрока

    	        ticks++;
    	        BUILDER.pos(pos.x, pos.y, pos.z).endVertex();
    	    }

    	    TESSELLATOR.draw(); // Заканчиваем отрисовку
        }

        // Выключаем режимы рендеринга
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix(); // Завершаем рендеринг методом popMatrix
	}
	
	private boolean isBadPotion(ItemStack stack) {
        if (stack.getItem() instanceof PotionItem || stack.getItem() instanceof ThrowablePotionItem) {
        	List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
            
            for (EffectInstance effect : effects) {
                if (effect.getPotion().getEffectType() == EffectType.HARMFUL) {
                    return true;
                }
            }
        }
        return false;
	}
	
	private boolean isLookingAtPlayer(PlayerEntity source, PlayerEntity target) {
        Vector3d lookVec = source.getLookVec();
        
        Vector3d toTarget = new Vector3d(
            target.getPosX() - source.getPosX(),
            target.getPosY() + target.getEyeHeight() - source.getPosY() - source.getEyeHeight(),
            target.getPosZ() - source.getPosZ()
        ).normalize();
        
        double dot = lookVec.x * toTarget.x + lookVec.y * toTarget.y + lookVec.z * toTarget.z;
        
        return dot > Math.cos(Math.toRadians(15));
    }
	
	/**
	 * Метод для расчёта следующей скорости ентити
	 * @param throwable - Сущность, скорость которой надо расчитать
	 * @param motion - Текущая скорость сущности
	 * @return Возвращает текущую скорость
	 */
	private Vector3d calculateNext(Entity ent, Vector3d motion) {
		// Уменьшаем скорость сущности
		motion = motion.scale(0.99);

        if (!ent.hasNoGravity()) { // Проверяем что сущность имеет гравитацию
        	// Расчитываем следующую скорость сущности по y
            motion.y -= ent instanceof ThrowableEntity th ? th.getGravityVelocity() : 0.05F; // Если это не "кидаемая" сущность, то устанавливаем гравитацию 0.05
        }
        
        return motion; // Возвращаем изменённую скорость
    }
	
	@Getter
	@AllArgsConstructor
	private class LandPosition {
		private final Vector3d startPos, pos;
		private final Entity entity;
		private final double totalDistance;
		private final double ticks;
	}
	
	@Getter
	@AllArgsConstructor
	private class PotionInfo {
		private final Vector3d startPos, pos;
		private final ItemStack item;
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
