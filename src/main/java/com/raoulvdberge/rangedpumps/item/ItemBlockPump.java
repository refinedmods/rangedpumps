package com.raoulvdberge.rangedpumps.item;

import com.raoulvdberge.rangedpumps.RangedPumps;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPump extends ItemBlock {
    public ItemBlockPump() {
        super(RangedPumps.PUMP);

        setRegistryName(RangedPumps.PUMP.getRegistryName());
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return RangedPumps.PUMP.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName() {
        return RangedPumps.PUMP.getUnlocalizedName();
    }
}
