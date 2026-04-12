package icu.suc.kkw557.realinvisibility.paper;

import com.github.retrooper.packetevents.event.CancellableEvent;
import com.github.retrooper.packetevents.event.PacketEvent;
import icu.suc.kkw557.realinvisibility.common.Setting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a value in the packet to be modified.
 */
public class RealInvisibilityEvent extends PacketEvent implements CancellableEvent {
    private final Player player;
    private final int entityId;
    private final Setting setting;

    private boolean cancelled;

    @ApiStatus.Internal
    public RealInvisibilityEvent(@NotNull Player player, int entityId, @NotNull Setting setting) {
        this.player = player;
        this.entityId = entityId;
        this.setting = setting;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the entity id
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * @return the setting
     */
    public Setting getSetting() {
        return setting;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
