package icu.suc.realinvisibility;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RealInvisibility extends JavaPlugin implements org.bukkit.event.Listener, PacketListener {

    private Indexes INDEXES;
    private Settings<EquipmentSlot> SETTINGS;
    private Updater UPDATER;

    private static final Set<Integer> ID = new HashSet<>();

    @Override
    public void onEnable() {
        var config = Config.loadConfig(getDataPath(), (mainhand, offhand, boots, leggings, chestplate, helmet, body) -> {
            Set<EquipmentSlot> set = new HashSet<>();
            if (mainhand) set.add(EquipmentSlot.MAIN_HAND);
            if (offhand) set.add(EquipmentSlot.OFF_HAND);
            if (boots) set.add(EquipmentSlot.BOOTS);
            if (leggings) set.add(EquipmentSlot.LEGGINGS);
            if (chestplate) set.add(EquipmentSlot.CHEST_PLATE);
            if (helmet) set.add(EquipmentSlot.HELMET);
            if (body) set.add(EquipmentSlot.BODY);
            return set;
        });
        INDEXES = config.getA();
        SETTINGS = config.getB();

        if (SETTINGS.equipment && SETTINGS.metadata) {
            UPDATER = new Updater(INDEXES, SETTINGS) {
                @Override
                protected void broadcast(@NotNull LivingEntity entity, @NotNull Collection<Packet<?>> packets) {
                    for (var connection : entity.moonrise$getTrackedEntity().seenBy) {
                        for (var packet : packets) {
                            connection.send(packet);
                        }
                    }
                }
            };

            getServer().getPluginManager().registerEvents(this, this);
            PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.HIGHEST);
        }
    }

    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            ID.add(player.getEntityId());
        }
    }

    @EventHandler
    private void onEntityPotionEffect(@NotNull EntityPotionEffectEvent event) {
        if (((CraftEntity) event.getEntity()).getHandle() instanceof LivingEntity entity && event.getModifiedType().equals(PotionEffectType.INVISIBILITY)) {
            int id = entity.getId();
            var action = event.getAction();
            if (action.equals(EntityPotionEffectEvent.Action.ADDED)) {
                ID.add(id);
                UPDATER.$(entity);
            } else if (action.equals(EntityPotionEffectEvent.Action.REMOVED) || action.equals(EntityPotionEffectEvent.Action.CLEARED)) {
                ID.remove(id);
                UPDATER.$(entity);
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            ID.remove(player.getEntityId());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.ENTITY_EQUIPMENT -> {
                var packet = new WrapperPlayServerEntityEquipment(event);
                int id = packet.getEntityId();
                if (ID.contains(id) && event.getUser().getEntityId() != id) {
                    for (Equipment equipment : packet.getEquipment()) {
                        if (equipment.getItem().isEmpty()) {
                            continue;
                        }
                        if (SETTINGS.slots.contains(equipment.getSlot())) {
                            equipment.setItem(ItemStack.EMPTY);
                        }
                    }
                    packet.write();
                }
            }
            case PacketType.Play.Server.ENTITY_METADATA -> {
                var packet = new WrapperPlayServerEntityMetadata(event);
                int id = packet.getEntityId();
                if (ID.contains(id) && event.getUser().getEntityId() != id) {
                    for (EntityData data : packet.getEntityMetadata()) {
                        int index = data.getIndex();
                        if (SETTINGS.particles && index == INDEXES.particles()) {
                            data.setValue(List.of());
                        } else if (SETTINGS.arrows && index == INDEXES.arrows()) {
                            data.setValue(0);
                        }
                    }
                    packet.write();
                }
            }
            default -> {}
        }
    }
}
