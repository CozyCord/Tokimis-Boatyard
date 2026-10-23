package net.cozystudios.tokimiboatyard.mixin.client;

import net.cozystudios.tokimiboatyard.resource.TokimiPackProvider;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public abstract class ResourcePackManagerMixin {

    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V", at = @At("TAIL"))
    private void tokimiboatyard$addProvider(RepositorySource[] providersArg, CallbackInfo ci) {
        Set<RepositorySource> merged = new LinkedHashSet<>(this.sources);
        merged.add(new TokimiPackProvider());
        this.sources = merged;
    }
}
