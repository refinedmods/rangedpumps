package com.refinedmods.rangedpumps.blockentity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum PumpState {
    ENERGY,
    REDSTONE,
    WORKING,
    FULL,
    DONE;

    public static Component getMessage(PumpBlockEntity pump) {
        switch (pump.getState()) {
            case ENERGY:
                return new TranslatableComponent("block.rangedpumps.pump.state.energy");
            case REDSTONE:
                return new TranslatableComponent("block.rangedpumps.pump.state.redstone");
            case WORKING:
                return new TranslatableComponent("block.rangedpumps.pump.state.working", pump.getCurrentPosition().getX(), pump.getCurrentPosition().getY(), pump.getCurrentPosition().getZ(), pump.getRange());
            case FULL:
                return new TranslatableComponent("block.rangedpumps.pump.state.full");
            case DONE:
                return new TranslatableComponent("block.rangedpumps.pump.state.done");
            default:
                return null;
        }
    }
}
