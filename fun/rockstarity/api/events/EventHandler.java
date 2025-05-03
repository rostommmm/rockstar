 package fun.rockstarity.api.events;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.autobuy.ui.AutoBuyScreen;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.connection.globals.ClientAPI;
import fun.rockstarity.api.constuctor.ConstructorScreen;
import fun.rockstarity.api.events.list.game.EventChat;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventMessage;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.ui.shaders.EventBlur;
import fun.rockstarity.api.events.list.render.world.EventUpdateCamera;
import fun.rockstarity.api.helpers.game.BoostUtility;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.ai.AIPredictor;
import fun.rockstarity.api.helpers.player.InvUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.PremiumModule;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.globals.marks.MarkManager;
import fun.rockstarity.api.render.menufilter.MenuFilter;
import fun.rockstarity.api.render.models.taksa.TaksaAI;
import fun.rockstarity.api.render.models.taksa.TaksaScheduler;
import fun.rockstarity.api.render.optimize.culling.processor.CullingProcessor;
import fun.rockstarity.api.render.shaders.list.Blur;
import fun.rockstarity.api.render.shaders.list.FrameFreeze;
import fun.rockstarity.api.render.shaders.list.KawaseBlur;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiRenderer;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiScreen;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiWindow;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.alt.BanProcessor;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.scripts.Script;
import fun.rockstarity.api.sounds.Sound;
import fun.rockstarity.api.via.ViaFixer;
import fun.rockstarity.client.modules.other.AutoBuy;
import fun.rockstarity.client.modules.other.Panic;
import fun.rockstarity.client.modules.player.FreeCam;
import fun.rockstarity.client.modules.render.Beautifully;
import fun.rockstarity.client.modules.render.ClickGui;
import fun.rockstarity.client.modules.render.Interface;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.PackScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.shaders.gui.GuiShaders;

/**
 * @author ConeTin
 * @since 3 июн. 2024 г.
 */

@UtilityClass
public class EventHandler implements IAccess {

	@Getter LivingEntity currentTarget;
	private boolean lateInit;
	private boolean resourcesLoaded;
	private boolean resetCfg = true;
	private TimerUtility lastBlur = new TimerUtility();
	
	public void handleEvent(Event event) {
		if (!Player.isInGame()) return;
		
		if (event instanceof EventUpdate) {
			try {
				for (EffectInstance effect : mc.player.getActivePotionEffects()) {
					if (effect.getDuration() <= 0) {
						mc.player.removePotionEffect(effect.getPotion());
					}
				}
			} catch (Exception e) {
				
			}
		}
		
		if (rock.isPanic()) {
			rock.getModules().get(Panic.class).onAllEvent(event);
		}
		
		if (rock.isPanic()) return;
		
		if (event instanceof EventAttack e) {
			currentTarget = e.getTarget();
		}
		
		// Minecraft issue bug fix
		if (event instanceof EventWorldChange) mc.ingameGUI.resetTitle();
		
		handleEvents(event);
		handleCalls(event);
		handleMenus(event);
		rock.getScriptConstructor().execute(event);
		
//		if (event instanceof EventUpdate) {
//			Move.updateAirTicks();
//		}
		
		if (event instanceof EventBlur e && Interface.blur()) {
			if (mc.currentScreen instanceof GuiShaders) {
				Round.draw(e.getMatrixStack(), new Rect(0, sr.getScaledHeight()-50, sr.getScaledWidth(), 50), 0, FixColor.WHITE);
			} else if (mc.currentScreen instanceof PackScreen) {
				//float width = 420;
				//Round.draw(e.getMatrixStack(), new Rect(sr.getScaledWidth()/2-width/2, 0, width, sr.getScaledHeight()), 0, FixColor.WHITE);
				Round.draw(e.getMatrixStack(), new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.WHITE);
			}
		}
		
		if (event instanceof EventRender2D e) {
			if (mc.currentScreen instanceof GuiShaders) {
				Round.draw(e.getMatrixStack(), new Rect(0, sr.getScaledHeight()-50, sr.getScaledWidth(), 50), 0, FixColor.BLACK.alpha(0.3f));
			} else if (mc.currentScreen instanceof PackScreen) {
				Round.draw(e.getMatrixStack(), new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.BLACK.alpha(0.3f));
			}
		}
		
		if (event instanceof EventRender2D && !lateInit && mc.currentScreen == null) {
			//InventoryScreen.drawEntityOnScreen(-100,-100, 30, (float) -3.298665, (float) 4.6999803, mc.player);
			
			rock.setClickGui(new ClickGuiScreen());
			
			lateInit = true;
			
			if (rock.getUser().getStarts() == 0) {
				Chat.msg("Подсказка", "Чтобы открыть меню рокстара, нажми Правый Shift");
				new Sound("nastya/rshift").play();
			}
		}
		
		if (event instanceof EventRender2D && resetCfg && mc.currentScreen == null) {
			GL11.glPushMatrix();
			GL11.glTranslated(10000, 10000, 0);
			rock.getClickGui().getWindow().getEspSettings().renderPage(new MatrixStack(), 0, 0, 0);
			GL11.glPopMatrix();
			resetCfg = false;
		}
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
			String message = packet.getChatComponent().getString().toLowerCase();
			
			if (message.contains("пока")) {
				ThreadManager.run(rock::save);
			}
		}
		
