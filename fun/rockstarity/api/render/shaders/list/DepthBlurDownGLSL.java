package fun.rockstarity.api.render.shaders.list;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.client.modules.combat.Aura;
import net.minecraft.entity.LivingEntity; 

/**
 * @author ConeTin
 * @since 3 дек. 2023 г.
 */

public class DepthBlurDownGLSL extends Shader {
	
	public static final DepthBlurDownGLSL SHADER = new DepthBlurDownGLSL();
	
	@Override
	public String getCode() {
		return readShader("DepthBlurDownGLSL");
	}
	
}
