package fun.rockstarity.client.modules.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Mode.Element;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="ObjectsInfo", desc="Отображает время до исчезновения трапок, пластов и т.д.", type=Category.RENDER)
public class ObjectsInfo extends Module {
	
	Mode mode = new Mode(this, "Режим отображения");
	Element circle = new Element(mode, "Круг");
	Element bar = new Element(mode, "Полоска");
	
	TimerUtility timer = new TimerUtility();
	List<BlockPos> toCheck = new ArrayList<>();
	Map<BlockPos, Info> infos = new HashMap<>();
	
	@Override
    public void onEvent(Event event) {
		// Добавление структур
		/*
        if (event instanceof EventReceivePacket e) {
            if (e.getPacket() instanceof SMultiBlockChangePacket packet) {
            	toCheck.clear();
                BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
                
                for (int i = 0; i < packet.getField_244306_b().length; ++i) {
                    short short1 = packet.getField_244306_b()[i];
                    blockpos$mutable.setPos(packet.getPos().func_243644_d(short1), packet.getPos().func_243645_e(short1), packet.getPos().func_243646_f(short1));

                    toCheck.add(blockpos$mutable);
                }
                
                timer.reset();
            }
        }
        */
		//volume: 0.5, pitch: 0.5, category: MASTER
		// block.piston.extend: volume: 0.7, pitch: 0.5, category: RECORDS
		// драконка фт entity.evoker_fangs.attack: volume: 0.5, pitch: 0.85, category: RECORDS
		// пласт фт  block.anvil.place: volume: 0.7, pitch: 1.1, category: RECORDS
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SPlaySoundEffectPacket play) {
	    //    Chat.debug(String.format("%s: volume: %s, pitch: %s, category: %s", play.getSound().getName().getPath(), play.getVolume(), play.getPitch(), play.getCategory()));
		}

		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SPlaySoundEffectPacket play && (play.getSound().getName().getPath().equals("block.piston.contract") || play.getSound().getName().getPath().equals("block.piston.extend"))) {
        	BlockPos pos = new BlockPos(play.getX(), play.getY(), play.getZ());
        	//toCheck.add(pos);
			if ((play.getVolume() == 0.5F || play.getVolume() == 0.7F) && play.getPitch() == 0.5F) {
				infos.put(pos, new Info(pos.up().getVec().add(0, 3.5f, 0).add(0.5f), ObjType.TRAP_FT));
			}

        	timer.reset();
        }
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SPlaySoundEffectPacket play && (play.getSound().getName().getPath().equals("entity.evoker_fangs.attack"))) {
        	BlockPos pos = new BlockPos(play.getX(), play.getY(), play.getZ());
        	//toCheck.add(pos);
			if ((play.getVolume() == 0.5F || play.getVolume() == 0.7F) && (play.getPitch() == 0.85F || play.getPitch() == 1F)) {
				infos.put(pos, new Info(pos.up().getVec().add(0, 4.5f, 0).add(0.5f), Server.is("spooky") ? ObjType.DRAGON_ST : ObjType.DRAGON_FT));
			}

        	timer.reset();
        }
        
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SPlaySoundEffectPacket play && (play.getSound().getName().getPath().equals("block.anvil.place"))) {
        	BlockPos pos = new BlockPos(play.getX(), play.getY(), play.getZ());
        	//toCheck.add(pos);
        	
			if ((play.getVolume() == 0.5F || play.getVolume() == 0.7F) && (play.getPitch() == 1.1F || play.getPitch() == 0.5F)) {
				infos.put(pos, new Info(pos.up().getVec().add(0.5f), Server.is("spooky") ? ObjType.PLAST_ST : ObjType.PLAST_FT));
			}

        	timer.reset();
        }
        
        
        if (event instanceof EventUpdate && timer.passed(100) && !toCheck.isEmpty()) {
        	/*
        	try {
        		for (BlockPos pos : toCheck) {
            		if (mc.world.getBlock(pos.add(0,3,0)) != Blocks.AIR) {
            			infos.put(pos, new Info(pos.up().getVec().add(0, 3.5f, 0).add(0.5f), ObjType.TRAP_FT));
            		}
            	}
        		toCheck.clear();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	*/
        }
        
        /*
        if (event instanceof EventRenderWorld e) {
        	Vector3d renderPos =  mc.getRenderManager().info.getProjectedView();
			
			GL11.glPushMatrix();
			GL11.glTranslated(-renderPos.x, -renderPos.y, -renderPos.z);
			
			try {
				for (BlockPos pos : toCheck) {
					Render.blockEsp(pos.add(0, 3, 0), Style.getMain().getRGB());
				}
			} catch (Exception e1) {
			}
			
			GL11.glPopMatrix();
        }
        */
        
        // Рендер тегов структур
        if (event instanceof EventRender2D e) {
        	BlockPos toRemove = null;
        	for (Entry<BlockPos, Info> entry : infos.entrySet()) {
        		Info info = entry.getValue();
        		info.draw(e);
        		if (info.start.passed(info.type.time))
        			toRemove = entry.getKey();
        	}
        	if (toRemove != null)
        		infos.remove(toRemove);
        }
        
    }

    @Override
    public void onDisable() {
    	
    }

    @Override
    public void onEnable() {
    	
    }
    
    @RequiredArgsConstructor
    class Info {
    	final Vector3d pos;
    	final ObjType type;
    	TimerUtility start = new TimerUtility();
    	
    	void draw(EventRender2D e) {
    		double[] wts = Render.worldToScreen(pos.x ,pos.y, pos.z);
            if (!PositionTracker.isInView(pos) || wts == null) return;
            
            MatrixStack ms = e.getMatrixStack();
            String remained = mode.is(bar) ? String.format("%s сек", TextUtility.formatNumberOld((type.time - start.getElapsed())/1000F)) : String.format("Осталось %s сек", TextUtility.formatNumberOld((type.time - start.getElapsed())/1000F));
            float width = mode.is(bar) ? 80 : 40 + semibold.get(10).getWidth(remained);
            float height = 20;
            float x = (float) wts[0]-width/2F;
            float y = (float) wts[1]-height/2F;
            
            Round.draw(ms, new Rect(x, y, width, height), 2, rock.getThemes().getFirstColor());
            {
            	ItemStack itemStack = new ItemStack(type.item);
            	GlStateManager.pushMatrix();
                GlStateManager.disableBlend();
                mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
                GlStateManager.translated(x + 3, y + 3, 0);
                GlStateManager.scalef(0.9f, 0.9f, 0.9f);
                mc.getItemRenderer().renderItemAndEffectIntoGUIOld(itemStack, 0, 0);
                GlStateManager.popMatrix();
            }
            bold.get(12).draw(ms, type.name, x + 20, y + 3, rock.getThemes().getTextFirstColor());

            if (mode.is(circle)) {
            	semibold.get(10).draw(ms, remained, x + 20, y + 10, rock.getThemes().getTextFirstColor());
                float size = 12;
                int from = 270;
                Round.draw(ms, new Rect(x + width - height/2F - size/2F, y + height/2F - size/2F, size, size), size/2F-0.1f, rock.getThemes().getSecondColor());
    			Render.drawClientCircle(x + width - height/2F, y + height/2F, size/2F-1, from, from + (int) (361 - 361 * (float) start.getElapsed() / (float) type.time), 1);
            } else if (mode.is(bar)) {
            	semibold.get(10).draw(ms, remained, x + 23 + bold.get(12).getWidth(type.name), y + 4, rock.getThemes().getTextFirstColor());
                Round.draw(ms, new Rect(x + 20, y + height - 8, width - 25, 4), 1, rock.getThemes().getSecondColor());
                Round.draw(ms, new Rect(x + 20, y + height - 8, (width - 25) * (1-(float) start.getElapsed() / (float) type.time), 4), 1, Style.getMain(), Style.getSecond(), Style.getMain(), Style.getSecond());
            }
    	}
    }
    
    @AllArgsConstructor
    enum ObjType {
    	TRAP_FT("Трапка", Items.NETHERITE_SCRAP, 15_000),
    	DRAGON_FT("Драконка", Items.NETHERITE_SCRAP, 30_000),
    	DRAGON_ST("Драконка", Items.NETHERITE_SCRAP, 60_000),
    	PLAST_FT("Пласт", Items.DRIED_KELP, 20_000),
    	PLAST_ST("Пласт", Items.DRIED_KELP, 60_000);
    	
    	String name;
    	Item item;
    	long time;
    }
}
