package nl.chimpgamer.donatorreclaim;

import nl.chimpgamer.donatorreclaim.commands.ReclaimCommand;
import nl.chimpgamer.donatorreclaim.handlers.DonatorsHandler;
import nl.chimpgamer.donatorreclaim.handlers.MessagesHandler;
import nl.chimpgamer.donatorreclaim.handlers.SettingsHandler;
import nl.chimpgamer.donatorreclaim.listeners.JoinListener;
import nl.chimpgamer.donatorreclaim.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public final class DonatorReclaim extends JavaPlugin {

    // Handlers
    @NotNull
    private final SettingsHandler settingsHandler = new SettingsHandler(this);
    @NotNull
    private final MessagesHandler messagesHandler = new MessagesHandler(this);
    @NotNull
    private final DonatorsHandler donatorsHandler = new DonatorsHandler(this);

    public DonatorReclaim() throws IOException {
    }

    @Override
    public void onLoad() {
        // Make sure that the DonatorReclaim folder exists.
        try {
            Path dataFolderPath = getDataFolder().toPath();
            if (!Files.isDirectory(getDataFolder().toPath())) {
                Files.createDirectories(dataFolderPath);
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Unable to create plugin directory", ex);
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("reclaim").setExecutor(new ReclaimCommand(this));
        this.getServer().getPluginManager().registerEvents(new JoinListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(this);
        donatorsHandler.writeJson();
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(Utils.formatColorCodes(message));
    }

    @NotNull
    public SettingsHandler getSettingsHandler() {
        return settingsHandler;
    }

    @NotNull
    public MessagesHandler getMessagesHandler() {
        return messagesHandler;
    }

    @NotNull
    public DonatorsHandler getDonatorsHandler() {
        return donatorsHandler;
    }
}