package com.refinedmods.rangedpumps.block;

import com.refinedmods.rangedpumps.RangedPumps;
import com.refinedmods.rangedpumps.blockentity.PumpBlockEntity;
import com.refinedmods.rangedpumps.blockentity.PumpState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public class PumpBlock extends Block implements EntityBlock {
    public PumpBlock() {
        super(Block.Properties.of().strength(1.9F).sound(SoundType.STONE));
    }

    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof PumpBlockEntity pump) {
                IEnergyStorage energy = pump.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (energy == null) {
                    return InteractionResult.SUCCESS;
                }

                Component message = PumpState.getMessage(pump);

                if (message != null) {
                    player.sendSystemMessage(message);
                }

                if (pump.getTank().getFluidAmount() == 0) {
                    player.sendSystemMessage(Component.translatable("block." + RangedPumps.ID + ".pump.state_empty", energy.getEnergyStored(), energy.getMaxEnergyStored()));
                } else {
                    player.sendSystemMessage(Component.translatable("block." + RangedPumps.ID + ".pump.state", pump.getTank().getFluidAmount(), pump.getTank().getFluid().getDisplayName(), energy.getEnergyStored(), energy.getMaxEnergyStored()));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PumpBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide ? (levelTicker, pos, stateTicker, blockEntity) -> ((PumpBlockEntity) blockEntity).tick() : null;
    }
}
