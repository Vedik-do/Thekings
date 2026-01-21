package com.pavithra.cataclysmomcompat;

import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This is the same idea as your Mowzie compat:
 * - OverhauledMusic keeps its own fading instances.
 * - We temporarily fade the current one to 0 so our boss music can play cleanly.
 *
 * It uses reflection because OverhauledMusic doesn't expose a public API.
 */
public final class OverhauledMusicBridge {
    private static final boolean PRESENT = ModList.get().isLoaded("overhauledmusic");

    private static Object director;              // com.overhauledmusic.client.MusicDirector
    private static Field directorCurrentField;   // current key
    private static Field directorInstancesField; // Map<?, FadingMusicInstance>

    private static Method fadingFadeTo;          // fadeTo(float, int)
    private static Method fadingSetActive;       // setActive(boolean)
    private static Field fadingInactiveTicks;    // inactiveTicks

    private static boolean initTried = false;
    private static boolean ok = false;

    private OverhauledMusicBridge() {}

    private static void initIfNeeded() {
        if (initTried) return;
        initTried = true;

        if (!PRESENT) return;

        try {
            // com.overhauledmusic.client.ClientEvents.DIRECTOR
            Class<?> clientEvents = Class.forName("com.overhauledmusic.client.ClientEvents");
            Field directorField = clientEvents.getDeclaredField("DIRECTOR");
            directorField.setAccessible(true);
            director = directorField.get(null);

            Class<?> musicDirector = Class.forName("com.overhauledmusic.client.MusicDirector");
            directorCurrentField = musicDirector.getDeclaredField("current");
            directorCurrentField.setAccessible(true);
            directorInstancesField = musicDirector.getDeclaredField("instances");
            directorInstancesField.setAccessible(true);

            Class<?> fading = Class.forName("com.overhauledmusic.client.FadingMusicInstance");
            fadingFadeTo = fading.getDeclaredMethod("fadeTo", float.class, int.class);
            fadingFadeTo.setAccessible(true);
            fadingSetActive = fading.getDeclaredMethod("setActive", boolean.class);
            fadingSetActive.setAccessible(true);
            fadingInactiveTicks = fading.getDeclaredField("inactiveTicks");
            fadingInactiveTicks.setAccessible(true);

            ok = true;
        } catch (Throwable t) {
            ok = false;
        }
    }

    private static Object currentInstance() throws Exception {
        if (!ok) return null;

        Object currentKey = directorCurrentField.get(director);
        if (currentKey == null) return null;

        @SuppressWarnings("unchecked")
        Map<Object, Object> instances = (Map<Object, Object>) directorInstancesField.get(director);
        if (instances == null) return null;

        return instances.get(currentKey);
    }

    public static void muteTick() {
        initIfNeeded();
        if (!ok) return;

        try {
            Object inst = currentInstance();
            if (inst == null) return;

            // Smoothly fade OverhauledMusic to 0 and keep it "active" so it doesn't try to replace itself.
            fadingFadeTo.invoke(inst, 0.0f, 20);
            fadingSetActive.invoke(inst, true);
            fadingInactiveTicks.setInt(inst, 0);
        } catch (Throwable ignored) {
        }
    }

    public static void unmuteNow() {
        initIfNeeded();
        if (!ok) return;

        try {
            Object inst = currentInstance();
            if (inst == null) return;

            // Bring it back quickly and allow it to manage itself again.
            fadingFadeTo.invoke(inst, 1.0f, 20);
            fadingSetActive.invoke(inst, false);
            fadingInactiveTicks.setInt(inst, 0);
        } catch (Throwable ignored) {
        }
    }
}
