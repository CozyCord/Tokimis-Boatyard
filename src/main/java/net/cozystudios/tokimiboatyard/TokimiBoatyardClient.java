package net.cozystudios.tokimiboatyard;

import net.cozystudios.tokimiboatyard.entity.client.ModModelLayers;
import net.cozystudios.tokimiboatyard.entity.client.UpgradedBoatBandModel;
import net.cozystudios.tokimiboatyard.entity.client.UpgradedBoatPartRenderer;
import net.cozystudios.tokimiboatyard.entity.client.UpgradedBoatRenderer;
import net.cozystudios.tokimiboatyard.entity.client.UpgradedRaftBandModel;
import net.cozystudios.tokimiboatyard.registry.ModEntities;
import net.cozystudios.tokimiboatyard.registry.ModScreens;
import net.cozystudios.tokimiboatyard.screen.UpgradedBoatScreen;
//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
//?} else if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
*///?}
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ChestRaftModel;

//? if neoforge {
/*@EventBusSubscriber(modid = TokimiBoatyard.MOD_ID, value = Dist.CLIENT)*/
//?}
public class TokimiBoatyardClient
    //? if fabric
    implements ClientModInitializer
{
    //? if fabric {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(
            ModModelLayers.UPGRADED_BOAT_CHUNK, ChestBoatModel::createBodyModel);
        EntityModelLayerRegistry.registerModelLayer(
            ModModelLayers.UPGRADED_BOAT_BAND, UpgradedBoatBandModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(
            ModModelLayers.UPGRADED_RAFT_CHUNK, ChestRaftModel::createBodyModel);
        EntityModelLayerRegistry.registerModelLayer(
            ModModelLayers.UPGRADED_RAFT_BAND, UpgradedRaftBandModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.UPGRADED_BOAT, UpgradedBoatRenderer::new);
        EntityRendererRegistry.register(ModEntities.UPGRADED_BOAT_PART, UpgradedBoatPartRenderer::new);
        MenuScreens.register(ModScreens.UPGRADED_BOAT, UpgradedBoatScreen::new);
    }
    //?} else if neoforge {
    /*@SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.UPGRADED_BOAT_CHUNK, ChestBoatModel::createBodyModel);
        event.registerLayerDefinition(ModModelLayers.UPGRADED_BOAT_BAND, UpgradedBoatBandModel::getTexturedModelData);
        event.registerLayerDefinition(ModModelLayers.UPGRADED_RAFT_CHUNK, ChestRaftModel::createBodyModel);
        event.registerLayerDefinition(ModModelLayers.UPGRADED_RAFT_BAND, UpgradedRaftBandModel::getTexturedModelData);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.UPGRADED_BOAT, UpgradedBoatRenderer::new);
        event.registerEntityRenderer(ModEntities.UPGRADED_BOAT_PART, UpgradedBoatPartRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModScreens.UPGRADED_BOAT, UpgradedBoatScreen::new);
    }
    *///?}
}
