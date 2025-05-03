package fun.rockstarity.api.waveycapes.renderlayers;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.secure.nativeapi.FastNative;
import fun.rockstarity.api.waveycapes.CapeHolder;
import fun.rockstarity.api.waveycapes.CapeMovement;
import fun.rockstarity.api.waveycapes.CapeRenderer;
import fun.rockstarity.api.waveycapes.CapeStyle;
import fun.rockstarity.api.waveycapes.VanillaCapeRenderer;
import fun.rockstarity.api.waveycapes.WaveyCapesBase;
import fun.rockstarity.api.waveycapes.WindMode;
import fun.rockstarity.api.waveycapes.math.Vector3;
import fun.rockstarity.api.waveycapes.sim.StickSimulation;
import fun.rockstarity.client.modules.player.Sneak;
import fun.rockstarity.client.modules.render.Cosmetics;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.Config;

public class CustomCapeRenderLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> implements IAccess {

    private static int partCount;
    private ModelRenderer[] customCape = new ModelRenderer[partCount];

    public CustomCapeRenderLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> renderLayerParent) {
        super(renderLayerParent);
        partCount = 16;
        buildMesh();
    }

    private void buildMesh() {
        customCape = new ModelRenderer[partCount];
        for (int i = 0; i < partCount; i++) {
            ModelRenderer base = new ModelRenderer(64, 32, 0, i);
            this.customCape[i] = base.addBox(-5.0F, (float) i, -1.0F, 10.0F, 1.0F, 1F);
        }
    }

    public void render(MatrixStack poseStack, IRenderTypeBuffer multiBufferSource, int i, AbstractClientPlayerEntity abstractClientPlayer, float f, float g, float delta, float j, float k, float l) {
        CapeRenderer renderer = this.getCapeRenderer(abstractClientPlayer, multiBufferSource);
        if (renderer == null || (!rock.getModules().get(Cosmetics.class).get() || !rock.getModules().get(Cosmetics.class).getCapes().get()) || mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA || rock.isPanic() || mc.player.isInvisible() || /* rock.getModules().get(Cosmetics.class).getModel(abstractClientPlayer) != 0 || */InventoryScreen.ON_SCREEN) {
            return;
        }
        
        if (abstractClientPlayer.hasCustomCape()) {
            abstractClientPlayer.updateSimulation(abstractClientPlayer, CustomCapeRenderLayer.partCount);

            if (WaveyCapesBase.config.capeStyle == CapeStyle.SMOOTH && renderer.vanillaUvValues()) {
            	boolean friend = rock.getFriendsHandler().isFriend(abstractClientPlayer);
            	FixColor color1 = (friend ? FixColor.GREEN : Style.getMain()).alpha(abstractClientPlayer.getDeathAnim().getSize());
            	FixColor color2 = (friend ? FixColor.GREEN.darker(0.4f) : Style.getSecond()).alpha(abstractClientPlayer.getDeathAnim().getSize());

            	GlStateManager.enableBlend();
            	
            	this.renderSmoothCape(poseStack, multiBufferSource, renderer, abstractClientPlayer, delta, i, FastNative.getImageResource("masks/cape/background.png"), rock.getThemes().getSecondColor().alpha(abstractClientPlayer.getDeathAnim().getSize()), rock.getThemes().getSecondColor().alpha(abstractClientPlayer.getDeathAnim().getSize()));
                
            	if (rock.isPremium()) {
            		this.renderSmoothCape(poseStack, multiBufferSource, renderer, abstractClientPlayer, delta, i, FastNative.getImageResource("masks/cape/premlogo.png"), FixColor.WHITE, FixColor.WHITE);
            	} else {
            		this.renderSmoothCape(poseStack, multiBufferSource, renderer, abstractClientPlayer, delta, i, FastNative.getImageResource("masks/cape/logo.png"), color1 , color2);
            	}
            	
                this.renderSmoothCape(poseStack, multiBufferSource, renderer, abstractClientPlayer, delta, i, FastNative.getImageResource("masks/cape/qr.png"), rock.getThemes().getTextFirstColor().alpha(abstractClientPlayer.getDeathAnim().getSize()), rock.getThemes().getTextFirstColor().alpha(abstractClientPlayer.getDeathAnim().getSize()));

            } else {
                final ModelRenderer[] parts = this.customCape;
                for (int part = 0; part < CustomCapeRenderLayer.partCount; ++part) {
                    final ModelRenderer model = parts[part];
                    this.modifyPoseStack(poseStack, abstractClientPlayer, delta, part);
                    renderer.render(abstractClientPlayer, part, model, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
                    poseStack.pop();
                }
            }

        } else {
            this.renderVanillaCape(poseStack, multiBufferSource, i, abstractClientPlayer, f, g, delta, j, k, l);
        }
    }

    public void renderVanillaCape(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.hasPlayerInfo() && !player.isInvisible() && player.isWearing(PlayerModelPart.CAPE) && player.getLocationSkin() != null) {
            ItemStack itemstack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);

            if (itemstack.getItem() != Items.ELYTRA) {
                matrixStackIn.push();
                matrixStackIn.translate(0.0D, 0.0D, 0.125D);
                double d0 = MathHelper.lerp(partialTicks, player.prevChasingPosX, player.chasingPosX) - MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX());
                double d1 = MathHelper.lerp(partialTicks, player.prevChasingPosY, player.chasingPosY) - MathHelper.lerp(partialTicks, player.prevPosY, player.getPosY());
                double d2 = MathHelper.lerp(partialTicks, player.prevChasingPosZ, player.chasingPosZ) - MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ());
                float f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset);
                double d3 = MathHelper.sin(f * ((float) Math.PI / 180F));
                double d4 = -MathHelper.cos(f * ((float) Math.PI / 180F));
                float f1 = (float) d1 * 10.0F;
                f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
                float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
                f3 = MathHelper.clamp(f3, -20.0F, 20.0F);

                if (f2 < 0.0F) {
                    f2 = 0.0F;
                }

                if (f2 > 165.0F) {
                    f2 = 165.0F;
                }

                if (f1 < -5.0F) {
                    f1 = -5.0F;
                }

                float f4 = MathHelper.lerp(partialTicks, player.prevCameraYaw, player.cameraYaw);
                f1 = f1 + MathHelper.sin(MathHelper.lerp(partialTicks, player.prevDistanceWalkedModified, player.distanceWalkedModified) * 6.0F) * 32.0F * f4;
                
                if (player.isCrouching() || (!rock.isPanic() && rock.getModules().get(Sneak.class).get() && player == mc.player)) {
                    f1 += 25.0f;
                    matrixStackIn.translate(0.0, 0.18160000596046448, 0.0);
                }

                float f5 = Config.getAverageFrameTimeSec() * 20.0F;
                f5 = Config.limit(f5, 0.02F, 1.0F);
                player.capeRotateX = MathHelper.lerp(f5, player.capeRotateX, 6.0F + f2 / 2.0F + f1);
                player.capeRotateZ = MathHelper.lerp(f5, player.capeRotateZ, f3 / 2.0F);
                player.capeRotateY = MathHelper.lerp(f5, player.capeRotateY, 180.0F - f3 / 2.0F);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(player.capeRotateX));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(player.capeRotateZ));
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(player.capeRotateY));

                try {
                	 IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntitySolid(player.getLocationCape()));
                     this.getEntityModel().renderCape(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
                } catch (Exception e) {

                }


                //IVertexBuilder ivertexbuilder1 = bufferIn.getBuffer(RenderType.getEntitySolid(FastNative.getImageResource("masks/cape/logo.png")));
                //Render.color(Style.getMain());
               // this.getEntityModel().renderCape(matrixStackIn, ivertexbuilder1, packedLightIn, OverlayTexture.NO_OVERLAY);


                //IVertexBuilder ivertexbuilder2 = bufferIn.getBuffer(RenderType.getEntitySolid(FastNative.getImageResource("masks/cape/qr.png")));
                //Render.color(FixColor.BLACK);
                //this.getEntityModel().renderCape(matrixStackIn, ivertexbuilder2, packedLightIn, OverlayTexture.NO_OVERLAY);


                matrixStackIn.pop();
            }
        }
    }

    private void renderSmoothCape(MatrixStack poseStack, IRenderTypeBuffer multiBufferSource, CapeRenderer capeRenderer, AbstractClientPlayerEntity abstractClientPlayer, float delta, int light, ResourceLocation cape, FixColor color1, FixColor color2) {
        IVertexBuilder bufferBuilder = capeRenderer.getVertexConsumer(multiBufferSource, abstractClientPlayer, cape);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        
        Matrix4f oldPositionMatrix = null;
        for (int part = 0; part < partCount; part++) {
            modifyPoseStack(poseStack, abstractClientPlayer, delta, part);

            if (oldPositionMatrix == null) {
                oldPositionMatrix = poseStack.getLast().getMatrix();
            }

            if (part == 0) {
                addTopVertex(bufferBuilder, poseStack.getLast().getMatrix(), oldPositionMatrix,
                        0.3F,
                        0,
                        0F,
                        -0.3F,
                        0,
                        -0.06F, part, light,
                        color1, color2);
            }

            if (part == partCount - 1) {
                addBottomVertex(bufferBuilder, poseStack.getLast().getMatrix(), poseStack.getLast().getMatrix(),
                        0.3F,
                        (part + 1) * (0.96F / partCount),
                        0F,
                        -0.3F,
                        (part + 1) * (0.96F / partCount),
                        -0.06F, part, light,
                        color1, color2);
            }

            addLeftVertex(bufferBuilder, poseStack.getLast().getMatrix(), oldPositionMatrix,
                    -0.3F,
                    (part + 1) * (0.96F / partCount),
                    0F,
                    -0.3F,
                    part * (0.96F / partCount),
                    -0.06F, part, light,
                    color1, color2);

            addRightVertex(bufferBuilder, poseStack.getLast().getMatrix(), oldPositionMatrix,
                    0.3F,
                    (part + 1) * (0.96F / partCount),
                    0F,
                    0.3F,
                    part * (0.96F / partCount),
                    -0.06F, part, light,
                    color1, color2);

            addBackVertex(bufferBuilder, poseStack.getLast().getMatrix(), oldPositionMatrix,
                    0.3F,
                    (part + 1) * (0.96F / partCount),
                    -0.06F,
                    -0.3F,
                    part * (0.96F / partCount),
                    -0.06F, part, light,
                    color1, color2);

            addFrontVertex(bufferBuilder, oldPositionMatrix, poseStack.getLast().getMatrix(),
                    0.3F,
                    (part + 1) * (0.96F / partCount),
                    0F,
                    -0.3F,
                    part * (0.96F / partCount),
                    0F, part, light,
                    color1, color2);

            oldPositionMatrix = poseStack.getLast().getMatrix().copy();
            poseStack.pop();
        }
    }

    private void modifyPoseStack(MatrixStack poseStack, AbstractClientPlayerEntity abstractClientPlayer, float h, int part) {
        if (WaveyCapesBase.config.capeMovement == CapeMovement.BASIC_SIMULATION) {
            modifyPoseStackSimulation(poseStack, abstractClientPlayer, h, part);
            return;
        }
        modifyPoseStackVanilla(poseStack, abstractClientPlayer, h, part);
    }

    private void modifyPoseStackSimulation(final MatrixStack poseStack, final AbstractClientPlayerEntity abstractClientPlayer, final float delta, final int part) {
        final StickSimulation simulation = ((CapeHolder) abstractClientPlayer).getSimulation();
        poseStack.push();
        Cosmetics cosm = rock.getModules().get(Cosmetics.class);
        final ItemStack itemStack = abstractClientPlayer.getItemStackFromSlot(EquipmentSlotType.CHEST);
        double z1 = !itemStack.isEmpty() ? 0.18 : 0.125;
        poseStack.translate(0.0, 0.0, z1);

        StickSimulation.Point capePoint = simulation.getPoints().get(0);
        float x = simulation.getPoints().get(part).getLerpX(delta) - capePoint.getLerpX(delta);
        if (x > 0.0f) {
            x = 0.0f;
        }
        final float y = capePoint.getLerpY(delta) - part - simulation.getPoints().get(part).getLerpY(delta);
        final float z = capePoint.getLerpZ(delta) - simulation.getPoints().get(part).getLerpZ(delta);
        final float sidewaysRotationOffset = 0.0f;
        final float partRotation = this.getRotation(delta, part, simulation);
        float height = 0.0f;
        if (abstractClientPlayer.isCrouching() || (!rock.isPanic() && rock.getModules().get(Sneak.class).get() && abstractClientPlayer == mc.player)) {
            height += 25.0f;
            poseStack.translate(0.0, 0.18160000596046448, 0.0);
        }
        
        if (cosm.getModel(abstractClientPlayer) == 1) {
        	height += 5.0f;
            poseStack.translate(0.0, 0.65, 0.0);
        }
        
        boolean friend = rock.getFriendsHandler().isFriend(abstractClientPlayer);
        final float naturalWindSwing = this.getNatrualWindSwing(part, abstractClientPlayer.canSwim());
        poseStack.rotate(Vector3f.XP.rotationDegrees(6.0f + height + naturalWindSwing));
        poseStack.rotate(Vector3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0f));
        poseStack.rotate(Vector3f.YP.rotationDegrees(180.0f - sidewaysRotationOffset / 2.0f));
        poseStack.translate(-z / CustomCapeRenderLayer.partCount, y / CustomCapeRenderLayer.partCount, x / CustomCapeRenderLayer.partCount);
        poseStack.translate(0.0, 0.03, -0.03);
        poseStack.translate(0.0, part * 1.0f / CustomCapeRenderLayer.partCount, 0);
        poseStack.rotate(Vector3f.XP.rotationDegrees(-partRotation));
        poseStack.translate(0.0, -part * 1.0f / CustomCapeRenderLayer.partCount, -part * (0) / CustomCapeRenderLayer.partCount);
        poseStack.translate(0.0, -0.03, 0.03);
    }

    private float getRotation(final float delta, final int part, final StickSimulation simulation) {
        if (part == CustomCapeRenderLayer.partCount - 1) {
            return this.getRotation(delta, part - 1, simulation);
        }
        return (float) this.getAngle(simulation.points.get(part).getLerpedPos(delta), simulation.points.get(part + 1).getLerpedPos(delta));
    }

    private double getAngle(final Vector3 a, final Vector3 b) {
        final Vector3 angle = b.subtract(a);
        return Math.toDegrees(Math.atan2(angle.x, angle.y)) + 245.0;
    }

    private void modifyPoseStackVanilla(final MatrixStack poseStack, final AbstractClientPlayerEntity abstractClientPlayer, final float h, final int part) {
        poseStack.push();
        poseStack.translate(0.0, 0.0, 0.125);
        final double d = MathHelper.lerp(h, abstractClientPlayer.prevChasingPosX, abstractClientPlayer.chasingPosX) - MathHelper.lerp(h, abstractClientPlayer.prevPosX, abstractClientPlayer.getPosX());
        final double e = MathHelper.lerp(h, abstractClientPlayer.prevChasingPosY, abstractClientPlayer.chasingPosY) - MathHelper.lerp(h, abstractClientPlayer.prevPosY, abstractClientPlayer.getPosY());
        final double m = MathHelper.lerp(h, abstractClientPlayer.prevChasingPosZ, abstractClientPlayer.chasingPosZ) - MathHelper.lerp(h, abstractClientPlayer.prevPosZ, abstractClientPlayer.getPosZ());
        final float n = abstractClientPlayer.prevRenderYawOffset + abstractClientPlayer.renderYawOffset - abstractClientPlayer.prevRenderYawOffset;
        final double o = MathHelper.sin(n * 0.017453292f);
        final double p = -MathHelper.cos(n * 0.017453292f);
        float height = (float) e * 10.0f;
        height = MathHelper.clamp(height, -6.0f, 32.0f);
        float swing = (float) (d * o + m * p) * easeOutSine(1.0f / CustomCapeRenderLayer.partCount * part) * 100.0f;
        swing = MathHelper.clamp(swing, 0.0f, 150.0f * easeOutSine(1.0f / CustomCapeRenderLayer.partCount * part));
        float sidewaysRotationOffset = (float) (d * p - m * o) * 100.0f;
        sidewaysRotationOffset = MathHelper.clamp(sidewaysRotationOffset, -20.0f, 20.0f);
        final float t = MathHelper.lerp(h, abstractClientPlayer.prevCameraYaw, abstractClientPlayer.cameraYaw);
        height += MathHelper.sin(MathHelper.lerp(h, abstractClientPlayer.prevDistanceWalkedModified, abstractClientPlayer.distanceWalkedModified) * 6.0f) * 32.0f * t;
        if (abstractClientPlayer.isCrouching()) {
            height += 25.0f;
            poseStack.translate(0.0, 0.15000000596046448, 0.0);
        }
        final float naturalWindSwing = this.getNatrualWindSwing(part, abstractClientPlayer.canSwim());
        poseStack.rotate(Vector3f.XP.rotationDegrees(6.0f + swing / 2.0f + height + naturalWindSwing));
        poseStack.rotate(Vector3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0f));
        poseStack.rotate(Vector3f.YP.rotationDegrees(180.0f - sidewaysRotationOffset / 2.0f));
    }

    private float getNatrualWindSwing(final int part, final boolean underwater) {
        final long highlightedPart = System.currentTimeMillis() / (underwater ? 9 : 3) % 360L;
        final float relativePart = (part + 1) / (float) CustomCapeRenderLayer.partCount;
        if (WaveyCapesBase.config.windMode == WindMode.WAVES) {
            return (float) (Math.sin(Math.toRadians(relativePart * 360.0f - highlightedPart)) * 13.0);
        }
        return 0.0f;
    }

    private static void addBackVertex(IVertexBuilder bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light, FixColor color1, FixColor color2) {
        float i;
        Matrix4f k;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;

            k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }

        float minU = .015625F;
        float maxU = .171875F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();

        bufferBuilder.pos(oldMatrix, x1, y2, z1).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y2, z1).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x2, y1, z2).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x1, y1, z2).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
    }

    private static void addFrontVertex(IVertexBuilder bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light, FixColor color1, FixColor color2) {
        float i;
        Matrix4f k;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;

            k = matrix;
            matrix = oldMatrix;
            oldMatrix = k;
        }

        float minU = .1875F;
        float maxU = .34375F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();

        bufferBuilder.pos(oldMatrix, x1, y1, z1).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y1, z1).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x2, y2, z2).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x1, y2, z2).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
    }

    private static void addLeftVertex(IVertexBuilder bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light, FixColor color1, FixColor color2) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = 0;
        float maxU = .015625F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();

        bufferBuilder.pos(matrix, x2, y1, z1).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x2, y1, z2).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y2, z2).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y2, z1).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
    }

    private static void addRightVertex(IVertexBuilder bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light, FixColor color1, FixColor color2) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .171875F;
        float maxU = .1875F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();

        bufferBuilder.pos(matrix, x2, y1, z2).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x2, y1, z1).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y2, z1).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y2, z2).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
    }

    private static void addBottomVertex(IVertexBuilder bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light, FixColor color1, FixColor color2) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .171875F;
        float maxU = .328125F;

        float minV = 0;
        float maxV = .03125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();
        bufferBuilder.pos(oldMatrix, x1, y2, z2).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y2, z2).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x2, y1, z1).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
        bufferBuilder.pos(matrix, x1, y1, z1).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(1, 0, 0).endVertex();
    }

    private static void addTopVertex(IVertexBuilder bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light, FixColor color1, FixColor color2) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .015625F;
        float maxU = .171875F;

        float minV = 0;
        float maxV = .03125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();

        bufferBuilder.pos(oldMatrix, x1, y2, z1).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(0, 1, 0).endVertex();
        bufferBuilder.pos(oldMatrix, x2, y2, z1).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, maxV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(0, 1, 0).endVertex();
        bufferBuilder.pos(matrix, x2, y1, z2).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(minU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(0, 1, 0).endVertex();
        bufferBuilder.pos(matrix, x1, y1, z2).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(maxU, minV).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(0, 1, 0).endVertex();
    }

    private static final VanillaCapeRenderer vanillaCape = new VanillaCapeRenderer();

    private CapeRenderer getCapeRenderer(AbstractClientPlayerEntity abstractClientPlayer, IRenderTypeBuffer multiBufferSource) {
        if (!abstractClientPlayer.hasPlayerInfo() || !abstractClientPlayer.isWearing(PlayerModelPart.CAPE)) {
            return null;
        }
        CustomCapeRenderLayer.vanillaCape.vertexConsumer = multiBufferSource.getBuffer(RenderType.getEntityCutout(FastNative.getImageResource("masks/cape/background.png")));
        return CustomCapeRenderLayer.vanillaCape;
    }

    private static final int scale = 1000 * 60 * 60;

    /**
     * Returns between 0 and 2
     *
     * @param posY
     * @return
     */
    private static float getWind(double posY) {
        float x = (System.currentTimeMillis() % scale) / 10000f;
        float mod = MathHelper.clamp(1f / 200f * (float) posY, 0f, 1f);
        return MathHelper.clamp((float) (Math.sin(2 * x) + Math.sin(Math.PI * x)) * mod, 0, 2);
    }


    /**
     * https://easings.net/#easeOutSine
     *
     * @param x
     * @return
     */
    private static float easeOutSine(float x) {
        return (float) Math.sin((x * Math.PI) / 2f);

    }
}
