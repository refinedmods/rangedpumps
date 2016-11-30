package com.raoulvdberge.rangedpumps.proxy;

import com.raoulvdberge.rangedpumps.RangedPumps;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ProxyClient extends ProxyCommon {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RangedPumps.PUMP), 0, new ModelResourceLocation(RangedPumps.ID + ":pump", "inventory"));
    }
}
