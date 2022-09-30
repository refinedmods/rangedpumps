package com.refinedmods.rangedpumps.item.group;

import com.refinedmods.rangedpumps.RangedPumps;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class MainCreativeModeTab extends CreativeModeTab {
    public MainCreativeModeTab() {
        super(RangedPumps.ID);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(RangedPumps.PUMP_BLOCK.get());
    }
}
