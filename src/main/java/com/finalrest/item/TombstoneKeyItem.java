package com.finalrest.item;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TombstoneKeyItem extends Item {
    public static final String TAG = "TombId";

    public TombstoneKeyItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createFor(UUID tombId) {
        ItemStack stack = new ItemStack(ModItems.TOMBSTONE_KEY);
        stack.getOrCreateNbt().putUuid(TAG, tombId);
        return stack;
    }

    @Nullable
    public static UUID getTombId(ItemStack stack) {
        if (!stack.isOf(ModItems.TOMBSTONE_KEY)) return null;
        NbtCompound nbt = stack.getNbt();
        return (nbt != null && nbt.containsUuid(TAG)) ? nbt.getUuid(TAG) : null;
    }

    public static boolean matches(ItemStack stack, @Nullable UUID tombId) {
        return tombId != null && tombId.equals(getTombId(stack));
    }

    private static boolean belongsTo(ItemStack stack, UUID tombId) {
        if (!stack.isOf(ModItems.TOMBSTONE_KEY) && !stack.isOf(ModItems.TOMBSTONE_NOTE)) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.containsUuid(TAG) && tombId.equals(nbt.getUuid(TAG));
    }

    public static void removeKeysFor(MinecraftServer server, @Nullable UUID tombId) {
        if (tombId == null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < inv.main.size(); i++) {
                if (belongsTo(inv.main.get(i), tombId)) inv.main.set(i, ItemStack.EMPTY);
            }
            for (int i = 0; i < inv.armor.size(); i++) {
                if (belongsTo(inv.armor.get(i), tombId)) inv.armor.set(i, ItemStack.EMPTY);
            }
            if (belongsTo(inv.offHand.get(0), tombId)) inv.offHand.set(0, ItemStack.EMPTY);
            inv.markDirty();
        }
    }
}