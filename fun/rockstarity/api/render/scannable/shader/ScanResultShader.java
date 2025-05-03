/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fun.rockstarity.api.render.scannable.shader;

import fun.rockstarity.api.render.scannable.shader.AbstractShader;
import net.minecraft.client.shader.ShaderDefault;
import net.minecraft.util.math.vector.Matrix4f;

public final class ScanResultShader
extends AbstractShader {
    public static ScanResultShader INSTANCE = new ScanResultShader();
    private static ShaderDefault projMatUniform;
    private static ShaderDefault viewMatUniform;

    public static void setProjectionMatrix(Matrix4f matrix) {
        projMatUniform.set(matrix);
    }

    public static void setViewMatrix(Matrix4f matrix) {
        viewMatUniform.set(matrix);
    }

    @Override
    public String getShaderName() {
        return "scan_result";
    }

    @Override
    public AbstractShader.Shader shader() {
        return AbstractShader.Shader.SCAN_RESULT;
    }

    @Override
    public void handleShaderLoad() {
        super.handleShaderLoad();
        projMatUniform = this.shaderInstance.safeGetUniform("projMat");
        viewMatUniform = this.shaderInstance.safeGetUniform("viewMat");
    }
}

