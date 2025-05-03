package fun.rockstarity.client.modules.render;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventTridentHitted;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.ui.EventRenderPreUI;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.particles.Particle3D;
import fun.rockstarity.api.render.shaders.list.Glass;
import fun.rockstarity.api.secure.nativeapi.FastNative;
import fun.rockstarity.client.modules.combat.AimAssist;
import fun.rockstarity.client.modules.combat.AimBot;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.combat.Aura;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.shaders.Shaders;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="TargetESP", desc="Отображает активного таргета", type=Category.RENDER)
public class TargetESP extends Module {
	Mode mode = new Mode(this, "Режим");
	Mode.Element circle = new Mode.Element(mode, "Кругляшок");
	Mode.Element ghosts = new Mode.Element(mode, "Призраки");
	Mode.Element soul = new Mode.Element(mode, "Души");
	Mode.Element chain = new Mode.Element(mode, "Цепи" /* Висят на папе */);
	Mode.Element energy = new Mode.Element(mode, "Энергия");
	Mode.Element torus = new Mode.Element(mode, "Вода");
	Mode.Element swords = new Mode.Element(mode, "Мечи");

	CheckBox trident = new CheckBox(this, "Трезубец").set(true).hide(() -> !mode.is(chain)).desc("Цепи к трезубцу");

	CheckBox rayTrace = new CheckBox(this, "Показывать при наводке");
	
	CheckBox bloom = new CheckBox(this, "Свечение").hide(() -> !mode.is(circle) && !mode.is(soul));
	Slider speed = new Slider(this, "Скорость").min(1).max(20).inc(1).set(10).hide(() -> !mode.is(circle) && !mode.is(soul));

	Slider noise = new Slider(this, "Искажение").min(1).max(10).inc(1).set(5).hide(() -> !mode.is(torus)).desc("Сила искажения бублика");
	Slider reflect = new Slider(this, "Водянистость").min(50).max(100).inc(5).set(100).hide(() -> !mode.is(torus)).desc("Насколько сильным будет эффект \"Воды\"");
	Slider blur = new Slider(this, "Размытие").min(0).max(10).inc(1).set(0).hide(() -> !mode.is(torus)).desc("Насколько сильным будет эффект размытия");
	
	CheckBox throughWalls = new CheckBox(this, "Через стены").hide(() -> swords.get());

	@NonFinal
	TridentEntity tridentEntity;
	@NonFinal
	LivingEntity hitted;
	TimerUtility removingTimer = new TimerUtility();

	Animation targetEspAnim = new Animation().setEasing(Easing.TARGETESP_EASE_OUT_BACK).setSpeed(300);

	Animation chainTargetAnim = new Animation().setEasing(Easing.TARGETESP_EASE_OUT_BACK).setSpeed(400);
	Animation chainTarget2Anim = new Animation().setEasing(Easing.TARGETESP_EASE_OUT_BACK).setSpeed(400);

	Animation chainEspAnim = new Animation().setEasing(Easing.TARGETESP_EASE_OUT_BACK).setSpeed(600);
	Animation hurtAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(100);
	InfinityAnimation moving = new InfinityAnimation();
	ArrayList<Particle3D> particles = new ArrayList<>();

	@NonFinal LivingEntity prevTarget, target;
	
	private Vector2f pos = Vector2f.ZERO;
	private Animation 
			breakAnim = new Animation().setSpeed(250).setForward(false).setEasing(Easing.BOTH_CUBIC),
			waitToBreak = new Animation().setSpeed(250).setForward(false).setEasing(Easing.BOTH_CUBIC),
			swingToBreak = new Animation().setSpeed(250).setForward(false).setEasing(Easing.BOTH_CUBIC),
			moveSwords = new Animation().setSpeed(250).setForward(false).setEasing(Easing.BOTH_CUBIC),
			
			alpha = new Animation().setSpeed(150).setForward(false).setEasing(Easing.BOTH_CUBIC),
			show = new Animation().setSpeed(150).setForward(false).setEasing(Easing.BOTH_CUBIC),
			sword1 = new Animation().setSpeed(350).setForward(false).setEasing(Easing.BOTH_CUBIC),
			sword2 = new Animation().setSpeed(350).setForward(false).setEasing(Easing.BOTH_CUBIC),
			swing = new Animation().setSpeed(250).setForward(false).setEasing(Easing.EASE_OUT_BACK);
	
