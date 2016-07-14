package rangedpumps.tile;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum EnumPumpState {
    UNKNOWN,
    UNPOWERED,
    WORKING,
    FULL,
    DONE;

    public static ITextComponent getMessage(TilePump pump) {
        switch (pump.getState()) {
            case UNPOWERED:
                return new TextComponentTranslation("block.rangedpumps:pump.state.unpowered");
            case WORKING:
                return new TextComponentTranslation("block.rangedpumps:pump.state.working", pump.getCurrentPosition().getX(), pump.getCurrentPosition().getY(), pump.getCurrentPosition().getZ());
            case FULL:
                return new TextComponentTranslation("block.rangedpumps:pump.state.full");
            case DONE:
                return new TextComponentTranslation("block.rangedpumps:pump.state.done");
            default:
                return null;
        }
    }
}
