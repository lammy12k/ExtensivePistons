package com.kyfstore.extensivePistons.mixin;

import com.kyfstore.extensivePistons.CarriedBlockData;
import com.kyfstore.extensivePistons.ExtensivePistons;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.TagValueOutput;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {
    @Unique
    private static final ThreadLocal<Map<BlockPos, CompoundTag>> PENDING_CAPTURED =
            ThreadLocal.withInitial(HashMap::new);

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private static void checkWhitelistAndBlacklist(
            BlockState state,
            Level level,
            BlockPos pos,
            Direction pushDir,
            boolean allowDestroy,
            Direction pistonFacing,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (state.is(Blocks.MOVING_PISTON)) {
            cir.setReturnValue(false);
            return;
        }

        if (state.hasProperty(PistonBaseBlock.EXTENDED) && state.getValue(PistonBaseBlock.EXTENDED)) {
            cir.setReturnValue(false);
            return;
        }

        Block block = state.getBlock();

        if (ExtensivePistons.isBlacklisted(block)) {
            cir.setReturnValue(false);
            return;
        }

        if (ExtensivePistons.isWhitelisted(block)) {
            if (state.hasBlockEntity() && !ExtensivePistons.isPushableTileEntities()) {
                cir.setReturnValue(false);
                return;
            }
            if (pos.getY() >= level.getMinY() && pos.getY() <= level.getMaxY() && level.getWorldBorder().isWithinBounds(pos)) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
            return;
        }

        if (state.hasBlockEntity() && !ExtensivePistons.isPushableTileEntities()) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(
            method = "isPushable",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z")
    )
    private static boolean allowBlockEntities(BlockState state, Operation<Boolean> original) {
        if (!ExtensivePistons.isPushableTileEntities()) {
            return original.call(state);
        }
        if (ExtensivePistons.isBlacklisted(state.getBlock())) {
            return true;
        }
        return false;
    }

    @ModifyReturnValue(method = "getNeighborSignal", at = @At("RETURN"))
    private boolean disableQuasiConnectivity(boolean original, SignalGetter signalGetter, BlockPos pos, Direction facing) {
        if (!ExtensivePistons.isDisableQc() || !original) {
            return original;
        }

        for (Direction dir : Direction.values()) {
            if (dir != facing && signalGetter.hasSignal(pos.relative(dir), dir)) {
                return true;
            }
        }
        return false;
    }

    @Inject(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;getToPush()Ljava/util/List;",
                    ordinal = 0
            )
    )
    private void prepareBlockEntitiesAfterResolve(
            Level level,
            BlockPos pistonPos,
            Direction pistonDirection,
            boolean extending,
            CallbackInfoReturnable<Boolean> cir,
            @Local PistonStructureResolver resolver
    ) {
        Map<BlockPos, CompoundTag> captured = PENDING_CAPTURED.get();
        captured.clear();

        if (!ExtensivePistons.isPushableTileEntities() || level.isClientSide()) {
            return;
        }

        Direction moveDir = resolver.getPushDirection();
        List<BlockPos> toPush = resolver.getToPush();

        for (BlockPos oldPos : toPush) {
            BlockState state = level.getBlockState(oldPos);
            if (!state.hasBlockEntity()) {
                continue;
            }

            extensivePistons$closeViewers(level, oldPos);

            BlockEntity oldBe = level.getBlockEntity(oldPos);
            if (oldBe != null) {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                oldBe.saveWithoutMetadata(output);
                captured.put(oldPos.relative(moveDir).immutable(), output.buildResult());

                if (oldBe instanceof Container container) {
                    container.clearContent();
                }
                level.removeBlockEntity(oldPos);
            }

            if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)) {
                ChestType chestType = state.getValue(ChestBlock.TYPE);
                if (chestType != ChestType.SINGLE) {
                    Direction connectedDir = ChestBlock.getConnectedDirection(state);
                    BlockPos partnerPos = oldPos.relative(connectedDir);
                    if (!toPush.contains(partnerPos)) {
                        level.setBlock(oldPos, state.setValue(ChestBlock.TYPE, ChestType.SINGLE), 50);
                        BlockState partnerState = level.getBlockState(partnerPos);
                        if (partnerState.is(state.getBlock()) && partnerState.hasProperty(ChestBlock.TYPE)) {
                            level.setBlock(partnerPos, partnerState.setValue(ChestBlock.TYPE, ChestType.SINGLE), 50);
                        }
                    }
                }
            }
        }
    }

    @WrapOperation(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;"
            )
    )
    private BlockEntity attachCapturedNbtToMovingEntity(
            BlockPos targetPos,
            BlockState movingBlockState,
            BlockState movedState,
            Direction direction,
            boolean extending,
            boolean isSourcePiston,
            Operation<BlockEntity> original
    ) {
        BlockEntity movingBe = original.call(targetPos, movingBlockState, movedState, direction, extending, isSourcePiston);
        if (movingBe instanceof PistonMovingBlockEntity pmbe) {
            CompoundTag tag = PENDING_CAPTURED.get().remove(targetPos);
            if (tag != null) {
                CarriedBlockData.attach(pmbe, tag);
            }
        }
        return movingBe;
    }

    @Inject(method = "moveBlocks", at = @At("RETURN"))
    private void cleanupCapturedNbt(
            Level level,
            BlockPos pistonPos,
            Direction pistonDirection,
            boolean extending,
            CallbackInfoReturnable<Boolean> cir
    ) {
        PENDING_CAPTURED.get().clear();
    }

    @Unique
    private static void extensivePistons$closeViewers(Level level, BlockPos pos) {
        org.bukkit.block.Block bukkitBlock = CraftBlock.at(level, pos);
        if (bukkitBlock.getState(false) instanceof InventoryHolder holder) {
            List<HumanEntity> viewers = new ArrayList<>(holder.getInventory().getViewers());
            for (HumanEntity viewer : viewers) {
                viewer.closeInventory();
            }
        }
    }
}
