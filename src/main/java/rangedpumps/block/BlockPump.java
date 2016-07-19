package rangedpumps.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import rangedpumps.RangedPumps;
import rangedpumps.tile.EnumPumpState;
import rangedpumps.tile.TilePump;

import javax.annotation.Nullable;

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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);

            if (tile instanceof TilePump) {
                TilePump pump = (TilePump) tile;

                ITextComponent message = EnumPumpState.getMessage(pump);

                if (message != null) {
                    player.addChatComponentMessage(message);
                }

                if (pump.getTank().getFluidAmount() == 0) {
                    player.addChatComponentMessage(new TextComponentTranslation("block." + RangedPumps.ID + ":pump.state_empty", pump.getEnergy().getEnergyStored(), pump.getEnergy().getMaxEnergyStored()));
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

                    player.addChatComponentMessage(new TextComponentTranslation("block." + RangedPumps.ID + ":pump.state", pump.getTank().getFluidAmount(), nameComponent, pump.getEnergy().getEnergyStored(), pump.getEnergy().getMaxEnergyStored()));
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
