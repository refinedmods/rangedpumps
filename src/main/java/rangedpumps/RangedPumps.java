package rangedpumps;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import rangedpumps.block.BlockPump;
import rangedpumps.proxy.CommonProxy;

@Mod(modid = RangedPumps.ID, version = RangedPumps.VERSION)
public final class RangedPumps {
    public static final String ID = "rangedpumps";
    public static final String VERSION = "0.3";

    @SidedProxy(clientSide = "rangedpumps.proxy.ClientProxy", serverSide = "rangedpumps.proxy.ServerProxy")
    public static CommonProxy PROXY;

    @Mod.Instance
    public static RangedPumps INSTANCE;

    public static final CreativeTabs TAB = new CreativeTabs(ID) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(PUMP);
        }
    };

    public static final BlockPump PUMP = new BlockPump();

    public int range;
    public int speed;
    public int capacity;
    public boolean replaceLiquidWithStone;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        PROXY.preInit(e);

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());

        range = config.getInt("range", "pump", 128, 0, 1024, "The range of the pump");
        speed = config.getInt("speed", "pump", 8, 0, 1024, "The interval in ticks for when to move on to the next block (higher is slower)");
        capacity = config.getInt("capacity", "pump", Fluid.BUCKET_VOLUME * 32, Fluid.BUCKET_VOLUME, Integer.MAX_VALUE, "The capacity of the internal pump tank");
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
