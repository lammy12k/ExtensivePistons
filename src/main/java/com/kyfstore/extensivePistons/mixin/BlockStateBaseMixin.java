package com.kyfstore.extensivePistons.mixin;

import com.kyfstore.extensivePistons.ExtensivePistons;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow
    public abstract Block getBlock();

    @ModifyReturnValue(method = "getPistonPushReaction", at = @At("RETURN"))
    private PushReaction modifyPushReaction(PushReaction original) {
        Block block = this.getBlock();
        if (ExtensivePistons.isBlacklisted(block)) {
            return PushReaction.IMMOVEABLE;
        }
        if (ExtensivePistons.isWhitelisted(block)) {
            return PushReaction.PUSH_PULL;
        }
        if (ExtensivePistons.isPushableTileEntities() && block instanceof ShulkerBoxBlock) {
            return PushReaction.PUSH_PULL;
        }
        return original;
    }
}
