package com.raoulvdberge.rangedpumps.block;

import com.raoulvdberge.rangedpumps.RangedPumps;
import com.raoulvdberge.rangedpumps.tile.PumpState;
import com.raoulvdberge.rangedpumps.tile.TilePump;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class BlockPump extends Block {
    public BlockPump() {
        super(Material.ROCK);

        setRegistryName(RangedPumps.ID, "pump");
        setHardness(3.8f);
        setCreativeTab(RangedPumps.TAB);
    }

    @Override
    public String getUnlocalizedName() {
        return "block." + RangedPumps.ID + ":pump";
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TilePump) {
                TilePump pump = (TilePump) tile;

                IEnergyStorage energy = pump.getCapability(CapabilityEnergy.ENERGY, null);

                ITextComponent message = PumpState.getMessage(pump);

                if (message != null) {
                    player.sendMessage(message);
                }

                if (pump.getTank().getFluidAmount() == 0) {
                    player.sendMessage(new TextComponentTranslation("block." + RangedPumps.ID + ":pump.state_empty", energy.getEnergyStored(), energy.getMaxEnergyStored()));
                } else {
                    String name = pump.getTank().getFluid().getUnlocalizedName();

                    ITextComponent nameComponent;

                    if (name.equals("fluid.tile.water")) {
                        nameComponent = new TextComponentString("water");
                    } else if (name.equals("fluid.tile.lava")) {
                        nameComponent = new TextComponentString("lava");
                    } else {
                        nameComponent = new TextComponentTranslation(name);
                    }

                    player.sendMessage(new TextComponentTranslation("block." + RangedPumps.ID + ":pump.state", pump.getTank().getFluidAmount(), nameComponent, energy.getEnergyStored(), energy.getMaxEnergyStored()));
                }
            }
        }

        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TilePump();
    }
}
