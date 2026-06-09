package com.molox.aerowind.handler;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3dc;

@OnlyIn(Dist.CLIENT)
public class WindSoundHandler {

    public static double minSpeedPerSecond = 3.0;
    public static double maxSpeedPerSecond = 25.0;
    public static float minVolume = 0.0f;
    public static float maxVolume = 1.0f;
    public static float blockedFactor = 0.5f;

    private static final double RAYCAST_DISTANCE = 3.0;
    private static final float FADEOUT_THRESHOLD = 0.002f;
    private static final int LEAVE_DELAY_TICKS = 10;

    private WindSoundInstance currentSound = null;
    private int leaveDelayCounter = 0;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            leaveDelayCounter = 0;
            fadeOut();
            return;
        }

        SubLevel trackingSubLevel = ((EntityMovementExtension) player).sable$getTrackingSubLevel();

        if (!(trackingSubLevel instanceof ClientSubLevel clientSubLevel)) {
            leaveDelayCounter++;
            if (leaveDelayCounter >= LEAVE_DELAY_TICKS) {
                fadeOut();
            }
            return;
        }

        leaveDelayCounter = 0;

        Vec3 velocity = getSubLevelVelocity(clientSubLevel);
        double speedPerSecond = velocity.length() * 20.0;

        if (speedPerSecond < minSpeedPerSecond) {
            fadeOut();
            return;
        }

        double t = Math.min((speedPerSecond - minSpeedPerSecond) / (maxSpeedPerSecond - minSpeedPerSecond), 1.0);
        float openVolume = minVolume + (float) t * (maxVolume - minVolume);

        boolean exposed = isExposed(player, clientSubLevel, velocity);
        float targetVolume = exposed ? openVolume : openVolume * blockedFactor;

        if (currentSound == null || currentSound.isStopped()) {
            currentSound = new WindSoundInstance(targetVolume);
            mc.getSoundManager().play(currentSound);
        } else {
            currentSound.setTargetVolume(targetVolume);
        }
    }

    private Vec3 getSubLevelVelocity(ClientSubLevel clientSubLevel) {
        Vector3dc currentPos = clientSubLevel.logicalPose().position();
        Vector3dc lastPos = clientSubLevel.lastPose().position();
        return new Vec3(
                currentPos.x() - lastPos.x(),
                currentPos.y() - lastPos.y(),
                currentPos.z() - lastPos.z()
        );
    }

    private boolean isExposed(LocalPlayer player, ClientSubLevel clientSubLevel, Vec3 velocityPerTick) {
        if (velocityPerTick.lengthSqr() < 1e-10) return true;

        Pose3dc pose = clientSubLevel.logicalPose();

        Vec3 localDir = pose.transformNormalInverse(velocityPerTick).normalize();
        Vec3 localPlayerEyePos = pose.transformPositionInverse(player.getEyePosition());
        Vec3 localEnd = localPlayerEyePos.add(localDir.scale(RAYCAST_DISTANCE));

        ClipContext clipContext = new ClipContext(
                localPlayerEyePos,
                localEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult hitResult = clientSubLevel.getLevel().clip(clipContext);
        return hitResult.getType() == HitResult.Type.MISS;
    }

    private void fadeOut() {
        if (currentSound != null) {
            currentSound.setTargetVolume(0.0f);
            currentSound = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class WindSoundInstance extends AbstractTickableSoundInstance {

        private float targetVolume;

        public WindSoundInstance(float initialVolume) {
            super(
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("create_improvement", "wind_rush")
                    ),
                    SoundSource.AMBIENT,
                    SoundInstance.createUnseededRandom()
            );
            this.volume = 0.0f;
            this.targetVolume = initialVolume;
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        public void setTargetVolume(float volume) {
            this.targetVolume = volume;
        }

        @Override
        public void tick() {
            float diff = targetVolume - this.volume;
            this.volume += diff * 0.1f;
            if (targetVolume == 0.0f && this.volume < FADEOUT_THRESHOLD) {
                this.stop();
            }
        }
    }
}