package com.raoulvdberge.rangedpumps.tile;

import com.raoulvdberge.rangedpumps.RangedPumps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PumpTile extends TileEntity implements ITickableTileEntity {
    @ObjectHolder(RangedPumps.ID + ":pump")
    public static final TileEntityType<PumpTile> TYPE = null;

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

    public PumpTile() {
        super(TYPE);
    }

    private void rebuildSurfaces() {
        surfaces.clear();

        if (range == -1) {
            surfaces.add(pos.down());

            return;
        }

        int hl = 3 + 2 * range;
        int vl = 1 + 2 * range;

        // Top
        for (int i = 0; i < hl; ++i) {
            surfaces.add(pos.add(-range - 1 + i, -1, -range - 1));
        }

        // Right
        for (int i = 0; i < vl; ++i) {
            surfaces.add(pos.add(-range - 1 + vl + 1, -1, -range - 1 + i + 1));
        }

        // Bottom
        for (int i = 0; i < hl; ++i) {
            surfaces.add(pos.add(-range - 1 + hl - i - 1, -1, -range - 1 + hl - 1));
        }

        // Left
        for (int i = 0; i < vl; ++i) {
            surfaces.add(pos.add(-range - 1, -1, -range - 1 + vl - i));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (surfaces.isEmpty()) {
            rebuildSurfaces();
        }
    }

    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }

        if (!RangedPumps.SERVER_CONFIG.getUseEnergy()) {
            energy.receiveEnergy(energy.getMaxEnergyStored(), false);
        }

        // Fill neighbors
        if (!tank.getFluid().isEmpty()) {
            List<IFluidHandler> fluidHandlers = new LinkedList<>();

            for (Direction facing : Direction.values()) {
                TileEntity tile = world.getTileEntity(pos.offset(facing));

                if (tile != null) {
                    IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).orElse(null);

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
            if (currentPos == null || currentPos.getY() == 0) {
                if (surfaces.isEmpty()) {
                    range++;

                    if (range > RangedPumps.SERVER_CONFIG.getRange()) {
                        return;
                    }

                    rebuildSurfaces();
                }

                currentPos = surfaces.poll();
            } else {
                currentPos = currentPos.down();
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
                            world.setBlockState(currentPos, blockToReplaceLiquidsWith.getDefaultState());
                        }
                    }

                    energy.extractEnergy(RangedPumps.SERVER_CONFIG.getEnergyUsagePerDrain(), false);
                }
            }

            markDirty();
        }

        ticks++;
    }

    @Nonnull
    private FluidStack drainAt(BlockPos pos, IFluidHandler.FluidAction action) {
        BlockState frontBlockState = world.getBlockState(pos);
        Block frontBlock = frontBlockState.getBlock();

        if (frontBlock instanceof FlowingFluidBlock) {
            // @Volatile: Logic from FlowingFluidBlock#pickupFluid
            if (frontBlockState.get(FlowingFluidBlock.LEVEL) == 0) {
                Fluid fluid = ((FlowingFluidBlock) frontBlock).getFluid();

                if (action == IFluidHandler.FluidAction.EXECUTE) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                }

                return new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
            }
        } else if (frontBlock instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) frontBlock;

            if (fluidBlock.canDrain(world, pos)) {
                return fluidBlock.drain(world, pos, action);
            }
        }

        return FluidStack.EMPTY;
    }

    BlockPos getCurrentPosition() {
        return currentPos == null ? pos.down() : currentPos;
    }

    int getRange() {
        return range;
    }

    PumpState getState() {
        if (range > RangedPumps.SERVER_CONFIG.getRange()) {
            return PumpState.DONE;
        } else if (world.isBlockPowered(pos)) {
            return PumpState.REDSTONE;
        } else if (energy.getEnergyStored() == 0) {
            return PumpState.ENERGY;
        } else if (tank.getFluidAmount() > tank.getCapacity() - FluidAttributes.BUCKET_VOLUME) {
            return PumpState.FULL;
        } else {
            return PumpState.WORKING;
        }
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.putInt("Energy", energy.getEnergyStored());

        if (currentPos != null) {
            tag.putLong("CurrentPos", currentPos.toLong());
        }

        tag.putInt("Range", range);

        ListNBT surfaces = new ListNBT();

        this.surfaces.forEach(s -> surfaces.add(LongNBT.valueOf(s.toLong())));

        tag.put("Surfaces", surfaces);

        tank.writeToNBT(tag);

        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        if (tag.contains("Energy")) {
            energy.receiveEnergy(tag.getInt("Energy"), false);
        }

        if (tag.contains("CurrentPos")) {
            currentPos = BlockPos.fromLong(tag.getLong("CurrentPos"));
        }

        if (tag.contains("Range")) {
            range = tag.getInt("Range");
        }

        if (tag.contains("Surfaces")) {
            ListNBT surfaces = tag.getList("Surfaces", Constants.NBT.TAG_LONG);

            for (INBT surface : surfaces) {
                this.surfaces.add(BlockPos.fromLong(((LongNBT) surface).getLong()));
            }
        }

        tank.readFromNBT(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyProxyCap.cast();
        }

        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandlerCap.cast();
        }

        return super.getCapability(cap, direction);
    }

    private class PumpTank extends FluidTank {
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
