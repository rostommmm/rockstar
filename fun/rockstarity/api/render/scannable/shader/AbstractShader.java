/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fun.rockstarity.api.render.scannable.shader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderDefault;
import net.minecraft.client.shader.ShaderInstance;

public abstract class AbstractShader {
    private static final long START_TIME = System.currentTimeMillis();
    protected ShaderInstance shaderInstance;
    protected ShaderDefault timeUniform;
    private static final String scan_effect_json = "{\n  \"blend\": {\n    \"func\": \"add\",\n    \"srcrgb\": \"one\",\n    \"dstrgb\": \"one\"\n  },\n  \"vertex\": \"wild:scan_effect\",\n  \"fragment\": \"wild:scan_effect\",\n  \"samplers\": [\n    { \"name\": \"depthTex\" }\n  ],\n  \"uniforms\": [\n    { \"name\": \"invViewMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"invProjMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"pos\",        \"type\": \"float\",     \"count\": 3,  \"values\": [ 0.0, 0.0, 0.0 ] },\n    { \"name\": \"center\",     \"type\": \"float\",     \"count\": 3,  \"values\": [ 0.0, 0.0, 0.0 ] },\n    { \"name\": \"outerColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"midColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"innerColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"scanlineColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"radius\",     \"type\": \"float\",     \"count\": 1,  \"values\": [ 1.0 ] }\n  ]\n}";
    private static final String scan_effect_fragment = "#version 120\n\nuniform mat4 invViewMat;\nuniform mat4 invProjMat;\nuniform vec3 pos;\nuniform vec3 center;\nuniform float radius;\nuniform sampler2D depthTex;\n\nvarying vec2 texCoord;\n\nconst float width = 10;\nconst float sharpness = 10;\nuniform vec4 outerColor;\nuniform vec4 midColor;\nuniform vec4 innerColor;\nuniform vec4 scanlineColor;\n\nfloat scanlines() {\n    return sin(gl_FragCoord.y)*0.5+0.5;\n}\n\nvec3 worldpos(float depth) {\n    float z = depth * 2.0 - 1.0;\n    vec4 clipSpacePosition = vec4(texCoord * 2.0 - 1.0, z, 1.0);\n    vec4 viewSpacePosition = invProjMat * clipSpacePosition;\n    viewSpacePosition /= viewSpacePosition.w;\n    vec4 worldSpacePosition = invViewMat * viewSpacePosition;\n\n    return pos + worldSpacePosition.xyz;\n}\n\nvoid main() {\n    vec4 color = vec4(0, 0, 0, 0);\n\n    float depth = texture2D(depthTex, texCoord).r;\n    vec3 pos = worldpos(depth);\n    float dist = distance(pos, center);\n\n    if (dist < radius && dist > radius - width && depth < 1) {\n        float diff = 1.0 - (radius - dist)/width;\n        vec4 edge = mix(midColor, outerColor, pow(diff, sharpness));\n        color = mix(innerColor, edge, diff) + scanlines()*scanlineColor;\n        color *= diff;\n    }\n\n    gl_FragColor = color;\n}\n";
    private static final String scan_effect_vertex = "#version 120\n\nvarying vec2 texCoord;\n\nvoid main() {\n    texCoord = gl_MultiTexCoord0.st;\n    gl_Position = ftransform();\n}";
    private static final String scan_result_json = "{\n  \"blend\": {\n    \"func\": \"add\",\n    \"srcrgb\": \"one\",\n    \"dstrgb\": \"one\"\n  },\n  \"cull\": false,\n  \"vertex\": \"wild:scan_result\",\n  \"fragment\": \"wild:scan_result\",\n  \"uniforms\": [\n    { \"name\": \"projMat\",          \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"viewMat\",          \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"time\",             \"type\": \"float\",     \"count\": 1,  \"values\": [ 0.0 ] }\n  ]\n}";
    private static final String scan_result_fragment = "#version 120\n\nuniform float time;\n\nvarying vec4 color;\nvarying vec2 texCoord;\n\nfloat scanlines() {\n    return sqrt(sin(gl_FragCoord.y+time*10)*0.5+0.5);\n}\n\nvoid main() {\n    float timeScale = (sin(time * 2.5) + 1.0) * 0.5;\n    timeScale = timeScale*0.15 + 0.85;\n\n    vec2 edgeDist = abs(texCoord.xy-0.5)*2.0;\n    edgeDist = edgeDist*0.25+0.75;\n    float edgeMul = pow(max(edgeDist.x, edgeDist.y), 8.0)*0.4+0.6;\n\n    vec4 c = color*scanlines();\n    c *= timeScale;\n    c *= edgeMul;\n    gl_FragColor = c;\n}";
    private static final String scan_result_vertex = "#version 120\n\nattribute vec4 Position;\n\nuniform mat4 projMat;\nuniform mat4 viewMat;\n\nvarying vec4 color;\nvarying vec2 texCoord;\n\nvoid main() {\n    gl_Position = projMat * viewMat * Position;\n    color = gl_Color;\n    texCoord = gl_MultiTexCoord0.st;\n}";

