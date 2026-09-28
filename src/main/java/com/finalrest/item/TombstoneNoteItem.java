package com.finalrest.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class TombstoneNoteItem extends Item {

    public TombstoneNoteItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createFor(UUID tombId, RegistryKey<World> dimension, BlockPos pos) {
        ItemStack stack = new ItemStack(ModItems.TOMBSTONE_NOTE);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid(TombstoneKeyItem.TAG, tombId);
        nbt.putInt("X", pos.getX());
        nbt.putInt("Y", pos.getY());
        nbt.putInt("Z", pos.getZ());
        nbt.putString("Dim", dimension.getValue().toString());
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound nbt = stack.getNbt();

        if (!world.isClient) {
            if (nbt != null && nbt.contains("X")) {
                String message = "Tu tumba está en X: " + nbt.getInt("X")
                        + "  Y: " + nbt.getInt("Y")
                        + "  Z: " + nbt.getInt("Z")
                        + " (" + dimensionName(nbt.getString("Dim")) + ")";
                user.sendMessage(Text.literal(message), false);
            } else {
                user.sendMessage(Text.literal("Esta nota no tiene coordenadas."), true);
            }
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    private static String dimensionName(String id) {
        return switch (id) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "End";
            default -> id;
        };
    }
}