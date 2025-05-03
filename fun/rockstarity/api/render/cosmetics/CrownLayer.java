package fun.rockstarity.api.render.cosmetics;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.client.modules.render.Cosmetics;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class CrownLayer<T extends LivingEntity, M extends EntityModel<T> & IHasHead> extends LayerRenderer<T, M> implements IAccess
{
    private final float field_239402_a_;
    private final float field_239403_b_;
    private final float field_239404_c_;

    public CrownLayer(IEntityRenderer<T, M> p_i50946_1_)
    {
        this(p_i50946_1_, 1.0F, 1.0F, 1.0F);
    }

    public CrownLayer(IEntityRenderer<T, M> p_i232475_1_, float p_i232475_2_, float p_i232475_3_, float p_i232475_4_)
    {
        super(p_i232475_1_);
        this.field_239402_a_ = p_i232475_2_;
        this.field_239403_b_ = p_i232475_3_;
        this.field_239404_c_ = p_i232475_4_;
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	if (entitylivingbaseIn != mc.player || rock.isPanic()) return;
        matrixStackIn.push();
        matrixStackIn.scale(this.field_239402_a_, this.field_239403_b_, this.field_239404_c_);

        if (entitylivingbaseIn.isChild())
        {
            float f = 2.0F;
            float f1 = 1.4F;
            matrixStackIn.translate(0.0D, 0.03125D, 0.0D);
            matrixStackIn.scale(0.7F, 0.7F, 0.7F);
            matrixStackIn.translate(0.0D, 1.0D, 0.0D);
        }

        this.getEntityModel().getModelHead().translateRotate(matrixStackIn);

        matrixStackIn.translate(0.0D, -0.25D, 0.0D);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
        matrixStackIn.scale(0.625F, -0.625F, -0.625F);
        
        rock.getModules().get(Cosmetics.class).getCrown().render(matrixStackIn);

        matrixStackIn.pop();
    }
}
