package com.refinedmods.rangedpumps.blockentity;

import com.refinedmods.rangedpumps.RangedPumps;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class PumpBlockEntity extends BlockEntity {
    private final PumpTank tank = new PumpTank();
    private final PumpEnergy energy = new PumpEnergy(RangedPumps.SERVER_CONFIG.getEnergyCapacity());

    private int ticks;

    @Nullable
    private BlockPos currentPos;
    private int range = -1;
    private int columnIndex;
    @Nullable
    private Block blockToReplaceLiquidsWith;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(RangedPumps.PUMP_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    private int getColumnCount() {
        return range == -1 ? 1 : 8 + 8 * range;
    }

    private BlockPos getColumn(int index) {
        if (range == -1) {
            return worldPosition.below();
        }
        final int hl = 3 + 2 * range;
        final int vl = 1 + 2 * range;
        if (index < hl) {
            // Top
            return worldPosition.offset(-range - 1 + index, -1, -range - 1);
        }
        index -= hl;
        if (index < vl) {
            // Right
            return worldPosition.offset(-range - 1 + vl + 1, -1, -range - 1 + index + 1);
        }
        index -= vl;
        if (index < hl) {
            // Bottom
            return worldPosition.offset(-range - 1 + hl - index - 1, -1, -range - 1 + hl - 1);
        }
        index -= hl;
        // Left
        return worldPosition.offset(-range - 1, -1, -range - 1 + vl - index);
    }

    public void tick() {
        if (level == null) {
            return;
        }
        fillWithEnergyIfEnergyUsageIsDisabled();
        fillNeighbors();
        final boolean mayDrain = RangedPumps.SERVER_CONFIG.getSpeed() == 0
            || (ticks % RangedPumps.SERVER_CONFIG.getSpeed() == 0);
        if (mayDrain && getState() == PumpState.WORKING) {
            drain();
        }
        ticks++;
    }

    private void fillNeighbors() {
        if (tank.getResource(0).isEmpty() || level == null) {
            return;
        }
        final List<ResourceHandler<FluidResource>> fluidHandlers = getNeighboringFluidHandlers();
        if (fluidHandlers.isEmpty()) {
            return;
        }
        boolean movedAny = false;
        int transfer = (int) Math.floor((float) tank.getAmountAsInt(0) / (float) fluidHandlers.size());
        for (ResourceHandler<FluidResource> fluidHandler : fluidHandlers) {
            final int moved = ResourceHandlerUtil.move(tank, fluidHandler, resource -> true, transfer,
                null);
            movedAny |= moved > 0;
        }
        if (movedAny) {
            setChanged();
        }
    }

    private List<ResourceHandler<FluidResource>> getNeighboringFluidHandlers() {
        if (level == null) {
            return Collections.emptyList();
        }
        final List<ResourceHandler<FluidResource>> fluidHandlers = new LinkedList<>();
        for (Direction facing : Direction.values()) {
            final ResourceHandler<FluidResource> handler = level.getCapability(
                Capabilities.Fluid.BLOCK,
                worldPosition.relative(facing),
                facing.getOpposite()
            );
            if (handler != null) {
                fluidHandlers.add(handler);
            }
        }
        return fluidHandlers;
    }

    private void fillWithEnergyIfEnergyUsageIsDisabled() {
        if (RangedPumps.SERVER_CONFIG.getUseEnergy()) {
            return;
        }
        if (energy.getAmountAsInt() == energy.getCapacityAsInt()) {
            return;
        }
        try (Transaction tx = Transaction.openRoot()) {
            energy.insert(energy.getCapacityAsInt(), tx);
            tx.commit();
        }
    }

    private void drain() {
        if (level == null || !updateCurrentPosition() || currentPos == null) {
            return;
        }
        try (Transaction tx = Transaction.openRoot()) {
            final FluidResource drained = drainFluidAt(currentPos, true);
            if (drained == null) {
                return;
            }
            if (tank.internalInsert(drained, tx) != FluidType.BUCKET_VOLUME) {
                return;
            }
            drainFluidAt(currentPos, false);
            tryReplaceLiquidWithBlock();
            energy.internalExtract(RangedPumps.SERVER_CONFIG.getEnergyUsagePerDrain(), tx);
            tx.commit();
            setChanged();
        }
    }

    private boolean updateCurrentPosition() {
        if (level == null) {
            return false;
        }
        if (currentPos == null || currentPos.getY() == level.dimensionType().minY()) {
            if (columnIndex >= getColumnCount()) {
                range++;
                columnIndex = 0;
                setChanged();
                if (range > RangedPumps.SERVER_CONFIG.getRange()) {
                    return false;
                }
            }
            currentPos = getColumn(columnIndex++);
        } else {
            currentPos = currentPos.below();
        }
        try (Transaction tx = Transaction.openRoot()) {
            energy.internalExtract(RangedPumps.SERVER_CONFIG.getEnergyUsagePerMove(), tx);
            tx.commit();
        }
        setChanged();
        return true;
    }

    @Nullable
    private FluidResource drainFluidAt(BlockPos pos, boolean simulate) {
        if (level == null) {
            return null;
        }
        BlockState frontBlockState = level.getBlockState(pos);
        if (!(frontBlockState.getBlock() instanceof LiquidBlock) || frontBlockState.getValue(LiquidBlock.LEVEL) != 0) {
            return null;
        }
        final Fluid fluid = frontBlockState.getFluidState().getType();
        if (!simulate) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        }
        return FluidResource.of(fluid);
    }

    private void tryReplaceLiquidWithBlock() {
        if (!RangedPumps.SERVER_CONFIG.getReplaceLiquidWithBlock() || level == null || currentPos == null) {
            return;
        }
        if (blockToReplaceLiquidsWith == null) {
            blockToReplaceLiquidsWith = BuiltInRegistries.BLOCK.getOptional(
                Identifier.parse(RangedPumps.SERVER_CONFIG.getBlockIdToReplaceLiquidsWith())
            ).orElse(null);
        }
        if (blockToReplaceLiquidsWith == null) {
            return;
        }
        level.setBlockAndUpdate(currentPos, blockToReplaceLiquidsWith.defaultBlockState());
    }

    BlockPos getCurrentPosition() {
        return currentPos == null ? worldPosition.below() : currentPos;
    }

    int getRange() {
        return range;
    }

    PumpState getState() {
        if (range > RangedPumps.SERVER_CONFIG.getRange()) {
            return PumpState.DONE;
        } else if (level != null && level.hasNeighborSignal(worldPosition)) {
            return PumpState.REDSTONE;
        } else if (energy.getAmountAsLong() == 0) {
            return PumpState.ENERGY;
        } else if (tank.getAmountAsLong(0) > RangedPumps.SERVER_CONFIG.getTankCapacity() - FluidType.BUCKET_VOLUME) {
            return PumpState.FULL;
        } else {
            return PumpState.WORKING;
        }
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    public SimpleEnergyHandler getEnergy() {
        return energy;
    }

    public Component getMessage() {
        final FluidResource stored = tank.getResource(0);
        if (stored.isEmpty()) {
            return Component.translatable("block." + RangedPumps.ID + ".pump.state_empty",
                energy.getAmountAsInt(), energy.getCapacityAsInt());
        }
        return Component.translatable("block." + RangedPumps.ID + ".pump.state",
            tank.getAmountAsInt(0), stored.getHoverName(),
            energy.getAmountAsInt(), energy.getCapacityAsInt());
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output);
        tank.serialize(output);
        if (currentPos != null) {
            output.putLong("currentPos", currentPos.asLong());
        }
        output.putInt("range", range);
        output.putInt("columnIndex", columnIndex);
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        energy.deserialize(input);
        tank.deserialize(input);
        input.getLong("currentPos").ifPresent(posAsLong -> currentPos = BlockPos.of(posAsLong));
        range = input.getInt("range").orElse(-1);
        columnIndex = input.getInt("columnIndex").orElse(0);
    }

    private static class PumpTank extends FluidStacksResourceHandler {
        public PumpTank() {
            super(1, RangedPumps.SERVER_CONFIG.getTankCapacity());
        }

        @Override
        public int insert(final FluidResource resource, final int amount, final TransactionContext transaction) {
            return 0;
        }

        private int internalInsert(final FluidResource resource, final TransactionContext transaction) {
            return super.insert(resource, FluidType.BUCKET_VOLUME, transaction);
        }
    }

    private static class PumpEnergy extends SimpleEnergyHandler {
        public PumpEnergy(final int capacity) {
            super(capacity, capacity, capacity);
        }

        @Override
        public int extract(final int amount, final TransactionContext transaction) {
            // Prevent external handlers (e.g. cables) from draining the pump; consumption goes through internalExtract.
            return 0;
        }

        private void internalExtract(final int amount, final TransactionContext transaction) {
            super.extract(amount, transaction);
        }
    }
}
