package com.raoulvdberge.rangedpumps.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fluids.FluidAttributes;

public class ServerConfig {
    private ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    private ForgeConfigSpec spec;

    private ForgeConfigSpec.IntValue range;
    private ForgeConfigSpec.IntValue speed;
    private ForgeConfigSpec.IntValue tankCapacity;
    private ForgeConfigSpec.IntValue energyCapacity;
    private ForgeConfigSpec.IntValue energyUsagePerMove;
    private ForgeConfigSpec.IntValue energyUsagePerDrain;
    private ForgeConfigSpec.BooleanValue useEnergy;
    private ForgeConfigSpec.BooleanValue replaceLiquidWithStone;

    public ServerConfig() {
        builder.push("pump");

        range = builder.comment("The range of the pump").defineInRange("range", 64, 0, 1024);
        speed = builder.comment("The interval in ticks for when to move on to the next block (higher is slower)").defineInRange("speed", 8, 0, 1024);
        tankCapacity = builder.comment("The capacity of the internal pump tank").defineInRange("tankCapacity", FluidAttributes.BUCKET_VOLUME * 32, FluidAttributes.BUCKET_VOLUME, Integer.MAX_VALUE);
        energyCapacity = builder.comment("The capacity of the energy storage").defineInRange("energyCapacity", 32000, 0, Integer.MAX_VALUE);
        energyUsagePerMove = builder.comment("Energy drained when moving to the next block").defineInRange("energyUsagePerMove", 0, 0, Integer.MAX_VALUE);
        energyUsagePerDrain = builder.comment("Energy drained when draining liquid").defineInRange("energyUsagePerDrain", 100, 0, Integer.MAX_VALUE);
        useEnergy = builder.comment("Whether the pump uses energy to work").define("useEnergy", true);
        replaceLiquidWithStone = builder.comment("Replaces the liquid that is removed with stone to reduce lag").define("replaceLiquidWithStone", true);

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

    public boolean getReplaceLiquidWithStone() {
        return replaceLiquidWithStone.get();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }
}
