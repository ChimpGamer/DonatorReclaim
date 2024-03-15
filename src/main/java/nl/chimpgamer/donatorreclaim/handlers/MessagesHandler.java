package nl.chimpgamer.donatorreclaim.handlers;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import nl.chimpgamer.donatorreclaim.DonatorReclaim;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class MessagesHandler {
    @NotNull
    private final DonatorReclaim plugin;
    @NotNull
    private final YamlDocument config;

    public MessagesHandler(@NotNull final DonatorReclaim plugin) throws IOException {
        this.plugin = plugin;
        File file = plugin.getDataFolder().toPath().resolve("messages.yml").toFile();
        LoaderSettings loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build();
        UpdaterSettings updaterSettings = UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build();
        try (InputStream inputStream = plugin.getResource("messages.yml")) {
            if (inputStream != null) {
                config = YamlDocument.create(file, inputStream, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings);
            } else {
                config = YamlDocument.create(file, GeneralSettings.DEFAULT, loaderSettings, DumperSettings.DEFAULT, updaterSettings);
            }
        }
    }

    public String alreadyClaimedRank() {
        return config.getString("alreadyClaimedRank");
    }
    public String successfullyReclaimedRank() {
        return config.getString("successfullyReclaimedRank");
    }
    public String reclaimRankInvalid() {
        return config.getString("reclaimRankInvalid");
    }
    public String reclaimJoinNotification() {
        return config.getString("reclaimJoinNotification");
    }
    public String nothingToReclaim() {
        return config.getString("nothingToReclaim");
    }
    public String playerOffline() {
        return config.getString("playerOffline");
    }
    public String noPermission() {
        return config.getString("noPermission");
    }
    public String reload() {
        return config.getString("reload");
    }

    public void reloadConfig() {
        try {
            config.reload();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Something went wrong trying to reload messages.yml.", ex);
        }
    }
}
