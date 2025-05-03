package fun.rockstarity.client.modules.render;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.EventType;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.fog.Vector2d;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;

@Info(name="TNTTimer", desc="Отображает время до взрыва ТНТ", type=Category.RENDER)
public class TNTTimer extends Module {
	
	@Override
	@EventType({EventRender2D.class})
	public void onEvent(Event event) {
		if (event instanceof EventRender2D event2d) {
			for (Entity e : mc.world.getAllEntities()) {
				if (e instanceof TNTEntity tnt) {
					String name = MathUtility.round(tnt.getFuse() / 20.0F, 1) + " сек";
					Vector3d pos = new Vector3d(tnt.getPosX(), tnt.getPosY() + tnt.getHeight() + 0.5, tnt.getPosZ());
					Vector2d vec = Render.project(pos.x, pos.y, pos.z);
					if (vec == null) return;
					
					double damage = this.getDamage(tnt);
					boolean showWarning = mc.player.getHealth() + mc.player.getAbsorptionAmount() < damage;
					
					float width = bold.get(14).getWidth(name);
					Round.draw(event2d.getMatrixStack(), new Rect((float) vec.x - width / 2 - 2, (float) vec.y - (damage > 0 ? 10 : 0), width + 5, 10), 1.5f, rock.getThemes().getFirstColor().alpha(0.8));
					bold.get(14).draw(event2d.getMatrixStack(), name, (float) vec.x - width / 2, (float) (vec.y - (damage > 0 ? 10 : 0)), rock.getThemes().getTextFirstColor());
					
					if (damage > 0) {
						String text = showWarning ? "Смертельно!" : (int) damage + " урона";
	                    float textWidth = bold.get(14).getWidth(text);
	                    Round.draw(event2d.getMatrixStack(), new Rect((float) vec.x - textWidth / 2 - 2, (float) vec.y + 1, textWidth + 5, 10), 1.5f, showWarning ? FixColor.RED.alpha(0.8) : rock.getThemes().getFirstColor().alpha(0.8));
	                    bold.get(14).draw(event2d.getMatrixStack(), text, (float) vec.x - textWidth / 2, (float) (vec.y + 1), showWarning ? FixColor.WHITE : rock.getThemes().getTextFirstColor());
					}
				}
			}
		}
	}

	private double getDamage(TNTEntity ent) {
    	float size = 4;
    	Vector3d pos = ent.getPositionVec();
    	double x = pos.x;
    	double y = ent.getPosYHeight(0.0625D);
    	double z = pos.z;
    	
    	Set<BlockPos> set = Sets.newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j)
        {
            for (int k = 0; k < 16; ++k)
            {
                for (int l = 0; l < 16; ++l)
                {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15)
                    {
                        double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = size * (0.7F + mc.world.rand.nextFloat() * 0.6F);
                        double d4 = x;
                        double d6 = y;
                        double d8 = z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F)
                        {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            BlockState blockstate = mc.world.getBlockState(blockpos);
                            FluidState fluidstate = mc.world.getFluidState(blockpos);

                            d4 += d0 * (double)0.3F;
                            d6 += d1 * (double)0.3F;
                            d8 += d2 * (double)0.3F;
                        }
                    }
                }
            }
        }

        float f2 = size * 2.0F;
        int k1 = MathHelper.floor(x - (double)f2 - 1.0D);
        int l1 = MathHelper.floor(x + (double)f2 + 1.0D);
        int i2 = MathHelper.floor(y - (double)f2 - 1.0D);
        int i1 = MathHelper.floor(y + (double)f2 + 1.0D);
        int j2 = MathHelper.floor(z - (double)f2 - 1.0D);
        int j1 = MathHelper.floor(z + (double)f2 + 1.0D);
        List<Entity> list = mc.world.getEntitiesWithinAABBExcludingEntity(ent, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
        Vector3d vector3d = new Vector3d(x, y, z);

        for (int k2 = 0; k2 < list.size(); ++k2)
        {
            Entity entity = list.get(k2);

            if (entity != mc.player) continue;
            
            if (!entity.isImmuneToExplosions())
            {
                double d12 = (double)(MathHelper.sqrt(entity.getDistanceSq(vector3d)) / f2);

                if (d12 <= 1.0D)
                {
                    double d5 = entity.getPosX() - x;
                    double d7 = (entity instanceof TNTEntity ? entity.getPosY() : entity.getPosYEye()) - y;
                    double d9 = entity.getPosZ() - z;
                    double d13 = (double)MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D)
                    {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = (double) Explosion.getBlockDensity(vector3d, entity);
                        double d10 = (1.0D - d12) * d14;
                        return (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f2 + 1.0D));
                    }
                }
            }
        }
        return 0;
    }
	
	
	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		
	}
    
}
