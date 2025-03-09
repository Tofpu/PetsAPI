package dev.lrxh.petsAPI;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

@Data
public class SkinData {
    private final String value;
    private final String signature;

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
}