package com.pavithra.cataclysmomcompat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CataclysmOMCompat.MODID)
public class CataclysmOMCompat {
    public static final String MODID = "cataclysmomcompat";

    public CataclysmOMCompat() {
        ModSounds.register(FMLJavaModLoadingContext.get().getModEventBus());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientEvents.init();
        });
    }
}
