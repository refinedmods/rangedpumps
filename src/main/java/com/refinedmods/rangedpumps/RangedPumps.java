package com.refinedmods.rangedpumps;

import com.refinedmods.rangedpumps.block.PumpBlock;
import com.refinedmods.rangedpumps.blockentity.PumpBlockEntity;
import com.refinedmods.rangedpumps.config.ServerConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(RangedPumps.ID)
public final class RangedPumps {
    public static final String ID = "rangedpumps";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ID);

    public static DeferredHolder<Block, PumpBlock> PUMP_BLOCK = BLOCKS.register("pump", PumpBlock::new);
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<PumpBlockEntity>> PUMP_BLOCK_ENTITY_TYPE =
        BLOCK_ENTITY_TYPES
            .register("pump", () -> BlockEntityType.Builder.of(PumpBlockEntity::new, PUMP_BLOCK.get()).build(null));

    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public RangedPumps(final IEventBus eventBus) {
        ITEMS.register("pump", () -> new BlockItem(PUMP_BLOCK.get(), new Item.Properties()));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        eventBus.addListener(RangedPumps::onRegister);
        eventBus.addListener(RangedPumps::onRegisterCapabilities);
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            PUMP_BLOCK_ENTITY_TYPE.get(),
            (be, ctx) -> be.getEnergy()
        );
        e.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            PUMP_BLOCK_ENTITY_TYPE.get(),
            (be, ctx) -> be.getTank()
        );
    }

    public static void onRegister(RegisterEvent e) {
        e.register(Registries.CREATIVE_MODE_TAB, helper -> {
            helper.register(new ResourceLocation(RangedPumps.ID, "general"), CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.rangedpumps"))
                .icon(() -> new ItemStack(RangedPumps.PUMP_BLOCK.get()))
                .displayItems((params, output) -> {
                    output.accept(RangedPumps.PUMP_BLOCK.get());
                })
                .build());
        });
    }
}
