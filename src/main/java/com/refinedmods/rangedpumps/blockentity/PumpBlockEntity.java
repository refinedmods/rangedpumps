package com.refinedmods.rangedpumps.blockentity;

import com.refinedmods.rangedpumps.RangedPumps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PumpBlockEntity extends BlockEntity {
    private PumpTank tank = new PumpTank();
    private IEnergyStorage energy = new EnergyStorage(RangedPumps.SERVER_CONFIG.getEnergyCapacity());

    private final LazyOptional<IEnergyStorage> energyProxyCap = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidHandlerCap = LazyOptional.of(() -> tank);

    private int ticks;

    @Nullable
    private BlockPos currentPos;
    private int range = -1;
    private Queue<BlockPos> surfaces = new LinkedList<>();
    private Block blockToReplaceLiquidsWith;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(RangedPumps.PUMP_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    private void rebuildSurfaces() {
        surfaces.clear();

        if (range == -1) {
            surfaces.add(worldPosition.below());

            return;
        }

        int hl = 3 + 2 * range;
        int vl = 1 + 2 * range;

        // Top
        for (int i = 0; i < hl; ++i) {
            surfaces.add(worldPosition.offset(-range - 1 + i, -1, -range - 1));
        }

        // Right
        for (int i = 0; i < vl; ++i) {
            surfaces.add(worldPosition.offset(-range - 1 + vl + 1, -1, -range - 1 + i + 1));
        }

        // Bottom
        for (int i = 0; i < hl; ++i) {
            surfaces.add(worldPosition.offset(-range - 1 + hl - i - 1, -1, -range - 1 + hl - 1));
        }

        // Left
        for (int i = 0; i < vl; ++i) {
            surfaces.add(worldPosition.offset(-range - 1, -1, -range - 1 + vl - i));
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (surfaces.isEmpty()) {
            rebuildSurfaces();
        }
    }

    public void tick() {
        if (!RangedPumps.SERVER_CONFIG.getUseEnergy()) {
            energy.receiveEnergy(energy.getMaxEnergyStored(), false);
        }

        // Fill neighbors
        if (!tank.getFluid().isEmpty()) {
            List<IFluidHandler> fluidHandlers = new LinkedList<>();

            for (Direction facing : Direction.values()) {
                BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(facing));

                if (blockEntity != null) {
                    IFluidHandler handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, facing.getOpposite()).orElse(null);

                    if (handler != null) {
                        fluidHandlers.add(handler);
                    }
                }
            }

            if (!fluidHandlers.isEmpty()) {
                int transfer = (int) Math.floor((float) tank.getFluidAmount() / (float) fluidHandlers.size());

                for (IFluidHandler fluidHandler : fluidHandlers) {
                    FluidStack toFill = tank.getFluid().copy();
                    toFill.setAmount(transfer);

                    tank.drain(fluidHandler.fill(toFill, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }

        if ((RangedPumps.SERVER_CONFIG.getSpeed() == 0 || (ticks % RangedPumps.SERVER_CONFIG.getSpeed() == 0)) && getState() == PumpState.WORKING) {
            if (currentPos == null || currentPos.getY() == level.dimensionType().minY()) {
                if (surfaces.isEmpty()) {
                    range++;

                    if (range > RangedPumps.SERVER_CONFIG.getRange()) {
                        return;
                    }

                    rebuildSurfaces();
                }

                currentPos = surfaces.poll();
            } else {
                currentPos = currentPos.below();
            }

            energy.extractEnergy(RangedPumps.SERVER_CONFIG.getEnergyUsagePerMove(), false);

            FluidStack drained = drainAt(currentPos, IFluidHandler.FluidAction.SIMULATE);

            if (!drained.isEmpty() && tank.fillInternal(drained, IFluidHandler.FluidAction.SIMULATE) == drained.getAmount()) {
                drained = drainAt(currentPos, IFluidHandler.FluidAction.EXECUTE);

                if (!drained.isEmpty()) {
                    tank.fillInternal(drained, IFluidHandler.FluidAction.EXECUTE);

                    if (RangedPumps.SERVER_CONFIG.getReplaceLiquidWithBlock()) {
                        if (blockToReplaceLiquidsWith == null) {
                            blockToReplaceLiquidsWith = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(RangedPumps.SERVER_CONFIG.getBlockIdToReplaceLiquidsWith()));
                        }

                        if (blockToReplaceLiquidsWith != null) {
                            level.setBlockAndUpdate(currentPos, blockToReplaceLiquidsWith.defaultBlockState());
                        }
                    }

                    energy.extractEnergy(RangedPumps.SERVER_CONFIG.getEnergyUsagePerDrain(), false);
                }
            }

            setChanged();
        }

        ticks++;
    }

    @Nonnull
    private FluidStack drainAt(BlockPos pos, IFluidHandler.FluidAction action) {
        BlockState frontBlockState = level.getBlockState(pos);
        Block frontBlock = frontBlockState.getBlock();

        if (frontBlock instanceof LiquidBlock) {
            // @Volatile: Logic from LiquidBlock#pickupFluid
            if (frontBlockState.getValue(LiquidBlock.LEVEL) == 0) {
                Fluid fluid = ((LiquidBlock) frontBlock).getFluid();

                if (action == IFluidHandler.FluidAction.EXECUTE) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                }

                return new FluidStack(fluid, FluidType.BUCKET_VOLUME);
            }
        } else if (frontBlock instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) frontBlock;

            if (fluidBlock.canDrain(level, pos)) {
                return fluidBlock.drain(level, pos, action);
            }
        }

        return FluidStack.EMPTY;
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
        } else if (level.hasNeighborSignal(worldPosition)) {
            return PumpState.REDSTONE;
        } else if (energy.getEnergyStored() == 0) {
            return PumpState.ENERGY;
        } else if (tank.getFluidAmount() > tank.getCapacity() - FluidType.BUCKET_VOLUME) {
            return PumpState.FULL;
        } else {
            return PumpState.WORKING;
        }
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("Energy", energy.getEnergyStored());

        if (currentPos != null) {
            tag.putLong("CurrentPos", currentPos.asLong());
        }

        tag.putInt("Range", range);

        ListTag surfaces = new ListTag();

        this.surfaces.forEach(s -> surfaces.add(LongTag.valueOf(s.asLong())));

        tag.put("Surfaces", surfaces);

        tank.writeToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Energy")) {
            energy.receiveEnergy(tag.getInt("Energy"), false);
        }

        if (tag.contains("CurrentPos")) {
            currentPos = BlockPos.of(tag.getLong("CurrentPos"));
        }

        if (tag.contains("Range")) {
            range = tag.getInt("Range");
        }

        if (tag.contains("Surfaces")) {
            ListTag surfaces = tag.getList("Surfaces", Tag.TAG_LONG);

            for (Tag surface : surfaces) {
                this.surfaces.add(BlockPos.of(((LongTag) surface).getAsLong()));
            }
        }

        tank.readFromNBT(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyProxyCap.cast();
        }

        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandlerCap.cast();
        }

        return super.getCapability(cap, direction);
    }

    private static class PumpTank extends FluidTank {
        public PumpTank() {
            super(RangedPumps.SERVER_CONFIG.getTankCapacity());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        public int fillInternal(FluidStack resource, FluidAction action) {
            return super.fill(resource, action);
        }
    }
}
