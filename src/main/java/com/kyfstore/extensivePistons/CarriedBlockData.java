package com.kyfstore.extensivePistons;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class CarriedBlockData {

    private static final Map<PistonMovingBlockEntity, CompoundTag> ENTITY_NBT =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CarriedBlockData() {}

    public static void attach(PistonMovingBlockEntity movingBe, CompoundTag tag) {
        ENTITY_NBT.put(movingBe, tag);
    }

    @Nullable
    public static CompoundTag detach(PistonMovingBlockEntity movingBe) {
        return ENTITY_NBT.remove(movingBe);
    }
}
