package com.molox.cimprovement;

import com.molox.cimprovement.network.UnwrapPackagePacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(CreateImprovement.MOD_ID)
public class CreateImprovement {

    public static final String MOD_ID = "create_improvement";

    public CreateImprovement(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(CreateImprovement::registerPayloads);
        modContainer.registerConfig(ModConfig.Type.CLIENT, CreateImprovementConfig.SPEC);
        modEventBus.addListener(CreateImprovementConfig::onLoad);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID).versioned("1");
        registrar.playToServer(
                UnwrapPackagePacket.TYPE,
                UnwrapPackagePacket.STREAM_CODEC,
                UnwrapPackagePacket::handle
        );
    }
}