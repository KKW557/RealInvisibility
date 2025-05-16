package icu.suc.realinvisibility;

import icu.suc.serverevents.ServerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class RealInvisibility implements ModInitializer {

    public static Updater UPDATER;

    public static final Set<Integer> ID = new HashSet<>();

    @Override
    public void onInitialize() {
        var config = Config.loadConfig(FabricLoader.getInstance().getConfigDir().resolve("realinvisibility"), (mainhand, offhand, boots, leggings, chestplate, helmet, body) -> {
            Set<EquipmentSlot> set = new HashSet<>();
            if (mainhand) set.add(EquipmentSlot.MAINHAND);
            if (offhand) set.add(EquipmentSlot.OFFHAND);
            if (boots) set.add(EquipmentSlot.FEET);
            if (leggings) set.add(EquipmentSlot.LEGS);
            if (chestplate) set.add(EquipmentSlot.CHEST);
            if (helmet) set.add(EquipmentSlot.HEAD);
            if (body) set.add(EquipmentSlot.BODY);
            return set;
        });
        var data = config.getA();
        var settings = config.getB();

        if (settings.equipment && settings.metadata) {
            UPDATER = new Updater(data, settings);

            ServerEvents.Player.MODIFY_JOIN_MESSAGE.register((player, message) -> {
                if (player.hasEffect(MobEffects.INVISIBILITY)) {
                    ID.add(player.getId());
                }
                return message;
            });
            ServerEvents.LivingEntity.Effect.ADD.register((affectedEntity, effect, sourceEntity) -> {
                int id = affectedEntity.getId();
                ID.add(id);
                UPDATER.$(affectedEntity);
                return true;
            });
            ServerEvents.LivingEntity.Effect.REMOVE.register((entity, effect) -> {
                int id = entity.getId();
                ID.remove(id);
                UPDATER.$(entity);
                return true;
            });
            ServerEvents.Player.MODIFY_LEAVE_MESSAGE.register((player, message) -> {
                if (player.hasEffect(MobEffects.INVISIBILITY)) {
                    ID.remove(player.getId());
                }
                return message;
            });
            ServerEvents.Connection.Send.MODIFY.register((packetListener, packet) -> {
                if (packetListener instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
                    if (packet instanceof ClientboundSetEquipmentPacket setEquipmentPacket) {
                        int id = setEquipmentPacket.getEntity();
                        if (ID.contains(id) && serverGamePacketListener.getPlayer().getId() != id) {
                            var slots = setEquipmentPacket.getSlots();
                            for (int i = 0; i < slots.size(); i++) {
                                var equipment = slots.get(i);
                                if (equipment.getSecond().isEmpty()) {
                                    continue;
                                }
                                if (settings.slots.contains(equipment.getFirst())) {
                                    slots.set(i, equipment.mapSecond(item -> ItemStack.EMPTY));
                                }
                            }
                        }
                    } else if (packet instanceof ClientboundSetEntityDataPacket(
                            int id, List<SynchedEntityData.DataValue<?>> values
                    )) {
                        if (ID.contains(id) && serverGamePacketListener.getPlayer().getId() != id) {
                            for (int i = 0; i < values.size(); i++) {
                                var value = values.get(i);
                                int index = value.id();
                                if (settings.particles && index == data.DATA_EFFECT_PARTICLES()) {
                                    values.set(i, SynchedEntityData.DataValue.create(UPDATER.DATA_EFFECT_PARTICLES, List.of()));
                                }
                                else if (settings.arrows && index == data.DATA_ARROW_COUNT_ID()) {
                                    values.set(i, SynchedEntityData.DataValue.create(UPDATER.DATA_ARROW_COUNT_ID, 0));
                                }
                                else if (settings.stingers && index == data.DATA_STINGER_COUNT_ID()) {
                                    values.set(i, SynchedEntityData.DataValue.create(UPDATER.DATA_STINGER_COUNT_ID, 0));
                                }
                                else if (settings.fire && index == data.DATA_SHARED_FLAGS_ID()) {
                                    values.set(i, SynchedEntityData.DataValue.create(UPDATER.DATA_SHARED_FLAGS_ID, (byte) ((byte) value.value() & ~data.BIT_MAP_FIRE())));
                                }
                            }
                        }
                    }
                }
                return packet;
            });
        }
    }
}
