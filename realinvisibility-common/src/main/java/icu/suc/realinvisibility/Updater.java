package icu.suc.realinvisibility;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Updater {

    private final Settings<?> settings;

    public final EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES;
    public final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID;
    public final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID;
    public final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;

    public Updater(@NotNull Data data, @NotNull Settings<?> settings) {
        this.settings = settings;

        DATA_EFFECT_PARTICLES = new EntityDataAccessor<>(data.DATA_EFFECT_PARTICLES(), EntityDataSerializers.PARTICLES);
        DATA_ARROW_COUNT_ID = new EntityDataAccessor<>(data.DATA_ARROW_COUNT_ID(), EntityDataSerializers.INT);
        DATA_STINGER_COUNT_ID = new EntityDataAccessor<>(data.DATA_STINGER_COUNT_ID(), EntityDataSerializers.INT);
        DATA_SHARED_FLAGS_ID = new EntityDataAccessor<>(data.DATA_SHARED_FLAGS_ID(), EntityDataSerializers.BYTE);
    }

    public void $(@NotNull LivingEntity entity) {
        broadcast(entity, packets(entity));
    }

    protected @NotNull Collection<Packet<?>> packets(@NotNull LivingEntity entity) {
        Collection<Packet<?>> packets = new HashSet<>();

        if (settings.equipment) {
            List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();

            if (settings.mainhand) {
                var item = entity.getItemBySlot(EquipmentSlot.MAINHAND);
                if (!item.isEmpty()) {
                    list.add(Pair.of(EquipmentSlot.MAINHAND, item));
                }
            }
            if (settings.offhand) {
                var item = entity.getItemBySlot(EquipmentSlot.OFFHAND);
                if (!item.isEmpty()) {
                    list.add(Pair.of(EquipmentSlot.OFFHAND, item));
                }
            }
            if (settings.boots) {
                var item = entity.getItemBySlot(EquipmentSlot.FEET);
                if (!item.isEmpty()) {
                    list.add(Pair.of(EquipmentSlot.FEET, item));
                }
            }
            if (settings.leggings) {
                var item = entity.getItemBySlot(EquipmentSlot.LEGS);
                if (!item.isEmpty()) {
                    list.add(Pair.of(EquipmentSlot.LEGS, item));
                }
            }
            if (settings.chestplate) {
                var item = entity.getItemBySlot(EquipmentSlot.CHEST);
                if (!item.isEmpty()) {
                    list.add(Pair.of(EquipmentSlot.CHEST, item));
                }
            }
            if (settings.helmet) {
                var item = entity.getItemBySlot(EquipmentSlot.HEAD);
                if (!item.isEmpty()) {
                    list.add(Pair.of(EquipmentSlot.HEAD, item));
                }
            }
            if (settings.body) {
                var item = entity.getItemBySlot(EquipmentSlot.BODY);
                if (!item.isEmpty()) {
                    list.add(Pair.of(EquipmentSlot.BODY, item));
                }
            }

            if (!list.isEmpty()) {
                packets.add(new ClientboundSetEquipmentPacket(entity.getId(), list));
            }
        }

        if (settings.metadata) {
            List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();

            if (settings.particles) {
                List<ParticleOptions> particles = entity.getActiveEffects().stream().filter(effect -> !effect.isVisible()).map(MobEffectInstance::getParticleOptions).toList();
                if (!particles.isEmpty()) {
                    list.add(SynchedEntityData.DataValue.create(DATA_EFFECT_PARTICLES, particles));
                }
            }
            if (settings.arrows) {
                list.add(SynchedEntityData.DataValue.create(DATA_ARROW_COUNT_ID, entity.getArrowCount()));
            }
            if (settings.stingers) {
                list.add(SynchedEntityData.DataValue.create(DATA_STINGER_COUNT_ID, entity.getStingerCount()));
            }
            if (settings.fire) {
                list.add(SynchedEntityData.DataValue.create(DATA_SHARED_FLAGS_ID, entity.getEntityData().get(DATA_SHARED_FLAGS_ID)));
            }

            if (!list.isEmpty()) {
                packets.add(new ClientboundSetEntityDataPacket(entity.getId(), list));
            }
        }

        return packets;
    }

    protected void broadcast(@NotNull LivingEntity entity, @NotNull Collection<Packet<?>> packets) {
        if (entity.level().getChunkSource() instanceof ServerChunkCache cache) {
            for (var packet : packets) {
                cache.broadcast(entity, packet);
            }
        }
    }
}
