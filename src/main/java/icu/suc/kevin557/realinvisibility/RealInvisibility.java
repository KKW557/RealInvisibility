package icu.suc.kevin557.realinvisibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Set;

public final class RealInvisibility extends JavaPlugin implements Listener {

    private static EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID;
    private static EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES;
    private static int MV_ARROWS;
    private static int MV_PARTICLES;

    private boolean helmet;
    private boolean chestplate;
    private boolean leggings;
    private boolean boots;
    private boolean mainhand;
    private boolean offhand;
    private boolean arrows;
    private boolean particles;

    private boolean equipment;
    private boolean metadata;

    private Set<EnumWrappers.ItemSlot> slots;

    private Set<Integer> players;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Configuration config = getConfig();
        config.addDefault("helmet", true);
        config.addDefault("chestplate", true);
        config.addDefault("leggings", true);
        config.addDefault("boots", true);
        config.addDefault("mainhand", true);
        config.addDefault("offhand", true);
        config.addDefault("arrows", true);
        config.addDefault("particles", true);
        config.options().copyDefaults(true);
        saveConfig();

        helmet = config.getBoolean("helmet");
        chestplate = config.getBoolean("chestplate");
        leggings = config.getBoolean("leggings");
        boots = config.getBoolean("boots");
        mainhand = config.getBoolean("mainhand");
        offhand = config.getBoolean("offhand");
        arrows = config.getBoolean("arrows");
        particles = config.getBoolean("particles");

        equipment = helmet || chestplate || leggings || boots || mainhand || offhand;

        if (equipment) {
            slots = Sets.newHashSet();
            if (helmet) {
                slots.add(EnumWrappers.ItemSlot.HEAD);
            }
            if (chestplate) {
                slots.add(EnumWrappers.ItemSlot.CHEST);
            }
            if (leggings) {
                slots.add(EnumWrappers.ItemSlot.LEGS);
            }
            if (boots) {
                slots.add(EnumWrappers.ItemSlot.FEET);
            }
            if (mainhand) {
                slots.add(EnumWrappers.ItemSlot.MAINHAND);
            }
            if (offhand) {
                slots.add(EnumWrappers.ItemSlot.OFFHAND);
            }
        }

