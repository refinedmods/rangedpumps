package com.raoulvdberge.rangedpumps.tile;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum PumpState {
    ENERGY,
    REDSTONE,
    WORKING,
    FULL,
    DONE;

    public static ITextComponent getMessage(TilePump pump) {
        switch (pump.getState()) {
            case ENERGY:
                return new TextComponentTranslation("block.rangedpumps:pump.state.energy");
            case REDSTONE:
                return new TextComponentTranslation("block.rangedpumps:pump.state.redstone");
            case WORKING:
                return new TextComponentTranslation("block.rangedpumps:pump.state.working", pump.getCurrentPosition().getX(), pump.getCurrentPosition().getY(), pump.getCurrentPosition().getZ(), pump.getRange());
            case FULL:
                return new TextComponentTranslation("block.rangedpumps:pump.state.full");
            case DONE:
                return new TextComponentTranslation("block.rangedpumps:pump.state.done");
            default:
                return null;
        }
    }
}
