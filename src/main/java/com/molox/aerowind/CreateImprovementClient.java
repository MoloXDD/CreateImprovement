package com.molox.aerowind;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(value = CreateImprovement.MOD_ID, dist = Dist.CLIENT)
public class CreateImprovementClient {

    public CreateImprovementClient(IEventBus modEventBus) {
        if (ModList.get().isLoaded("sable")) {
            SableIntegration.registerClientEvents();
        }
    }
}