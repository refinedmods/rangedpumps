package com.raoulvdberge.rangedpumps.block;

import com.raoulvdberge.rangedpumps.RangedPumps;
import com.raoulvdberge.rangedpumps.tile.PumpState;
import com.raoulvdberge.rangedpumps.tile.PumpTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class PumpBlock extends Block {
    @ObjectHolder(RangedPumps.ID + ":pump")
    public static final PumpBlock BLOCK = null;

    public PumpBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.9F).sound(SoundType.STONE));

        setRegistryName(RangedPumps.ID, "pump");
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PumpTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof PumpTile) {
                PumpTile pump = (PumpTile) tile;

                IEnergyStorage energy = pump.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                if (energy == null) {
                    return ActionResultType.SUCCESS;
                }

                ITextComponent message = PumpState.getMessage(pump);

                if (message != null) {
                    player.sendMessage(message);
                }

                if (pump.getTank().getFluidAmount() == 0) {
                    player.sendMessage(new TranslationTextComponent("block." + RangedPumps.ID + ".pump.state_empty", energy.getEnergyStored(), energy.getMaxEnergyStored()));
                } else {
                    player.sendMessage(new TranslationTextComponent("block." + RangedPumps.ID + ".pump.state", pump.getTank().getFluidAmount(), pump.getTank().getFluid().getDisplayName(), energy.getEnergyStored(), energy.getMaxEnergyStored()));
                }
            }
        }

        return ActionResultType.SUCCESS;
    }
}
