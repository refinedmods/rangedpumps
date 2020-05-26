package com.refinedmods.rangedpumps.item.group;

import com.refinedmods.rangedpumps.RangedPumps;
import com.refinedmods.rangedpumps.block.PumpBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class MainItemGroup extends ItemGroup {
    public MainItemGroup() {
        super(RangedPumps.ID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(PumpBlock.BLOCK);
    }
}
