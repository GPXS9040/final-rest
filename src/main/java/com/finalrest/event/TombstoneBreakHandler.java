package com.finalrest.event;

import com.finalrest.block.TombstoneBlock;
import com.finalrest.block.entity.TombstoneBlockEntity;
import com.finalrest.item.TombstoneKeyItem;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.text.Text;

public class TombstoneBreakHandler {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(state.getBlock() instanceof TombstoneBlock) || world.isClient) {
                return true;
            }
            if (!(blockEntity instanceof TombstoneBlockEntity tombstone)) {
                return true;
            }

            boolean hasKey = TombstoneKeyItem.matches(player.getMainHandStack(), tombstone.getTombId());
            boolean isOwner = tombstone.isOwner(player);

            if (isOwner || hasKey) {
                tombstone.giveItemsTo(player);
                return true;
            }

            if (player.isCreative()) {
                return true;
            }

            player.sendMessage(Text.literal("Esta tumba no es tuya."), true);
            return false;
        });
    }
}