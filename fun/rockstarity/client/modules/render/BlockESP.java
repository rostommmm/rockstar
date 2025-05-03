package fun.rockstarity.client.modules.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import org.lwjgl.opengl.GL11;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.render.world.EventRenderWorld;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.color.themes.Style;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author Malecharik
 * @since 21 апр. 2024 г. 15:05:32
 */
@Info(name = "BlockESP", desc = "Подсвечивает выбранные блоки", type = Category.RENDER)
public class BlockESP extends Module {
	private final Select blocks = new Select(this, "Блоки").desc("Блоки которые будут подсвечиваться").min(1);
	private final Select.Element chest = new Select.Element(blocks, "Сундук").set(true);
	private final Select.Element enderChest = new Select.Element(blocks, "Ендер Сундуки").set(true);
	private final Select.Element spawner = new Select.Element(blocks, "Спавнер").set(false);
	private final Select.Element shulker = new Select.Element(blocks, "Шалкер").set(false);

	/*
	private final Select ores = new Select(this, "Блоки").desc("Руда которая будет подсвечиваться").min(1);
	private final Select.Element lom = new Select.Element(ores, "Древние обломки").set(true);
	private final Select.Element diamond = new Select.Element(ores, "Алмазная руда").set(true);
	private final Select.Element gold = new Select.Element(ores, "Золотая руда").set(true);
	private final Select.Element lapis = new Select.Element(ores, "Лазуритовая руда").set(true);
	private final Select.Element redpil = new Select.Element(ores, "Редстоуновая руда").set(true);
	private final Select.Element iron = new Select.Element(ores, "Железная руда").set(true);
	*/
	
	@Override
	@EventType({EventRenderWorld.class})
	public void onEvent(Event event) {
		
		if (event instanceof EventRenderWorld) {
			Vector3d renderPos =  mc.getRenderManager().info.getProjectedView();
			
			GL11.glPushMatrix();
			GL11.glTranslated(-renderPos.x, -renderPos.y, -renderPos.z);
			for (TileEntity entity : this.mc.world.loadedTileEntityList) {
				
				BlockPos pos = entity.getPos();
				if (this.chest.isEnabled() && entity instanceof ChestTileEntity) Render.blockEsp(pos, Style.getMain().getRGB());
				if (this.enderChest.isEnabled() && entity instanceof EnderChestTileEntity) Render.blockEsp(pos, Style.getMain().getRGB());
				if (this.spawner.isEnabled() && entity instanceof MobSpawnerTileEntity) Render.blockEsp(pos, Style.getMain().getRGB());
				if (this.shulker.isEnabled() && entity instanceof ShulkerBoxTileEntity) Render.blockEsp(pos, Style.getMain().getRGB());
			}
			GL11.glPopMatrix();
/*
			GL11.glPushMatrix();
			GL11.glTranslated(-renderPos.x, -renderPos.y, -renderPos.z);
			int radius = 25;

			for (int x = -radius; x <= radius; x++) { // пофиксите пж оч лагает
				for (int y = -radius; y <= radius; y++) {
					for (int z = -radius; z <= radius; z++) {
						BlockPos pos = mc.player.getPosition().add(x, y, z);
						BlockState blockState = mc.world.getBlockState(pos);

						if (this.lom.isEnabled() && blockState.getBlock() == Blocks.ANCIENT_DEBRIS) Render.blockEsp(pos, Style.getMain().getRGB());
						if (this.diamond.isEnabled() && blockState.getBlock() == Blocks.DIAMOND_ORE) Render.blockEsp(pos, Style.getMain().getRGB());
						if (this.gold.isEnabled() && blockState.getBlock() == Blocks.GOLD_ORE) Render.blockEsp(pos, Style.getMain().getRGB());
						if (this.lapis.isEnabled() && blockState.getBlock() == Blocks.LAPIS_ORE) Render.blockEsp(pos, Style.getMain().getRGB());
						if (this.redpil.isEnabled() && blockState.getBlock() == Blocks.REDSTONE_ORE) Render.blockEsp(pos, Style.getMain().getRGB());
						if (this.iron.isEnabled() && blockState.getBlock() == Blocks.IRON_ORE) Render.blockEsp(pos, Style.getMain().getRGB());
					}
				}
			}

			GL11.glPopMatrix();
			*/
		}
	}
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
}
