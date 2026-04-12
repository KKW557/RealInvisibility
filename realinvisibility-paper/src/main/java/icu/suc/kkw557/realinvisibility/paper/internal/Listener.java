package icu.suc.kkw557.realinvisibility.paper.internal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import icu.suc.kkw557.realinvisibility.common.Setting;
import icu.suc.kkw557.realinvisibility.common.Util;
import icu.suc.kkw557.realinvisibility.paper.RealInvisibilityEvent;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public record Listener(Set<Integer> tracked,
                       Set<Setting> settings) implements org.bukkit.event.Listener, PacketListener {
    public Listener(Set<Setting> settings) {
        this(ConcurrentHashMap.newKeySet(), settings);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        tracked.add(player.getEntityId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        tracked.remove(player.getEntityId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPotionEffect(@NotNull EntityPotionEffectEvent event) {
        if (!event.getModifiedType().equals(PotionEffectType.INVISIBILITY)) return;

        var entity = (net.minecraft.world.entity.LivingEntity) ((CraftEntity) event.getEntity()).getHandle();
        int id = entity.getId();

        switch (event.getAction()) {
            case ADDED -> {
                tracked.add(id);
                Util.update(entity, settings);
            }
            case REMOVED, CLEARED -> {
                tracked.remove(id);
                Util.update(entity, settings);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityRemove(@NotNull EntityRemoveEvent event) {
        var entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (!livingEntity.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        tracked.remove(livingEntity.getEntityId());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.ENTITY_EQUIPMENT -> {
                var player = (Player) event.getPlayer();
                int playerId = player.getEntityId();

                var packet = new WrapperPlayServerEntityEquipment(event);
                int id = packet.getEntityId();

                if (playerId == id) return;
                if (!tracked.contains(id)) return;

                boolean keep = true;
                for (var equipment : packet.getEquipment()) {
                    if (equipment.getItem().isEmpty()) continue;
                    keep &= switch (equipment.getSlot()) {
                        case MAIN_HAND -> clearEquipment(player, id, Setting.MAINHAND, equipment);
                        case OFF_HAND -> clearEquipment(player, id, Setting.OFFHAND, equipment);
                        case BOOTS -> clearEquipment(player, id, Setting.FEET, equipment);
                        case LEGGINGS -> clearEquipment(player, id, Setting.LEGS, equipment);
                        case CHEST_PLATE -> clearEquipment(player, id, Setting.CHEST, equipment);
                        case HELMET -> clearEquipment(player, id, Setting.HEAD, equipment);
                        case BODY -> clearEquipment(player, id, Setting.BODY, equipment);
                        case SADDLE -> clearEquipment(player, id, Setting.SADDLE, equipment);
                    };
                }

                if (keep) return;

                packet.write();
                event.markForReEncode(true);
            }
            case PacketType.Play.Server.ENTITY_METADATA -> {
                var player = (Player) event.getPlayer();
                int playerId = player.getEntityId();

                var packet = new WrapperPlayServerEntityMetadata(event);
                int id = packet.getEntityId();

                if (playerId == id) return;
                if (!tracked.contains(id)) return;

                boolean keep = true;
                for (EntityData data : packet.getEntityMetadata()) {
                    keep &= switch (data.getIndex()) {
                        case Util.DATA_SHARED_FLAGS_ID ->
                                clearData(player, id, Setting.FIRE, data, Util.setSharedFlag((byte) data.getValue(), Util.BIT_FIRE, false));
                        case Util.DATA_EFFECT_PARTICLES ->
                                clearData(player, id, Setting.EFFECT_PARTICLES, data, List.of());
                        case Util.DATA_ARROW_COUNT_ID -> clearData(player, id, Setting.ARROWS, data, 0);
                        case Util.DATA_STINGER_COUNT_ID -> clearData(player, id, Setting.STINGERS, data, 0);
                        default -> true;
                    };
                }

                if (keep) return;

                packet.write();
                event.markForReEncode(true);
            }
            default -> {
            }
        }
    }

    private boolean clearEquipment(@NotNull Player player, int entityId, @NotNull Setting setting, @NotNull Equipment equipment) {
        if (!settings.contains(setting)) return true;
        if (callEvent(player, entityId, setting)) return true;
        equipment.setItem(ItemStack.EMPTY);
        return false;
    }

    private boolean clearData(@NotNull Player player, int entityId, @NotNull Setting setting, EntityData<Object> data, @NotNull Object value) {
        if (!settings.contains(setting)) return true;
        if (callEvent(player, entityId, setting)) return true;
        data.setValue(value);
        return false;
    }

    private boolean callEvent(@NotNull Player player, int entityId, @NotNull Setting setting) {
        var event = new RealInvisibilityEvent(player, entityId, setting);
        PacketEvents.getAPI().getEventManager().callEvent(event);
        return event.isCancelled();
    }
}
