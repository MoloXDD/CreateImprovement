package com.molox.aerowind;

import net.neoforged.fml.ModList;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CreateImprovementConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue WIND_MIN_SPEED = BUILDER
            .comment("Minimum speed (blocks/second) to start playing wind sound")
            .defineInRange("wind_min_speed", 3.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue WIND_MAX_SPEED = BUILDER
            .comment("Speed (blocks/second) at which wind sound reaches maximum volume")
            .defineInRange("wind_max_speed", 25.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue WIND_MIN_VOLUME = BUILDER
            .comment("Volume when moving at minimum speed threshold (exposed)")
            .defineInRange("wind_min_volume", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue WIND_MAX_VOLUME = BUILDER
            .comment("Volume when moving at max speed and above (exposed)")
            .defineInRange("wind_max_volume", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue WIND_BLOCKED_FACTOR = BUILDER
            .comment("Volume multiplier applied when obstructed in the direction of travel (0.0 = silent, 1.0 = same as exposed)")
            .defineInRange("wind_blocked_factor", 0.4, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void onLoad(final ModConfigEvent event) {
        if (ModList.get().isLoaded("sable")) {
            SableIntegration.applyConfig(
                    WIND_MIN_SPEED.get(),
                    WIND_MAX_SPEED.get(),
                    WIND_MIN_VOLUME.get().floatValue(),
                    WIND_MAX_VOLUME.get().floatValue(),
                    WIND_BLOCKED_FACTOR.get().floatValue()
            );
        }
    }
}