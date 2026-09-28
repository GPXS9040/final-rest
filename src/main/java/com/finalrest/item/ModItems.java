package com.finalrest.item;

import com.finalrest.FinalRest;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
    public static final Item TOMBSTONE_KEY = registerItem("tombstone_key",
            new TombstoneKeyItem(new Item.Settings().maxCount(1)));

    public static final Item TOMBSTONE_NOTE = registerItem("tombstone_note",
            new TombstoneNoteItem(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, FinalRest.id(name), item);
    }

    public static void registerModItems() {
        FinalRest.LOGGER.info("Registrando items de " + FinalRest.MOD_ID);
    }
}