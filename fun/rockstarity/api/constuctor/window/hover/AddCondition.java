package fun.rockstarity.api.constuctor.window.hover;

import java.util.HashMap;
import java.util.Map.Entry;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.constuctor.blocks.types.actions.BlockCondition;
import fun.rockstarity.api.constuctor.interfaces.ICondition;
import fun.rockstarity.api.constuctor.window.Window;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;

public class AddCondition extends Window implements IAccess {
	
	private final HashMap<String, Runnable> actions = new HashMap<>();
	
	public AddCondition(float x, float y, float width, float height) {
		super(x, y, width, height);
		
		opening.setForward(true);
		
        for (Entry<String, ICondition> conditionEntry : rock.getScriptConstructor().getConditions().entrySet()) {
            String conditionName = conditionEntry.getKey();
            actions.put(conditionName, () -> {
                ICondition condition = conditionEntry.getValue();
                BlockCondition blockCondition = new BlockCondition(conditionName, x, y, 150, 30, condition);
                rock.getScriptConstructor().getBlocks().add(blockCondition);
                opening.setForward(false);
            });
        }
	}
	
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        black = rock.getThemes().getTextFirstColor().alpha(opening.get());
        bgColor = rock.getThemes().getFirstColor().alpha(opening.get());
        actionsColor = rock.getThemes().getFirstColor().alpha(opening.get());
        
        Round.draw(matrixStack, new Rect(x, y, width, height * opening.get()), 3, bgColor);
        
        Stencil.init();
        Round.draw(matrixStack, new Rect(x, y, width, height * opening.get()), 3, bgColor);
        Stencil.read(1);
        
        bold.get(14).draw(matrixStack, "Добавить условие", x + 5, y + 4, black);
        
        float yOff = 0;
        for (Entry<String, Runnable> action : actions.entrySet()) {
            Rect rect = new Rect(x + 5, y + 17 + yOff, width - 10, 15);
            Round.draw(matrixStack, rect, 2, actionsColor.darker(Hover.isHovered(rect, mouseX, mouseY) ? 0.05f : 0));
            bold.get(16).draw(matrixStack, action.getKey(), x + 8, y + 19 + yOff, black);
            
            yOff += 17;
        }

        Stencil.finish();
        
        height = yOff + 21;
    }
    
    @Override
    public boolean clicked(double mouseX, double mouseY, int button) {
        if (!Hover.isHovered(this, mouseX, mouseY)) {
            opening.setForward(false);
        } else {
            float yOff = 0;
            for (Entry<String, Runnable> action : actions.entrySet()) {
                Rect rect = new Rect(x + 5, y + 17 + yOff, width - 10, 15);
                if (Hover.isHovered(rect, mouseX, mouseY)) {
                    action.getValue().run();
                    opening.setForward(false);
                    return true;
                }
                
                yOff += 17;
            }

        }
        return false;
    }
}
