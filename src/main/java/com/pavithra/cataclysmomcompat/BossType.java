package com.pavithra.cataclysmomcompat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Objects;
import java.util.function.Supplier;

public enum BossType {
    // Entity IDs are from Cataclysm’s registry. If a future Cataclysm update changes them,
    // you can just edit these strings and rebuild.
    NETHERITE_MONSTROSITY("cataclysm:netherite_monstrosity", ModSounds.NETHERITE_MONSTROSITY::get),
    ENDER_GUARDIAN("cataclysm:ender_guardian", ModSounds.ENDER_GUARDIAN::get),
    IGNIS("cataclysm:ignis", ModSounds.IGNIS::get),
    THE_HARBINGER("cataclysm:the_harbinger", null), // special: idle + combat
    THE_LEVIATHAN("cataclysm:the_leviathan", ModSounds.LEVIATHAN::get),
    ANCIENT_REMNANT("cataclysm:ancient_remnant", ModSounds.ANCIENT_REMNANT::get),
    MALEDICTUS("cataclysm:maledictus", ModSounds.MALEDICTUS::get),
    SCYLLA("cataclysm:scylla", ModSounds.SCYLLA::get);

    public final ResourceLocation entityId;
    private final Supplier<SoundEvent> track;

    BossType(String entityId, Supplier<SoundEvent> track) {
        this.entityId = new ResourceLocation(entityId);
        this.track = track;
    }

    public boolean matches(ResourceLocation entityTypeId) {
        return Objects.equals(this.entityId, entityTypeId);
    }

    public SoundEvent pickTrack(LivingEntity boss) {
        if (this == THE_HARBINGER) {
            // “Option B” — ONLY Harbinger gets idle vs combat.
            // He flips to combat if he has a target OR if he has taken any damage.
            boolean inCombat = boss.getHealth() < boss.getMaxHealth();
            if (boss instanceof Mob mob && mob.getTarget() != null) {
                inCombat = true;
            }
            return inCombat ? ModSounds.HARBRINGER_COMBAT.get() : ModSounds.HARBRINGER_IDLE.get();
        }
        return track.get();
    }
}
