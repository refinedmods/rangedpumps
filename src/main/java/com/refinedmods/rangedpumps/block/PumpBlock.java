package com.refinedmods.rangedpumps.block;

import com.refinedmods.rangedpumps.RangedPumps;
import com.refinedmods.rangedpumps.blockentity.PumpState;
import com.refinedmods.rangedpumps.blockentity.PumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ObjectHolder;

public class PumpBlock extends Block implements EntityBlock {
    @ObjectHolder(RangedPumps.ID + ":pump")
    public static final PumpBlock BLOCK = null;

    public PumpBlock() {
        super(Block.Properties.of(Material.STONE).strength(1.9F).sound(SoundType.STONE));

        setRegistryName(RangedPumps.ID, "pump");
    }


    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof PumpBlockEntity pump) {
                IEnergyStorage energy = pump.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                if (energy == null) {
                    return InteractionResult.SUCCESS;
                }

                Component message = PumpState.getMessage(pump);

                if (message != null) {
                    player.sendMessage(message, player.getUUID());
                }

                if (pump.getTank().getFluidAmount() == 0) {
                    player.sendMessage(new TranslatableComponent("block." + RangedPumps.ID + ".pump.state_empty", energy.getEnergyStored(), energy.getMaxEnergyStored()), player.getUUID());
                } else {
                    player.sendMessage(new TranslatableComponent("block." + RangedPumps.ID + ".pump.state", pump.getTank().getFluidAmount(), pump.getTank().getFluid().getDisplayName(), energy.getEnergyStored(), energy.getMaxEnergyStored()), player.getUUID());
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
