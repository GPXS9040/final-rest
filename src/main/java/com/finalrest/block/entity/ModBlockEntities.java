package com.finalrest.block.entity;

import com.finalrest.FinalRest;
import com.finalrest.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static final BlockEntityType<TombstoneBlockEntity> TOMBSTONE_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, FinalRest.id("tombstone_be"),
                    FabricBlockEntityTypeBuilder.create(TombstoneBlockEntity::new, ModBlocks.TOMBSTONE).build());

    public static void registerBlockEntities() {
        FinalRest.LOGGER.info("Registrando block entities de " + FinalRest.MOD_ID);
    }
}