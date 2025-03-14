package dev.lrxh.petsAPI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.Metadata;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pet {
    private final SkinData skinData;
    private final List<PacketWrapper> packets;
    private WrapperEntity armourStand;
    private Vector offset;
    private Player player;
    private float yaw;
    private float pitch;
    private boolean lookAtPlayer;

    public Pet(SkinData skinData) {
        this.skinData = skinData;
        this.offset = new Vector(1, 1, 1);
        this.packets = new ArrayList<>();
        this.yaw = Float.MAX_VALUE;
        this.pitch = Float.MAX_VALUE;
        this.lookAtPlayer = false;
    }

    public Pet(AnimalSkinData animalSkinData) {
        this.skinData = animalSkinData.getSkinData();
        this.offset = new Vector(1, 1, 1);
        this.packets = new ArrayList<>();
        this.yaw = Float.MAX_VALUE;
        this.pitch = Float.MAX_VALUE;
        this.lookAtPlayer = false;
    }

    public void spawn(Player player) {
        this.player = player;
        UUID uuid = UUID.randomUUID();
        int id = EntityLib.getPlatform().getEntityIdProvider().provide(uuid, EntityTypes.ARMOR_STAND);

        ArmorStandMeta armorStandMeta = new ArmorStandMeta(id, new Metadata(id));
        armorStandMeta.setInvisible(true);

        if (!PacketEvents.getAPI().getServerManager().getVersion().is(VersionComparison.NEWER_THAN, ServerVersion.V_1_14)) {
            armorStandMeta.setMaskBit(10, (byte) 1, true);
        } else {
            armorStandMeta.setSmall(true);
        }

        armourStand = new WrapperEntity(id, uuid, EntityTypes.ARMOR_STAND, armorStandMeta);

        Location location = player.getLocation().clone();
        location.add(offset);

        if (lookAtPlayer) {
            Vector direction = location.subtract(offset).toVector().subtract(location.toVector());

            float yaw = (float) Math.toDegrees(Math.atan2(direction.getZ(), direction.getX())) - 90;
            float pitch = (float) -Math.toDegrees(Math.asin(direction.getY() / direction.length()));

            setYaw(yaw);
            setPitch(pitch);
        }

        if (yaw != Float.MAX_VALUE) {
            location.setYaw(yaw);
        }

        if (pitch != Float.MAX_VALUE) {
            location.setPitch(pitch);
        }

        armourStand.spawn(SpigotConversionUtil.fromBukkitLocation(location));

        List<Equipment> equipment = new ArrayList<>();

        equipment.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(getPlayerHead(skinData))));

        WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment(armourStand.getEntityId(), equipment);

        packets.add(equip);

        PetsAPI.add(player, this);
    }

    public void remove() {
        PetsAPI.kill(this);
    }

    public ItemStack getPlayerHead(@NotNull SkinData skinData) {
        if (PacketEvents.getAPI().getServerManager().getVersion().is(VersionComparison.NEWER_THAN, ServerVersion.V_1_14)) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
            playerProfile.setProperty(new ProfileProperty("textures",
                    skinData.getValue(),
                    skinData.getSignature()
            ));
            skullMeta.setPlayerProfile(playerProfile);
            head.setItemMeta(skullMeta);
            return head;
        } else {
            ItemStack head = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", skinData.getValue()));
            Field profileField;
            try {
                profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                PetsAPI.instance.getLogger().severe(e.getMessage());
            }
            head.setItemMeta(meta);
            return head;
        }
    }

    public WrapperEntity getEntity() {
        return armourStand;
    }

    public Vector getOffset() {
        return offset;
    }

    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    @Nullable
    public Location getLocation() {
        if (player == null) return null;
        return player.getLocation().add(offset);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isLookAtPlayer() {
        return lookAtPlayer;
    }

    public void setLookAtPlayer(boolean lookAtPlayer) {
        this.lookAtPlayer = lookAtPlayer;
    }

    protected List<PacketWrapper> getPackets() {
        return packets;
    }
}
