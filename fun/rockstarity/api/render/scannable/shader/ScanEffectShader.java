/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fun.rockstarity.api.render.scannable.shader;

import fun.rockstarity.api.render.scannable.shader.AbstractShader;
import net.minecraft.client.shader.ShaderDefault;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

public final class ScanEffectShader
extends AbstractShader {
    public static final ScanEffectShader INSTANCE = new ScanEffectShader();
    private ShaderDefault inverseViewMatrixUniform;
    private ShaderDefault inverseProjectionMatrixUniform;
    private ShaderDefault positionUniform;
    private ShaderDefault centerUniform;
    private ShaderDefault outerColorUniform;
    private ShaderDefault midColorUniform;
    private ShaderDefault innerColorUniform;
    private ShaderDefault scanlineColorUniform;
    private ShaderDefault radiusUniform;

    public void setInverseViewMatrix(Matrix4f value) {
        this.inverseViewMatrixUniform.set(value);
    }

    public void setInverseProjectionMatrix(Matrix4f value) {
        this.inverseProjectionMatrixUniform.set(value);
    }

    public void setPosition(Vector3d value) {
        this.positionUniform.set((float)value.x, (float)value.y, (float)value.z);
    }

    public void setCenter(Vector3d value) {
        this.centerUniform.set((float)value.x, (float)value.y, (float)value.z);
    }

    public void setOuterColor(Vector4f value) {
        this.outerColorUniform.set(value.getX(), value.getY(), value.getZ(), value.getW());
    }

    public void setMidColor(Vector4f value) {
        this.midColorUniform.set(value.getX(), value.getY(), value.getZ(), value.getW());
    }

    public void setInnerColor(Vector4f value) {
        this.innerColorUniform.set(value.getX(), value.getY(), value.getZ(), value.getW());
    }

    public void setScanlineColor(Vector4f value) {
        this.scanlineColorUniform.set(value.getX(), value.getY(), value.getZ(), value.getW());
    }

    public void setRadius(float value) {
        this.radiusUniform.set(value);
    }

    public void setDepthBuffer(int buffer) {
        this.shaderInstance.setSampler("depthTex", () -> buffer);
    }

    @Override
    public String getShaderName() {
        return "scan_effect";
    }

    @Override
    public AbstractShader.Shader shader() {
        return AbstractShader.Shader.SCAN_EFFECT;
    }

    @Override
    public void handleShaderLoad() {
        super.handleShaderLoad();
        this.inverseViewMatrixUniform = this.shaderInstance.safeGetUniform("invViewMat");
        this.inverseProjectionMatrixUniform = this.shaderInstance.safeGetUniform("invProjMat");
        this.positionUniform = this.shaderInstance.safeGetUniform("pos");
        this.centerUniform = this.shaderInstance.safeGetUniform("center");
        this.outerColorUniform = this.shaderInstance.safeGetUniform("outerColor");
        this.midColorUniform = this.shaderInstance.safeGetUniform("midColor");
        this.innerColorUniform = this.shaderInstance.safeGetUniform("innerColor");
        this.scanlineColorUniform = this.shaderInstance.safeGetUniform("scanlineColor");
        this.radiusUniform = this.shaderInstance.safeGetUniform("radius");
    }
}

