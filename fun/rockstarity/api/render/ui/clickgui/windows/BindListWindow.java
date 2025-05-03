package fun.rockstarity.api.render.ui.clickgui.windows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.api.binds.BindType;
import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.helpers.game.Binds;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.Window;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.settings.KeyBinding;

/**
 * @author ConeTin
 * @since 12 дек. 2023 г.
 */

public class BindListWindow extends Window {
	
	@Getter @Setter
	private Bind active;
	private BindWindow child;
	@Getter
	private final Bindable bindable;

	private final LinkedHashMap<String, Runnable> actions = new LinkedHashMap<>();
	
	public BindListWindow(Bindable bindable, float x, float y, float width, float height) {
		super(x, y, width, height);
		this.bindable = bindable;
		
		update();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		update();
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		Round.draw(matrixStack, this.height(height*opening.get()), 4, bgColor);
		
		Stencil.init();
		Round.draw(matrixStack, this.height(height*opening.get()), 4, bgColor);
		//Outline.draw(matrixStack, this.height(height*opening.get()), 4, 0.01f, actionsColor);
		Stencil.read(1);
		
		float yOff = 0;
		for (Entry<String, Runnable> action : actions.entrySet()) {
			Rect rect = new Rect(x + 2, y + 2 + yOff, width - 4, 15);
			if (Hover.isHovered(rect, mouseX, mouseY) || active != null && action.getKey().contains(Binds.getName(active.getKey(), active.getScancode())))
				Round.draw(matrixStack, rect, 2, active != null && action.getKey().contains(Binds.getName(active.getKey(), active.getScancode())) ? bgColor.darker(0.02f) : bgColor.darker(0.05f));
			bold.get(14).draw(matrixStack, action.getKey(), x + 6, y + 4.5f + yOff, text);
			if (bold.get(14).getWidth(action.getKey()) + 12 > width) width = bold.get(14).getWidth(action.getKey()) + 12;
			
			yOff += 17;
		}
		
		height = yOff + 2;
		
		Stencil.finish();
	}
	
	@Override
	public boolean clicked(double mouseX, double mouseY, int button) {
		float yOff = 0;
		for (Entry<String, Runnable> action : actions.entrySet()) {
			Rect rect = new Rect(x + 2, y + 2 + yOff, width - 4, 15);
			if (Hover.isHovered(rect, mouseX, mouseY)) {
				if (child != null) child.getOpening().setForward(false);
				action.getValue().run();
			}
			
			yOff += 17;
		}
		
		if (child != null && Hover.isHovered(child, mouseX, mouseY)) {
			return false;
		}
		
		return super.clicked(mouseX, mouseY, button);
	}
	
	@Override
	public void update() {
		actions.clear();
		actions.put("Добавить", () -> {
			Bind bind = new Bind(-1);
			bindable.getBinds().add(bind);
			showBind(bind);
		});
		
		for (Bind bind : bindable.getBinds()) {
			actions.put(String.format("%s '%s'", bind.getType().getName(), Binds.getName(bind.getKey(), bind.getScancode())), () -> {
				showBind(bind);
			});
		}
		
		actions.put("Очистить", () -> {
			bindable.getBinds().clear();
		});
	}
	
	@Override
	public boolean pressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != scanCode && child == null && bindable.getBinds().size() < 5) {
			if (Arrays.stream(new KeyBinding[] {
					mc.getGameSettings().keyBindSneak,
	                mc.getGameSettings().keyBindSprint,
	                mc.getGameSettings().keyBindForward,
	                mc.getGameSettings().keyBindBack,
	                mc.getGameSettings().keyBindLeft,
	                mc.getGameSettings().keyBindRight,
	                mc.getGameSettings().keyBindJump
			}).anyMatch(key -> {
				return key.getKeyCode().getKeyCode() == keyCode;
			}))
				return super.pressed(keyCode, scanCode, modifiers);
			
			bindable.getBinds().add(new Bind(keyCode, scanCode, BindType.TOGGLE));
			rock.getAlertHandler().alert("Бинд добавлен!", AlertType.INFO);
			this.update();
		}
		
		return super.pressed(keyCode, scanCode, modifiers);
	}
	
	private void showBind(Bind bind) {
		float height1 = 35*1.2f;
		float width1 = bindable instanceof Select || bindable instanceof Mode ? 140 : 120;
		rock.getClickGui().getWindow().getWindows().add(child = new BindWindow(this, bind, x + width + 5, y + bindable.getBinds().indexOf(bind) * 17 + 17 + 9.5f - height1 / 2, width1, height1));
		active = bind;
	}
	
}
