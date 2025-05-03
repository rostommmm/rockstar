package fun.rockstarity.api.constuctor.blocks.types.actions;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.constuctor.blocks.Block;
import fun.rockstarity.api.constuctor.interfaces.ICondition;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;

public class BlockCondition extends Block {

    @Getter
    private ICondition condition;
    private boolean inverted;
    
    public BlockCondition(String name, float x, float y, float width, float height, ICondition condition) {
        super(name, x, y, width, height);
        this.condition = condition;
        this.inverted = false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        
        String conditionStatus = inverted ? " (Не)" : "";
        bold.get(14).draw(matrixStack, name + conditionStatus, x + 4, y + 3, rock.getThemes().getTextFirstColor());

        Round.draw(matrixStack, new Rect(x + 5, y + height - 15, 10, 10), 5, bgColor.darker(0.1f));
        if (parent != null)
            Round.draw(matrixStack, new Rect(x + 5, y + height - 15, 10, 10), 5, clientColor);
    }
    
    @Override
    public boolean clicked(double mouseX, double mouseY, int button) {
        if (Hover.isHovered(x + 5, y + height - 15, 10, 10, mouseX, mouseY)) {
            inverted = !inverted; 
            condition = condition.invert();
            return true;
        }
        return super.clicked(mouseX, mouseY, button);
    }
}