    public void initialize() {
        Minecraft.getInstance().deferTask(this::reloadShaders);
    }

    public void bind() {
        if (this.shaderInstance != null) {
            this.timeUniform.set((float)(System.currentTimeMillis() - START_TIME) / 1000.0f);
            this.shaderInstance.apply();
        }
    }

    public void unbind() {
        if (this.shaderInstance != null) {
            this.shaderInstance.clear();
        }
    }

    private void reloadShaders() {
        if (this.shaderInstance != null) {
            this.shaderInstance.close();
            this.shaderInstance = null;
        }
        try {
            this.shaderInstance = new ShaderInstance(this.getShaderName(), this.shader().getJsonSet(), this.shader().getFragmentShader(), this.shader().getVertexShader());
            this.handleShaderLoad();
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    public abstract String getShaderName();

    public abstract Shader shader();

    public void handleShaderLoad() {
        this.timeUniform = this.shaderInstance.safeGetUniform("time");
    }

    public static enum Shader {
        SCAN_EFFECT(new ByteArrayInputStream("{\n  \"blend\": {\n    \"func\": \"add\",\n    \"srcrgb\": \"one\",\n    \"dstrgb\": \"one\"\n  },\n  \"vertex\": \"wild:scan_effect\",\n  \"fragment\": \"wild:scan_effect\",\n  \"samplers\": [\n    { \"name\": \"depthTex\" }\n  ],\n  \"uniforms\": [\n    { \"name\": \"invViewMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"invProjMat\", \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"pos\",        \"type\": \"float\",     \"count\": 3,  \"values\": [ 0.0, 0.0, 0.0 ] },\n    { \"name\": \"center\",     \"type\": \"float\",     \"count\": 3,  \"values\": [ 0.0, 0.0, 0.0 ] },\n    { \"name\": \"outerColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"midColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"innerColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"scanlineColor\",     \"type\": \"float\",     \"count\": 4,  \"values\": [ 1.0, 1.0, 1.0, 1.0 ] },\n    { \"name\": \"radius\",     \"type\": \"float\",     \"count\": 1,  \"values\": [ 1.0 ] }\n  ]\n}".getBytes()), new ByteArrayInputStream("#version 120\n\nuniform mat4 invViewMat;\nuniform mat4 invProjMat;\nuniform vec3 pos;\nuniform vec3 center;\nuniform float radius;\nuniform sampler2D depthTex;\n\nvarying vec2 texCoord;\n\nconst float width = 10;\nconst float sharpness = 10;\nuniform vec4 outerColor;\nuniform vec4 midColor;\nuniform vec4 innerColor;\nuniform vec4 scanlineColor;\n\nfloat scanlines() {\n    return sin(gl_FragCoord.y)*0.5+0.5;\n}\n\nvec3 worldpos(float depth) {\n    float z = depth * 2.0 - 1.0;\n    vec4 clipSpacePosition = vec4(texCoord * 2.0 - 1.0, z, 1.0);\n    vec4 viewSpacePosition = invProjMat * clipSpacePosition;\n    viewSpacePosition /= viewSpacePosition.w;\n    vec4 worldSpacePosition = invViewMat * viewSpacePosition;\n\n    return pos + worldSpacePosition.xyz;\n}\n\nvoid main() {\n    vec4 color = vec4(0, 0, 0, 0);\n\n    float depth = texture2D(depthTex, texCoord).r;\n    vec3 pos = worldpos(depth);\n    float dist = distance(pos, center);\n\n    if (dist < radius && dist > radius - width && depth < 1) {\n        float diff = 1.0 - (radius - dist)/width;\n        vec4 edge = mix(midColor, outerColor, pow(diff, sharpness));\n        color = mix(innerColor, edge, diff) + scanlines()*scanlineColor;\n        color *= diff;\n    }\n\n    gl_FragColor = color;\n}\n".getBytes()), new ByteArrayInputStream("#version 120\n\nvarying vec2 texCoord;\n\nvoid main() {\n    texCoord = gl_MultiTexCoord0.st;\n    gl_Position = ftransform();\n}".getBytes())),
        SCAN_RESULT(new ByteArrayInputStream("{\n  \"blend\": {\n    \"func\": \"add\",\n    \"srcrgb\": \"one\",\n    \"dstrgb\": \"one\"\n  },\n  \"cull\": false,\n  \"vertex\": \"wild:scan_result\",\n  \"fragment\": \"wild:scan_result\",\n  \"uniforms\": [\n    { \"name\": \"projMat\",          \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"viewMat\",          \"type\": \"matrix4x4\", \"count\": 16, \"values\": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },\n    { \"name\": \"time\",             \"type\": \"float\",     \"count\": 1,  \"values\": [ 0.0 ] }\n  ]\n}".getBytes()), new ByteArrayInputStream("#version 120\n\nuniform float time;\n\nvarying vec4 color;\nvarying vec2 texCoord;\n\nfloat scanlines() {\n    return sqrt(sin(gl_FragCoord.y+time*10)*0.5+0.5);\n}\n\nvoid main() {\n    float timeScale = (sin(time * 2.5) + 1.0) * 0.5;\n    timeScale = timeScale*0.15 + 0.85;\n\n    vec2 edgeDist = abs(texCoord.xy-0.5)*2.0;\n    edgeDist = edgeDist*0.25+0.75;\n    float edgeMul = pow(max(edgeDist.x, edgeDist.y), 8.0)*0.4+0.6;\n\n    vec4 c = color*scanlines();\n    c *= timeScale;\n    c *= edgeMul;\n    gl_FragColor = c;\n}".getBytes()), new ByteArrayInputStream("#version 120\n\nattribute vec4 Position;\n\nuniform mat4 projMat;\nuniform mat4 viewMat;\n\nvarying vec4 color;\nvarying vec2 texCoord;\n\nvoid main() {\n    gl_Position = projMat * viewMat * Position;\n    color = gl_Color;\n    texCoord = gl_MultiTexCoord0.st;\n}".getBytes()));

        private final InputStream jsonSet;
        private final InputStream fragmentShader;
        private final InputStream vertexShader;

        public InputStream getJsonSet() {
            return this.jsonSet;
        }

        public InputStream getFragmentShader() {
            return this.fragmentShader;
        }

        public InputStream getVertexShader() {
            return this.vertexShader;
        }

        private Shader(InputStream jsonSet, InputStream fragmentShader, InputStream vertexShader) {
            this.jsonSet = jsonSet;
            this.fragmentShader = fragmentShader;
            this.vertexShader = vertexShader;
        }
    }
}