		if (event instanceof EventRender2D e && !Screen.DIRT_ANIM.finished(false)) {
			Screen.DIRT_ANIM.setForward(false);
			
			rock.getMenuManager().drawBackground(e.getMatrixStack(), 0, 0, 0);
			
            if (!Screen.DIRT_ANIM.isForward() && mc.currentScreen == null) {
            	Screen.DIRT_ANIM.setEasing(Easing.EASE_OUT_BACK);
            	
            	Render.initRotate(sr.getScaledWidth()/2F, sr.getScaledHeight()/2F, 45 * Screen.DIRT_ANIM.get());
            	Render.scale(sr.getScaledWidth()/2F, sr.getScaledHeight()/2F, 1 - Screen.DIRT_ANIM.get()*1f);
                FrameFreeze.draw(e.getMatrixStack(), 1);
                Render.end();
                Render.endRotate();
            }
            
		}
		
		MenuFilter.onEvent(event);
	}
	
	private void handleEvents(Event event) {
		if (event instanceof EventRender2D e) {
			MatrixStack ms = e.getMatrixStack();
			
			boolean blur = Interface.blur();
			if (blur) {
				Interface ui = rock.getModules().get(Interface.class);

				Blur.renderTicks++;
				
				Stencil.init();
				new EventBlur(ms, e.getPartialTicks()).hook();
				
				//Round.draw(ms, new Rect(300, 300, 100, 100), 10, FixColor.WHITE);				
				Stencil.read(1);
				//if (lastBlur.passed(20) && mc.isWindowFocused()) {
				
				
				
				//BlurNew.renderBlur(10 * ui.getBlurOffset().get());
				KawaseBlur.renderBlur(ms, 2, (int)ui.getBlurOffset().get());
				//GaussianBlur.draw(ms, 8, 2);
				
				
				//} else {
				//	BlurNew.renderLast();
				//}
				Stencil.finish();
			} else {
				new EventBlur(ms, e.getPartialTicks()).hook();
			}
			
			/*
			if (Interface.glow()) {
				Bloom.initBloom();
				new EventBloom(ms, e.getPartialTicks()).hook();
				Bloom.finishBloom(ms, 3, 1);
			}
			*/
		}
	}
	
	private void handleCalls(Event event) {
		//TaksaScheduler.onEvent(event);
		
		if (event instanceof EventReceivePacket e && Server.isBravo()) {
			if (e.getPacket() instanceof SChatPacket packet) {
				if (packet.getChatComponent().getString().contains(mc.player.getNameClear()) && (packet.getChatComponent().getString().contains("какой чит")
						|| packet.getChatComponent().getString().contains("какой софт")
						|| packet.getChatComponent().getString().contains("что за чит")
						|| packet.getChatComponent().getString().contains("что за софт"))) {
					mc.player.sendChatMessage("!Rockstar Client");
				}
			}
		}
		
		if (event instanceof EventRender2D e) {
			fun.rockstarity.api.scripts.wrappers.Render.setStack(e.getMatrixStack());
		}
		
		if (rock.getScriptHandler() != null)
			for (Script script : rock.getScriptHandler().getEnabledScripts()) {
				script.onEvent(event);
			}
		
		if (event instanceof EventUpdate) {
			for (Page page : Page.values()) {
				page.getShowing().setForward(false);
			}
			
		}
		
		if (event instanceof EventAttack) {
		//	AIPredictor.init();
		}
		
		//RotationParser.onEvent(event);
		//
		AIPredictor.onEvent(event);
		
		rock.getBotsHandler().onEvent(event);
		rock.getAutoBuy().onEvent(event);
		rock.getTpsHandler().onEvent(event);
		rock.getScheduleManager().onEvent(event);
		rock.getConfigHandler().getWindow().onEvent(event);
		
		CullingProcessor.onEvent(event);
		InvUtility.onEvent(event);
		MarkManager.onEvent(event);
		BoostUtility.onEvent(event);
		ViaFixer.onEvent(event);
		
		for (Command command : rock.getCommands().values()) {
			command.onEvent(event);
		}
		
		if (event instanceof EventKey e && (mc.currentScreen == null || e.isReleased())) handleKeys(e);
		
		if (event instanceof EventMessage e) handleCommands(e);
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SChatPacket packet) {
	    	String message = packet.getChatComponent().getString().toLowerCase();
	    	if (message.contains("вы забанены") && Server.isFT()) {
	    		BanProcessor.updateBan(Server.getIP(), message);
	    	}
	    	if (message.toLowerCase().contains("вас забанили на сервере") && Server.isRW()) {
	    		BanProcessor.updateBan(Server.getIP(), message);
	    	}
		}
		
	    for (Module module : rock.getModules().getModules()) {
	    	if (event instanceof EventBlur || event instanceof EventRender3D || event instanceof EventRender2D) {
	    		RenderSystem.runAsFancy(() -> {
	    			module.onAllEvent(event);
	    	    	
	    	    	if (module.get()) {
	    	    		if (module instanceof PremiumModule && !rock.isPremium() && module.get()) {
		            		Chat.msg("Этот модуль доступен только для премиум пользователей");
		            		module.set(false);
		            	}
		            	
		    			module.onEvent(event);
	    	    	}
	    		});
	    		continue;
	    	}
	    	
	    	module.onAllEvent(event);
	    	
	    	if (!module.get()) continue;
	    	
        	if (module instanceof PremiumModule && !rock.isPremium() && module.get()) {
        		Chat.msg("Этот модуль доступен только для премиум пользователей");
        		module.set(false);
        	}
        	
			module.onEvent(event);
	    }
			
		rock.getEmotions().onEvent(event);
		
		if (event instanceof EventRender2D && mc.currentScreen == null) {
			FrameFreeze.save();
		}
	}
	
	private void handleMenus(Event event) {
		if (event instanceof EventRender2D e && mc.currentScreen != rock.getClickGui() && rock.getClickGui() != null) {
			if (!ClickGuiRenderer.opening.isForward() && !ClickGuiRenderer.opening.finished(false) && (!rock.getModules().get(ClickGui.class).getTech().get() || rock.getModules().get(FreeCam.class).get() || mc.getGameSettings().getPointOfView() == PointOfView.THIRD_PERSON_FRONT)) {
				//ClickGui clickGui = rock.getModules().get(ClickGui.class);
				//if (!clickGui.getDarkness().finished(false))
				//	Round.draw(e.getMatrixStack(), new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.BLACK.alpha(0.5f * clickGui.getDarkness().get() * ClickGuiRenderer.opening.get()));
				rock.getClickGui().getWindow().getRenderer().render(e.getMatrixStack(), 0, 0, e.getPartialTicks());
			}
		}
		
		if (event instanceof EventUpdateCamera e && mc.currentScreen != rock.getClickGui() && rock.getClickGui() != null) {
			if (!ClickGuiRenderer.opening.isForward() && !ClickGuiRenderer.opening.finished(false) && rock.getModules().get(ClickGui.class).getTech().get() && !rock.getModules().get(FreeCam.class).get() && mc.getGameSettings().getPointOfView() != PointOfView.THIRD_PERSON_FRONT) {
				ClickGuiWindow window = rock.getClickGui().getWindow();
				//window.getRenderer().render(e.getMatrixStack(), 0, 0, 1);
				
				MatrixStack ms = e.getMatrixStack();

		        if (window != null) {
		        	GL11.glPushMatrix();
		        	ms.push();
		        	Beautifully cam = rock.getModules().get(Beautifully.class);
		        	GL11.glRotated(mc.player.rotationPitch, 1.0, 0.0, 0.0);
					GL11.glRotated(mc.player.rotationYaw + 180, 0.0, 1.0, 0.0);
					ms.rotate(Vector3f.YN.rotationDegrees(mc.player.rotationYaw + 180));
					ms.rotate(Vector3f.XN.rotationDegrees(mc.player.rotationPitch));
			        RenderSystem.disableDepthTest();
			        RenderSystem.depthMask(true);
			        RenderSystem.disableCull();
			        GlStateManager.enableBlend();
			        Vector3d pos = window.getRenderer().getClosePos();
			        GL11.glTranslated(pos.x - mc.getRenderManager().info.getProjectedView().getX(),
			        		pos.y - mc.getRenderManager().info.getProjectedView().getY(),
			        		pos.z - mc.getRenderManager().info.getProjectedView().getZ());
			        //matrixStack.rotate(Vector3f.XP.rotationDegrees(-window.getRenderer().getPitch() + 180));
			        GL11.glRotated(-window.getRenderer().getYaw()/4f + 180, 0, 1, 0);
			        GL11.glRotated(-window.getRenderer().getPitch()/4f, 1, 0, 0);
			        float xRotate = -(window.getRenderer().getYaw()-mc.player.rotationYaw*4F)/10F;
			        float yRotate = -(window.getRenderer().getPitch()-mc.player.rotationPitch*4F)/10F;
			        //GL11.glRotated(xRotate, 0, 1, 0);
			        //GL11.glRotated(yRotate, 1, 0, 0);
			        //GL11.glTranslatef(-5, 0, -1.7f);
			        GL11.glScalef(1, -1, 1);
			        float scale = 0.01f;
			        GL11.glScalef(scale, scale, scale);
			        GL11.glTranslatef(-window.getWidth()+10, -window.getHeight()/1.2f+10, -175);
		            
			        //ClickGui clickGui = rock.getModules().get(ClickGui.class);
					//if (!clickGui.getDarkness().finished(false))
					//	Round.draw(e.getMatrixStack(), new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()), 0, FixColor.BLACK.alpha(0.5f * clickGui.getDarkness().get() * ClickGuiRenderer.opening.get()));
			        if (cam.get() && cam.getRealCamera().get()) {
						ms.rotate(Vector3f.ZP.rotationDegrees(-(MathHelper.clamp(cam.getCamera().getPitch(), -20, 20) + MathHelper.clamp((mc.player.rotationYaw-cam.getCamera().getYaw())/2F, -10, 10))));
		        	}
			        
			        float fov = (float) ((mc.gameSettings.fov));
			        Render.scale(window.getX() + window.getWidth() / 2, window.getY() + window.getHeight() / 2, fov/110F);

			        Render.scale(window.getX() + window.getWidth() / 2, window.getY() + window.getHeight() / 2, fov-110, 0.5f + (float) (ClickGuiRenderer.opening.get()) * 0.5f /*- 0.03f * dragAnim.get()*/);
			        //int mouseX = (int) (sr.getScaledWidth()/2 + xRotate*9F);
			        //int mouseY = (int) (sr.getScaledHeight()/2 + yRotate*9F * Math.max(1, Math.abs(xRotate/15F)));
			        window.getRenderer().renderWindow(ms, 0, 0, 1);
					Render.end();
					Render.end();
					

			        RenderSystem.enableCull();
			        RenderSystem.depthMask(true);
			        RenderSystem.enableDepthTest();

			        ms.pop();
			        GL11.glPopMatrix();
		        }
			}
		}
		
		if (event instanceof EventRender2D e && mc.currentScreen != rock.getConstructor() && rock.getConstructor() != null) {
			if (!ConstructorScreen.opening.isForward() && !ConstructorScreen.opening.finished(false)) {
				rock.getConstructor().render(e.getMatrixStack(), 0, 0, e.getPartialTicks());
			}
		}
		
		if (event instanceof EventRender2D e && rock.getModules().get(fun.rockstarity.client.modules.other.AutoBuy.class) != null) {
			AutoBuyScreen autoBuyScreen = rock.getModules().get(AutoBuy.class).getScreen();
			if (mc.currentScreen != autoBuyScreen && autoBuyScreen != null) {
				if (autoBuyScreen.getRenderer().getOpening().isForward() || !autoBuyScreen.getRenderer().getOpening().finished(false)) {
					autoBuyScreen.getRenderer().render(e.getMatrixStack(), 0, 0, e.getPartialTicks());
				}
			}
		}
		if (mc.currentScreen != null && mc.currentScreen != rock.getClickGui()) ClickGuiRenderer.opening.setForward(false);
		
	}
	
	private void handleCommands(EventMessage event) {
		String msg = event.getMessage();
		
		if (rock.getCommands().executeRaw(msg)) event.cancel();
	}
	
	private void handleKeys(EventKey event) {
		List<Module> modules = new ArrayList<>();
		
		rock.getModules().values().forEach(mod -> modules.add(mod));
		for (Script script : rock.getScriptHandler().getEnabledScripts()) {
			script.getScriptModules().forEach(mod -> modules.add(mod));
		}
		
		modules.stream()
        .forEach(module -> {
        	if (module.getBindByKey(event).isPresent() && (module.getBindByKey(event).get().getType() == BindType.HOLD || !event.isReleased())) {
        		if (event.isReleased() && !module.get()) return;
        		
        		module.getBindByKey(event).get().setHolding(!event.isReleased());
        		module.toggleWithBind(module.getBindByKey(event));
        	}
        	module.getSettings().forEach(setting -> {
        		if (setting.getBindByKey(event).isPresent() && (setting.getBindByKey(event).get().getType() == BindType.HOLD || !event.isReleased())) {
        			if (event.isReleased() && (setting instanceof Binding || (setting instanceof CheckBox box && !box.get()))) return;
        			
        			setting.getBindByKey(event).get().setHolding(!event.isReleased());
        			setting.toggleWithBind(setting.getBindByKey(event));
            	}
        		
        		if (setting instanceof Select select) {
        			select.getElements().forEach(elmt -> {
                		if (elmt.getBindByKey(event).isPresent() && (elmt.getBindByKey(event).get().getType() == BindType.HOLD || !event.isReleased())) {
                			if (event.isReleased() && !elmt.get()) return;
                			
                			elmt.getBindByKey(event).get().setHolding(!event.isReleased());
                			elmt.toggleWithBind(elmt.getBindByKey(event));
                    	}
                	});
        		}
        	});
        });
	}
	
	public void resetConfig() {
		resetCfg = true;
	}
	
}
