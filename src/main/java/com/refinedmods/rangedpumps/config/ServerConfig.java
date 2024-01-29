package com.refinedmods.rangedpumps.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.fluids.FluidType;

public class ServerConfig {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final ModConfigSpec.IntValue range;
    private final ModConfigSpec.IntValue speed;
    private final ModConfigSpec.IntValue tankCapacity;
    private final ModConfigSpec.IntValue energyCapacity;
    private final ModConfigSpec.IntValue energyUsagePerMove;
    private final ModConfigSpec.IntValue energyUsagePerDrain;
    private final ModConfigSpec.BooleanValue useEnergy;
    private final ModConfigSpec.BooleanValue replaceLiquidWithBlock;
    private final ModConfigSpec.ConfigValue<String> blockIdToReplaceLiquidsWith;

    public ServerConfig() {
        builder.push("pump");

        range = builder.comment("The range of the pump").defineInRange("range", 64, 0, 1024);
        speed = builder.comment("The interval in ticks for when to move on to the next block (higher is slower)")
            .defineInRange("speed", 8, 0, 1024);
        tankCapacity = builder.comment("The capacity of the internal pump tank")
            .defineInRange("tankCapacity", FluidType.BUCKET_VOLUME * 32, FluidType.BUCKET_VOLUME, Integer.MAX_VALUE);
        energyCapacity = builder.comment("The capacity of the energy storage")
            .defineInRange("energyCapacity", 32000, 0, Integer.MAX_VALUE);
        energyUsagePerMove = builder.comment("Energy drained when moving to the next block")
            .defineInRange("energyUsagePerMove", 0, 0, Integer.MAX_VALUE);
        energyUsagePerDrain = builder.comment("Energy drained when draining liquid")
            .defineInRange("energyUsagePerDrain", 100, 0, Integer.MAX_VALUE);
        useEnergy = builder.comment("Whether the pump uses energy to work").define("useEnergy", true);
        replaceLiquidWithBlock = builder.comment(
                "Replaces liquids that are removed with a block defined in 'blockIdToReplaceLiquidsWith' (to reduce lag)")
            .define("replaceLiquidWithBlock", true);
        blockIdToReplaceLiquidsWith =
            builder.comment("The block that liquids are replaced with when 'replaceLiquidWithBlock' is true")
                .define("blockIdToReplaceLiquidsWith", "minecraft:stone");

        builder.pop();

        spec = builder.build();
    }

    public int getRange() {
        return range.get();
    }

    public int getSpeed() {
        return speed.get();
    }

    public int getTankCapacity() {
        return tankCapacity.get();
    }

    public int getEnergyCapacity() {
        return energyCapacity.get();
    }

    public int getEnergyUsagePerMove() {
        return energyUsagePerMove.get();
    }

    public int getEnergyUsagePerDrain() {
        return energyUsagePerDrain.get();
    }

    public boolean getUseEnergy() {
        return useEnergy.get();
    }

    public boolean getReplaceLiquidWithBlock() {
        return replaceLiquidWithBlock.get();
    }

    public String getBlockIdToReplaceLiquidsWith() {
        return blockIdToReplaceLiquidsWith.get();
    }

    public ModConfigSpec getSpec() {
        return spec;
    }
}
