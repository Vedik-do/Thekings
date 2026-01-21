package com.pavithra.cataclysmomcompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ClientEvents {
    private static final double SEARCH_RADIUS = 30.0;

    private static BossThemeSoundInstance currentTheme = null;
    private static SoundEventKey currentKey = null;
    private static int noBossTicks = 0;
    private static boolean overhauledMuted = false;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        LivingEntity boss = findClosestBoss(player);
        if (boss != null) {
            noBossTicks = 0;

            BossType bossType = bossTypeOf(boss);
            if (bossType == null) return;

            // Decide which sound to use (Harbinger idle vs combat, others just combat).
            var soundEvent = bossType.pickTrack(boss);

            ResourceLocation soundId = BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
            SoundEventKey desiredKey = new SoundEventKey(bossType, soundId);

            if (currentTheme == null || currentTheme.isDone() || currentKey == null || !currentKey.equals(desiredKey)) {
                // Fade out previous theme (fast) and start the new one.
                if (currentTheme != null && !currentTheme.isDone()) {
                    currentTheme.beginFadeOut(20, true);
                }

                currentTheme = new BossThemeSoundInstance(soundEvent);
                currentKey = desiredKey;

                SoundManager sm = mc.getSoundManager();
                sm.play(currentTheme);
            }

            applyOverhauledMusicOverride(true);
        } else {
            noBossTicks++;

            if (currentTheme != null && !currentTheme.isDone()) {
                // Start fading out as soon as boss leaves range.
                if (noBossTicks == 1) {
                    currentTheme.beginFadeOut(40, false);
                }
                // If we haven't had a boss in a while, hard-stop once silent.
                if (noBossTicks >= 1200) {
                    currentTheme.hardStopAfterFade();
                }
            }

            // If theme ended, clear and unmute OverhauledMusic.
            if (currentTheme == null || currentTheme.isDone()) {
                currentTheme = null;
                currentKey = null;
                applyOverhauledMusicOverride(false);
            }
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() == null) return;

        ResourceLocation id = event.getSound().getLocation();
        if (id == null) return;

        // Cancel Cataclysm's OWN boss music (namespace cataclysm, SoundSource.MUSIC).
        // This prevents double-music. Records/ambience aren't touched.
        if ("cataclysm".equals(id.getNamespace()) && event.getSound().getSource() == net.minecraft.sounds.SoundSource.MUSIC) {
            event.setSound(null);
        }
    }

    private static LivingEntity findClosestBoss(LocalPlayer player) {
        AABB box = player.getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, box);

        LivingEntity best = null;
        double bestDist2 = Double.MAX_VALUE;

        for (LivingEntity e : list) {
            BossType bt = bossTypeOf(e);
            if (bt == null) continue;

            double d2 = player.distanceToSqr(e);
            if (d2 < bestDist2) {
                bestDist2 = d2;
                best = e;
            }
        }
        return best;
    }

    private static BossType bossTypeOf(LivingEntity e) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        if (id == null) return null;

        for (BossType bt : BossType.values()) {
            if (bt.matches(id)) return bt;
        }
        return null;
    }

    private static void applyOverhauledMusicOverride(boolean wantMute) {
        if (wantMute) {
            OverhauledMusicBridge.muteTick();
            overhauledMuted = true;
        } else if (overhauledMuted) {
            OverhauledMusicBridge.unmuteNow();
            overhauledMuted = false;
        }
    }

    private record SoundEventKey(BossType bossType, ResourceLocation soundId) {}
}
