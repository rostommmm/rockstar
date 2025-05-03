package fun.rockstarity.client.modules.render;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.game.EventCameraClip;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Block;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.DeadBushBlock;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.MushroomBlock;
import net.minecraft.block.NetherSproutsBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.potion.Effects;

/**
 * @author Malecharik
 * @since 9 Mar 2024 00:03:01
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="NoRender", desc="Отключает ненужный рендер", type=Category.RENDER)
public class NoRender extends Module {
	
	
	Select camera = new Select(this, "Камера"); // Камера
	Element cameraClip = new Element(camera, "Камера клип").set(true);
	Element hurt = new Element(camera, "Тряска камеры").set(true);
	Element fire = new Element(camera, "Огонь").set(true);
	Element pump = new Element(camera, "Фон тыквы");
	
	Select ui = new Select(this, "Интерфейс"); // Интерфейс, гуишки там худ короче вся эта залупа
	Element bossBar = new Element(ui, "Босс бар");
	Element scoreboardNumbers = new Element(ui, "Числа в скорборде").hide(() -> this.scoreboard.get());
	Element scoreboard = new Element(ui, "СкорБорд");
	Element hurtWither = new Element(ui, "Иссушение").set(true);
	Element fon = new Element(ui, "Задний фон");
	
	Select world = new Select(this, "Мир"); // Вся хуйня в мире
	Element particles = new Element(world, "Частицы");
	Element glow = new Element(world, "Свечение сущностей");
	Element players = new Element(world, "Игроков");
	Element items = new Element(world, "Предметы на земле");
	Element badEffect = new Element(world, "Плохие эффекты");
	Element holograms = new Element(world, "Голограммы");
	Element flowers = new Element(world, "Растения").onEnable(() -> onEnable()).onDisable(() -> onDisable());
	Element frames = new Element(world, "Рамки");

	Select optmize = new Select(this, "Оптимизация").desc("Оптимизация игры. Может каким-либо образом ломать отображение, поэтому есть возможность отключить"); // Супер мега оптимизон $$$
	Element offScreen = new Element(optmize, "Сущности за экраном").set(true);
	Element collision = new Element(optmize, "Сущности за стенами").set(true);
	Element particlesCollision = new Element(optmize, "Частицы за стенами").set(true);
	//Element chunksCollision = new Element(optmize, "Чанки за стенами").set(true);
	
	public NoRender() {
		set(true);
	}
	
	@Override
	@EventType({EventCameraClip.class, EventRender3D.class})
	public void onEvent(Event event) {
		//Обрабатываем камера клип
		if (event instanceof EventCameraClip && this.cameraClip.get()) {
			event.cancel();
		}
		
		if (event instanceof EventRender3D) {
			if (this.badEffect.get()) {
				mc.player.removeActivePotionEffect(Effects.NAUSEA);
			}
		}
		
		if (event instanceof EventTick && items.get()) {
			try {
				for (Entity entity : mc.world.getAllEntities()) {
					if (entity instanceof ExperienceBottleEntity) {
						mc.world.removeEntityFromWorld(entity.getEntityId());
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	public boolean canRender(Block block) {
		if (!this.get()) return true;
		
		if ((block instanceof TallGrassBlock
				|| block instanceof FlowerBlock
				|| block instanceof DoublePlantBlock
				|| block instanceof DeadBushBlock
				|| block instanceof MushroomBlock
				|| block instanceof CropsBlock
				|| block instanceof NetherSproutsBlock) && flowers.get())
			return false;
		
		return true;
	}
	
	public boolean canRender(Entity entity) {
		
		if (entity instanceof ArmorStandEntity && holograms.get())
			return false;
		
		if (entity instanceof ItemFrameEntity && frames.get())
			return false;
		
		return true;
	}
	
	@Override
	public void onEnable() {
		mc.worldRenderer.loadRenderers();
	}
	
	@Override
	public void onDisable() {
		mc.worldRenderer.loadRenderers();
	}
	
}
