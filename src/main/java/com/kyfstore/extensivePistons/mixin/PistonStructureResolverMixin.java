package com.kyfstore.extensivePistons.mixin;

import com.kyfstore.extensivePistons.ExtensivePistons;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.List;

@Mixin(PistonStructureResolver.class)
public class PistonStructureResolverMixin {
    @Shadow
    @Final
    private Level level;

    @Shadow
    @Final
    private Direction pushDirection;

    @Shadow
    @Final
    private List<BlockPos> toPush;

    @Shadow
    @Final
    private List<BlockPos> toDestroy;

    @ModifyConstant(method = "addBlockLine", constant = @Constant(intValue = 12))
    private int modifyPushLimit(int original) {
        return ExtensivePistons.getPushLimit();
    }

    @ModifyReturnValue(method = "resolve", at = @At("RETURN"))
    private boolean validateBoundsAndTileEntityCap(boolean resolved) {
        if (!resolved) {
            return false;
        }

        int tileEntityCount = 0;
        int maxTileEntities = ExtensivePistons.getMaxTileEntitiesPerPush();

        for (BlockPos pos : this.toPush) {
            BlockPos dest = pos.relative(this.pushDirection);
            if (dest.getY() > this.level.getMaxY() || dest.getY() < this.level.getMinY()) {
                return false;
            }

            BlockState state = this.level.getBlockState(pos);
            if (state.hasBlockEntity()) {
                tileEntityCount++;
                if (tileEntityCount > maxTileEntities) {
                    return false;
                }
            }
        }

        return true;
    }
}
