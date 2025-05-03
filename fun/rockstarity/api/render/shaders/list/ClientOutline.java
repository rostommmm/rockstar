package fun.rockstarity.api.render.shaders.list;

import fun.rockstarity.api.render.shaders.Shader;

/**
 * @author ConeTin
 * @since 25 мар. 2024 г.
 */

public class ClientOutline extends Shader {

	@Override
	public String getCode() {
		return readShader("clientoutline");
	}
	
}
