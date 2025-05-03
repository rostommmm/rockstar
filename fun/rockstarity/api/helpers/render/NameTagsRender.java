package fun.rockstarity.api.helpers.render;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.render.ui.widgets.Window;
import fun.rockstarity.client.modules.render.NameTags;

public class NameTagsRender extends Window {
	
	private final Module element;
    private final List<SettingRect> settings = new ArrayList<>();
	
    public NameTagsRender(Module element) {
    	super(10, (sr.getScaledHeight() / 2) - 100, 145, 10); 
    	
    	this.element = element;
    	
    	for (Setting setting : element.getSettings()) {
    		this.settings.add(new SettingRect(setting));
    	}
	}
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        x = 10;
        y = (sr.getScaledHeight() / 2) - height / 2;
        
        Render.glow(matrixStack, this, opening.get());

        Round.draw(matrixStack, this, 8, rock.getThemes().getFirstColor().alpha(opening.get()));

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
        
        Render.outline(matrixStack, this, opening.get());

        height = yOff + 10;
    }
    
    @Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		float yOff = 0;
		for (SettingRect setting : this.settings) {
			if (Hover.isHovered(x, y + yOff - 2 - 10 + 10 * opening.get() * (1 - setting.getHide().get()), 145, setting.getHeight(), mouseX, mouseY)) {
				rock.getModules().get(NameTags.class).setVisible(true);
			} else {
				rock.getModules().get(NameTags.class).setVisible(false);
			}
			
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
}
