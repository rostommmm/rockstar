package fun.rockstarity.client.modules.combat;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.Reacher;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.client.modules.render.GlowESP;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 14 апр. 2024 г. 16:10:02
 */

/*
 * С чего вы дети пасту принимаете за скид?
 * А код деда поколенью дал поддержки больше СМИ
 * За сколько долларов, ответь, спастил бы бэк трэк
 * Порванный ценник, потому что, сука, это не купить
 * 
 * Хвиды на пастебине Raw (йааааа!)
 * Факаю чела когда он лоу (ваааау!)
 * Обморожено лицо, даже селфкодер не помог понять
 * Что это за пастерок (ктаа?)
 */

@Info(name="BackTrack", desc="Задерживает хитбокс", type=Category.COMBAT)
public class BackTrack extends Module {
	
	private final Mode mode = new Mode(this, "Отображать..").desc("Позволяет выбрать режим отображения");
	
	private final Mode.Element nothing = new Mode.Element(mode, "Ничего");
	private final Mode.Element entity = new Mode.Element(mode, "Модель");
	private final Mode.Element box = new Mode.Element(mode, "Хитбокс");
	
	private final Slider time = new Slider(this, "Время").min(50).max(1000).inc(50).set(100).desc("Позволяет выбрать время, через которое след за игроком удаляться");
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate) {
			for (PlayerEntity player : mc.world.getPlayers()) {
				if (player == mc.player || rock.getFriendsHandler().isFriend(player.getName().getString())) continue;
				if (player.getBacktrack().size() > 2) {
					player.getBacktrack().remove(0);
        		}
			}
		}
		
		if (event instanceof EventRenderWorld && mode.is(entity)) {
			MatrixStack matrixStack = new MatrixStack();
			ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();

			float time = this.time.get();
			
			GlowESP.SILENT_RENDERING = true;
			
			GL11.glPushMatrix();
			
			GlStateManager.enableBlend();
			GlStateManager.depthMask(false);
			GlStateManager.disableTexture();
			GL11.glShadeModel(7425);
			GlStateManager.disableCull();
			GlStateManager.enableDepthTest();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
			GlStateManager.glBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE.param, GlStateManager.SourceFactor.ZERO.param, GlStateManager.DestFactor.ONE.param);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			
			
			for (PlayerEntity player : mc.world.getPlayers()) {
				if (player == mc.player || rock.getFriendsHandler().isFriend(player.getName().getString())) continue;
				for (Position position : player.getBacktrack()) {
					Vector3d cameraPos = mc.getRenderManager().info.getProjectedView();
					try {
						Vector3d pos = position.getPos();
						double x = pos.x - mc.getRenderManager().info.getProjectedView().getX(),
								y = pos.y - mc.getRenderManager().info.getProjectedView().getY(),
					        	z = pos.z - mc.getRenderManager().info.getProjectedView().getZ();
				        
						EntityRendererManager renderManager = mc.getRenderManager();
						
						if (renderManager == null)
							continue;

						float partialTicks = mc.getRenderPartialTicks();
						
						Vector3d camera = activeRenderInfo.getProjectedView();
						
						Reacher.ENTITY_ALPHA = (1 - (float) (System.currentTimeMillis() - position.getTime()) / (float) time) * 0.5f;
						Reacher.SILENT = true;
						
						renderManager.renderEntityStaticSilent(
								player, 
								x, 
								y, 
								z, 
								MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw), 
								partialTicks, matrixStack, 
								mc.getRenderTypeBuffers().getBufferSource(), 
								renderManager.getPackedLight(player, partialTicks)
						);
						
						Reacher.ENTITY_ALPHA = 1;
						Reacher.SILENT = false;
						
						if ((System.currentTimeMillis() - position.getTime()) > time) {
							player.getBacktrack().remove(position);
							break;
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			GlStateManager.blendFunc(770, 771);
			GlStateManager.shadeModel(7424);
			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
			GlStateManager.depthMask(true);
			GlStateManager.enableCull();
			GlStateManager.popMatrix();
			GlowESP.SILENT_RENDERING = false;
		}
		
		
		if (event instanceof EventRender3D e && mode.is(box)) {
			
			for (PlayerEntity player : mc.world.getPlayers()) {
				if (player == mc.player || rock.getFriendsHandler().isFriend(player.getName().getString())) continue;
				for (Position position : player.getBacktrack()) {
					Vector3d cameraPos = mc.getRenderManager().info.getProjectedView();
					try {
						Vector3d pos = position.getPos();
						GL11.glRotated(e.getRenderInfo().getPitch(), 1.0, 0.0, 0.0);
						GL11.glRotated(e.getRenderInfo().getYaw() + 180, 0.0, 1.0, 0.0);
						double x = pos.x - mc.getRenderManager().info.getProjectedView().getX(),
								y = pos.y - mc.getRenderManager().info.getProjectedView().getY(),
					        	z = pos.z - mc.getRenderManager().info.getProjectedView().getZ();
						GL11.glPushMatrix();
				        GL11.glEnable(GL11.GL_BLEND);
				        GL11.glLineWidth(2);
				        GL11.glDisable(GL11.GL_TEXTURE_2D);
				        GL11.glDisable(GL11.GL_DEPTH_TEST);
				        
				        Render.setColor(Style.getMain().alpha(1 - (float) (System.currentTimeMillis() - position.getTime()) / time.get()).getRGB());
				        Render.drawBoxing(new AxisAlignedBB(x-0.3f, y, z-0.3f, x + 0.3f, y+player.getHeight(), z + 0.3f));
				        GL11.glLineWidth(9.5f);
				        GL11.glEnable(GL11.GL_TEXTURE_2D);
				        GL11.glEnable(GL11.GL_DEPTH_TEST);
				        GL11.glDisable(GL11.GL_BLEND);
				        Render.resetColor();
				        GL11.glPopMatrix();
				        
				        GL11.glRotated(e.getRenderInfo().getYaw() + 180, 0.0, -1.0, 0.0);
						GL11.glRotated(e.getRenderInfo().getPitch(), -1.0, 0.0, 0.0);
						
						
						if ((System.currentTimeMillis() - position.getTime()) > this.time.get()) {
							player.getBacktrack().remove(position);
							break;
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		
	}
	
	@Override
	@NativeInclude
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
	
	@Getter
	@AllArgsConstructor
	public static class Position {
		private final Vector3d pos;
		private final long time;
	}
}
