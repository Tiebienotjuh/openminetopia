package nl.openminetopia.modules.prefix.commands.subcommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.configuration.MessageConfiguration;
import nl.openminetopia.modules.player.utils.PlaytimeUtil;
import nl.openminetopia.modules.prefix.objects.Prefix;
import nl.openminetopia.utils.ChatUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("prefix")
public class PrefixAddCommand extends BaseCommand {

    /**
     * Add a prefix to a player.
     * @param expiresAt The time in minutes when the prefix expires.
     */
    @Subcommand("add")
    @Syntax("<player> <minutes> <prefix>")
    @CommandCompletion("@players")
    @CommandPermission("openminetopia.prefix.add")
    @Description("Add a prefix to a player.")
    public void addPrefix(Player player, OfflinePlayer offlinePlayer, Integer expiresAt, String prefix) {
        MinetopiaPlayer minetopiaPlayer = PlayerManager.getInstance().getMinetopiaPlayer(player);
        if (minetopiaPlayer == null) return;

        if (offlinePlayer.getPlayer() == null) {
            ChatUtils.sendFormattedMessage(minetopiaPlayer, MessageConfiguration.message("player_not_found"));
            return;
        }

        MinetopiaPlayer targetMinetopiaPlayer = PlayerManager.getInstance().getMinetopiaPlayer(offlinePlayer);
        if (targetMinetopiaPlayer == null) return;

        for (Prefix prefix1 : targetMinetopiaPlayer.getPrefixes()) {
            if (prefix1.getPrefix().equalsIgnoreCase(prefix)) {
                ChatUtils.sendFormattedMessage(minetopiaPlayer, MessageConfiguration.message("prefix_already_exists")
                        .replace("<player>", (offlinePlayer.getName() == null ? "null" : offlinePlayer.getName()))
                        .replace("<prefix>", prefix));
                return;
            }
        }

        long expiresAtMillis = System.currentTimeMillis() + (expiresAt * 60 * 1000);

        if (expiresAt == -1) expiresAtMillis = -1;

        Prefix prefix1 = new Prefix(prefix, expiresAtMillis);
        targetMinetopiaPlayer.addPrefix(prefix1);

        ChatUtils.sendFormattedMessage(minetopiaPlayer, MessageConfiguration.message("prefix_added")
                .replace("<player>", (offlinePlayer.getName() == null ? "null" : offlinePlayer.getName()))
                .replace("<prefix>", prefix)
                .replace("<time>", expiresAt == -1 ? "nooit" : PlaytimeUtil.formatPlaytime(minutesToMillis(expiresAt))));
    }

    private int minutesToMillis(int minutes) {
        return minutes * 60 * 1000;
    }
}