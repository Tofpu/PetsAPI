package dev.lrxh.petsAPI;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class PetsAPI {
    public static JavaPlugin instance;
    private static HashMap<UUID, List<Pet>> pets;
    private static HashMap<UUID, MoveRunnable> runnables;

    public static void init(JavaPlugin plugin) {
        PacketEvents.getAPI().init();

        EntityLib.init(
                new SpigotEntityLibPlatform(plugin),
                new APIConfig(PacketEvents.getAPI()));
        instance = plugin;
        pets = new HashMap<>();
        runnables = new HashMap<>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (MoveRunnable moveRunnable : runnables.values()) {
                Bukkit.getScheduler().runTask(plugin, moveRunnable);
            }
        }, 0L, 2L);
    }

    static void add(Player player, Pet pet) {
        if (!pets.containsKey(player.getUniqueId())) {
            List<Pet> pets = new ArrayList<>();
            pets.add(pet);
            PetsAPI.pets.put(player.getUniqueId(), pets);
        }

        pets.get(player.getUniqueId()).add(pet);

        for (Player online : Bukkit.getOnlinePlayers()) {
            load(online);
        }

        runnables.put(player.getUniqueId(), new MoveRunnable(player, pet));
    }

    static void load(Player player) {
        for (List<Pet> pets : pets.values()) {
            for (Pet pet : pets) {
                pet.armourStand.addViewer(player.getUniqueId());
                for (PacketWrapper packet : pet.packets) {
                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
                }
            }
        }
    }

    static void kill(Player player) {
        for (Pet pet : pets.get(player.getUniqueId())) {
            pet.armourStand.despawn();
        }

        pets.remove(player.getUniqueId());
        runnables.remove(player.getUniqueId());
    }
}
