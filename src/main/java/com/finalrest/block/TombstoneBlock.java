package com.finalrest.block;

import com.finalrest.block.entity.TombstoneBlockEntity;
import com.finalrest.item.TombstoneKeyItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TombstoneBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    private static final VoxelShape SHAPE_NS = VoxelShapes.cuboid(1 / 16.0, 0, 0, 15 / 16.0, 1.0, 1.0);
    private static final VoxelShape SHAPE_EW = VoxelShapes.cuboid(0, 0, 1 / 16.0, 1.0, 1.0, 15 / 16.0);

    public TombstoneBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends TombstoneBlock> getCodec() {
        return createCodec(TombstoneBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return (facing == Direction.NORTH || facing == Direction.SOUTH) ? SHAPE_NS : SHAPE_EW;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TombstoneBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ItemActionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos) instanceof TombstoneBlockEntity tombstone) {
            boolean hasKey = TombstoneKeyItem.matches(stack, tombstone.getTombId());
            boolean isOwner = tombstone.isOwner(player);

            if (isOwner || hasKey) {
                // Los items van directo al inventario
                tombstone.giveItemsTo(player);
                // onStateReplaced borra las llaves de esta tumba
                world.removeBlock(pos, false);
                return ItemActionResult.SUCCESS;
            }

            player.sendMessage(Text.literal("Esta tumba no es tuya."), true);
            return ItemActionResult.FAIL;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (!world.isClient && world instanceof ServerWorld serverWorld
                    && world.getBlockEntity(pos) instanceof TombstoneBlockEntity tombstone) {
                // Si ya se entregaron al jugador, esto no suelta nada (lista vacía)
                tombstone.dropAllItems(serverWorld, pos);
                TombstoneKeyItem.removeKeysFor(serverWorld.getServer(), tombstone.getTombId());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}