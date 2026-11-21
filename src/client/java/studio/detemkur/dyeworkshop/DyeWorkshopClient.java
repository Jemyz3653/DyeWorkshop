package studio.detemkur.dyeworkshop;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public final class DyeWorkshopClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        MenuScreens.register(DyeWorkshop.MENU,SoakingScreen::new);
        BlockEntityRendererRegistry.register(DyeWorkshop.RACK_ENTITY,DryingRackRenderer::new);
    }
}
