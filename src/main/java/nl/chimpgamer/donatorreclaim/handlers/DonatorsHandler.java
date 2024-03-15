package nl.chimpgamer.donatorreclaim.handlers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import nl.chimpgamer.donatorreclaim.DonatorReclaim;
import nl.chimpgamer.donatorreclaim.models.Rank;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DonatorsHandler {
    @NotNull
    private final Gson gson = new Gson();

    @NotNull
    private final DonatorReclaim plugin;
    @NotNull
    private final File file;
    @NotNull
    private Map<UUID, List<String>> donators = new ConcurrentHashMap<>();

    public DonatorsHandler(@NotNull final DonatorReclaim donatorReclaim) throws FileNotFoundException {
        this.plugin = donatorReclaim;
        Path donatorsPath = plugin.getDataFolder().toPath().resolve("donators.json");
        this.file = donatorsPath.toFile();
        try {
            if (Files.notExists(donatorsPath)) {
                Files.createFile(donatorsPath);
                return;
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Something went wrong trying to create donators.json file.", ex);
            return;
        }

        // Check if file isn't empty.
        if (file.length() > 0) {
            donators = gson.fromJson(new JsonReader(new FileReader(file)), ConcurrentHashMap.class);
        }
    }

    public boolean donatorExists(@NotNull final OfflinePlayer offlinePlayer) {
        return donators.containsKey(offlinePlayer.getUniqueId());
    }

    @NotNull
    public List<String> getRedeemed(@NotNull final OfflinePlayer offlinePlayer) {
        if (donatorExists(offlinePlayer)) {
            return donators.get(offlinePlayer.getUniqueId());
        } else {
            return new ArrayList<>();
        }
    }

    public boolean hasRedeemed(@NotNull final OfflinePlayer offlinePlayer, @Nullable final Rank rank) {
        if (rank == null) return false;
        return this.getRedeemed(offlinePlayer).contains(rank.getName().toLowerCase());
    }

    public void redeemRank(@NotNull final Player player, @NotNull final Rank rank) {
        CommandSender commandSender = plugin.getServer().getConsoleSender();
        rank.getCommands().forEach(command -> plugin.getServer().dispatchCommand(commandSender, command
                .replace("%playername%", player.getName())
                .replace("%rank%", rank.getName())));

        List<String> redeemed = this.getRedeemed(player);
        redeemed.add(rank.getName().toLowerCase());
        donators.put(player.getUniqueId(), redeemed);
    }

    public void redeemRanks(@NotNull final Player player, @NotNull final List<Rank> ranks) {
        List<String> redeemed = this.getRedeemed(player);
        for (Rank rank : ranks) {
            CommandSender commandSender = plugin.getServer().getConsoleSender();
            rank.getCommands().forEach(command -> plugin.getServer().dispatchCommand(commandSender, command
                    .replace("%playername%", player.getName())
                    .replace("%rank%", rank.getName())));

            redeemed.add(rank.getName().toLowerCase());
        }
        donators.put(player.getUniqueId(), redeemed);
    }

    public void removeRank(@NotNull final OfflinePlayer offlinePlayer, @NotNull final Rank rank) {
        List<String> redeemed = this.getRedeemed(offlinePlayer);
        redeemed.remove(rank.getName().toLowerCase());
        donators.put(offlinePlayer.getUniqueId(), redeemed);
    }

    public void reset(@NotNull UUID uuid) {
        donators.remove(uuid);
    }

    public void resetAll() {
        donators.clear();
    }

    public void writeJson() {
        try {
            gson.toJson(donators, new FileWriter(file));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Something went wrong trying to save donators.json.", ex);
        }
    }
}
