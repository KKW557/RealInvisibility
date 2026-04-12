package icu.suc.kkw557.realinvisibility.fabric.internal;

import icu.suc.kkw557.realinvisibility.common.Setting;
import icu.suc.kkw557.realinvisibility.common.Util;
import icu.suc.mc.serverevents.ServerEventPriority;
import icu.suc.mc.serverevents.ServerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.util.Set;

public final class Main implements ModInitializer {
    private static final String PATH = "realinvisibility.properties";

    public static Main INSTANCE;

    public Set<Setting> settings;
    public Listener listener;

    @Override
    public void onInitialize() {
        INSTANCE = this;

        try {
            settings = Util.loadSettings(FabricLoader.getInstance().getConfigDir().resolve(PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        listener = new Listener(settings);

        registerEvents();
    }

    private void registerEvents() {
        ServerEvents.register(ServerEventPriority.HIGHEST, listener, ServerEvents.Connection.State.Play.JOIN, ServerEvents.Connection.State.Play.DISCONNECT, ServerEvents.LivingEntity.Effect.ADD, ServerEvents.LivingEntity.Effect.REMOVE, ServerEvents.Entity.UNLOAD, ServerEvents.Connection.Send.MODIFY);
    }
}
