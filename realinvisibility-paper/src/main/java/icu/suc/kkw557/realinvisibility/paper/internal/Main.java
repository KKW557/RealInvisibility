package icu.suc.kkw557.realinvisibility.paper.internal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import icu.suc.kkw557.realinvisibility.common.Setting;
import icu.suc.kkw557.realinvisibility.common.Util;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Set;

public final class Main extends JavaPlugin {
    private static final String PATH = "settings.properties";

    public static Main INSTANCE;

    public Set<Setting> settings;
    public Listener listener;

    @Override
    public void onEnable() {
        INSTANCE = this;

        try {
            settings = Util.loadSettings(getDataPath().resolve(PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        listener = new Listener(settings);

        registerEvents();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(listener, this);
        PacketEvents.getAPI().getEventManager().registerListener(listener, PacketListenerPriority.HIGHEST);
    }
}
