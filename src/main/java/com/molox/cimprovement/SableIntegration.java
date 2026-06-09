package com.molox.cimprovement;

import com.molox.cimprovement.handler.WindSoundHandler;
import net.neoforged.neoforge.common.NeoForge;

public class SableIntegration {

    public static void registerClientEvents() {
        NeoForge.EVENT_BUS.register(new WindSoundHandler());
    }

    public static void applyConfig(
            double minSpeed, double maxSpeed,
            float minVolume, float maxVolume,
            float blockedFactor) {
        WindSoundHandler.minSpeedPerSecond = minSpeed;
        WindSoundHandler.maxSpeedPerSecond = maxSpeed;
        WindSoundHandler.minVolume = minVolume;
        WindSoundHandler.maxVolume = maxVolume;
        WindSoundHandler.blockedFactor = blockedFactor;
    }
}