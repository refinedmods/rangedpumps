package rangedpumps.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import rangedpumps.RangedPumps;

public class TilePump extends TileEntity implements ITickable {
    private FluidTank tank;

    private int ticks;

    private BlockPos currentPos;
    private BlockPos startPos;

    public TilePump() {
        tank = new FluidTank(RangedPumps.INSTANCE.capacity) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();

                markDirty();
            }
        };

        tank.setCanFill(false);
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            return;
        }

        if (startPos == null) {
            startPos = pos.add(-RangedPumps.INSTANCE.range / 2, -1, -RangedPumps.INSTANCE.range / 2);
        }

        boolean firstUpdate = false;

        if (currentPos == null) {
            currentPos = new BlockPos(startPos);

            firstUpdate = true;
        }

        if ((RangedPumps.INSTANCE.speed == 0 || (ticks % RangedPumps.INSTANCE.speed == 0)) && getState() == EnumPumpState.WORKING) {
            if (!firstUpdate) {
                if (currentPos.getY() - 1 < 1) {
                    currentPos = new BlockPos(currentPos.getX() + 1, startPos.getY(), currentPos.getZ());
                } else {
                    currentPos = currentPos.add(0, -1, 0);
                }
            }

            if (currentPos.getX() >= startPos.getX() + RangedPumps.INSTANCE.range) {
                currentPos = new BlockPos(startPos.getX(), startPos.getY(), currentPos.getZ() + 1);
            }

            markDirty();

            if (!isOverLastRow()) {
                Block block = worldObj.getBlockState(currentPos).getBlock();

                IFluidHandler handler = null;

                if (block instanceof BlockLiquid) {
                    handler = new BlockLiquidWrapper((BlockLiquid) block, worldObj, currentPos);
                } else if (block instanceof IFluidBlock) {
                    handler = new FluidBlockWrapper((IFluidBlock) block, worldObj, currentPos);
                }

                if (handler != null) {
                    FluidStack drained = handler.drain(RangedPumps.INSTANCE.capacity, false);

                    if (drained != null && tank.fillInternal(drained, false) == drained.amount) {
                        tank.fillInternal(handler.drain(RangedPumps.INSTANCE.capacity, true), true);

                        if (RangedPumps.INSTANCE.replaceLiquidWithStone) {
                            worldObj.setBlockState(currentPos, Blocks.STONE.getDefaultState());
                        }
                    }
                }
            }
        }

        ticks++;
    }

    private boolean isOverLastRow() {
        return currentPos.getZ() == startPos.getZ() + RangedPumps.INSTANCE.range + 1;
    }

    public BlockPos getCurrentPosition() {
        return currentPos;
    }

    public EnumPumpState getState() {
        if (currentPos != null && startPos != null) {
            if (isOverLastRow()) {
                return EnumPumpState.DONE;
            } else if (!worldObj.isBlockPowered(pos)) {
                return EnumPumpState.UNPOWERED;
            } else if (tank.getFluidAmount() > tank.getCapacity() - Fluid.BUCKET_VOLUME) {
                return EnumPumpState.FULL;
            } else {
                return EnumPumpState.WORKING;
            }
        }

        return EnumPumpState.UNKNOWN;
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setLong("CurrentPos", currentPos.toLong());

        tank.writeToNBT(tag);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if (tag.hasKey("CurrentPos")) {
            currentPos = BlockPos.fromLong(tag.getLong("CurrentPos"));
        }

        tank.readFromNBT(tag);
    }


    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }

        return super.getCapability(capability, facing);
    }
}
