package com.finalrest.block;

import com.finalrest.FinalRest;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static final Block TOMBSTONE = registerBlock("tombstone",
            new TombstoneBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.PURPLE)
                    .strength(2.0f)
                    .nonOpaque()));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, FinalRest.id(name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, FinalRest.id(name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        FinalRest.LOGGER.info("Registrando bloques de " + FinalRest.MOD_ID);
    }
}