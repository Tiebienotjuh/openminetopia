package nl.openminetopia.modules.chat.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.configuration.DefaultConfiguration;
import nl.openminetopia.configuration.MessageConfiguration;
import nl.openminetopia.modules.chat.utils.SpyUtils;
import nl.openminetopia.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class PlayerChatListener implements Listener {

    private final DefaultConfiguration configuration = OpenMinetopia.getDefaultConfiguration();

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player source = event.getPlayer();
        MinetopiaPlayer minetopiaPlayer = (MinetopiaPlayer) PlayerManager.getInstance().getMinetopiaPlayer(source);
        if (minetopiaPlayer == null) return;

        if (!minetopiaPlayer.isInPlace()) return;
        if (minetopiaPlayer.isStaffchatEnabled()) return;

        List<Player> recipients = new ArrayList<>();

        event.setCancelled(true);

        Bukkit.getServer().getOnlinePlayers().forEach(target -> {
            if (target.getWorld().equals(source.getWorld())
                    && source.getLocation().distance(target.getLocation()) <= configuration.getChatRadiusRange())
                recipients.add(target);
        });

        recipients.remove(source);
        if (recipients.isEmpty() && configuration.isNotifyWhenNobodyInRange()) {
            event.getPlayer().sendMessage(MessageConfiguration.component("chat_no_players_in_range"));
            return;
        }
        recipients.add(source);

        // Format the message
        String originalMessage = ChatUtils.stripMiniMessage(event.message());
        String formattedMessage = configuration.getChatFormat();

        SpyUtils.chatSpy(source, originalMessage, recipients);

        // Iterate over recipients
        recipients.forEach(player -> {
            // Replace <message> placeholder with original message
            String finalMessage = formattedMessage.replace("<message>", originalMessage);

            // Check if the player's name is in the original message and highlight it
            if (originalMessage.contains(player.getName())) {
                String highlightedMessage = originalMessage.replace(player.getName(), "<green>" + player.getName() + "<white>");
                finalMessage = formattedMessage.replace("<message>", highlightedMessage);

                // Play sound for the mentioned player
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }

            // Send the formatted message to the player
            player.sendMessage(ChatUtils.format(minetopiaPlayer, finalMessage));
        });
    }
}
