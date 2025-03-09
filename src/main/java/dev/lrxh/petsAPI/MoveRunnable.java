package dev.lrxh.petsAPI;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class MoveRunnable implements Runnable {
    private final Player player;
    private final Pet pet;

    @Override
    public void run() {
        if (player == null) {
            return;
        }

        Location location = player.getLocation().clone();

        WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(
                pet.armourStand.getEntityId(),
                SpigotConversionUtil.fromBukkitLocation(location.add(pet.offset)),
                false);

        for (Player online : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(online, teleport);
        }
    }
}