    @Override
    @EventType({EventTridentHitted.class, EventRenderPreUI.class})
    public void onEvent(Event event) {
    	if (event instanceof EventRender3D) {
			if (rock.getModules().get(Beautifully.class).get() && rock.getModules().get(Beautifully.class).getBloom().get() && Shaders.shaderPackLoaded) {
				Glass.draw(FixColor.WHITE, 0, 0, 0);
				Glass.end();
			}
    	}

    	// Цепь к трезубцу
    	if (this.mode.is(this.chain) && this.trident.get()) {
        	if (event instanceof EventTridentHitted e) {
        		if (this.tridentEntity != null && this.hitted == null && this.tridentEntity == e.getTrident()) {
        			this.hitted = e.getHitted();
        			this.tridentEntity = null;
        			this.removingTimer.reset();
        		}
        	}
        	
        	if (event instanceof EventRender3D e) {
        		MatrixStack ms = e.getMatrixStack();
        		for (Entity entity : mc.world.getAllEntities()) {
            		if (entity instanceof TridentEntity ent && mc.player.getDistance(ent) <= 2 && this.tridentEntity == null) {
            			this.tridentEntity = ent;
            		}
            	}
        		
        		if (this.hitted != null) {
        			this.renderFromToChain(ms, this.hitted);
        			if (this.removingTimer.passed(1000L)) this.hitted = null;
        		}
        		
        		if (this.tridentEntity != null) {
        			if (!mc.world.getAllEntities().contains(this.tridentEntity) || this.tridentEntity.isInGround()) {
        				this.tridentEntity = null;
        				return;
        			}
        			this.renderFromToChain(ms, this.tridentEntity);    	
        		}
        	}
    	}
    	
    	RayTraceResult traceResult = mc.objectMouseOver;
    	
    	// Таргет есп
        this.target = Stream.of(
        		rock.getModules().get(Aura.class).getTarget(),
        		rock.getModules().get(AimAssist.class).getTarget(), 
        		rock.getModules().get(AimBot.class).getTarget()
        ).filter(Objects::nonNull).findFirst().orElse(null);
        
        if (rock.getModules().containsKey(Aura.class) && target == null) {
			target = rock.getModules().get(Aura.class).getTarget();
		}
        
		if (rayTrace.get() && target == null && traceResult != null && traceResult.getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult) traceResult).getEntity();
			
