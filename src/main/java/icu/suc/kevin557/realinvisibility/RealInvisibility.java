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
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
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
import java.util.Objects;
import java.util.Set;

public final class RealInvisibility extends JavaPlugin implements Listener {

    private Set<EnumWrappers.ItemSlot> slots;

    private Set<Integer> players;
    private int arrows;

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
        config.options().copyDefaults(true);
        saveConfig();

        slots = Sets.newHashSet();
        if (config.getBoolean("helmet")) {
            slots.add(EnumWrappers.ItemSlot.HEAD);
        }
        if (config.getBoolean("chestplate")) {
            slots.add(EnumWrappers.ItemSlot.CHEST);
        }
        if (config.getBoolean("leggings")) {
            slots.add(EnumWrappers.ItemSlot.LEGS);
        }
        if (config.getBoolean("boots")) {
            slots.add(EnumWrappers.ItemSlot.FEET);
        }
        if (config.getBoolean("mainhand")) {
            slots.add(EnumWrappers.ItemSlot.MAINHAND);
        }
        if (config.getBoolean("offhand")) {
            slots.add(EnumWrappers.ItemSlot.OFFHAND);
        }

        players = Sets.newHashSet();

        getServer().getPluginManager().registerEvents(this, this);

        ProtocolLibrary.getProtocolManager().addPacketListener(new EntityEquipmentAdapter());

        if (config.getBoolean("arrows")) {
            arrows = LivingEntity.DATA_ARROW_COUNT_ID.id();
            ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMetadataAdapter());
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
            } else if (action.equals(EntityPotionEffectEvent.Action.REMOVED)) {
                players.remove(id);
                update(player);
            }
        }
    }

    public void update(Player player) {
        ServerPlayer handle = ((CraftPlayer) player).getHandle();

        List<com.mojang.datafixers.util.Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipmentList = Lists.newArrayList();
        Inventory inventory = handle.getInventory();
        NonNullList<net.minecraft.world.item.ItemStack> armor = inventory.armor;
        net.minecraft.world.item.ItemStack helmet = armor.get(3);
        if (!helmet.isEmpty()) {
            equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.FEET, helmet));
        }
        net.minecraft.world.item.ItemStack chestplate = armor.get(2);
        if (!chestplate.isEmpty()) {
            equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.LEGS, chestplate));
        }
        net.minecraft.world.item.ItemStack leggings = armor.get(1);
        if (!leggings.isEmpty()) {
            equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.LEGS, leggings));
        }
        net.minecraft.world.item.ItemStack boots = armor.get(0);
        if (!boots.isEmpty()) {
            equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.FEET, boots));
        }
        net.minecraft.world.item.ItemStack mainhand = inventory.getSelected();
        if (!mainhand.isEmpty()) {
            equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.MAINHAND, mainhand));
        }
        net.minecraft.world.item.ItemStack offhand = inventory.offhand.getFirst();
        if (!offhand.isEmpty()) {
            equipmentList.add(new com.mojang.datafixers.util.Pair<>(EquipmentSlot.OFFHAND, offhand));
        }

        List<SynchedEntityData.DataValue<?>> dataList = handle.getEntityData().packDirty();
        if (dataList == null) {
            dataList = Lists.newArrayList();
        }
        boolean flag = true;
        for (SynchedEntityData.DataValue<?> value : dataList) {
            if (value.id() == arrows) {
                flag = false;
                break;
            }
        }
        if (flag) {
            dataList.add(SynchedEntityData.DataValue.create(LivingEntity.DATA_ARROW_COUNT_ID, player.getArrowsInBody()));
        }

        int id = handle.getId();
        for (ServerPlayerConnection connection : handle.moonrise$getTrackedEntity().seenBy) {
            connection.send(new ClientboundSetEquipmentPacket(id, equipmentList, true));
            connection.send(new ClientboundSetEntityDataPacket(id, dataList));
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
            if (players.contains(id)) {
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
            if (players.contains(id)) {
                StructureModifier<List<WrappedDataValue>> modifier = packet.getDataValueCollectionModifier();
                List<WrappedDataValue> list = modifier.readSafely(0);
                for (WrappedDataValue value : list) {
                    if (value.getIndex() == arrows) {
                        value.setValue(0);
                    }
                }
                modifier.writeSafely(0, list);
            }
        }
    }
}
