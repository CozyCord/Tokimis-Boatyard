package net.cozystudios.tokimiboatyard.mixin;

import net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BoatEntity.class)
public class BoatEntitySpeedMixin {
    @ModifyConstant(method = "updatePaddles", constant = @Constant(floatValue = 0.04F))
    private float tokimiboatyard_scaleForwardAccel(float original) {
        if ((Object) this instanceof UpgradedBoatEntity ube) {
            return original * ube.getTier().speedMultiplier();
        }
        return original;
    }
}
