package com.refinedmods.rangedpumps;

import com.refinedmods.rangedpumps.config.ServerConfig;
import com.refinedmods.rangedpumps.item.group.MainCreativeModeTab;
import com.refinedmods.rangedpumps.setup.CommonSetup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RangedPumps.ID)
public final class RangedPumps {
    public static final String ID = "rangedpumps";
    public static final CreativeModeTab MAIN_TAB = new MainCreativeModeTab();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public RangedPumps() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, CommonSetup::onRegisterBlocks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(BlockEntityType.class, CommonSetup::onRegisterBlockEntities);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, CommonSetup::onRegisterItems);
    }
}
