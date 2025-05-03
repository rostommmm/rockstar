package fun.rockstarity.api.configs;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventChatScreen;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.render.ui.widgets.Window;
import fun.rockstarity.client.modules.render.Interface;
import lombok.Getter;

@Getter
public class ConfigWindow extends Window {
	
	private final ConfigWrapper element;
    private final List<SettingRect> settings = new ArrayList<>();
    private boolean canRender;
    
    public ConfigWindow(ConfigWrapper element) {
    	super(165, (sr.getScaledHeight() / 2) - 100, 145, 10); 
    	
    	this.element = element;
    	
    	for (Setting setting : element.getSettings()) {
    		this.settings.add(new SettingRect(setting));
    	}
	}
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    	if (opening.finished(false) || !canRender) return;
    	Interface ui = rock.getModules().get(Interface.class);

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 999);
    	
    	x = sr.getScaledWidth() - width - 5;
        y = sr.getScaledHeight() - height - 20;
        
        Round.draw(matrixStack, this, 8, rock.getThemes().getFirstColor().alpha(opening.get()));

        String title = "Настройки загрузки конфига";
        bold.get(15).draw(matrixStack, title, x + width/2f - bold.get(15).getWidth(title)/2F, y - 13, FixColor.WHITE.alpha(opening.get()));

        Stencil.init();
        Round.draw(matrixStack, this, 8, rock.getThemes().getFirstColor().alpha(opening.get()));
        Stencil.read(1);

        float yOff = 0;
        for (SettingRect setting : this.settings) {
            setting.getHide().setForward(setting.getParent().isHide());

            setting.set(x, y + yOff - 2 - 10 + 10 * opening.get() * (1 - setting.getHide().get()), 145, setting.getHeight());
            setting.render(matrixStack, mouseX, mouseY, partialTicks, opening.get() * (1 - setting.getHide().get()), false, false);

            yOff += (setting.getHeight() - 13) * (1 - setting.getHide().get());
        }
        Stencil.finish();
        
		Outline.draw(matrixStack, this.y(this.getY()).size(-0.25f), 8, ui.getOutlineWidth().get()/4F, rock.getThemes().getFoursColor().alpha(opening.get()));
        
        GL11.glPopMatrix();

        height = yOff + 10;
    }
    
    public void onEvent(Event event) {
		if (event instanceof EventChatScreen e) {
			boolean can = e.getField().getText().contains(".cfg load") || e.getField().getText().contains(".config load");
			opening.setForward(can);
			if (can)
				canRender = true;
		}
		
		if (event instanceof EventUpdate && mc.currentScreen == null) {
			opening.setForward(false);
		}
    }
    
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
	    if (!Hover.isHovered(x,y ,width, height, mouseX, mouseY) || button != 0) {
	        return super.released(mouseX, mouseY, button);
	    }
		
		float yOff = 0;
		for (SettingRect setting : this.settings) {
			if (Hover.isHovered(setting.y(y + yOff - 2 - 10 + 10 * opening.get() + 3).height(setting.getHeight() - 14), mouseX, mouseY)) {
				setting.clicked(mouseX, mouseY, button, false);
			}
			
			yOff += (setting.getHeight() - 13) * (1 - setting.getHide().get());
		}
		
		return super.clicked(mouseX, mouseY, button);
	}
    
	@Override
	public boolean released(double mouseX, double mouseY, int button) {
		if (button != 0) return super.clicked(mouseX, mouseY, button);
		
		for (SettingRect setting : this.settings) {
			if (setting.getParent().isHide())
				continue;
			
			setting.clicked(mouseX, mouseY, button, true);
		}
		
		return super.released(mouseX, mouseY, button);
	}
	
	@Override
    public boolean pressed(int keyCode, int scanCode, int modifiers) {
    	for (SettingRect setting : this.settings) {
			if (setting.getParent().isHide())
				continue;
			
			setting.pressed(keyCode, scanCode, modifiers);
		}
		return false;
    }
	
    public void tick() {
    	for (SettingRect setting : this.settings) {
			if (setting.getParent().isHide())
				continue;
			
			setting.tick();
		}
    }
    
    public void charTyped(char codePoint, int modifiers) {
    	for (SettingRect setting : this.settings) {
			if (setting.getParent().isHide())
				continue;
			
			setting.charTyped(codePoint, modifiers);
		}
    }
    
}
