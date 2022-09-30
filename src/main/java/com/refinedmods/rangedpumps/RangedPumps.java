package com.refinedmods.rangedpumps;

import com.refinedmods.rangedpumps.block.PumpBlock;
import com.refinedmods.rangedpumps.blockentity.PumpBlockEntity;
import com.refinedmods.rangedpumps.config.ServerConfig;
import com.refinedmods.rangedpumps.item.group.MainCreativeModeTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RangedPumps.ID)
public final class RangedPumps {
    public static final String ID = "rangedpumps";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);

    public static RegistryObject<PumpBlock> PUMP_BLOCK;
    public static RegistryObject<BlockEntityType<PumpBlockEntity>> PUMP_BLOCK_ENTITY_TYPE;

    public static final CreativeModeTab CREATIVE_MODE_TAB = new MainCreativeModeTab();
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public RangedPumps() {
        PUMP_BLOCK = BLOCKS.register("pump", PumpBlock::new);
        ITEMS.register("pump", () -> new BlockItem(PUMP_BLOCK.get(), new Item.Properties().tab(RangedPumps.CREATIVE_MODE_TAB)));
        PUMP_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("pump", () -> BlockEntityType.Builder.of(PumpBlockEntity::new, PUMP_BLOCK.get()).build(null));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