			if (!(entity instanceof LivingEntity)) return;
			target = (LivingEntity) entity;
		}
        
        if (target != null) this.prevTarget = target;
        
        if (event instanceof EventRender3D e && prevTarget != null) {
            MatrixStack ms = e.getMatrixStack();
            if (mode.is(circle)) {
            	renderCircle(ms, target);
            } else if (mode.is(ghosts)) {
            	renderGhost(e, target);
            } else if (mode.is(chain)) {
            	renderChain(ms, target);
            } else if (mode.is(energy)) {
            	renderEnergy(ms, target);
            } else if (mode.is(soul)) {
            	renderSoul(ms);
            } else if (mode.is(torus)) {
            	renderTorus(ms, target);
            } else if (mode.is(swords)) {
            	updatePos(prevTarget);
            }
        }
        
        if (event instanceof EventRenderPreUI e && swords.get() && prevTarget != null && PositionTracker.isInView(prevTarget)) {
        	alpha.setForward(target != null);
        	if (alpha.finished(false)) return;
        	
			show.setForward(alpha.finished() && alpha.isForward() && target != null);
			if (sword1.finished()) sword1.setForward(!sword1.isForward());
			sword2.setForward(sword1.isForward());
			swing.setForward(target != null && target.hurtTime >= 5);
			moveSwords.setForward(target != null && target.breakShield);
			waitToBreak.setForward(moveSwords.finished() && moveSwords.isForward());
			swingToBreak.setForward(waitToBreak.finished() && waitToBreak.isForward());
			breakAnim.setForward(swingToBreak.finished() && swingToBreak.isForward() || (target != null && !target.breakShield));
			
			if (breakAnim.finished() && breakAnim.isForward()) target.breakShield = false;
			
			float size = 50;
			
			GL11.glPushMatrix();
			GL11.glTranslated(pos.x, pos.y, 0);
			
			{
				GL11.glPushMatrix();
				GL11.glRotated(-45 + 45 * show.get() - 2.5f + 5 * sword1.get() + 15 * swing.get() - 25 * moveSwords.get(), 0, 0, 1);
				
				float cord = - size  + size / 2 * show.get() + 5 * swing.get() - 35 * moveSwords.get() + 35 * swingToBreak.get();
				
				Render.drawImage(e.getMatrixStack(), "masks/freetargetesp/sword.png", cord, cord, 0, size, size, Style.getMain().alpha(alpha.get()));
				
				GL11.glPopMatrix();
			}
					
			{
				GL11.glPushMatrix();
				GL11.glRotated(315 - 45 * show.get() + 2.5f - 5 * sword1.get() - 15 * swing.get() + 25 * moveSwords.get(), 0, 0, 1);
				
				float cord = - size / 2 * show.get() - 5 * swing.get() + 35 * moveSwords.get() - 35 * swingToBreak.get();
				
				Render.drawImage(e.getMatrixStack(), "masks/freetargetesp/sword.png", cord, cord, 0, size, size, Style.getSecond().alpha(alpha.get()));
				
				GL11.glPopMatrix();
			}
			
			if (moveSwords.isForward()) {
				GL11.glPushMatrix();
				
				float right = - size / 2 + size / 2 * breakAnim.get();
				Render.drawImage(e.getMatrixStack(), "masks/freetargetesp/shieldright.png", right, - size / 2, 0, size, size, Style.getSecond().alpha((1-breakAnim.get())));
				
				float down = - size / 2 + size / 2 * breakAnim.get();
				Render.drawImage(e.getMatrixStack(), "masks/freetargetesp/shielddown.png", - size / 2, down, 0, size, size, Style.getSecond().alpha((1-breakAnim.get())));
				
				float left = - size / 2 - size / 2 * breakAnim.get();
				Render.drawImage(e.getMatrixStack(), "masks/freetargetesp/shieldleft.png", left, - size / 2, 0, size, size, Style.getSecond().alpha((1-breakAnim.get())));
				
				GL11.glPopMatrix();
			}
			
			size = 150;
			float cord = - size  + size / 2;
			
			Render.image("masks/glow.png", cord, cord, size, size, Style.getMain().alpha(show.get()*0.5f));
			
			GL11.glPopMatrix();
        }
    }
    
    

	private void updatePos(Entity e) {
        //pos = Vector2f.ZERO;
        
        if (e == null) return;
        
        float pTicks = mc.timer.renderPartialTicks;
        
        double x = e.lastTickPosX + (e.getPosX() - e.lastTickPosX) * (double) pTicks;
        double y = e.lastTickPosY + (e.getPosY() - e.lastTickPosY) * (double) pTicks + e.getHeight() / 2 + 0.2f;
        double z = e.lastTickPosZ + (e.getPosZ() - e.lastTickPosZ) * (double) pTicks;
        
        if (PositionTracker.isInView(e)) {
        	  pos.x = (float) Render.worldToScreen(x, y, z)[0];
              pos.y = (float) Render.worldToScreen(x, y, z)[1];
        }
	}
        
    private void renderTorus(MatrixStack matrices, Entity ent) {
    	double x = prevTarget.lastTickPosX + (prevTarget.getPosX() - prevTarget.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double y = prevTarget.lastTickPosY + (prevTarget.getPosY() - prevTarget.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY() + prevTarget.getHeight() / 2F + prevTarget.getHeight() / 2F * Math.sin(Math.toRadians(this.moving.get()));
		double z = prevTarget.lastTickPosZ + (prevTarget.getPosZ() - prevTarget.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
		
		this.targetEspAnim.setForward(this.target != null);
		this.moving.animate(this.moving.get() + 20, 70);
		if (this.targetEspAnim.finished(false)) return;
		
    	BufferBuilder builder = Tessellator.getInstance().getBuffer();
    	
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        
        Glass.draw(FixColor.WHITE, noise.get(), reflect.get(), blur.get());
        if (throughWalls.get()) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableCull();
		}
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        
    	matrices.push();

	    FixColor render = FixColor.WHITE.alpha(1);

	    float innerRadius = Math.max(0, 0.1f * targetEspAnim.get());
	    float outerRadius = prevTarget.getWidth();

	    Matrix4f matrix = matrices.getLast().getMatrix();
	       
	    Vector3d camera = Render.cameraPos();

	    matrices.translate(x, y, z);

	    int segments = 30;

	    for (int i = 0; i < segments; i++) {
	        double theta1 = 2 * Math.PI * i / segments;
	        double theta2 = 2 * Math.PI * (i + 1) / segments;

	        for (int j = 0; j < segments; j++) {
	            double phi1 = 2 * Math.PI * j / segments;
	            double phi2 = 2 * Math.PI * (j + 1) / segments;

	            float x1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta1));
	            float y1 = (float) (innerRadius * Math.sin(phi1));
	            float z1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta1));

	            float x2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta1));
	            float y2 = (float) (innerRadius * Math.sin(phi2));
	            float z2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta1));

	            float x3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta2));
	            float y3 = (float) (innerRadius * Math.sin(phi2));
	            float z3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta2));

	            float x4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta2));
	            float y4 = (float) (innerRadius * Math.sin(phi1));
	            float z4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta2));

	            builder.pos(matrix, x1, y1, z1).color(render.getRGB()).endVertex();
	            builder.pos(matrix, x2, y2, z2).color(render.getRGB()).endVertex();
	            builder.pos(matrix, x3, y3, z3).color(render.getRGB()).endVertex();
	            builder.pos(matrix, x4, y4, z4).color(render.getRGB()).endVertex();
	        }
	    }

	    matrices.pop();
	    tessellator.draw();
        Glass.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        Render.resetColor();
    }

    private void renderFromToChain(MatrixStack ms, Entity ent) {
    	double playerX = mc.player.lastTickPosX + (mc.player.getPosX() - mc.player.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double playerY = mc.player.lastTickPosY + (mc.player.getPosY() - mc.player.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY();
		double playerZ = mc.player.lastTickPosZ + (mc.player.getPosZ() - mc.player.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
		
		double entX = ent.lastTickPosX + (ent.getPosX() - ent.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double entY = ent.lastTickPosY + (ent.getPosY() - ent.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY() + ent.getHeight()/2F;
		double entZ = ent.lastTickPosZ + (ent.getPosZ() - ent.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
		
		float width = 1;
		
		GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0);
        mc.getTextureManager().bindTexture(FastNative.getImageResource("masks/chain.png"));
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        float yaw = Rotation.get(ent.getPositionVec()).x;
        float sin = (float) (Math.sin(Math.toRadians(-yaw-90)));
		float cos = (float) (Math.cos(Math.toRadians(-yaw-90)));

        Matrix4f matrix = ms.getLast().getMatrix();
        FixColor color = Style.getMain();
        FixColor color2 = Style.getSecond();
        int color1RGB = color.getRGB();
        int color2RGB = color2.getRGB();
        int color3RGB = color2.getRGB();
        int color4RGB = color.getRGB();
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10241, 9729);
        for (int i = 0; i < 2; i++) {
        	BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
            BUILDER.pos(matrix, (float) entX-sin, (float) (entY), (float) entZ-cos).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
            BUILDER.pos(matrix, (float) playerX-sin, (float) (playerY), (float) playerZ-cos).color(color.getRed(), color.getGreen(), color.getBlue(), color2.getAlpha()).tex(mc.player.getDistance(ent)/2, 1 - 0.01f).lightmap(0, 240).endVertex();
            BUILDER.pos(matrix, (float) playerX+sin, (float) (playerY), (float) playerZ+cos).color(color.getRed(), color.getGreen(), color.getBlue(), color2.getAlpha()).tex(mc.player.getDistance(ent)/2, 0).lightmap(0, 240).endVertex();
            BUILDER.pos(matrix, (float) entX+sin, (float) (entY), (float) entZ+cos).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).tex(0, 0).lightmap(0, 240).endVertex();
            
            TESSELLATOR.draw();
        }
        GlStateManager.disableBlend();
    }
    
    private void renderEnergy(MatrixStack ms, LivingEntity target) {
		this.targetEspAnim.setForward(this.target != null);
		this.moving.animate(this.moving.get() + 20, 70);
		if (this.targetEspAnim.finished(false)) return;
		float size = 1.5f;
		this.hurtAnim.setForward(this.prevTarget.hurtTime > 0);
		
		double x = prevTarget.lastTickPosX + (prevTarget.getPosX() - prevTarget.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double y = prevTarget.lastTickPosY + (prevTarget.getPosY() - prevTarget.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY();
		double z = prevTarget.lastTickPosZ + (prevTarget.getPosZ() - prevTarget.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
		float width = prevTarget.getWidth() * 1.5f;
       	GL11.glDisable(GL11.GL_CULL_FACE);

       	for (int i1 = 0; i1 < 2; i1++) {
       		ms.push();
            GlStateManager.depthMask(false);
            
           	ms.translate(x, y + this.prevTarget.getHeight() / 2F, z);
           	
       		ms.rotate(Vector3f.XP.rotationDegrees(90));
           	ms.rotate(Vector3f.ZP.rotationDegrees(this.moving.get() * 2 + 180 * i1));
           	
           	for (int i = 0; i < 4; i++) {
           		if (throughWalls.get()) {
        			RenderSystem.disableDepthTest();
        			RenderSystem.disableCull();
        		}
           		Render.drawImage(ms, "masks/energy.png", (float) -size / 2, -size / 2, 0, size, size, 
                		Style.getPoint(90).alpha(this.targetEspAnim.get()),
                		Style.getPoint(180).alpha(this.targetEspAnim.get()),
                		Style.getPoint(240).alpha(this.targetEspAnim.get()),
                		Style.getPoint(360).alpha(this.targetEspAnim.get()));
           	}
           	
           	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729);
            GlStateManager.depthMask(true);
            ms.pop();
       	}
    }
    
	private void renderChain(MatrixStack ms, LivingEntity target) {
		this.chainTargetAnim.setForward(this.target != null);
		this.chainTarget2Anim.setForward(this.chainTargetAnim.get() >= 0.95f);
		this.moving.animate(this.moving.get() + 20, 70);
		if (this.chainTargetAnim.finished(false)) return;
		float size = 1f;
		Render.startImageRendering("masks/glow.png");
		
		if (throughWalls.get()) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableCull();
		}
		
		this.hurtAnim.setForward(this.prevTarget.hurtTime > 0);
		
		double entX = prevTarget.lastTickPosX + (prevTarget.getPosX() - prevTarget.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double entY = prevTarget.lastTickPosY + (prevTarget.getPosY() - prevTarget.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY() - 0.5f;
		double entZ = prevTarget.lastTickPosZ + (prevTarget.getPosZ() - prevTarget.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
		
		float rotSpeed = 0.5f;
		float chainSize = 4;
		
        float down = 1;
        
        float gradusX = (float) (20 * Math.min(1+Math.sin(Math.toRadians(this.moving.get())), 1));
        float gradusZ = (float) (20 * (Math.min(1+Math.sin(Math.toRadians(this.moving.get())), 2)-1));
        float width = prevTarget.getWidth() * 1.5f;
        
		for (int chain = 0; chain < 2; chain++) {
			float val = 1.2f - 0.5f * (chain == 0 ? chainTargetAnim.get() : this.chainTarget2Anim.get());
			
			ms.push();
			ms.translate(entX, entY + prevTarget.getHeight() / 2, entZ);
			float x = 0, y = 0, z = 0;
	        
	        mc.getTextureManager().bindTexture(FastNative.getImageResource("masks/chain.png"));
	        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

	        Matrix4f matrix = ms.getLast().getMatrix();
	        
	    	ms.rotate(Vector3f.ZP.rotationDegrees(chain == 0 ? gradusX : -gradusX));
	    	ms.rotate(Vector3f.XP.rotationDegrees(chain == 0 ? gradusZ : -gradusZ));
	    	 
        	GL11.glDisable(GL11.GL_CULL_FACE);
            BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
            
            int modif = 45/2;
           // chainSize *= modif;
            for (int i = 0; i<360*2; i+=modif) {
            	FixColor color = Style.getPoint(i * 3).alpha(chain == 0 ? chainTargetAnim.get() : this.chainTarget2Anim.get()).darker(0);
                
    			float prevSin = (float) (x + (chain == 0 ? gradusX : -gradusX) / 100F + Math.sin(Math.toRadians(i-modif+this.moving.get() * rotSpeed)) * width * val);
    			float prevCos = (float) (z + (chain == 0 ? -gradusZ : gradusZ) / 100F+ Math.cos(Math.toRadians(i-modif+this.moving.get() * rotSpeed)) * width * val);
    			
    			float sin = (float) (x + (chain == 0 ? gradusX : -gradusX) / 100F+ Math.sin(Math.toRadians(i+this.moving.get() * rotSpeed)) * width * val);
    			float cos = (float) (z +(chain == 0 ? -gradusZ : gradusZ) / 100F+  Math.cos(Math.toRadians(i+this.moving.get() * rotSpeed)) * width * val);
    			
    			int r = color.getRed();
    			int g = color.getGreen();
    			int b = color.getBlue();
    			int a = color.getAlpha();

    			float max = 1/(float)modif;
    			float circle = 1/(float)modif;
    			float coff = i/(float)modif;
    			
    			//System.out.println(i + " - " + max/circle*coff*chainSize + " - " + 1/360F*(float)i*chainSize);
    			BUILDER.pos(matrix, (float) prevSin, (float) y, (float) prevCos).color(r, g, b, a).tex(1/360F*(float)(i-modif)*chainSize, 0).lightmap(0, 240).endVertex();
    			BUILDER.pos(matrix, (float) (sin), (float) y, (float) cos).color(r, g, b, a).tex(1/360F*(float)(i)*chainSize, 0).lightmap(0, 240).endVertex();
    			BUILDER.pos(matrix, (float) (sin), (float) (y + down), (float) cos).color(r, g, b, a).tex(1/360F*(float)(i)*chainSize, 1 - 0.01f).lightmap(0, 240).endVertex();
    			BUILDER.pos(matrix, (float) prevSin, (float) (y + down), (float) prevCos).color(r, g, b, a).tex(1/360F*(float)(i-modif)*chainSize, 1 - 0.01f).lightmap(0, 240).endVertex();
            }
            
            TESSELLATOR.draw();

	        ms.pop();
		}
        
		Render.finishImageRendering();
	}
	
    private void renderCircle(MatrixStack ms, LivingEntity target) {
    	targetEspAnim.setSpeed(500);
		this.targetEspAnim.setForward(this.target != null);
		this.moving.animate(this.moving.get() + 10 + speed.get(), 70);
		if (this.targetEspAnim.finished(false)) return;
		float size = 0.3f;
		float bigSize = 1f;
		Render.startImageRendering("masks/glow.png");
		this.hurtAnim.setForward(this.prevTarget.hurtTime > 0);
		
		if (throughWalls.get()) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableCull();
		}
		
		double x = prevTarget.lastTickPosX + (prevTarget.getPosX() - prevTarget.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double y = prevTarget.lastTickPosY + (prevTarget.getPosY() - prevTarget.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY();
		double z = prevTarget.lastTickPosZ + (prevTarget.getPosZ() - prevTarget.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
		float width = prevTarget.getWidth() * 1.5f;
		
		for (int i = 0; i<360; i++) {
			if ((int) (i / 45F) % 2 == 0) continue;
			
			float val = Math.max(0.5f, (0.7f-0.2f * this.hurtAnim.get()) + 0.2f - 0.2f * this.targetEspAnim.get());
			float sin = (float) (x + Math.sin(Math.toRadians(i+this.moving.get() * 1)) * width * val);
			float cos = (float) (z + Math.cos(Math.toRadians(i+this.moving.get() * 1)) * width * val);
			
			ms.push();
			ms.translate(sin, y + prevTarget.getHeight() / 2 + prevTarget.getHeight() / 2F * Math.sin(Math.toRadians(this.moving.get())), cos);
			ms.rotate(mc.getRenderManager().info.getRotation());
            
			if (bloom.get())
            	Render.drawCleanImage(ms, (float) -bigSize / 2, -bigSize / 2, (float) -size / 2, bigSize, bigSize, Style.getPoint(i*3).alpha(targetEspAnim.get() * 0.05f));
			
            Render.drawCleanImage(ms, (float) -size / 2, -size / 2, (float) -size / 2, size, size, Style.getPoint(i*3).alpha(targetEspAnim.get()));
            
            ms.pop();
		}
        
		Render.finishImageRendering();
    }

    private void renderGhost(EventRender3D e, LivingEntity target) {
    	float count = 3;
		if (this.particles.size() < count && target != null) {
			this.particles.add(new Particle3D(prevTarget.getPositionVec(), new Vector3d(0, 0, 0), 0.5f));
		}
		ArrayList<Particle3D> removing = new ArrayList<>();
		Render.startImageRendering("masks/glow.png");
		
		if (throughWalls.get()) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableCull();
		}
		
		for (Particle3D particle : this.particles) {
			this.moving.animate(this.moving.get() + 20, 40);
			this.targetEspAnim.setSpeed(150);
			this.targetEspAnim.setForward(prevTarget.hurtTime > 7);
			Beautifully aspect = rock.getModules().get(Beautifully.class);
			float angle = this.moving.get() + this.particles.indexOf(particle) * 360/count;
			float size = 1.3f * (aspect.get() ? aspect.getAspectRatio().get() : 1);
			float x = (float) Math.sin(Math.toRadians(angle)) * (size-this.targetEspAnim.get()*size);
			float z = (float) Math.cos(Math.toRadians(angle)) * (size-this.targetEspAnim.get()*size);
			float mul = 0.05f / Math.max((float) Minecraft.debugFPS, 5) * 500;
			
			if (target == null) {
				mul *= 0.2f;
				particle.setMotion(prevTarget.getPositionVec().subtract(particle.getPosition()).mul(mul, mul, mul));
			} else {
				particle.setMotion(prevTarget.getPositionVec().add(x, 0.2f + target.getHeight() / 2 * Math.sin(Math.toRadians(moving.get()/(this.particles.indexOf(particle)+1))), z).subtract(particle.getPosition()).mul(mul, mul, mul));
			}
			
			particle.render(e);
			
			if (target == null || particle.getAlpha() < 1) particle.setAlpha(particle.getAlpha() - 0.005f / Math.max((float) Minecraft.debugFPS, 5) * 300);
			if (particle.getAlpha() < 0) removing.add(particle);
		}
		Render.finishImageRendering();
		this.particles.removeAll(removing);
    }
    
    private void renderSoul(MatrixStack ms) {
    	targetEspAnim.setEasing(Easing.BOTH_CUBIC);
    	targetEspAnim.setSpeed(300);
		this.targetEspAnim.setForward(this.target != null);
		this.moving.animate(this.moving.get() + 10 + speed.get(), 70);
		if (this.targetEspAnim.finished(false)) return;
		
		Render.startImageRendering("masks/glow.png");
		this.hurtAnim.setForward(this.prevTarget.hurtTime > 0);
		
		if (throughWalls.get()) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableCull();
		}
		
		double x = prevTarget.lastTickPosX + (prevTarget.getPosX() - prevTarget.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
		double y = prevTarget.lastTickPosY + (prevTarget.getPosY() - prevTarget.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getY();
		double z = prevTarget.lastTickPosZ + (prevTarget.getPosZ() - prevTarget.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();
		float width = prevTarget.getWidth() * 1.5f;
		
		int step = 2;
		int wormTick = 0;
		int wormCD = 0;
		int wormCount = 0;
		
		for (int i = 0; i<360; i+=step) {
			float size = 0.2f + 0.005f * (float) wormTick;
			float bigSize = 1f + 0.005f * (float) wormTick;
			
			if (wormCD > 0) {
				wormCD-=step;
				continue;
			}
			
			wormTick+=step;
			
			if (wormTick > 50) {
				wormCD = 100;
				wormTick = 0;
				wormCount++;
				continue;
			}
			
			float val = Math.max(0.5f, 0.7f + 0.5f - 0.5f * targetEspAnim.get());
			float sin = (float) (x + Math.sin(Math.toRadians(i+moving.get() * 1)) * width * val);
			float cos = (float) (z + Math.cos(Math.toRadians(i+moving.get() * 1)) * width * val);
			
			ms.push();
			ms.translate(sin, y + prevTarget.getHeight() / 1.5f + prevTarget.getHeight() / 3F * Math.sin(Math.toRadians(i / 2F + moving.get() / 5F)), cos);
			ms.rotate(mc.getRenderManager().info.getRotation());
            
			if (bloom.get())
            	Render.drawCleanImage(ms, (float) -bigSize / 2, -bigSize / 2, (float) -size / 2, bigSize, bigSize, Style.getPoint(i*3).alpha(targetEspAnim.get() * 0.05f));
			
            Render.drawCleanImage(ms, (float) -size / 2, -size / 2, (float) -size / 2, size, size, Style.getPoint(i*3).alpha(targetEspAnim.get()));
            
            ms.pop();
		}
        
		Render.finishImageRendering();
    }
    
    @Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