        if (equipment) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new EntityEquipmentAdapter());
        }

        if (arrows) {
            DATA_ARROW_COUNT_ID = LivingEntity.DATA_ARROW_COUNT_ID;
            MV_ARROWS = DATA_ARROW_COUNT_ID.id();
        }
        if (particles) {
            try {
                DATA_EFFECT_PARTICLES = (EntityDataAccessor<List<ParticleOptions>>) FieldUtils.readStaticField(LivingEntity.class, "DATA_EFFECT_PARTICLES", true);
                MV_PARTICLES = DATA_EFFECT_PARTICLES.id();
            } catch (Exception e) {
                particles = false;
            }
        }
        if (arrows || particles) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMetadataAdapter());
            metadata = true;
        }

        if (equipment && metadata) {
            players = Sets.newHashSet();

            getServer().getPluginManager().registerEvents(this, this);
        }
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player && event.getModifiedType().equals(PotionEffectType.INVISIBILITY)) {
            int id = player.getEntityId();
            EntityPotionEffectEvent.Action action = event.getAction();
            if (action.equals(EntityPotionEffectEvent.Action.ADDED)) {
                players.add(id);
                update(player);
            } else if (action.equals(EntityPotionEffectEvent.Action.REMOVED) || action.equals(EntityPotionEffectEvent.Action.CLEARED)) {
                players.remove(id);
                update(player);
            }
        }
    }

    public void update(Player player) {
        ServerPlayer handle = ((CraftPlayer) player).getHandle();

        List<com.mojang.datafixers.util.Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipmentList = Lists.newArrayList();
        if (equipment) {

            Inventory inventory = handle.getInventory();
            NonNullList<net.minecraft.world.item.ItemStack> armor = inventory.armor;

            if (helmet) {
                net.minecraft.world.item.ItemStack helmet = armor.get(3);
                if (!helmet.isEmpty()) {
                    equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.HEAD, helmet));
                }
            }
            if (chestplate) {
                net.minecraft.world.item.ItemStack chestplate = armor.get(2);
                if (!chestplate.isEmpty()) {
                    equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.CHEST, chestplate));
                }
            }
            if (leggings) {
                net.minecraft.world.item.ItemStack leggings = armor.get(1);
                if (!leggings.isEmpty()) {
                    equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.LEGS, leggings));
                }
            }
            if (boots) {
                net.minecraft.world.item.ItemStack boots = armor.getFirst();
                if (!boots.isEmpty()) {
                    equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.FEET, boots));
                }
            }
            if (mainhand) {
                net.minecraft.world.item.ItemStack mainhand = inventory.getSelected();
                if (!mainhand.isEmpty()) {
                    equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.MAINHAND, mainhand));
                }
            }
            if (offhand) {
                net.minecraft.world.item.ItemStack offhand = inventory.offhand.getFirst();
                if (!offhand.isEmpty()) {
                    equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.OFFHAND, offhand));
                }
            }
        }

        List<SynchedEntityData.DataValue<?>> dataList = Lists.newArrayList();
        if (metadata) {
            if (arrows) {
                dataList.add(SynchedEntityData.DataValue.create(DATA_ARROW_COUNT_ID, player.getArrowsInBody()));
            }
            if (particles) {
                List<ParticleOptions> list = handle.activeEffects.values().stream().filter(effect -> !effect.isVisible()).map(MobEffectInstance::getParticleOptions).toList();
                if (!list.isEmpty()) {
                    dataList.add(SynchedEntityData.DataValue.create(DATA_EFFECT_PARTICLES, list));
                }
            }
        }

        int id = handle.getId();
        for (ServerPlayerConnection connection : handle.moonrise$getTrackedEntity().seenBy) {
            if (!equipmentList.isEmpty()) {
                connection.send(new ClientboundSetEquipmentPacket(id, equipmentList, true));
            }
            if (!dataList.isEmpty()) {
                connection.send(new ClientboundSetEntityDataPacket(id, dataList));
            }
        }
    }

    public class EntityEquipmentAdapter extends PacketAdapter {

        public EntityEquipmentAdapter() {
            super(RealInvisibility.this, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_EQUIPMENT);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            int id = packet.getIntegers().readSafely(0);
            if (players.contains(id) && event.getPlayer().getEntityId() != id) {
                StructureModifier<List<Pair<EnumWrappers.ItemSlot, ItemStack>>> modifier = packet.getSlotStackPairLists();
                List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = modifier.readSafely(0);
                for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : list) {
                    if (pair.getSecond().isEmpty()) {
                        continue;
                    }
                    if (slots.contains(pair.getFirst())) {
                        pair.setSecond(ItemStack.empty());
                    }
                }
                modifier.writeSafely(0, list);
            }
        }
    }

    public class EntityMetadataAdapter extends PacketAdapter {

        public EntityMetadataAdapter() {
            super(RealInvisibility.this, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_METADATA);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packet = event.getPacket();
            int id = packet.getIntegers().readSafely(0);
            if (players.contains(id) && event.getPlayer().getEntityId() != id) {
                StructureModifier<List<WrappedDataValue>> modifier = packet.getDataValueCollectionModifier();
                List<WrappedDataValue> list = modifier.readSafely(0);
                for (WrappedDataValue value : list) {
                    if (arrows && value.getIndex() == MV_ARROWS) {
                        value.setValue(0);
                    }
                    if (particles && value.getIndex() == MV_PARTICLES) {
                        value.setValue(List.of());
                    }
                }
                modifier.writeSafely(0, list);
            }
        }
    }
}
