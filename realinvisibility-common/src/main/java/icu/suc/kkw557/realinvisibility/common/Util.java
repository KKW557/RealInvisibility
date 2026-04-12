package icu.suc.kkw557.realinvisibility.common;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@ApiStatus.Internal
public final class Util {
    /**
     * @see Entity#DATA_SHARED_FLAGS_ID
     * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Entity">Minecraft Wiki</a>
     */
    public static final int DATA_SHARED_FLAGS_ID = 0;
    /**
     * @see Entity#isOnFire()
     */
    public static final int BIT_FIRE = 0;
    /**
     * @see LivingEntity#DATA_EFFECT_PARTICLES
     * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Living_Entity">Minecraft Wiki</a>
     */
    public static final int DATA_EFFECT_PARTICLES = 10;
    /**
     * @see LivingEntity#DATA_ARROW_COUNT_ID
     * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Living_Entity">Minecraft Wiki</a>
     */
    public static final int DATA_ARROW_COUNT_ID = 12;
    /**
     * @see LivingEntity#DATA_STINGER_COUNT_ID
     * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Living_Entity">Minecraft Wiki</a>
     */
    public static final int DATA_STINGER_COUNT_ID = 13;

    private Util() {
    }

    /**
     * @see Entity#setSharedFlag(int, boolean)
     */
    public static byte setSharedFlag(byte data, int flag, boolean set) {
        if (set) return (byte) (data | 1 << flag);
        else return (byte) (data & ~(1 << flag));
    }

    /**
     * Broadcast a living entity's equipment and metadata.
     *
     * @param entity   the entity
     * @param settings enabled features
     */
    public static void update(@NotNull LivingEntity entity, @NotNull Set<Setting> settings) {
        var packets = packets(entity, settings);
        broadcast(entity, packets);
    }

    private static void broadcast(@NotNull LivingEntity entity, @NotNull Collection<@NotNull Packet<? super ClientGamePacketListener>> packets) {
        var source = ((ServerLevel) entity.level()).getChunkSource();
        for (var packet : packets) {
            source.sendToTrackingPlayers(entity, packet);
        }
    }

    private static @NotNull Collection<@NotNull Packet<? super ClientGamePacketListener>> packets(@NotNull LivingEntity entity, @NotNull Set<Setting> settings) {
        var packets = new ArrayList<Packet<? super ClientGamePacketListener>>();

        var equipmentPacket = equipmentPacket(entity, settings);
        if (equipmentPacket != null) packets.add(equipmentPacket);

        var dataPacket = dataPacket(entity, settings);
        if (dataPacket != null) packets.add(dataPacket);

        return packets;
    }

    private static @Nullable ClientboundSetEquipmentPacket equipmentPacket(@NotNull LivingEntity entity, @NotNull Set<Setting> settings) {
        var list = new ArrayList<Pair<EquipmentSlot, ItemStack>>();

        for (var setting : settings) {
            switch (setting) {
                case MAINHAND -> addEquipment(entity, EquipmentSlot.MAINHAND, list);
                case OFFHAND -> addEquipment(entity, EquipmentSlot.OFFHAND, list);
                case FEET -> addEquipment(entity, EquipmentSlot.FEET, list);
                case LEGS -> addEquipment(entity, EquipmentSlot.LEGS, list);
                case CHEST -> addEquipment(entity, EquipmentSlot.CHEST, list);
                case HEAD -> addEquipment(entity, EquipmentSlot.HEAD, list);
                case BODY -> addEquipment(entity, EquipmentSlot.BODY, list);
                case SADDLE -> addEquipment(entity, EquipmentSlot.SADDLE, list);
            }
        }

        return list.isEmpty() ? null : new ClientboundSetEquipmentPacket(entity.getId(), list);
    }

    private static void addEquipment(@NotNull LivingEntity entity, @NotNull EquipmentSlot slot, @NotNull List<@NotNull Pair<@NotNull EquipmentSlot, @NotNull ItemStack>> list) {
        var item = entity.getItemBySlot(slot);
        if (item.isEmpty()) return;
        list.add(Pair.of(slot, item));
    }

    private static @Nullable ClientboundSetEntityDataPacket dataPacket(@NotNull LivingEntity entity, @NotNull Set<Setting> settings) {
        var list = new ArrayList<SynchedEntityData.DataValue<?>>();

        for (var setting : settings) {
            switch (setting) {
                case FIRE ->
                        list.add(new SynchedEntityData.DataValue<>(DATA_SHARED_FLAGS_ID, EntityDataSerializers.BYTE, entity.getEntityData().get(new EntityDataAccessor<>(DATA_SHARED_FLAGS_ID, EntityDataSerializers.BYTE))));
                case EFFECT_PARTICLES -> {
                    var particles = new ArrayList<ParticleOptions>();
                    for (var effect : entity.getActiveEffects()) {
                        if (!effect.isVisible()) continue;
                        particles.add(effect.getParticleOptions());
                    }
                    if (particles.isEmpty()) continue;
                    list.add(new SynchedEntityData.DataValue<>(DATA_EFFECT_PARTICLES, EntityDataSerializers.PARTICLES, particles));
                }
                case ARROWS ->
                        list.add(new SynchedEntityData.DataValue<>(DATA_ARROW_COUNT_ID, EntityDataSerializers.INT, entity.getArrowCount()));
                case STINGERS ->
                        list.add(new SynchedEntityData.DataValue<>(DATA_STINGER_COUNT_ID, EntityDataSerializers.INT, entity.getStingerCount()));
            }
        }

        return list.isEmpty() ? null : new ClientboundSetEntityDataPacket(entity.getId(), list);
    }

    public static @NotNull Set<Setting> loadSettings(@NotNull Path path) throws IOException {
        var parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        if (Files.notExists(path)) Files.createFile(path);

        var properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }

        var settings = new HashSet<>(List.of(Setting.values()));
        for (var setting : Setting.values()) {
            if (Boolean.parseBoolean(properties.getProperty(setting.getName(), "true"))) continue;
            settings.remove(setting);
        }

        try (var writer = Files.newBufferedWriter(path)) {
            for (var setting : Setting.values()) {
                writer.write("# " + setting.getComments());
                writer.newLine();
                writer.write(setting.getName() + "=" + settings.contains(setting));
                writer.newLine();
            }
        }

        return settings;
    }
}
