package rangedpumps.proxy;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import rangedpumps.RangedPumps;
import rangedpumps.item.ItemBlockPump;
import rangedpumps.tile.TilePump;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        GameRegistry.registerTileEntity(TilePump.class, RangedPumps.ID + ":pump");

        GameRegistry.register(RangedPumps.PUMP);
        GameRegistry.register(new ItemBlockPump());

        GameRegistry.addRecipe(new ItemStack(RangedPumps.PUMP),
            "opo",
            "ldw",
            "opo",
            'o', new ItemStack(Blocks.OBSIDIAN),
            'p', new ItemStack(Items.IRON_PICKAXE),
            'l', new ItemStack(Items.LAVA_BUCKET),
            'w', new ItemStack(Items.WATER_BUCKET),
            'd', new ItemStack(Blocks.DIAMOND_BLOCK)
        );
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
    }
}
