package fun.rockstarity.api.modules.settings.list;

import java.util.function.Supplier;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 5 дек. 2
 * 023 г.
 */

public class Input extends Setting {
	
	@Getter @Setter
	private boolean onlyNumbers;
	private String text;
	@Getter @Setter private InputWidget input;
	
	@Getter
	private Animation enableAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);

	public Input(Bindable parent, String name) {
		super(parent, name);
		this.input = new InputWidget(bold.get(12), 0, 0, 0, 12, new TranslationTextComponent(""), false);
	}

	public Input set(String text) {
		this.text = text;
		input.setText(text);
		
		return this;
	}
	
	public Input set(boolean only) {
		this.onlyNumbers = only;
		
		if (only) {
			this.input = new InputWidget(bold.get(12), 0, 0, 0, 12, new TranslationTextComponent(""), false) {
	            @Override
	            public boolean charTyped(char codePoint, int modifiers) {
	                if (!this.canWrite()) {
	                    return false;
	                } else if (Character.isDigit(codePoint)) {
	                    if (this.isEnabled) {
	                        this.writeText(Character.toString(codePoint));
	                    }
	                    return true;
	                } else if (codePoint != 0x03 /* Ctrl+C */ && codePoint != 0x16 /* Ctrl+V */ && codePoint != 0x18 /* Ctrl+X */) {
	                    // Дополнительно, если нажаты клавиши Ctrl+C, Ctrl+V, или Ctrl+X, они не удаляют символы
	                    if (this.isEnabled) {
	                        this.deleteFromCursor(1);
	                    }
	                    return true;
	                } else {
	                    return false;
	                }
	            }
	        };
		} else {
			this.input = new InputWidget(bold.get(12), 0, 0, 0, 12, new TranslationTextComponent(""), false);
		}
		
		return this;
	}
	
	@Override
	public void reset() {
	    set("");
	    super.reset();
	}
	
	public String get() {
		return this.text;
	}
	
	public Input hide(Supplier<Boolean> hide) {
		this.hide = hide;
		return this;
	}
	
	public Input desc(String desc) {
		this.desc = desc;
		return this;
	}
	
}
