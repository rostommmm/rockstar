package fun.rockstarity.client.modules.player;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jbk
 * @since 02.02.2025
 */
@Info(name = "ClickTrough", desc = "Позволяет взаимодействовать с контейнерами через стены", type = Category.PLAYER, module = { "GhostHand", "OpenWalls" } )
public class ClickTrough extends Module {
    private final Set<BlockPos> checkedPositions = new HashSet<>();

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null || mc.playerController == null) return;
        if (!mc.gameSettings.keyBindUseItem.isPressed() || mc.player.isSneaking()) return;

        if (event instanceof EventTick) {
        Vector3d offset = new Vector3d(0, 0, 0.1)
                .rotatePitch(-(float) Math.toRadians(mc.player.rotationPitch))
                .rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));

            checkedPositions.clear();

            // Проходим по всем позициям от игрока
            for (int i = 1; i < 50; i++) {
                BlockPos targetPos = new BlockPos(getCameraPosVec(mc.getRenderPartialTicks()).add(offset.mul(i)));

                // Если эта позиция уже проверялась, пропускаем её
                if (checkedPositions.contains(targetPos)) {
                    continue;
                }

                checkedPositions.add(targetPos);

                // Проверяем все сущности в мире на эту позицию
                for (TileEntity blockEntity : mc.world.loadedTileEntityList) {
                    if (blockEntity.getPos().equals(targetPos)) {
                        // взаимодействуем с блоком, если он найден (func_217292_a это interactBlock, обожаю эти маппинги)
                        mc.playerController.func_217292_a(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5), Direction.UP, targetPos, true));
                        return;
                    }
                }
            }
        }
    }

    /**
     * Метод для вычисления позиции камеры игрока
     * Скорее всего есть уже существующий метод которые делает то же самое и это дубликат, стоит это убрать
     */
    public Vector3d getCameraPosVec(float partialTicks) {
        if (mc.player == null) return Vector3d.ZERO;

        double x = MathHelper.lerp(partialTicks, mc.player.prevPosX, mc.player.getPosX());
        double y = MathHelper.lerp(partialTicks, mc.player.prevPosY, mc.player.getPosY()) + mc.player.getEyeHeight(mc.player.getPose());
        double z = MathHelper.lerp(partialTicks, mc.player.prevPosZ, mc.player.getPosZ());
        return new Vector3d(x, y, z);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}