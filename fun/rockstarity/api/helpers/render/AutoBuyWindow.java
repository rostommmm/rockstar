package fun.rockstarity.api.helpers.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.render.ui.widgets.Window;

public class AutoBuyWindow extends Window {
	
	private final Module element;
    private final List<SettingRect> settings = new ArrayList<>();
    
    public AutoBuyWindow(Module element) {
    	super(165, (sr.getScaledHeight() / 2) - 100, 145, 10); 
    	
    	this.element = element;
    	
    	for (Setting setting : element.getSettings()) {
    		this.settings.add(new SettingRect(setting));
    	}
	}
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    	if (opening.finished(false)) return;
    	
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 999);
    	
    	x = 165;
        y = (sr.getScaledHeight() / 2) - height / 2;
        
        Render.glow(matrixStack, this, opening.get());

        Round.draw(matrixStack, this, 4, rock.getThemes().getFirstColor().alpha(opening.get()));

        bold.get(16).draw(matrixStack, "AutoBuy", x, y - 13, FixColor.WHITE.alpha(opening.get()));

        Stencil.init();
        Round.draw(matrixStack, this, 4, rock.getThemes().getFirstColor().alpha(opening.get()));
        Stencil.read(1);

        float yOff = 0;
        for (SettingRect setting : this.settings) {
            setting.getHide().setForward(setting.getParent().isHide());

            setting.set(x, y + yOff - 2 - 10 + 10 * opening.get() * (1 - setting.getHide().get()), 145, setting.getHeight());
            setting.render(matrixStack, mouseX, mouseY, partialTicks, opening.get() * (1 - setting.getHide().get()), false, false);

            yOff += (setting.getHeight() - 13) * (1 - setting.getHide().get());
        }
        Stencil.finish();
        
        Render.outline(matrixStack, this, opening.get());
        
        GL11.glPopMatrix();

        height = yOff + 10;
    }
    
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
	    if (!Hover.isHovered(x,y ,width, height, mouseX, mouseY) || button != 0) {
	        return super.released(mouseX, mouseY, button);
	    }
		
		float yOff = 0;
		for (SettingRect setting : this.settings) {
			if (Hover.isHovered(setting.y(y + yOff - 2 - 10 + 10 * opening.get() + 3).height(setting.getHeight() - 14), mouseX, mouseY))
			setting.clicked(mouseX, mouseY, button, false);
			
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
