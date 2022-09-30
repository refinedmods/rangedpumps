package com.refinedmods.rangedpumps.blockentity;

import com.refinedmods.rangedpumps.RangedPumps;
import net.minecraft.network.chat.Component;

public enum PumpState {
    ENERGY,
    REDSTONE,
    WORKING,
    FULL,
    DONE;

    public static Component getMessage(PumpBlockEntity pump) {
        return switch (pump.getState()) {
            case ENERGY -> Component.translatable("block.rangedpumps.pump.state.energy");
            case REDSTONE -> Component.translatable("block.rangedpumps.pump.state.redstone");
            case WORKING ->
                Component.translatable("block.rangedpumps.pump.state.working", pump.getCurrentPosition().getX(), pump.getCurrentPosition().getY(), pump.getCurrentPosition().getZ(), pump.getRange(), RangedPumps.SERVER_CONFIG.getRange());
            case FULL -> Component.translatable("block.rangedpumps.pump.state.full");
            case DONE -> Component.translatable("block.rangedpumps.pump.state.done");
        };
    }
}
