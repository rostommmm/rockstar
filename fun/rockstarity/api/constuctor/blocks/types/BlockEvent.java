package fun.rockstarity.api.constuctor.blocks.types;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.constuctor.blocks.Block;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 8 дек. 2023 г.
 */

public class BlockEvent extends Block {
	
	@Getter
	private final Class event;

	public BlockEvent(String name, float x, float y, float width, float height, Class event) {
		super(name, x, y, width, height);
		this.event = event;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	public boolean clicked(double mouseX, double mouseY, int button) {
		super.clicked(mouseX, mouseY, button);
		
		return false;
	}

}
