package net.cozystudios.tokimiboatyard.mixin.client;

import net.cozystudios.tokimiboatyard.resource.TokimiPackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin {

    @Shadow
    @Final
    @Mutable
    private Set<ResourcePackProvider> providers;

    @Inject(method = "<init>([Lnet/minecraft/resource/ResourcePackProvider;)V", at = @At("TAIL"))
    private void tokimiboatyard$addProvider(ResourcePackProvider[] providersArg, CallbackInfo ci) {
        Set<ResourcePackProvider> merged = new LinkedHashSet<>(this.providers);
        merged.add(new TokimiPackProvider());
        this.providers = merged;
    }
}
