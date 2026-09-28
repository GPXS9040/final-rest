package com.finalrest.event;

import com.finalrest.block.ModBlocks;
import com.finalrest.block.TombstoneBlock;
import com.finalrest.block.entity.TombstoneBlockEntity;
import com.finalrest.item.TombstoneKeyItem;
import com.finalrest.item.TombstoneNoteItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathHandler {

    private record PendingTombstone(UUID tombId, RegistryKey<World> dimension, BlockPos pos) {}

    private static final Map<UUID, PendingTombstone> PENDING = new HashMap<>();

    private static final List<Runnable> DEFERRED = new ArrayList<>();

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(PlayerDeathHandler::onAfterRespawn);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (DEFERRED.isEmpty()) return;
            List<Runnable> toRun = new ArrayList<>(DEFERRED);
            DEFERRED.clear();
            toRun.forEach(Runnable::run);
        });
    }

    public static void handleRealDeath(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();

        if (world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        DefaultedList<ItemStack> savedItems = DefaultedList.ofSize(41, ItemStack.EMPTY);
        boolean hasAnyItem = false;

        for (int i = 0; i < inv.main.size(); i++) {
            if (!inv.main.get(i).isEmpty()) {
                savedItems.set(i, inv.main.get(i).copy());
                inv.main.set(i, ItemStack.EMPTY);
                hasAnyItem = true;
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            if (!inv.armor.get(i).isEmpty()) {
                savedItems.set(36 + i, inv.armor.get(i).copy());
                inv.armor.set(i, ItemStack.EMPTY);
                hasAnyItem = true;
            }
        }
        if (!inv.offHand.get(0).isEmpty()) {
            savedItems.set(40, inv.offHand.get(0).copy());
            inv.offHand.set(0, ItemStack.EMPTY);
            hasAnyItem = true;
        }

        if (!hasAnyItem) {
            return;
        }

        final BlockPos deathPos = player.getBlockPos();
        final Direction facing = player.getHorizontalFacing().getOpposite();
        final UUID ownerUuid = player.getUuid();
        final String ownerName = player.getName().getString();
        final UUID tombId = UUID.randomUUID();

        PENDING.put(ownerUuid, new PendingTombstone(tombId, world.getRegistryKey(), deathPos));

        DEFERRED.add(() -> placeTombstone(world, deathPos, facing, savedItems, tombId, ownerUuid, ownerName));
    }

    private static void placeTombstone(ServerWorld world, BlockPos deathPos, Direction facing,
                                       DefaultedList<ItemStack> items, UUID tombId,
                                       UUID ownerUuid, String ownerName) {
        BlockPos pos = deathPos;
        for (int i = 0; i < 10 && !world.getBlockState(pos).isReplaceable(); i++) {
            pos = pos.up();
        }
        final BlockPos finalPos = pos;

        world.setBlockState(finalPos, ModBlocks.TOMBSTONE.getDefaultState()
                .with(TombstoneBlock.FACING, facing));

        if (world.getBlockEntity(finalPos) instanceof TombstoneBlockEntity tombstone) {
            tombstone.setOwnerData(ownerUuid, ownerName);
            tombstone.setTombId(tombId);
            tombstone.setItems(items);
        }

        PENDING.computeIfPresent(ownerUuid,
                (k, v) -> new PendingTombstone(v.tombId(), world.getRegistryKey(), finalPos));
    }

    private static void onAfterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        PendingTombstone pending = PENDING.remove(oldPlayer.getUuid());
        if (pending == null) {
            return;
        }

        giveOrDrop(newPlayer, TombstoneKeyItem.createFor(pending.tombId()));
        giveOrDrop(newPlayer, TombstoneNoteItem.createFor(pending.tombId(), pending.dimension(), pending.pos()));
    }

    private static void giveOrDrop(ServerPlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }
}