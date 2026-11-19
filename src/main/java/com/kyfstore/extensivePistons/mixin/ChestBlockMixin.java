package com.kyfstore.extensivePistons.mixin;

import com.kyfstore.extensivePistons.ExtensivePistons;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @ModifyReturnValue(method = "updateShape", at = @At("RETURN"))
    private BlockState rejoinPushedChests(
            BlockState original,
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (!ExtensivePistons.isPushableTileEntities()) {
            return original;
        }

        if (original.hasProperty(ChestBlock.TYPE) && original.getValue(ChestBlock.TYPE) == ChestType.SINGLE
                && neighborState.is(original.getBlock())
                && neighborState.hasProperty(ChestBlock.TYPE) && neighborState.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            Direction facing = original.getValue(ChestBlock.FACING);
            if (neighborState.getValue(ChestBlock.FACING) == facing && direction.getAxis() == facing.getClockWise().getAxis()) {
                ChestType candidate = (direction == facing.getClockWise()) ? ChestType.LEFT : ChestType.RIGHT;
                BlockState result = original.setValue(ChestBlock.TYPE, candidate);
                if (ChestBlock.getConnectedDirection(result) == direction) {
                    return result;
                }
            }
        }

        return original;
    }
}
