package fun.rockstarity.api.render.ui.clickgui.windows;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.render.ui.clickgui.esp.ESPElement;
import fun.rockstarity.api.render.ui.widgets.Window;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.Interface.UIElement;

/**
 * @author ConeTin
 * @since 25 июл. 2024 г.
 */

public class SettingsWindow extends Window {

	private final Bindable element;
	
	private final ArrayList<SettingRect> settings = new ArrayList<>();
	
	public SettingsWindow(Bindable parent, float x, float y) {
		super(x, y, 145, 10);
		this.element = parent;
		

		if (parent instanceof ESPElement ui) {
			for (Setting setting : ui.getSettings()) {
				this.settings.add(new SettingRect(setting));
			}
			return;
		}

		if (parent instanceof Setting ui) {
			for (Setting setting : ui.getSettings()) {
				this.settings.add(new SettingRect(setting));
			}
			return;
		}
		
		if (parent instanceof UIElement ui) {
			for (Setting setting : ui.getSettings()) {
				this.settings.add(new SettingRect(setting));
			}
			return;
		}
		
		if (parent instanceof Select.Element ui) {
			for (Setting setting : ui.getSettings()) {
				this.settings.add(new SettingRect(setting));
			}
			return;
		}
		
		if (parent instanceof Mode.Element ui) {
			for (Setting setting : ui.getSettings()) {
				this.settings.add(new SettingRect(setting));
			}
			return;
		}
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int count = 0;
		
		x = Math.min(sr.getScaledWidth()-width, x);
		y = Math.min(sr.getScaledHeight()-height-10, y);

		Round.draw(matrixStack, this, 8, rock.getThemes().getFirstColor().alpha(opening.get()));

		Stencil.init();
		Round.draw(matrixStack, this, 8, rock.getThemes().getFirstColor().alpha(opening.get()));
		Stencil.read(1);
		
		float yOff = 0;
		for (SettingRect setting : settings) {
			setting.getHide().setForward(setting.getParent().isHide());
			
			setting.set(x, y + yOff - 2 - 10 + 10 * opening.get() * (1-setting.getHide().get()), 145, setting.getHeight());
			setting.render(matrixStack, mouseX, mouseY, partialTicks, opening.get() * (1-setting.getHide().get()), false, false);
			
			if (Hover.isHovered(setting, mouseX, mouseY)) {
				rock.getClickGui().getWindow().getRenderer().setHovered(setting);
			}
			
			if (!setting.getParent().isHide())
				count++;
			
			yOff += (setting.getHeight() - 13) * (1-setting.getHide().get());
		}
		Stencil.finish();
		
		Interface ui = rock.getModules().get(Interface.class);

		FixColor[] circle = Interface.getCircle(opening.get());
		
		Outline.draw(matrixStack, this.y(this.getY()).size(-0.25f), 8, ui.getOutlineWidth().get()/4F, rock.getThemes().getFoursColor().alpha(opening.get()));

		height = yOff + 10;
		
		if (count == 0) {
			opening.setForward(false);
		}
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.clicked(mouseX, mouseY, button);
		
		float yOff = 0;
		for (SettingRect setting : settings) {
			if (setting.getParent().isHide())
				continue;
			
			if (Hover.isHovered(setting.y(y + yOff - 2 - 10 + 10 * opening.get() + 3).height(setting.getHeight() - 14), mouseX, mouseY))
				setting.clicked(mouseX, mouseY, button, false);
			
			yOff += setting.getHeight() - 13;
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
	public void charTyped(char codePoint, int modifiers) {
		for (SettingRect setting : this.settings) {
			if (setting.getParent().isHide())
				continue;
			
			setting.charTyped(codePoint, modifiers);
		}
		super.charTyped(codePoint, modifiers);
	}
	
	@Override
	public void tick() {
		for (SettingRect setting : this.settings) {
			if (setting.getParent().isHide())
				continue;
			
			setting.tick();
		}
		super.tick();
	}
	
	@Override
	public boolean pressed(int keyCode, int scanCode, int modifiers) {
		for (SettingRect setting : this.settings) {
			if (setting.getParent().isHide())
				continue;
			
			setting.pressed(keyCode, scanCode, modifiers);
		}
		return super.pressed(keyCode, scanCode, modifiers);
	}

}
