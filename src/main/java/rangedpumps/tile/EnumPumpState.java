package rangedpumps.tile;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum EnumPumpState {
    UNKNOWN,
    WORKING,
    FULL,
    DONE;

    public static ITextComponent getMessage(TilePump pump) {
        switch (pump.getState()) {
            case WORKING:
                return new TextComponentTranslation("block.rangedpumps:pump.state.working", pump.getCurrentPosition().getX(), pump.getCurrentPosition().getY(), pump.getCurrentPosition().getZ());
            case FULL:
                return new TextComponentTranslation("block.rangedpumps:pump.state.full");
            case DONE:
                return new TextComponentTranslation("block.rangedpumps:pump.state.done");
            default:
                return new TextComponentTranslation("block.rangedpumps:pump.state.unknown");
        }
    }
}
