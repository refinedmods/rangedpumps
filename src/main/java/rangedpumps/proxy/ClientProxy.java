package rangedpumps.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import rangedpumps.RangedPumps;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RangedPumps.PUMP), 0, new ModelResourceLocation(RangedPumps.ID + ":pump", "inventory"));
    }
}
