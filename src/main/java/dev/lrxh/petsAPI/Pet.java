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
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.meta.Metadata;
import me.tofaa.entitylib.meta.other.ArmorStandMeta;
import me.tofaa.entitylib.wrapper.WrapperEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pet {
    private final SkinData skinData;
    private WrapperEntity armourStand;
    private List<PacketWrapper> packets;
    private Vector offset;

    public Pet(SkinData skinData) {
        this.skinData = skinData;
        this.offset = new Vector(1, 1, 1);
        this.packets = new ArrayList<>();
    }

    public WrapperEntity getEntity(){
        return armourStand;
    }

    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    public Vector getOffset() {
        return offset;
    }

    protected List<PacketWrapper> getPackets() {
        return packets;
    }

    public void spawn(Player player) {
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
        armourStand.spawn(SpigotConversionUtil.fromBukkitLocation(player.getLocation()));

        List<Equipment> equipment = new ArrayList<>();
        equipment.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(getPlayerHead(skinData))));

        WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment(armourStand.getEntityId(), equipment);

        packets.add(equip);

        PetsAPI.add(player, this);
    }

    public void remove() {
        PetsAPI.kill(this);
    }

    public ItemStack getPlayerHead(SkinData skinData) {
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
    }
}
