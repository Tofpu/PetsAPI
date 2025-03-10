package dev.lrxh.petsAPI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public class SkinData {
    private final String value;
    private final String signature;

    public SkinData(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    public static SkinData ofPlayerName(String name) {
        Player player = Bukkit.getPlayerExact(name);

        if (player == null) {
            return AnimalSkinData.STEVE.getSkinData();
        }

        if (PacketEvents.getAPI().getServerManager().getVersion().is(VersionComparison.NEWER_THAN, ServerVersion.V_1_14)) {
            PlayerProfile profile = player.getPlayerProfile();

            Optional<ProfileProperty> property = profile.getProperties().stream().filter(loopProperty -> loopProperty.getName().equals("textures")).findFirst();
            return property.map(signedProperty -> new SkinData(signedProperty.getValue(), signedProperty.getSignature())).orElse(null);
        } else {

            EntityPlayer ep = ((CraftPlayer) player).getHandle();
            GameProfile gameProfile = ep.getProfile();

            return gameProfile.getProperties().get("textures").stream()
                    .map(signedProperty -> new SkinData(signedProperty.getValue(), signedProperty.getSignature()))
                    .findFirst()
                    .orElse(AnimalSkinData.STEVE.getSkinData());
        }
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }
}