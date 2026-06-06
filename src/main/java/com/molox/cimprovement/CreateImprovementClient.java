package com.molox.cimprovement;

import com.molox.cimprovement.handler.ClientPackageUnwrapHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CreateImprovement.MOD_ID, dist = Dist.CLIENT)
public class CreateImprovementClient {

    public CreateImprovementClient(IEventBus modEventBus) {
        // 注册客户端事件监听器
        NeoForge.EVENT_BUS.register(new ClientPackageUnwrapHandler());
    }
}