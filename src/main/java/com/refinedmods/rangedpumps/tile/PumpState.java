package com.refinedmods.rangedpumps.tile;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum PumpState {
    ENERGY,
    REDSTONE,
    WORKING,
    FULL,
    DONE;

    public static ITextComponent getMessage(PumpTile pump) {
        switch (pump.getState()) {
            case ENERGY:
                return new TranslationTextComponent("block.rangedpumps.pump.state.energy");
            case REDSTONE:
                return new TranslationTextComponent("block.rangedpumps.pump.state.redstone");
            case WORKING:
                return new TranslationTextComponent("block.rangedpumps.pump.state.working", pump.getCurrentPosition().getX(), pump.getCurrentPosition().getY(), pump.getCurrentPosition().getZ(), pump.getRange());
            case FULL:
                return new TranslationTextComponent("block.rangedpumps.pump.state.full");
            case DONE:
                return new TranslationTextComponent("block.rangedpumps.pump.state.done");
            default:
                return null;
        }
    }
}
