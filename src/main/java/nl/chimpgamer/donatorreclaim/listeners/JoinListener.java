package nl.chimpgamer.donatorreclaim.listeners;

import nl.chimpgamer.donatorreclaim.DonatorReclaim;
import nl.chimpgamer.donatorreclaim.handlers.DonatorsHandler;
import nl.chimpgamer.donatorreclaim.handlers.MessagesHandler;
import nl.chimpgamer.donatorreclaim.handlers.SettingsHandler;
import nl.chimpgamer.donatorreclaim.models.Rank;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.stream.Collectors;

public class JoinListener implements Listener {
    private final DonatorReclaim donatorReclaim;

    public JoinListener(DonatorReclaim donatorReclaim) {
        this.donatorReclaim = donatorReclaim;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        SettingsHandler settingsHandler = donatorReclaim.getSettingsHandler();
        MessagesHandler messagesHandler = donatorReclaim.getMessagesHandler();
        DonatorsHandler donatorsHandler = donatorReclaim.getDonatorsHandler();

        if (settingsHandler.executeRedeemOnFirstJoin()) {
            if (player.hasPlayedBefore()) {
                return;
            }
            if (settingsHandler.isOnlyReclaimHighestRank()) {
                Rank rank = settingsHandler.getHighestAvailableRank(player);

                if (rank == null) return;
                donatorsHandler.redeemRank(player, rank);
                donatorReclaim.sendMessage(player, messagesHandler.successfullyReclaimedRank().replace("%rank%", rank.getName()));
                Sound reclaimSound = settingsHandler.reclaimSound();
                if (reclaimSound != null) {
                    player.playSound(player.getLocation(), reclaimSound, 1.0F, 1.0F);
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
        } else if (!donatorsHandler.hasRedeemed(player, settingsHandler.getHighestAvailableRank(player))) {
            donatorReclaim.sendMessage(player, messagesHandler.reclaimJoinNotification());
        }
    }
}