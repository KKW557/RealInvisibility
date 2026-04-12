package icu.suc.kkw557.realinvisibility.paper;

import icu.suc.kkw557.realinvisibility.common.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a value in the packet to be modified.
 */
public class RealInvisibilityEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final int entityId;
    private final Setting setting;

    private boolean cancelled;

    @ApiStatus.Internal
    public RealInvisibilityEvent(@NotNull Player player, int entityId, @NotNull Setting setting) {
        super(player);
        this.entityId = entityId;
        this.setting = setting;
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

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
