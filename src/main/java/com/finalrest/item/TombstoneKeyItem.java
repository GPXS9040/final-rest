package com.finalrest.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
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
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid(TAG, tombId);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }

    @Nullable
    public static UUID getTombId(ItemStack stack) {
        if (!stack.isOf(ModItems.TOMBSTONE_KEY)) return null;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return null;
        NbtCompound nbt = component.copyNbt();
        return nbt.containsUuid(TAG) ? nbt.getUuid(TAG) : null;
    }

    public static boolean matches(ItemStack stack, @Nullable UUID tombId) {
        return tombId != null && tombId.equals(getTombId(stack));
    }

    /** Llave o nota que pertenecen a esa tumba. */
    private static boolean belongsTo(ItemStack stack, UUID tombId) {
        if (!stack.isOf(ModItems.TOMBSTONE_KEY) && !stack.isOf(ModItems.TOMBSTONE_NOTE)) return false;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;
        NbtCompound nbt = component.copyNbt();
        return nbt.containsUuid(TAG) && tombId.equals(nbt.getUuid(TAG));
    }

    /** Borra del inventario de todos los jugadores online las llaves y notas de esa tumba. */
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