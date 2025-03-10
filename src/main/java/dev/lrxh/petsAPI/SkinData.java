package dev.lrxh.petsAPI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
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
        PlayerProfile profile;

        if (player != null) {
            profile = player.getPlayerProfile();
        } else {
            profile = Bukkit.getOfflinePlayer(name).getPlayerProfile();
        }

        Optional<ProfileProperty> property = profile.getProperties().stream().filter(loopProperty -> loopProperty.getName().equals("textures")).findFirst();
        return property.map(signedProperty -> new SkinData(signedProperty.getValue(), signedProperty.getSignature())).orElse(null);
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }
}