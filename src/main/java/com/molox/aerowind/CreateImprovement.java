package com.molox.aerowind;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(CreateImprovement.MOD_ID)
public class CreateImprovement {

    public static final String MOD_ID = "aeronautics_windsound";

    public CreateImprovement(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, CreateImprovementConfig.SPEC);
        modEventBus.addListener(CreateImprovementConfig::onLoad);
    }
}