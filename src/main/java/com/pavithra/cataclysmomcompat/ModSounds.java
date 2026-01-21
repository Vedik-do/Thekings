package com.pavithra.cataclysmomcompat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CataclysmOMCompat.MODID);

    // Harbinger: idle + combat (your “Option B”)
    public static final RegistryObject<SoundEvent> HARBRINGER_IDLE =
            register("music.harbinger.idle");
    public static final RegistryObject<SoundEvent> HARBRINGER_COMBAT =
            register("music.harbinger.combat");

    // Other Cataclysm bosses (single track each)
    public static final RegistryObject<SoundEvent> ENDER_GUARDIAN =
            register("music.boss.ender_guardian");
    public static final RegistryObject<SoundEvent> ANCIENT_REMNANT =
            register("music.boss.ancient_remnant");
    public static final RegistryObject<SoundEvent> LEVIATHAN =
            register("music.boss.the_leviathan");
    public static final RegistryObject<SoundEvent> IGNIS =
            register("music.boss.ignis");
    public static final RegistryObject<SoundEvent> MALEDICTUS =
            register("music.boss.maledictus");
    public static final RegistryObject<SoundEvent> NETHERITE_MONSTROSITY =
            register("music.boss.netherite_monstrosity");
    public static final RegistryObject<SoundEvent> SCYLLA =
            register("music.boss.scylla");

    private static RegistryObject<SoundEvent> register(String path) {
        ResourceLocation id = new ResourceLocation(CataclysmOMCompat.MODID, path);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}
