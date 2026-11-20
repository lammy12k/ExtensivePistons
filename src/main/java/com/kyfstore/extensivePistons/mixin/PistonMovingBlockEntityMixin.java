package com.kyfstore.extensivePistons.mixin;

import com.kyfstore.extensivePistons.CarriedBlockData;
import com.kyfstore.extensivePistons.ExtensivePistons;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity {
    public PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 0.5F))
    private static float modifyPistonAnimationSpeed(float originalStep) {
        return originalStep * ExtensivePistons.getPistonSpeed();
    }

    @Unique
    private static void extensivePistons$applyData(String source, Level level, BlockPos pos, PistonMovingBlockEntity movingBe) {
        if (level.isClientSide()) {
            return;
        }

        BlockEntity newBe = level.getBlockEntity(pos);
        if (newBe != null && newBe != movingBe) {
            CompoundTag data = CarriedBlockData.detach(movingBe);
            if (data != null) {
                newBe.loadWithComponents(
                        TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data)
                );
                newBe.setChanged();
                BlockState placedState = level.getBlockState(pos);
                level.sendBlockUpdated(pos, placedState, placedState, 3);
                level.updateNeighborsAt(pos, placedState.getBlock());
            }
        }
    }

    @Inject(method = "finalTick", at = @At("RETURN"))
    private void onFinalTickReturn(CallbackInfo ci) {
        if (this.level != null) {
            extensivePistons$applyData("finalTick", this.level, this.worldPosition, (PistonMovingBlockEntity) (Object) this);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private static void onTickReturn(Level level, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
        extensivePistons$applyData("tick", level, pos, blockEntity);
    }
}