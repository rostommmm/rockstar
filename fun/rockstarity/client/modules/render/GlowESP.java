package fun.rockstarity.client.modules.render;

import java.awt.Color;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.shaders.list.ClientBloom;
import fun.rockstarity.api.render.shaders.list.ClientOutline;
import fun.rockstarity.api.render.shaders.list.Outline2;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.shaders.Shaders;

/**
 * @author ConeTin
 * @since 11 дек. 2023 г.
 */

//@PriorityInfo(Priority.LOW)

@Info(name = "GlowESP", desc = "Рисует свечение у сущностей", type = Category.RENDER)
public class GlowESP extends Module {

	public static boolean SILENT_RENDERING;
	
	public static Framebuffer defaultFrameBuffer = new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(),true);
	public static Framebuffer friendsFrameBuffer = new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(),true);

	public static Framebuffer outlineFrameBuffer = new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(),true);
	public static Framebuffer glowFrameBuffer = new Framebuffer(sr.getFramebufferWidth(), sr.getFramebufferHeight(),true);

	public static final Outline2 outlineShader = new Outline2();

    public static final ClientBloom glowShader = new ClientBloom();

    private static final ClientOutline glowOutlineShader = new ClientOutline();
    
	private final Select targets = new Select(this, "Отображать").min(1);
	
	private final Select.Element self = new Select.Element(targets, "Себя").set(true);
	private final Select.Element players = new Select.Element(targets, "Игроков").set(true);
	private final Select.Element mobs = new Select.Element(targets, "Мобов");
	private final Element bots = new Element(targets, "Ботов");
	
	private final Select elements = new Select(this, "Выбор").min(1);
	
	private final Select.Element glow = new Select.Element(elements, "Свечение").set(true);
	private final Select.Element outline = new Select.Element(elements, "Обводка").set(true);

	private final Slider glowSize = new Slider(this, "Размер свечения").min(10f).max(30).inc(0.5f).set(20);
	private final Slider strength = new Slider(this, "Сила свечения").min(0.5f).max(1).inc(0.1f).set(0.7f);
	
	private final CheckBox highlightFriends = new CheckBox(this, "Выделять друзей").set(true);

	@Override
	public void onEvent(Event event) {
		if (Shaders.shaderPackLoaded) return;
		
		if (event instanceof EventRenderWorld e)
			this.handleRenderWorld(e);

		if (event instanceof EventRender2D e)
			this.handleRender2D(e);

		if (event instanceof EventWorldChange) {
			this.defaultFrameBuffer.framebufferClear();
			this.friendsFrameBuffer.framebufferClear();
		}
	}
	
	private void handleRenderWorld(Event e) {
		MatrixStack matrixStack = new MatrixStack();
		ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();

		SILENT_RENDERING = true;
		
		{ // Запись в фреймбаффер обычных энтити
			this.defaultFrameBuffer = Shader.createFrameBuffer(defaultFrameBuffer);
			this.defaultFrameBuffer.framebufferClear();
			this.defaultFrameBuffer.bindFramebuffer(true);
			
			renderOthers(mc.getRenderPartialTicks(), matrixStack, activeRenderInfo);
			
			this.defaultFrameBuffer.unbindFramebuffer();
		}
		
		if (this.highlightFriends.get()) { // Запись в фреймбаффер друзей
			this.friendsFrameBuffer = Shader.createFrameBuffer(friendsFrameBuffer);
			this.friendsFrameBuffer.framebufferClear();
			this.friendsFrameBuffer.bindFramebuffer(true);
			
			renderFriends(mc.getRenderPartialTicks(), matrixStack, activeRenderInfo);
			
			this.friendsFrameBuffer.unbindFramebuffer();
		} else {
			this.friendsFrameBuffer.framebufferClear();
		}
		
		mc.getFramebuffer().bindFramebuffer(true);
		
		SILENT_RENDERING = false;
	}
	
	private void renderOthers(float ticks, MatrixStack stack, ActiveRenderInfo activeRenderInfo) {
		for (Entity entity : mc.world.getAllEntities()) {
			if (!isValid(entity) || this.isFriend(entity))
				continue;
			
			EntityRendererManager renderManager = mc.getRenderManager();
			
			if (renderManager == null)
				continue;

			float partialTicks = mc.getRenderPartialTicks();
			
			Vector3d camera = activeRenderInfo.getProjectedView();
			Vector3d pos = entity.getPositionVec();
			
			renderManager.renderEntityStaticSilent(
					entity, 
					MathHelper.lerp(partialTicks, entity.lastTickPosX, pos.x) - camera.getX(), 
					MathHelper.lerp(partialTicks, entity.lastTickPosY, pos.y) - camera.getY(), 
					MathHelper.lerp(partialTicks, entity.lastTickPosZ, pos.z) - camera.getZ(), 
					MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw), 
					partialTicks, stack, 
					mc.getRenderTypeBuffers().getBufferSource(), 
					renderManager.getPackedLight(entity, partialTicks)
			);
		}
	}
	
	private void renderFriends(float ticks, MatrixStack stack, ActiveRenderInfo activeRenderInfo) {
		for (Entity entity : mc.world.getAllEntities()) {
			if (!isValid(entity) || !this.isFriend(entity))
				continue;
			
			EntityRendererManager renderManager = mc.getRenderManager();
			
			if (renderManager == null)
				continue;

			float partialTicks = mc.getRenderPartialTicks();
			
			Vector3d camera = activeRenderInfo.getProjectedView();
			Vector3d pos = entity.getPositionVec();
			
			renderManager.renderEntityStaticSilent(
					entity, 
					MathHelper.lerp(partialTicks, entity.lastTickPosX, pos.x) - camera.getX(), 
					MathHelper.lerp(partialTicks, entity.lastTickPosY, pos.y) - camera.getY(), 
					MathHelper.lerp(partialTicks, entity.lastTickPosZ, pos.z) - camera.getZ(), 
					MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw), 
					partialTicks, stack, 
					mc.getRenderTypeBuffers().getBufferSource(), 
					renderManager.getPackedLight(entity, partialTicks)
			);
		}
	}
	
	private void handleRender2D(EventRender2D e) {
		render2D(defaultFrameBuffer, this.glowSize.get(), this.strength.get(), this.strength.get(), this.outline.get(), false, false);
		
		if (this.highlightFriends.get()) render2D(friendsFrameBuffer, this.glowSize.get(), this.strength.get(), this.strength.get(), this.outline.get(), false, true);
	}

	private void render2D(Framebuffer framebuffer, float blurRadius, float blurExposure, float outlineExposure, boolean outline, boolean full, boolean friends) {
		boolean glow = this.glow.get();
        if (framebuffer != null && outlineFrameBuffer != null) {
            /**GLOW*/
        	//Render.bindTexture(framebuffer.framebufferTexture);
            //Shader.drawScaledQuads();
            if (glow) {
                float rng = blurRadius;
                float exposure = blurExposure;
                RenderSystem.enableAlphaTest();
                RenderSystem.alphaFunc(516, 0.0f);
                RenderSystem.enableBlend();
                int dir1 = 0;
                float dir2 = 0.3f;
                RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                outlineFrameBuffer.framebufferClear();
                outlineFrameBuffer.bindFramebuffer(true);
                outlineShader.start();
                setupOutlineUniforms(dir1, dir2,rng);
                Render.bindTexture(framebuffer.framebufferTexture);
                Shader.drawScaledQuads();
                outlineShader.start();
                setupOutlineUniforms(dir2, dir1,rng);
                Render.bindTexture(framebuffer.framebufferTexture);
                Shader.drawScaledQuads();
                outlineShader.finish();
                outlineFrameBuffer.unbindFramebuffer();

                RenderSystem.color4f(1, 1, 1, 1);
                glowFrameBuffer.framebufferClear();
                glowFrameBuffer.bindFramebuffer(true);
                glowShader.start();
                setupGlowUniforms(dir1, dir2,rng,exposure,friends);
                Render.bindTexture(full ? framebuffer.framebufferTexture : outlineFrameBuffer.framebufferTexture);
                Shader.drawScaledQuads();
                glowShader.finish();
                glowFrameBuffer.unbindFramebuffer();


                mc.getFramebuffer().bindFramebuffer(true);
                glowShader.start();
                setupGlowUniforms(dir2, dir1,rng,exposure,friends);

                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                Render.bindTexture(glowFrameBuffer.framebufferTexture);
                Shader.drawScaledQuads();
                RenderSystem.color4f(1f,1f,1f,1f);
                glowShader.finish();
            }
            
            /**OUTLINE*/
            if (outline){
                float range = 0.55f;
                float exposure = outlineExposure;
                RenderSystem.enableAlphaTest();
                RenderSystem.alphaFunc(516, 0.0f);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                outlineFrameBuffer.framebufferClear();
                outlineFrameBuffer.bindFramebuffer(true);
                outlineShader.start();
                setupOutlineUniforms(0, 1, 1.8f);
                RenderSystem.bindTexture(framebuffer.framebufferTexture);
                Shader.drawScaledQuads();
                outlineShader.start();
                setupOutlineUniforms(1, 0,1.8f);
                RenderSystem.bindTexture(framebuffer.framebufferTexture);
                Shader.drawScaledQuads();
                outlineShader.finish();
                outlineFrameBuffer.unbindFramebuffer();

                RenderSystem.color4f(1, 1, 1, 1);
                glowFrameBuffer.framebufferClear();
                glowFrameBuffer.bindFramebuffer(true);
                glowOutlineShader.start();
                setupGlowOutlineUniforms(0, 1,range,exposure,friends);
                RenderSystem.bindTexture(outlineFrameBuffer.framebufferTexture);
                Shader.drawScaledQuads();
                glowOutlineShader.finish();
                glowFrameBuffer.unbindFramebuffer();

                mc.getFramebuffer().bindFramebuffer(true);
                glowOutlineShader.start();
                setupGlowOutlineUniforms(1, 0,range,exposure,friends);

                //GL13.glActiveTexture(GL13.GL_TEXTURE16);
                //Render.bindTexture(framebuffer.framebufferTexture);
                if (outline && !full) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE16);
                    Render.bindTexture(framebuffer.framebufferTexture);
                }
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                Render.bindTexture(glowFrameBuffer.framebufferTexture);
                Shader.drawScaledQuads();
                RenderSystem.color4f(1f,1f,1f,1f);
                glowOutlineShader.finish();
            }
        }

    }
    
	public static void setupOutlineUniforms(float dir1, float dir2, float range) {
        float rng = 10;
        FixColor color = FixColor.WHITE;
        outlineShader.setInt("texture", 0);
        outlineShader.setFloat("radius", range);
        outlineShader.setFloat("texelSize", 1.0f / sr.getScaledWidth(), 1.0f / sr.getScaledHeight());
        outlineShader.setFloat("direction", dir1, dir2);
        outlineShader.setFloat("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }
	public static final long detime = System.currentTimeMillis();

	public static void setupGlowOutlineUniforms(float dir1, float dir2, float range, float exposure, boolean friend) {
        glowOutlineShader.setFloat("noColorfulColor", 0.12f,0.12f,0.12f);

        glowShader.setInt("colorful", 1);

        FixColor color = FixColor.WHITE;
        glowOutlineShader.setInt("u_texture", 16);
        //glowShader.setInt("u_texel_size", 0);
        glowOutlineShader.setFloat("u_radius", range);
        glowOutlineShader.setFloat("u_texel_size", 1.0f / sr.getScaledWidth(), 1.0f / sr.getScaledHeight());
        glowOutlineShader.setFloat("resa", (float) (200 * sr.getGuiScaleFactor()), (float) ((sr.getHeight() - (120 * sr.getGuiScaleFactor())) - (200 * sr.getGuiScaleFactor())));

        glowOutlineShader.setFloat("u_direction", dir2, dir1);
        glowOutlineShader.setFloat("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        Color color1 = Style.getPoint(0);
        Color color2 = Style.getPoint(90);
        Color color3 = Style.getPoint(180);
        Color color4 = Style.getPoint(230);

        //if (!ClientUI.isColorful()) {
        //    color1 = new Color(0);
        ///    color2 = new Color(0x1C1C1C);
        //}
        if (friend) {
        	 color1 = Color.green;
        	 color2 = Color.green;
        	 color3 = Color.green;
        	 color4 = Color.green;
        }
        float r = color1.getRed() / 255f;
        float g = color1.getGreen() / 255f;
        float b = color1.getBlue() / 255f;
        float rr = color2.getRed() / 255f;
        float gg = color2.getGreen() / 255f;
        float bb = color2.getBlue() / 255f;
        float rrr = color3.getRed() / 255f;
        float ggg = color3.getGreen() / 255f;
        float bbb = color3.getBlue() / 255f;
        float rrrr = color4.getRed() / 255f;
        float gggg = color4.getGreen() / 255f;
        float bbbb = color4.getBlue() / 255f;
        glowOutlineShader.setFloat("color1", r, g, b);
        glowOutlineShader.setFloat("color2", rr, gg, bb);
        glowOutlineShader.setFloat("color3", rrr, ggg, bbb);
        glowOutlineShader.setFloat("color4", rrrr, gggg, bbbb);

        glowOutlineShader.setFloat("exposure",exposure);
        glowOutlineShader.setInt("avoidTexture", 1);
        glowOutlineShader.setFloat("time", (System.currentTimeMillis() - detime) / 1000F);
        glowOutlineShader.setFloat("colorSpeed", 35);
        glowOutlineShader.setInt("blurAgain", 0);
        glowOutlineShader.setInt("rainbow", 0);
        glowOutlineShader.setFloat("blurAgainRadius", range);
        glowOutlineShader.setFloat("gradientRange", 10);

        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        for (int i = 1; i <= range; i++) {
            buffer.put(MathUtility.calculateGaussianValue(i, range / 2));
        }
        buffer.rewind();

        RenderSystem.glUniform1(glowOutlineShader.getInt("u_kernel"), buffer);
        glowOutlineShader.setFloat("force", 1f);
    }
	
	private void setupGlowUniforms(float dir1, float dir2, float range, float exposure,boolean friend) {
		boolean blurAgain = false;
        Color color = Color.white;

        glowShader.setFloat("noColorfulColor", 0.12f,0.12f,0.12f);

        glowShader.setInt("colorful", 1);

        glowShader.setInt("customColor", 0);
        glowShader.setInt("u_other_sampler", 16);
        glowShader.setInt("u_diffuse_sampler", 0);
        glowShader.setFloat("u_radius", range);
        glowShader.setFloat("u_texel_size", 1.0f / sr.getScaledWidth(), 1.0f / sr.getScaledHeight());
        glowShader.setFloat("resa", (float) (200 * sr.getGuiScaleFactor()), (float) ((sr.getHeight() - (120 * sr.getGuiScaleFactor())) - (200 * sr.getGuiScaleFactor())));

        glowShader.setFloat("u_direction", dir2, dir1);
        glowShader.setFloat("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        Color color1 = Style.getPoint(0);
        Color color2 = Style.getPoint(90);
        Color color3 = Style.getPoint(180);
        Color color4 = Style.getPoint(230);

        if (friend) {
        	color1 = Color.green;
          	 color2 = Color.green;
          	 color3 = Color.green;
          	 color4 = Color.green;
        }
        float r = color1.getRed() / 255f;
        float g = color1.getGreen() / 255f;
        float b = color1.getBlue() / 255f;
        float rr = color2.getRed() / 255f;
        float gg = color2.getGreen() / 255f;
        float bb = color2.getBlue() / 255f;
        float rrr = color3.getRed() / 255f;
        float ggg = color3.getGreen() / 255f;
        float bbb = color3.getBlue() / 255f;
        float rrrr = color4.getRed() / 255f;
        float gggg = color4.getGreen() / 255f;
        float bbbb = color4.getBlue() / 255f;

        glowShader.setFloat("color1", r, g, b);
        glowShader.setFloat("color2", rr, gg, bb);
        glowShader.setFloat("color3", rrr, ggg, bbb);
        glowShader.setFloat("color4", rrrr, gggg, bbbb);

        glowShader.setFloat("exposure",exposure);
        glowShader.setInt("avoidTexture", 1);
        glowShader.setFloat("time", (System.currentTimeMillis() - detime) / 1000F);
        glowShader.setFloat("colorSpeed", 35);
        glowShader.setInt("blurAgain", blurAgain ? 1 : 0);
        glowShader.setInt("rainbow", 0);
        glowShader.setFloat("blurAgainRadius", 10);
        glowShader.setFloat("gradientRange", 10);
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        if (range > 1) {
            for (int i = 1; i <= range; i++) {
                buffer.put(MathUtility.calculateGaussianValue(i, range / 2));
            }
        }else{
            for (float i = 0.01f; i <= range; i+=0.01f) {
                buffer.put(MathUtility.calculateGaussianValue(i, range / 2));
            }
        }
        
        if (blurAgain) {
            for (float i = 0.1f; i <= range; i+= 0.1f) {
                buffer.put(MathUtility.calculateGaussianValue(i, range / 2));
            }
        }
        
        buffer.rewind();

        RenderSystem.glUniform1(glowShader.getInt("u_kernel"), buffer);
        glowShader.setFloat("force", 1f);
    }
	
	private boolean isFriend(Entity entity) {
		return this.highlightFriends.get() && rock.getFriendsHandler().isFriend(entity);
	}

	private boolean isValid(Entity entity) {
	    if (!(entity instanceof LivingEntity)) return false;
	    
	    if (!entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(entity.getName().getString())) && !bots.get()) return false;
	    
	    // Если мобы оффнуты, то скипаем мобов
	    if (((entity instanceof MobEntity || entity instanceof AnimalEntity) && !this.mobs.get())) return false;
	    
	    // Если игроки оффнуты, скипаем игроков
	    if (entity instanceof PlayerEntity && !this.players.get() && entity != mc.player) return false;
	    
	    // Проверка на себя
	    return !(entity instanceof ArmorStandEntity) && entity != mc.player || (!mc.getGameSettings().getPointOfView().func_243192_a() && self.get());
	}

	@Override
	public void onEnable() {
		
	}

	@Override
	public void onDisable() {
		
	}

}