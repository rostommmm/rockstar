package fun.rockstarity.client.modules.render;

import java.util.Arrays;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.connection.globals.ClientAPI;
import fun.rockstarity.api.connection.globals.ServerAPI;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.player.EventModels;
import fun.rockstarity.api.events.list.render.world.EventRenderWorldEntities;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.models.taksa.TaksaBrain;
import fun.rockstarity.api.render.models.taksa.TaksaModel;
import fun.rockstarity.api.render.shaders.list.Glass;
import fun.rockstarity.client.modules.other.Globals;
import fun.rockstarity.client.modules.render.cosmetics.Crown;
import fun.rockstarity.client.modules.render.cosmetics.JumpCircle;
import fun.rockstarity.client.modules.render.cosmetics.Naruto;
import fun.rockstarity.client.modules.render.cosmetics.Tail;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.optifine.shaders.Shaders;

/**
 * @author ConeTin
 * @since 5 мая 2024 г.
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="Cosmetics", desc="3D косметика игрока", type=Category.RENDER, module={"Model", "СustomModel", "Naruto", "Capes", "Circle", "JumpCircle", "Tail"})
public class Cosmetics extends Module {
	
	Select elements = new Select(this, "Элементы");
	Cosmetic pet = new Cosmetic(elements, "Питомец").onEnable(() -> {
		if (rock.getModules().get(Globals.class).get()) {
			ThreadManager.run(() -> ServerAPI.updateName());
		}
	}).onDisable(() -> {
		if (rock.getModules().get(Globals.class).get()) {
			ThreadManager.run(() -> ServerAPI.updateName());
		}
	});
	@NonFinal Cosmetic capes = (Cosmetic) new Cosmetic(elements, "Плащ").set(true);
	CheckBox capesFriend = new CheckBox(capes, "Плащ у друзей").hide(() -> !this.capes.get());
	
	@NonFinal public Crown crown;
	
	Mode model = new Mode(this, "Модель").onChange(() -> {
		if (rock.getModules().get(Globals.class).get()) {
			ThreadManager.run(() -> ServerAPI.updateName());
		}
	});
	Mode friendModel = new Mode(this, "Модель у друзей");
	Mode otherModel = new Mode(this, "Модель у остальных");
	
	public Cosmetics() {
		for (Mode mode : Arrays.asList(model, friendModel, otherModel)) {
			new Mode.Element(mode, "Игрок");
			new Mode.Element(mode, "Бешеный кролик");
			new Mode.Element(mode, "Демон");
			new Mode.Element(mode, "Соник");
			//new Mode.Element(mode, "Такса");
		}
		
		crown = (Crown) new Crown(this, this.elements).hide(() -> model.getElements().indexOf(model.getCurrent()) != 0);
		new Tail(this, this.elements);
		new JumpCircle(this, this.elements);
		new Naruto(this, this.elements);
	}
	
	// ДААА БЛЯТЬ Я ЭТО СДЕЛАЛ СЮДАААА
	@NonFinal TaksaModel taksa = new TaksaModel(RenderType::getEntityCutoutNoCull);
	TaksaBrain brain = new TaksaBrain();
	
	@Override
	@EventType({EventRender3D.class, EventMotion.class, EventJump.class, EventModels.class})
	public void onEvent(Event event) {
		if (pet.get()) {
			if (event instanceof EventRenderWorldEntities e) {
				brain.setEntity(mc.player);
				
				MatrixStack ms = e.getMatrix();

				ms.push();
				ms.translate(brain.getPos().sub(Render.cameraPos()));
				
				taksa.setRotationAngles(mc.player.ticksExisted, brain);
				taksa.render(e.getMatrix(), e, brain);

				ms.pop();
			}
			
			try {
				if (rock.getModules().get(Globals.class).get() && (event instanceof EventUpdate || event instanceof EventRenderWorldEntities)) {
					for (PlayerEntity player : mc.world.getPlayers()) {
						if (ClientAPI.isRockstar(player.getName().getString()) && ClientAPI.getTaksa(player.getName().getString()).equals("taksa_default") && !(player instanceof ClientPlayerEntity)) {
							if (event instanceof EventRenderWorldEntities e) {
								player.brain.setEntity(player);
								
								MatrixStack ms = e.getMatrix();
								ms.push();
								ms.translate(player.brain.getPos().sub(Render.cameraPos()));
								
								player.taksa.setRotationAngles(mc.player.ticksExisted, player.brain);
								player.taksa.render(e.getMatrix(), e, player.brain);

								ms.pop();
							}
							
						}
						player.brain.onEvent(event);
					}
				}
				
				if (event instanceof EventUpdate) {
					brain.onEvent(event);
				}
			} catch (Exception e) {
			}
		}
		
		if (event instanceof EventRender3D) {
			if (rock.getModules().get(Beautifully.class).get() && rock.getModules().get(Beautifully.class).getBloom().get() && Shaders.shaderPackLoaded) {
				Glass.draw(FixColor.WHITE, 0, 0, 0);
				Glass.end();
			}
    	}
		
		if (event instanceof EventRender3D || event instanceof EventMotion || event instanceof EventJump || event instanceof EventModels)
			this.elements.getElements().forEach(element -> {
				Cosmetic elmt = ((Cosmetic) element);
				
				elmt.update();
				
				if (elmt.get() || !elmt.showing.finished(false)) {
					if (!elmt.showing.finished(false))
						elmt.onEvent(event);
				}
			});
	}
	
	public int getModel(Entity entity) {
		if (!this.get() || rock.isPanic())
			return 0;

		if (entity instanceof ClientPlayerEntity)
			return model.getElements().indexOf(model.getCurrent());
		
		if (ClientAPI.isRockstar(entity.getName().getString()))
			return ClientAPI.getModel(entity.getName().getString());
		
		if (rock.getFriendsHandler().isFriend(entity))
			return friendModel.getElements().indexOf(friendModel.getCurrent());
		
		if (entity instanceof PlayerEntity)
			return otherModel.getElements().indexOf(otherModel.getCurrent());
		
		return 0;
	}
	
	public int getLocal() {
		return model.getElements().indexOf(model.getCurrent());
	}
	
	public static class Cosmetic extends Select.Element implements IAccess {
		
		protected final Animation showing = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

		public Cosmetic(Select parent, String name) {
			super(parent, name);
		}

		public void onEvent(Event event) {}
		
		public void update() {
			this.showing.setForward(this.get());
		}
		
		@Override
		public Cosmetic onEnable(Runnable val) {
			return (Cosmetic) super.onEnable(val);
		}
		
		@Override
		public Cosmetic onDisable(Runnable val) {
			return (Cosmetic) super.onDisable(val);
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
