package com.refinedmods.rangedpumps;

import com.refinedmods.rangedpumps.config.ServerConfig;
import com.refinedmods.rangedpumps.item.group.MainItemGroup;
import com.refinedmods.rangedpumps.setup.CommonSetup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RangedPumps.ID)
public final class RangedPumps {
    public static final String ID = "rangedpumps";
    public static final ItemGroup MAIN_GROUP = new MainItemGroup();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public RangedPumps() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());

        CommonSetup commonSetup = new CommonSetup();

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, commonSetup::onRegisterBlocks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(TileEntityType.class, commonSetup::onRegisterTiles);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, commonSetup::onRegisterItems);
    }
}
