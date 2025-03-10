package dev.lrxh.petsAPI;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MoveRunnable implements Runnable {
    protected final Pet pet;
    private final Player player;

    public MoveRunnable(Player player, Pet pet) {
        this.pet = pet;
        this.player = player;
    }

    @Override
    public void run() {
        if (player == null) {
            PetsAPI.kill(pet);
            return;
        }

        Location location = player.getLocation().clone();

        WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(
                pet.getEntity().getEntityId(),
                SpigotConversionUtil.fromBukkitLocation(location.add(pet.getOffset())),
                true);

        for (Player online : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(online, teleport);
        }
    }
}
