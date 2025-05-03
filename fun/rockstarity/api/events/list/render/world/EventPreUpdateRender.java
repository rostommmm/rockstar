package fun.rockstarity.api.events.list.render.world;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.math.vector.Matrix4f;

@Getter
@Setter
@AllArgsConstructor
public class EventPreUpdateRender extends Event {
    private MatrixStack matrixStackIn;
    private float partialTicks;
    private long finishTimeNano;
    private boolean drawBlockOutline;
    private ActiveRenderInfo activerenderinfo;
    private GameRenderer gameRenderer;
    private LightTexture lightmapTexture;
    private Matrix4f matrix4f;

    public void set(MatrixStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline, ActiveRenderInfo activerenderinfo, GameRenderer gameRenderer, LightTexture lightmapTexture, Matrix4f matrix4f) {
        this.matrixStackIn = matrixStackIn;
        this.partialTicks = partialTicks;
        this.finishTimeNano = finishTimeNano;
        this.drawBlockOutline = drawBlockOutline;
        this.activerenderinfo = activerenderinfo;
        this.gameRenderer = gameRenderer;
        this.lightmapTexture = lightmapTexture;
        this.matrix4f = matrix4f;
    }
}