package fun.rockstarity.api.render.ui.clickgui.windows;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.Clickable;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.cursor.CursorType;
import fun.rockstarity.api.render.cursor.CursorUtility;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.render.ui.fonts.FontSize;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.Window;

/**
 * @author ConeTin
 * @since 12 дек. 2023 г.
 */

public class BindWindow extends Window {
	
	private boolean binding;
	private final Bind bind;
	private final BindListWindow parent;
	protected final Animation deleting = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300).setSize(1);
	private SettingRect settingRect;
	
	public BindWindow(BindListWindow parent, Bind bind, float x, float y, float width, float height) {
		super(x, y, width, height);
		this.bind = bind;
		this.parent = parent;
		if (this.parent.getBindable() instanceof Setting setting && !(setting instanceof Clickable) && !(setting instanceof Binding) && !(setting instanceof Select) && !(setting instanceof Input) && !(setting instanceof ColorPicker)) {
			this.settingRect = new SettingRect(setting);
		}
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		Round.draw(matrixStack, this.width(width*opening.get()), 4, bgColor);
		//Outline.draw(matrixStack, this.height(height*opening.get()), 4, 0.01f, actionsColor);
		//Render.drawTriangle(x, y + 35*1.2f/2, 5, 90, bgColor.alpha(parent.getOpening().get()).getRGB());
		
		Stencil.init();
		Round.draw(matrixStack, this.width(width*opening.get()), 4, bgColor);
		//Outline.draw(matrixStack, this.height(height*opening.get()), 4, 0.01f, actionsColor);
		Stencil.read(1);
		
		float yOff = -1;
		
		FontSize font = bold.get(14);
		float inc = 12;
		
		
		font.draw(matrixStack, "Клавиша", x + 5, y + 5 + yOff, text);
		String bind = this.binding ? "..." : Binds.getName(this.bind.getKey(), this.bind.getScancode()).toUpperCase();
		float bindWidth = font.getWidth(bind) + 4;
		Round.draw(matrixStack, new Rect(x + width - 5 - bindWidth, y + 5 + yOff, bindWidth, 10), 2, bgColor.darker(0.05f));
		font.draw(matrixStack, bind, x + width - 5 - bindWidth + 1.7f, y + 5.5f + yOff, text);
		yOff += inc;
		font.draw(matrixStack, "Тип", x + 5, y + 5 + yOff, text);
		float globalWidth = font.getWidth("ToggleHold") + 8;
		Round.draw(matrixStack, new Rect(x + width - 5 - globalWidth, y + 5 + yOff, globalWidth, 10), 2, bgColor.darker(0.05f));
		float xOff = 0;
		for (BindType type : BindType.values()) {
			float nameWidth = font.getWidth(type.getName());
			if (this.bind.getType() == type) {
				if (type == BindType.HOLD) {
					Round.draw(matrixStack, new Rect(x + width - 5 - globalWidth + xOff, y + 5 + yOff, nameWidth + 4.5f, 10), 2, 0, 2, 0, Style.getMain().alpha(this.opening.get()));
				} else {
					Round.draw(matrixStack, new Rect(x + width - 5 - globalWidth + xOff, y + 5 + yOff, nameWidth + 4.5f, 10), 0, 2, 0, 2, Style.getMain().alpha(this.opening.get()));
				}
			}
			font.draw(matrixStack, type.getName(), x + width - 5 - globalWidth + xOff + 2, y + 5 + yOff, this.bind.getType() == type ? FixColor.WHITE.alpha(opening.get()) : text);
			xOff += nameWidth + 4;
		}
		yOff += inc;

		font.draw(matrixStack, "Управление", x + 5, y + 5 + yOff, text);
		String del = "Удалить";
		deleting.setForward(Hover.isHovered(x + 5, y + 5 + yOff, width - 10, inc, mouseX, mouseY));
		font.draw(matrixStack, del, x + width - 6 - font.getWidth(del), y + 5 + yOff, text.move(FixColor.RED, this.deleting.get()));
		yOff += inc;
		
		if (settingRect != null) {
			font.draw(matrixStack, "Значение", x + 5, y + 5.5f + yOff, text);
			settingRect.set(x - 3, y + yOff - 4, width+6, settingRect.getHeight());
			settingRect.render(matrixStack, mouseX, mouseY, 0, this.opening.get(), true, false);
			yOff += settingRect.getParent().getBindHeight() - 12;
		}
		
		this.setHeight(yOff+6);
		
		Stencil.finish();
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		if (this.binding) {
			bind.setKey(-1);
			bind.setScancode(button);
			this.binding = false;
			return false;
		}
		
		float yOff = 0;
		
		FontSize font = bold.get(14);
		float inc = 11;
		
		String bind = Binds.getName(this.bind.getKey(), this.bind.getScancode()).toUpperCase();
		float bindWidth = font.getWidth(bind) + 4;
		if (Hover.isHovered(x + width - 5 - bindWidth, y + 5 + yOff, bindWidth, 10, mouseX, mouseY)) {
			this.binding = true;
		}
		yOff += inc;
		
		float globalWidth = font.getWidth("ToggleHold") + 8;
		float xOff = 0;
		for (BindType type : BindType.values()) {
			float nameWidth = font.getWidth(type.getName());
			if (Hover.isHovered(x + width - 5 - globalWidth + xOff, y + 5 + yOff, nameWidth + 4.5f, 10, mouseX, mouseY)) {
				this.bind.setType(type);
			}
			xOff += nameWidth + 4;
		}
		yOff += inc;
		
		if (Hover.isHovered(x + 5, y + 5 + yOff, width - 10, inc, mouseX, mouseY)) {
			parent.getBindable().getBinds().remove(this.bind);
			opening.setForward(false);
		}
		yOff += inc;
		
		if (!super.clicked(mouseX, mouseY, button)) {
			parent.setActive(null);
			return true;
		}
		
		if (settingRect != null) {
			settingRect.clicked(mouseX, mouseY, button, false, true);
		}
		
		return false;
	}
	
	@Override
	public boolean released(double mouseX, double mouseY, int button) {
		if (settingRect != null) {
			settingRect.clicked(mouseX, mouseY, button, true, true);
		}
		return super.released(mouseX, mouseY, button);
	}
	
	@Override
	public boolean dragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		FontSize font = bold.get(14);
		float inc = 11;
		float yOff = 0;
		yOff += inc * 3;
		
		if (settingRect != null) {
			settingRect.dragged(mouseX, mouseY, button, dragX, dragY, true);
		}
		
		return super.dragged(mouseX, mouseY, button, dragX, dragY);
	}
	
	@Override
	public boolean pressed(int keyCode, int scanCode, int modifiers) {
		if (this.binding) {
			bind.setKey(keyCode);
			bind.setScancode(scanCode);
			this.binding = false;
		}
		
		if (settingRect != null) {
			parent.pressed(keyCode, scanCode, modifiers);
		}
		
		return super.pressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void charTyped(char codePoint, int modifiers) {
		if (settingRect != null) {
			settingRect.charTyped(codePoint, modifiers);
		}
		
		super.charTyped(codePoint, modifiers);
	}
	
	@Override
	public void tick() {
		if (settingRect != null) {
			settingRect.tick();
		}
		
		super.tick();
	}
	
}
