package nl.chimpgamer.donatorreclaim.handlers;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import nl.chimpgamer.donatorreclaim.DonatorReclaim;
import nl.chimpgamer.donatorreclaim.models.Rank;
import nl.chimpgamer.donatorreclaim.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SettingsHandler {
    @NotNull
    private final DonatorReclaim plugin;
    @NotNull
    private final YamlDocument config;

    public SettingsHandler(@NotNull final DonatorReclaim plugin) throws IOException {
        this.plugin = plugin;
        File file = plugin.getDataFolder().toPath().resolve("settings.yml").toFile();
        try (InputStream inputStream = plugin.getResource("settings.yml")) {
            if (inputStream != null) {
                config = YamlDocument.create(file, inputStream);
            } else {
                config = YamlDocument.create(file);
            }
        }
    }

    public boolean isOnlyReclaimHighestRank() {
        return config.getBoolean("onlyReclaimHighestRank");
    }

    public boolean fullRedeemWhenNotInOrder() {
        return config.getBoolean("fullRedeemWhenNotInOrder");
    }

    @Nullable
    public Sound reclaimSound() {
        String soundAsString = config.getString("reclaimSound");
        if (soundAsString.isEmpty()) return null;
        Sound sound = null;
        try {
            sound = Sound.valueOf(soundAsString);
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger().warning(soundAsString + " is not a valid Sound. Please use the correct sound for your server version. Check https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html for the Sounds of the latest version.");
        }

        return sound;
    }

    public boolean executeRedeemOnFirstJoin() {
        return config.getBoolean("executeRedeemOnFirstJoin");
    }

    @NotNull
    public List<Rank> getRanks() {
        List<Rank> ranks = new ArrayList<>();
        Section section = config.getSection("ranks");
        for (Object obj : section.getKeys()) {
            String key = obj.toString();
            String permission = section.getString(key + ".permission");
            List<String> commands = section.getStringList(key + ".commands");
            List<String> upgradeCommands = section.getStringList(key + ".upgrade-commands");
            Rank rank = new Rank(Utils.capitalize(key), permission, commands, upgradeCommands);
            ranks.add(rank);
        }

        return ranks;
    }

    @Nullable
    public Rank getRank(String rankName) {
        return this.getRanks().stream().filter(rank -> rank.getName().equalsIgnoreCase(rankName)).findFirst().orElse(null);
    }

    @Nullable
    public Rank getHighestAvailableRank(@NotNull Player player) {
        Rank rank = null;
        for (Rank rank1 : this.getRanks()) {
            if (player.hasPermission(rank1.getPermission())) {
                rank = rank1;
            }
        }
        return rank;
    }

    @NotNull
    public List<Rank> getAvailableRanks(Player player) {
        return this.getRanks().stream().filter(rank -> player.hasPermission(rank.getPermission())).collect(Collectors.toList());
    }

    public void reloadConfig() {
        try {
            config.reload();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Something went wrong trying to reload settings.yml.", ex);
        }
    }
}
