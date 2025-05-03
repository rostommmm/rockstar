package fun.rockstarity.api.render.shaders.fog.depth;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.shaders.fog.CustomFramebuffer;
import fun.rockstarity.api.render.shaders.list.DepthBlurDownGLSL;
import fun.rockstarity.api.render.shaders.list.DepthBlurUpGLSL;
import fun.rockstarity.client.modules.render.World;

public enum DepthBlur implements IAccess {
    INSTANCE;

    public final CustomFramebuffer BLURRED = new CustomFramebuffer(false).setLinear();
    public final CustomFramebuffer ADDITIONAL = new CustomFramebuffer(false).setLinear();

    private final Shader kawaseUp = DepthBlurUpGLSL.SHADER;
    private final Shader kawaseDown = DepthBlurDownGLSL.SHADER;

    public void update(float offset, int steps) {
    	if ( rock.getModules().get(World.class).getBuffer() == null) return;
    	
        ADDITIONAL.setup();
        rock.getModules().get(World.class).getBuffer().bindFramebufferTexture();
        kawaseDown.start();
        kawaseDown.setFloat("offset", offset);
        kawaseDown.setFloat("resolution", 1f / mc.getMainWindow().getWidth(), 1f / mc.getMainWindow().getHeight());
        CustomFramebuffer.drawQuads();
        CustomFramebuffer[] buffers = {this.ADDITIONAL, this.BLURRED};
        for (int i = 1; i < steps; ++i) {
            int step = i % 2;
            buffers[step].setup();
            buffers[(step + 1) % 2].draw();
            buffers[step].stop();
        }
        kawaseDown.finish();
        kawaseUp.start();
        kawaseUp.setFloat("offset", offset);
        kawaseUp.setFloat("resolution", 1f / mc.getMainWindow().getWidth(), 1f / mc.getMainWindow().getHeight());
        for (int i = 0; i < steps; ++i) {
            int step = i % 2;
            buffers[(step + 1) % 2].setup();
            buffers[step].draw();
            buffers[step].stop();
        }
        kawaseUp.finish();
        mc.getFramebuffer().bindFramebuffer(false);
        ADDITIONAL.stop();
    }
}
