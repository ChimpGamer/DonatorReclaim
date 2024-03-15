package nl.chimpgamer.donatorreclaim.commands;

import nl.chimpgamer.donatorreclaim.DonatorReclaim;
import nl.chimpgamer.donatorreclaim.handlers.DonatorsHandler;
import nl.chimpgamer.donatorreclaim.handlers.MessagesHandler;
import nl.chimpgamer.donatorreclaim.handlers.SettingsHandler;
import nl.chimpgamer.donatorreclaim.models.Rank;
import nl.chimpgamer.donatorreclaim.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReclaimCommand implements CommandExecutor {
    private final DonatorReclaim donatorReclaim;

    public ReclaimCommand(DonatorReclaim donatorReclaim) {
        this.donatorReclaim = donatorReclaim;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        SettingsHandler settingsHandler = donatorReclaim.getSettingsHandler();
        MessagesHandler messagesHandler = donatorReclaim.getMessagesHandler();
        DonatorsHandler donatorsHandler = donatorReclaim.getDonatorsHandler();

        Player player = (Player) sender;
        if (args.length == 0) {
            if (settingsHandler.isOnlyReclaimHighestRank()) {
                Rank rank = settingsHandler.getHighestAvailableRank(player);
                if (rank == null) {
                    donatorReclaim.sendMessage(player, messagesHandler.nothingToReclaim());
                    return true;
                }
                if (donatorsHandler.hasRedeemed(player, rank)) {
                    donatorReclaim.sendMessage(player, messagesHandler.alreadyClaimedRank().replace("%rank%", rank.getName()));
                } else {
                    donatorsHandler.redeemRank(player, rank);
                    donatorReclaim.sendMessage(player, messagesHandler.successfullyReclaimedRank().replace("%rank%", rank.getName()));
                    Sound reclaimSound = settingsHandler.reclaimSound();
                    if (reclaimSound != null) {
                        player.playSound(player.getLocation(), reclaimSound, 1.0F, 1.0F);
                    }
                }
            } else {
                List<Rank> toRedeem = settingsHandler.getAvailableRanks(player)
                        .stream().filter(rank -> !donatorsHandler.hasRedeemed(player, rank))
                        .collect(Collectors.toList());

                donatorsHandler.redeemRanks(player, toRedeem);
                toRedeem.forEach(rank -> donatorReclaim.sendMessage(player, messagesHandler.successfullyReclaimedRank().replace("%rank%", rank.getName())));
                Sound reclaimSound = settingsHandler.reclaimSound();
                if (reclaimSound != null) {
                    player.playSound(player.getLocation(), reclaimSound, 1.0F, 1.0F);
                }
            }
            return true;
        } else {
            if (args[0].equalsIgnoreCase("reset") && args.length >= 2) {
                if (sender.hasPermission("donatorreclaim.commands.reclaim.reset")) {
                    if (args[1].equalsIgnoreCase("all")) {
                        donatorsHandler.resetAll();
                        return true;
                    }
                    OfflinePlayer target;
                    if (Utils.isUUID(args[1])) {
                        target = donatorReclaim.getServer().getOfflinePlayer(UUID.fromString(args[1]));
                    } else {
                        target = donatorReclaim.getServer().getPlayer(args[1]);
                    }
                    if (target == null) {
                        donatorReclaim.sendMessage(player, messagesHandler.playerOffline().replace("%player%", args[1]));
                        return true;
                    }

                    if (args.length == 3) {
                        String rankName = args[2];
                        Rank rank = settingsHandler.getRank(rankName);
                        if (rank == null) {
                            donatorReclaim.sendMessage(player, messagesHandler.reclaimRankInvalid().replace("%rank%", rankName));
                            return true;
                        }
                        donatorsHandler.removeRank(target, rank);
                    } else {
                        donatorsHandler.reset(target.getUniqueId());
                    }
                } else {
                    donatorReclaim.sendMessage(player, messagesHandler.noPermission());
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("donatorreclaim.commands.reclaim.reload")) {
                    settingsHandler.reloadConfig();
                    messagesHandler.reloadConfig();
                    donatorReclaim.sendMessage(player, messagesHandler.reload());
                } else {
                    donatorReclaim.sendMessage(player, messagesHandler.noPermission());
                }
                return true;
            } else {
                donatorReclaim.sendMessage(sender, "      Donator Reclaim Help       ");
                donatorReclaim.sendMessage(sender, "&8- &6/reclaim");
                donatorReclaim.sendMessage(sender, "&8- &6/reclaim reset <all/player> [rank]");
                donatorReclaim.sendMessage(sender, "&8- &6/reclaim reload");
            }
        }
        return false;
    }
}