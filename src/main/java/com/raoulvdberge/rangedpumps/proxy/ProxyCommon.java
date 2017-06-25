package com.raoulvdberge.rangedpumps.proxy;

import com.raoulvdberge.rangedpumps.RangedPumps;
import com.raoulvdberge.rangedpumps.item.ItemBlockPump;
import com.raoulvdberge.rangedpumps.tile.TilePump;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon {
    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);

        GameRegistry.registerTileEntity(TilePump.class, RangedPumps.ID + ":pump");
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> registry) {
        registry.getRegistry().register(RangedPumps.PUMP);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> registry) {
        registry.getRegistry().register(new ItemBlockPump());
    }

    public void init(FMLInitializationEvent e) {
        // NO OP
    }

    public void postInit(FMLPostInitializationEvent e) {
        // NO OP
    }
}
