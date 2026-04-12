package icu.suc.kkw557.realinvisibility.fabric;

import icu.suc.kkw557.realinvisibility.common.Setting;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class RealInvisibilityEvents {
    /**
     * An event that allows a value in packet to be modified.
     */
    public static final Event<RealInvisibilityEvents.Allow> ALLOW = EventFactory.createArrayBacked(RealInvisibilityEvents.Allow.class, callbacks -> (player, entityId, setting) -> {
        for (RealInvisibilityEvents.Allow callback : callbacks) {
            if (!callback.allow(player, entityId, setting)) {
                return false;
            }
        }
        return true;
    });

    private RealInvisibilityEvents() {
    }

    @FunctionalInterface
    public interface Allow {
        /**
         * Called when a value in the packet to be modified.
         *
         * @param player   the player
         * @param entityId the entity id
         * @param setting  the setting
         * @return {@code true} if the value should be modified, otherwise {@code false}
         */
        boolean allow(@NotNull ServerPlayer player, int entityId, @NotNull Setting setting);
    }
}
