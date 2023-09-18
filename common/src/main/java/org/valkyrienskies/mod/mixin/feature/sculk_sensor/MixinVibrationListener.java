package org.valkyrienskies.mod.mixin.feature.sculk_sensor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(VibrationListener.class)
public abstract class MixinVibrationListener {

    @Unique
    private int step = 0;

    @Inject(
        method = "handleGameEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Z",
        at = @At(
            value = "HEAD"
        ),
        cancellable = true
    )
    public void handleGameEvent(
        final Level level,
        final GameEvent gameEvent,
        final Entity entity,
        final BlockPos blockPos,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (step == 0) {
            step = 1;

            final Vector3d newPos =
                VSGameUtilsKt.toWorldCoordinates(
                    level,
                    new Vector3d(
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ()
                    )
                );

            final BlockPos newBlockPos = new BlockPos(
                newPos.x,
                newPos.y,
                newPos.z
            );

            cir.setReturnValue(VibrationListener.class.cast(this)
                .handleGameEvent(level, gameEvent, entity, newBlockPos));
            cir.cancel();
            return;
        }
        step = 0;
    }

}
