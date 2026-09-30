package com.finalrest.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class TombstoneBlockEntity extends BlockEntity implements Inventory {

    // 0-35 mochila, 36-39 armadura, 40 offhand
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(41, ItemStack.EMPTY);
    private UUID ownerUuid;
    private String ownerName = "";
    private UUID tombId;

    public TombstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOMBSTONE_BLOCK_ENTITY, pos, state);
    }

    public void setOwner(PlayerEntity player) {
        this.ownerUuid = player.getUuid();
        this.ownerName = player.getName().getString();
        markDirty();
    }

    public void setOwnerData(UUID uuid, String name) {
        this.ownerUuid = uuid;
        this.ownerName = name;
        markDirty();
    }

    public boolean isOwner(PlayerEntity player) {
        return ownerUuid != null && ownerUuid.equals(player.getUuid());
    }

    public void setTombId(UUID id) {
        this.tombId = id;
        markDirty();
    }

    public UUID getTombId() {
        return tombId;
    }

    public void setItems(DefaultedList<ItemStack> newItems) {
        this.items = newItems;
        markDirty();
    }

    /**
     * Devuelve los ítems directamente al inventario del jugador.
     * Cada ítem vuelve a su slot original si está libre; si no, al primer hueco.
     * Lo que no quepa cae al suelo (invulnerable) junto al jugador.
     */
    public void giveItemsTo(PlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        DefaultedList<ItemStack> leftovers = DefaultedList.of();

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            boolean placed = false;
            if (i < 36) {
                if (inv.main.get(i).isEmpty()) {
                    inv.main.set(i, stack);
                    placed = true;
                }
            } else if (i < 40) {
                if (inv.armor.get(i - 36).isEmpty()) {
                    inv.armor.set(i - 36, stack);
                    placed = true;
                }
            } else {
                if (inv.offHand.get(0).isEmpty()) {
                    inv.offHand.set(0, stack);
                    placed = true;
                }
            }

            if (!placed) {
                leftovers.add(stack);
            }
        }

        for (ItemStack stack : leftovers) {
            if (!inv.insertStack(stack) && !stack.isEmpty()) {
                dropSafely(player, stack);
            }
        }

        items.clear();
        inv.markDirty();
        markDirty();
    }

    private static void dropSafely(PlayerEntity player, ItemStack stack) {
        ItemEntity entity = new ItemEntity(player.getWorld(),
                player.getX(), player.getY() + 0.5, player.getZ(), stack);
        entity.setInvulnerable(true);
        entity.setToDefaultPickupDelay();
        entity.setVelocity(0, 0.25, 0);
        player.getWorld().spawnEntity(entity);
    }

    /**
     * Respaldo para cuando la tumba se destruye sin un jugador con permiso
     * (explosión, creativo de otro jugador, etc.). Suelta invulnerables.
     */
    public void dropAllItems(ServerWorld world, BlockPos pos) {
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;

            ItemEntity entity = new ItemEntity(world,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    stack.copy());
            entity.setInvulnerable(true);
            entity.setToDefaultPickupDelay();
            entity.setVelocity(
                    (world.random.nextDouble() - 0.5) * 0.1,
                    0.25,
                    (world.random.nextDouble() - 0.5) * 0.1);
            world.spawnEntity(entity);
        }
        items.clear();
        markDirty();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(items, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        items.clear();
        markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, items, registryLookup);
        if (ownerUuid != null) {
            nbt.putUuid("OwnerUUID", ownerUuid);
        }
        nbt.putString("OwnerName", ownerName);
        if (tombId != null) {
            nbt.putUuid("TombId", tombId);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        items = DefaultedList.ofSize(41, ItemStack.EMPTY);
        Inventories.readNbt(nbt, items, registryLookup);
        if (nbt.containsUuid("OwnerUUID")) {
            ownerUuid = nbt.getUuid("OwnerUUID");
        }
        ownerName = nbt.getString("OwnerName");
        if (nbt.containsUuid("TombId")) {
            tombId = nbt.getUuid("TombId");
        }
    }
}