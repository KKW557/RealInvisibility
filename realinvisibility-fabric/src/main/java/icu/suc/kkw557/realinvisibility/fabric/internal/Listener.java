package icu.suc.kkw557.realinvisibility.fabric.internal;

import com.mojang.datafixers.util.Pair;
import icu.suc.kkw557.realinvisibility.common.Setting;
import icu.suc.kkw557.realinvisibility.common.Util;
import icu.suc.kkw557.realinvisibility.fabric.RealInvisibilityEvents;
import icu.suc.mc.serverevents.ServerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public record Listener(Set<Integer> tracked,
                       Set<Setting> settings) implements ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect, ServerEvents.LivingEntity.Effect.Add, ServerEvents.LivingEntity.Effect.Remove, ServerEntityEvents.Unload, ServerEvents.Connection.Send.Modify {
    public Listener(Set<Setting> settings) {
        this(ConcurrentHashMap.newKeySet(), settings);
    }

    @Override
    public void onPlayReady(@NotNull ServerGamePacketListenerImpl handler, @NotNull PacketSender sender, @NotNull MinecraftServer server) {
        var player = handler.getPlayer();
        if (!player.hasEffect(MobEffects.INVISIBILITY)) return;
        tracked.add(player.getId());
    }

    @Override
    public void onPlayDisconnect(@NotNull ServerGamePacketListenerImpl handler, @NotNull MinecraftServer server) {
        var player = handler.getPlayer();
        if (!player.hasEffect(MobEffects.INVISIBILITY)) return;
        tracked.remove(handler.getPlayer().getId());
    }

    @Override
    public boolean addEffect(@NotNull LivingEntity affectedEntity, @NotNull MobEffectInstance effect, @Nullable Entity sourceEntity) {
        if (!effect.getEffect().equals(MobEffects.INVISIBILITY)) return true;
        tracked.add(affectedEntity.getId());
        Util.update(affectedEntity, settings);
        return true;
    }

    @Override
    public boolean removeEffect(@NotNull LivingEntity entity, @Nullable MobEffectInstance effect) {
        if (effect == null) return true;
        if (!effect.getEffect().equals(MobEffects.INVISIBILITY)) return true;
        tracked.remove(entity.getId());
        Util.update(entity, settings);
        return true;
    }

    @Override
    public void onUnload(@NotNull Entity entity, @NotNull ServerLevel world) {
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (!livingEntity.hasEffect(MobEffects.INVISIBILITY)) return;
        tracked.remove(livingEntity.getId());
    }

    @Override
    public @NotNull Packet<?> modifySend(@Nullable PacketListener packetListener, @NotNull Packet<?> packet) {
        if (!(packetListener instanceof ServerGamePacketListenerImpl serverGamePacketListener)) return packet;

        var player = serverGamePacketListener.getPlayer();
        var playerId = player.getId();

        switch (packet) {
            case ClientboundSetEquipmentPacket equipmentPacket -> {
                int id = equipmentPacket.getEntity();

                if (playerId == id) return packet;
                if (!tracked.contains(id)) return packet;

                var slots = equipmentPacket.getSlots();
                for (int i = 0; i < slots.size(); i++) {
                    var equipment = slots.get(i);
                    if (equipment.getSecond().isEmpty()) continue;
                    switch (equipment.getFirst()) {
                        case MAINHAND -> clearEquipment(player, id, Setting.MAINHAND, equipment, slots, i);
                        case OFFHAND -> clearEquipment(player, id, Setting.OFFHAND, equipment, slots, i);
                        case FEET -> clearEquipment(player, id, Setting.FEET, equipment, slots, i);
                        case LEGS -> clearEquipment(player, id, Setting.LEGS, equipment, slots, i);
                        case CHEST -> clearEquipment(player, id, Setting.CHEST, equipment, slots, i);
                        case HEAD -> clearEquipment(player, id, Setting.HEAD, equipment, slots, i);
                        case BODY -> clearEquipment(player, id, Setting.BODY, equipment, slots, i);
                        case SADDLE -> clearEquipment(player, id, Setting.SADDLE, equipment, slots, i);
                    }
                }
            }
            case ClientboundSetEntityDataPacket(int id, var packedItems) -> {
                if (playerId == id) return packet;
                if (!tracked.contains(id)) return packet;

                for (int i = 0; i < packedItems.size(); i++) {
                    var data = packedItems.get(i);

                    switch (data.id()) {
                        case Util.DATA_SHARED_FLAGS_ID ->
                                clearData(player, id, Setting.FIRE, packedItems, i, () -> new SynchedEntityData.DataValue<>(Util.DATA_SHARED_FLAGS_ID, EntityDataSerializers.BYTE, Util.setSharedFlag((byte) data.value(), Util.BIT_FIRE, false)));
                        case Util.DATA_EFFECT_PARTICLES ->
                                clearData(player, id, Setting.EFFECT_PARTICLES, packedItems, i, () -> new SynchedEntityData.DataValue<>(Util.DATA_EFFECT_PARTICLES, EntityDataSerializers.PARTICLES, Collections.emptyList()));
                        case Util.DATA_ARROW_COUNT_ID ->
                                clearData(player, id, Setting.ARROWS, packedItems, i, () -> new SynchedEntityData.DataValue<>(Util.DATA_ARROW_COUNT_ID, EntityDataSerializers.INT, 0));
                        case Util.DATA_STINGER_COUNT_ID ->
                                clearData(player, id, Setting.STINGERS, packedItems, i, () -> new SynchedEntityData.DataValue<>(Util.DATA_STINGER_COUNT_ID, EntityDataSerializers.INT, 0));
                    }
                }
            }
            default -> {
            }
        }

        return packet;
    }

    private void clearEquipment(@NotNull ServerPlayer player, int entityId, @NotNull Setting setting, @NotNull Pair<@NotNull EquipmentSlot, @NotNull ItemStack> equipment, @NotNull List<@NotNull Pair<@NotNull EquipmentSlot, @NotNull ItemStack>> slots, int i) {
        if (!settings.contains(setting)) return;
        boolean bool = RealInvisibilityEvents.ALLOW.invoker().allow(player, entityId, setting);
        if (bool) {
            slots.set(i, equipment.mapSecond(itemStack -> ItemStack.EMPTY));
        }
    }

    private void clearData(@NotNull ServerPlayer player, int entityId, @NotNull Setting setting, @NotNull List<SynchedEntityData.@NotNull DataValue<?>> packedItems, int i, @NotNull Supplier<SynchedEntityData.@NotNull DataValue<?>> supplier) {
        if (!settings.contains(setting)) return;
        boolean bool = RealInvisibilityEvents.ALLOW.invoker().allow(player, entityId, setting);
        if (bool) {
            packedItems.set(i, supplier.get());
        }
    }
}
