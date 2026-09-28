package com.finalrest.mixin;

import com.finalrest.event.PlayerDeathHandler;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerEntity.class, priority = 2000)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void finalrest$onDeath(DamageSource source, CallbackInfo ci) {
        PlayerDeathHandler.handleRealDeath((ServerPlayerEntity) (Object) this);
    }
}