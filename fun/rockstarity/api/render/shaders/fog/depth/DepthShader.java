package fun.rockstarity.api.render.shaders.fog.depth;

import fun.rockstarity.api.helpers.render.ColorUtility;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.Shader;
import fun.rockstarity.api.render.shaders.fog.AbstractShader;
import net.minecraft.client.shader.ShaderDefault;

public final class DepthShader extends AbstractShader {
    private ShaderDefault nearUniform;
    private ShaderDefault farUniform;
    private ShaderDefault distance;
    private ShaderDefault color1;
    private ShaderDefault color2;
    private ShaderDefault color3;
    private ShaderDefault color4;
    private ShaderDefault clientColor;
    private ShaderDefault saturation;

    public void saturation(final float value) {
        saturation.set(value);
    }

    public void clientColor(final boolean value) {
        clientColor.set(value ? 1.0F : 0.0F);
    }

    public void color1(FixColor color) {
        color1.set(ColorUtility.getRGBAFloat(color));
    }

    public void color2(FixColor color) {
        color2.set(ColorUtility.getRGBAFloat(color));
    }

    public void color3(FixColor color) {
        color3.set(ColorUtility.getRGBAFloat(color));
    }

    public void color4(FixColor color) {
        color4.set(ColorUtility.getRGBAFloat(color));
    }

    public void setNear(final float value) {
        nearUniform.set(value);
    }


    public void far(final float value) {
        farUniform.set(value);
    }

    public void distance(final float value) {
        distance.set(value);
    }


    public void depthBuffer(final int buffer) {
        shaderInstance.setSampler("depthTex", () -> buffer);
    }

    public void blurBuffer(final int buffer) {
        shaderInstance.setSampler("blur", () -> buffer);
    }

    public void minecraftBuffer(final int buffer) {
        shaderInstance.setSampler("minecraft", () -> buffer);
    }

    @Override
    public String getShaderName() {
        return "depth_shader";
    }

    @Override
    public Shader shader() {
        return Shader.DEPTH_SHADER;
    }

    @Override
    public void handleShaderLoad() {
        super.handleShaderLoad();
        nearUniform = shaderInstance.safeGetUniform("near");
        farUniform = shaderInstance.safeGetUniform("far");
        distance = shaderInstance.safeGetUniform("distance");
        color1 = shaderInstance.safeGetUniform("color1");
        color2 = shaderInstance.safeGetUniform("color2");
        color3 = shaderInstance.safeGetUniform("color3");
        color4 = shaderInstance.safeGetUniform("color4");
        clientColor = shaderInstance.safeGetUniform("clientColor");
        saturation = shaderInstance.safeGetUniform("saturation");
    }
}