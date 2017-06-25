package com.raoulvdberge.rangedpumps;

import com.raoulvdberge.rangedpumps.block.BlockPump;
import com.raoulvdberge.rangedpumps.proxy.ProxyCommon;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = RangedPumps.ID, version = RangedPumps.VERSION)
public final class RangedPumps {
    public static final String ID = "rangedpumps";
    public static final String VERSION = "0.5";

    @SidedProxy(clientSide = "com.raoulvdberge.rangedpumps.proxy.ProxyClient", serverSide = "com.raoulvdberge.rangedpumps.proxy.ProxyCommon")
    public static ProxyCommon PROXY;

    @Mod.Instance
    public static RangedPumps INSTANCE;

    public static final CreativeTabs TAB = new CreativeTabs(ID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(PUMP);
        }
    };

    public static final BlockPump PUMP = new BlockPump();

    public int range;
    public int speed;
    public int tankCapacity;
    public int energyCapacity;
    public int energyUsagePerMove;
    public int energyUsagePerDrain;
    public boolean usesEnergy;
    public boolean replaceLiquidWithStone;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        PROXY.preInit(e);

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());

        range = config.getInt("range", "pump", 64, 0, 1024, "The range of the pump");
        speed = config.getInt("speed", "pump", 4, 0, 1024, "The interval in ticks for when to move on to the next block (higher is slower)");
        tankCapacity = config.getInt("tankCapacity", "pump", Fluid.BUCKET_VOLUME * 32, Fluid.BUCKET_VOLUME, Integer.MAX_VALUE, "The capacity of the internal pump tank");
        energyCapacity = config.getInt("energyCapacity", "pump", 32000, 0, Integer.MAX_VALUE, "The capacity of the energy storage");
        energyUsagePerMove = config.getInt("energyUsagePerMove", "pump", 0, 0, Integer.MAX_VALUE, "Energy drained when moving to the next block");
        energyUsagePerDrain = config.getInt("energyUsagePerDrain", "pump", 100, 0, Integer.MAX_VALUE, "Energy drained when draining liquid");
        usesEnergy = config.getBoolean("usesEnergy", "pump", true, "Whether the pump uses energy to work");
        replaceLiquidWithStone = config.getBoolean("replaceLiquidWithStone", "pump", true, "Replaces the liquid that is removed with stone to reduce lag");

        config.save();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        PROXY.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        PROXY.postInit(e);
    }
}
