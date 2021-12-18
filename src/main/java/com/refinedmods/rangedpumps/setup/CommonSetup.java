package com.refinedmods.rangedpumps.setup;

import com.refinedmods.rangedpumps.RangedPumps;
import com.refinedmods.rangedpumps.block.PumpBlock;
import com.refinedmods.rangedpumps.blockentity.PumpBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CommonSetup {
    private CommonSetup() {
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        e.getRegistry().register(new PumpBlock());
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(new BlockItem(PumpBlock.BLOCK, new Item.Properties().tab(RangedPumps.CREATIVE_MODE_TAB)).setRegistryName(RangedPumps.ID, "pump"));
    }

    @SubscribeEvent
    public static void onRegisterBlockEntities(RegistryEvent.Register<BlockEntityType<?>> e) {
        e.getRegistry().register(BlockEntityType.Builder.of(PumpBlockEntity::new, PumpBlock.BLOCK).build(null).setRegistryName(RangedPumps.ID, "pump"));
    }
}
