package com.refinedmods.rangedpumps.block;

import com.refinedmods.rangedpumps.RangedPumps;
import com.refinedmods.rangedpumps.blockentity.PumpBlockEntity;
import com.refinedmods.rangedpumps.blockentity.PumpState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class PumpBlock extends Block implements EntityBlock {
    public PumpBlock() {
        super(Block.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RangedPumps.ID, "pump")))
            .strength(1.9F)
            .sound(SoundType.STONE));
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state,
                                               final Level level,
                                               final BlockPos pos,
                                               final Player player,
                                               final BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PumpBlockEntity pump)) {
            return InteractionResult.SUCCESS;
        }
        player.sendSystemMessage(PumpState.getMessage(pump));
        player.sendSystemMessage(pump.getMessage());
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PumpBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return !level.isClientSide() ? (levelTicker, pos, stateTicker, blockEntity) -> {
            if (blockEntity instanceof PumpBlockEntity pump) {
                pump.tick();
            }
        } : null;
    }
}
