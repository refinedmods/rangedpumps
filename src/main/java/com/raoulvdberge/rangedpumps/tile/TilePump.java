package com.raoulvdberge.rangedpumps.tile;

import com.raoulvdberge.rangedpumps.RangedPumps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Queue;

public class TilePump extends TileEntity implements ITickable {
    private FluidTank tank;
    private IEnergyStorage energy = new EnergyStorage(RangedPumps.INSTANCE.energyCapacity);

    private int ticks;

    @Nullable
    private BlockPos currentPos;
    private int range = -1;
    private Queue<BlockPos> surfaces = new LinkedList<>();

    public TilePump() {
        this.tank = new FluidTank(RangedPumps.INSTANCE.tankCapacity) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();

                markDirty();
            }
        };

        this.tank.setCanFill(false);
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
    public void update() {
        if (world.isRemote) {
            return;
        }

        if (!RangedPumps.INSTANCE.usesEnergy) {
            energy.receiveEnergy(energy.getMaxEnergyStored(), false);
        }

        if ((RangedPumps.INSTANCE.speed == 0 || (ticks % RangedPumps.INSTANCE.speed == 0)) && getState() == PumpState.WORKING) {
            if (currentPos == null || currentPos.getY() == 0) {
                if (surfaces.isEmpty()) {
                    range++;

                    if (range > RangedPumps.INSTANCE.range) {
                        return;
                    }

                    rebuildSurfaces();
                }

                currentPos = surfaces.poll();
            } else {
                currentPos = currentPos.down();
            }

            energy.extractEnergy(RangedPumps.INSTANCE.energyUsagePerMove, false);

            Block block = getWorld().getBlockState(currentPos).getBlock();

            IFluidHandler handler = null;

            if (block instanceof BlockLiquid) {
                handler = new BlockLiquidWrapper((BlockLiquid) block, getWorld(), currentPos);
            } else if (block instanceof IFluidBlock) {
                handler = new FluidBlockWrapper((IFluidBlock) block, getWorld(), currentPos);
            }

            if (handler != null) {
                FluidStack drained = handler.drain(RangedPumps.INSTANCE.tankCapacity, false);

                if (drained != null && tank.fillInternal(drained, false) == drained.amount) {
                    tank.fillInternal(handler.drain(RangedPumps.INSTANCE.tankCapacity, true), true);

                    if (RangedPumps.INSTANCE.replaceLiquidWithStone) {
                        world.setBlockState(currentPos, Blocks.STONE.getDefaultState());
                    }

                    energy.extractEnergy(RangedPumps.INSTANCE.energyUsagePerDrain, false);
                }
            }

            markDirty();
        }

        ticks++;
    }

    BlockPos getCurrentPosition() {
        return currentPos == null ? pos.down() : currentPos;
    }

    int getRange() {
        return range;
    }

    PumpState getState() {
        if (energy.getEnergyStored() == 0) {
            return PumpState.ENERGY;
        } else if (world.isBlockPowered(pos)) {
            return PumpState.REDSTONE;
        } else if (tank.getFluidAmount() > tank.getCapacity() - Fluid.BUCKET_VOLUME) {
            return PumpState.FULL;
        } else if (range > RangedPumps.INSTANCE.range) {
            return PumpState.DONE;
        } else {
            return PumpState.WORKING;
        }
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setInteger("Energy", energy.getEnergyStored());

        if (currentPos != null) {
            tag.setLong("CurrentPos", currentPos.toLong());
        }

        tag.setInteger("Range", range);

        NBTTagList surfaces = new NBTTagList();

        this.surfaces.forEach(s -> surfaces.appendTag(new NBTTagLong(s.toLong())));

        tag.setTag("Surfaces", surfaces);

        tank.writeToNBT(tag);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if (tag.hasKey("Energy")) {
            energy.receiveEnergy(tag.getInteger("Energy"), false);
        }

        if (tag.hasKey("CurrentPos")) {
            currentPos = BlockPos.fromLong(tag.getLong("CurrentPos"));
        }

        if (tag.hasKey("Range")) {
            range = tag.getInteger("Range");
        }

        if (tag.hasKey("Surfaces")) {
            NBTTagList surfaces = tag.getTagList("Surfaces", Constants.NBT.TAG_LONG);

            for (NBTBase surface : surfaces) {
                this.surfaces.add(BlockPos.fromLong(((NBTTagLong) surface).getLong()));
            }
        }

        tank.readFromNBT(tag);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energy);
        }

        return super.getCapability(capability, facing);
    }
}
